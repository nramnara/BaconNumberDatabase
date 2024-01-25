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

public class HasRelationshipHandler implements RequestHandler{

	@Override
	public void handle(HttpExchange request) throws IOException {
        List<String> requiredKeys = Arrays.asList("actorId","movieId");
        Map<String, String> queryParams = Utils.getQueryParams(request, requiredKeys);

        //missing parameters
        if (queryParams == null) {
            // If any required key is missing or has an empty value, return 400 Bad Request
            Utils.sendResponse(request, 400, "Bad Request: Missing or empty required parameters.");
            return;
        }
        
        Neo4jConnection dbConnection = Neo4jConnection.getInstance();
        String actorId = queryParams.get("actorId");
        String movieId = queryParams.get("movieId");

        try {
        	JSONObject queryResult = checkRelationship(dbConnection, actorId, movieId);
        	if(queryResult != null) {
        		/*
        		 * return response, with specified response body
        		 */
        		Utils.sendResponse(request, 200, queryResult.toString());
        	} else {
        		Utils.sendResponse(request, 404, "No actor or movie in the database exists with actorId: " + actorId + " or movieId: " + movieId);
        	}
        	
        } catch (Exception E) {
        	Utils.sendResponse(request, 500, "Internal Server Error: An unexpected error occurred.");
        }
	}
	
	/*
	 * Checks to see if there is a relationship between the movieId and actorId.
	 * The actor's movie list is checked to see if the movieId is contained within it.
	 * If it is then a boolean response is returned as a JSONObject, true if there is 
	 * a relationship, and false if there isn't.
	 * If the actorId or movieId does not exist within the database, then null is returned.
	 * 
	 * @return JSONObject that contains a boolean value representing if there is a 
	 * relationship between the actorId and movieId, identified by "hasRelationship".
	 */
	private JSONObject checkRelationship(Neo4jConnection dbConnection, String actorId, String movieId) throws Exception {
    	try (Session session = dbConnection.driver.session()) {
    		try (Transaction tx = session.beginTransaction()) {
                StatementResult node_result = tx.run(
                        "MATCH (a:Actor {id: $x}) OPTIONAL MATCH (a)-[:ACTED_IN]->(m:Movie) RETURN a, COLLECT(m) AS movies",
                        parameters("x", actorId)
                    );
    			
                /*
                 * we get a list of movies the actor has acted in, to search if the 
                 * movieId matches one in the list
                 */
    			if (node_result.hasNext()) {
    			    Record record = node_result.single();
    			    List<Node> movies = record.get("movies").asList(Value::asNode);
    			    
                    /*
                     * Check each movie the actor has acted to see if that movie matches the inputed movieId
                     */
    			    boolean hasRelationship = false;
                    for (Node movie : movies) {
                        String movieActorIsIn = movie.get("id").asString();
                        
                        if(movieActorIsIn.equals(movieId)) {
                        	hasRelationship = true;
                        } else {
                        	return null;
                        }
                    }
                    
                    //use the json utility to create the JSONObject to be returned
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("actorId", actorId);
                    resultMap.put("movieId", movieId);
                    resultMap.put("hasRelationship", hasRelationship);

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
