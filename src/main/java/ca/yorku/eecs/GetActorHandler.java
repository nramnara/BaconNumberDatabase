package ca.yorku.eecs;

import static org.neo4j.driver.v1.Values.parameters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.types.Node;

import com.sun.net.httpserver.HttpExchange;

public class GetActorHandler implements RequestHandler {

    @Override
    public void handle(HttpExchange request) throws IOException {
        // TODO Auto-generated method stub
        List<String> requiredKeys = Arrays.asList("actorId");
        Map<String, String> queryParams = Utils.getQueryParams(request, requiredKeys);
        // missing parameters
        if (queryParams == null) {
            // If any required key is missing or has an empty value, return 400 Bad Request
            Utils.sendResponse(request, 400, "Bad Request: Missing or empty required parameters.");
            return;
        }
        Neo4jConnection dbConnection = Neo4jConnection.getInstance();

        String actorId = queryParams.get("actorId");
        /*
         * boilerplate for querying a node with some matching credentials
         */
        try {
        	JSONObject queryResult = queryActor(dbConnection,actorId);
        	
        	if(queryResult != null) {
        		/*
        		 * return response, with specified response body
        		 */
        		Utils.sendResponse(request, 200, queryResult.toString());
        	} else {
        		Utils.sendResponse(request, 404, "No actor in the database exists with id: " + actorId);
        	}
        	
        } catch (Exception E) {
        	Utils.sendResponse(request, 500, "Internal Server Error: An unexpected error occurred.");
        }
    }
    
    private JSONObject queryActor(Neo4jConnection dbConnection, String actorId) throws Exception {
    	try (Session session = dbConnection.driver.session()) {
    		try (Transaction tx = session.beginTransaction()) {
                StatementResult node_result = tx.run(
                        "MATCH (a:Actor {id: $x}) OPTIONAL MATCH (a)-[:ACTED_IN]->(m:Movie) RETURN a, COLLECT(m) AS movies",
                        parameters("x", actorId)
                    );
    			
    			if (node_result.hasNext()) {
                	/*
                	 * get the id, name and list of movies they're in
                	 * 
                	 */
    			    Record record = node_result.single();
    			    Node actorNode = record.get("a").asNode();
                    String actorName = actorNode.get("name").asString();
                    String id = actorNode.get("id").asString();
    			    List<Node> movies = record.get("movies").asList(Value::asNode);
    			    
    			    //map the name of the key to the value, and call the json utility class
                    Map<String, Object> resultMap = new HashMap<>();
                    
                    List<String> movieIds = new ArrayList<>();
                    for (Node movie : movies) {
                        String movieId = movie.get("id").asString();
                        movieIds.add(movieId);
                    }
                    resultMap.put("movies", movieIds);
                    resultMap.put("name", actorName);
                    resultMap.put("actorId", id);

                    
                    JSONObject jsonObject = Utils.createJsonObjectFromMap(resultMap);

    			    return jsonObject;
    			} else {
    				return null;
    			}
    		}
    	} catch (Exception e) {
    		throw e;
    	}
    }
}
