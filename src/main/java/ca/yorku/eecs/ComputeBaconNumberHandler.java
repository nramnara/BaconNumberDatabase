package ca.yorku.eecs;

import static org.neo4j.driver.v1.Values.parameters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.types.Node;

import com.sun.net.httpserver.HttpExchange;

public class ComputeBaconNumberHandler implements RequestHandler{	
	private String BACON_ID = "nm0000102";

	@Override
	public void handle(HttpExchange request) throws IOException {
        List<String> requiredKeys = Arrays.asList("actorId");
        Map<String, String> queryParams = Utils.getQueryParams(request, requiredKeys);

        if (queryParams == null) {
            // If any required key is missing or has an empty value, return 400 Bad Request
            Utils.sendResponse(request, 400, "Bad Request: Missing or empty required parameters.");
            return;
        }
        
        Neo4jConnection dbConnection = Neo4jConnection.getInstance();
        String actorId = queryParams.get("actorId");        
        
        try {
        	JSONObject queryResult = computeBaconNumber(dbConnection, actorId);
			if(queryResult != null) {
				/*
        		 * return response, with specified response body
        		 */
				Utils.sendResponse(request, 200, queryResult.toString());
			} else {
        		Utils.sendResponse(request, 404, "There is no movie or actor in the database that exists with that actorId/movieId or there is no path to Kevin Bacon.");
        	}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Utils.sendResponse(request, 500, "Internal Server Error: An unexpected error occurred.");
			e.printStackTrace();
		}
	}
	
	/*
	 * Returns the distance from the actorId to kevin bacon.
	 * Calculated based on half the length of the path between the two IDs.
	 * 
	 * If there is no path, then null is returned.
	 * If the actorId equals BACON_ID then a value of 0 is returned as a JSONObject.
	 * If there is a path between the actorId and kevin bacon, then the distance is 
	 * returned as a JSONObject.
	 * 
	 * @return JSONObject that contains an int value identified by "baconNumber", 
	 * representing the distance between actorId kevin bacon
	 */
	public JSONObject computeBaconNumber(Neo4jConnection dbConnection, String actorId) {
		try (Session session = dbConnection.driver.session()) {
			/*
			 * If actorId == BACON_ID, we return a value of 0
			 */
			if(actorId.equals(BACON_ID)) {
				Map<String, Object> resultMap = new HashMap<>();
	            resultMap.put("baconNumber", 0);
	            JSONObject jsonObject = Utils.createJsonObjectFromMap(resultMap);
	            
				return jsonObject;
			}
			
    		try (Transaction tx = session.beginTransaction()) {
    			/*
    			 * shortestPath gets the shortest path between two nodes, we take the length and
    			 * divide by two since the path counts relationship arrows
    			 */
    			StatementResult result = tx.run("MATCH p=shortestPath((a:Actor{id:$x})-[*]-(b:Actor{id:$y})) " +
    		            "RETURN length(p)/2 as baconNumber", parameters("x", actorId, "y", BACON_ID));
    			/*
    			 * get the value for baconNumber
    			 */
    			List<Record> records = result.list();
    			if (!records.isEmpty()){
    				String baconNumber = records.get(0).asMap().get("baconNumber").toString();
    				
    				//use the json utility to create the JSONObject to be returned
    				if(!baconNumber.equals("0")) {
    					Map<String, Object> resultMap = new HashMap<>();
        	            resultMap.put("baconNumber", Integer.parseInt(baconNumber));
        	            JSONObject jsonObject = Utils.createJsonObjectFromMap(resultMap);
        				return jsonObject;
    				}
    				return null;	
    			}
    			// No path found between actors
    			return null;
    			
    		} catch(Exception e) {
    			e.printStackTrace();
    		}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}	
}
