package ca.yorku.eecs;

import static org.neo4j.driver.v1.Values.parameters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Path;
import org.neo4j.driver.v1.types.Path.Segment;
import org.neo4j.driver.v1.types.Relationship;

import com.sun.net.httpserver.HttpExchange;

public class ComputeBaconPathHandler implements RequestHandler{
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
        	JSONObject queryResult = queryPath(dbConnection,actorId);
        	if(queryResult != null) {
        		/*
        		 * return response, with specified response body
        		 */
        		Utils.sendResponse(request, 200, queryResult.toString());
        	} else {
        		Utils.sendResponse(request, 404, "No actor in the database exists with id: " + actorId);
        	}
        } catch (Exception E) {
        	E.printStackTrace();
        	Utils.sendResponse(request, 500, "Internal Server Error: An unexpected error occurred.");
        }
	}
	
	/*
	 * Returns a path from the actorId to kevin bacon.
	 * If there is no path, then null is returned.
	 * If the actorId equals BACON_ID then a list with just kevin bacon's 
	 * ID is returned as a JSONObject.
	 * If there is a path, then a list of actor and movie IDs is returned 
	 * as a JSONObject.
	 * 
	 * @return JSONObject that contains a path from the actorId to kevin bacon, in the 
	 * form of a list of Strings representing actorIds and movieIds, identified as "baconPath".
	 */
	private JSONObject queryPath(Neo4jConnection dbConnection, String actorId) {
	    try (Session session = dbConnection.driver.session()) {
	    	
	    	/*
	    	 * Check if the actorID is Kevin Bacon, if so return list with just his ID.
	    	 */
	    	if(actorId.equals(BACON_ID)) {
				List<String> idArray = new ArrayList<>();	idArray.add(BACON_ID);
				
				Map<String, Object> resultMap = new HashMap<>();
	            resultMap.put("baconPath", idArray.toString());
				JSONObject jsonObject = Utils.createJsonObjectFromMap(resultMap);
				return jsonObject;
			}
	    	
	        try (Transaction tx = session.beginTransaction()) {
	        	/*
    			 * shortestPath gets the shortest path between two nodes
    			 */
	        	StatementResult result = tx.run("MATCH p=shortestPath((a:Actor{id:$actorId})-[*]-(b:Actor{id:$BACON_ID})) " +
	                    "RETURN p as baconPath", parameters("actorId", actorId, "BACON_ID", BACON_ID));
	            
	        	/*
	        	 * we get the path from the result, and use formatPathAsIdArray to convert it to a list
	        	 */
	            List<Record> records = result.list();
	            if (!records.isEmpty()) {
	            	Map recordsMap = records.get(0).asMap();
		            Path path = (Path) recordsMap.get("baconPath");
	                List<String> idArray = formatPathAsIdArray(path);
	                
	                //use the json utility to create the JSONObject to be returned
	                Map<String, Object> resultMap = new HashMap<>();
		            resultMap.put("baconPath", idArray.toString());
					JSONObject jsonObject = Utils.createJsonObjectFromMap(resultMap);
	                
	                return jsonObject;
	                
	            } else {
	                // No path found between actors
	            	return null;
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}

	// Helper method to format the path as a string
	private List<String> formatPathAsIdArray(Path path) {
	    List<String> idArray = new ArrayList<>();

	    // Extract nodes and relationships from the path
	    Iterable<Node> nodes = path.nodes();
	    Iterator<Node> nodeIterator = nodes.iterator();

	    while (nodeIterator.hasNext()) {
	        Node node = nodeIterator.next();
	        String nodeId = node.get("id").asString(); // Get the 'id' property assigned by you
	        idArray.add(nodeId);
	    }
	    return idArray;
	}
}
