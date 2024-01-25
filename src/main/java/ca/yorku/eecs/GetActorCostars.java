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

public class GetActorCostars implements RequestHandler{
	
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
    
    /*
     * Gets a list of all actors that the actorId has acted with, across all movies they've starred in.
     * 
     * If the actor does not exist in the database, then null is returned.
     * If the actor exists, but has not acted with any other actors, then an empty list is returned as a JSONObject.
     * Otherwise a list of actorIds is returned as a JSONObject.
     * 
     * @return JSONObject that contains a list of actorIds, identified by "costars"
     */
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
    			    List<Node> movies = record.get("movies").asList(Value::asNode);
    			    
                    Map<String, Object> resultMap = new HashMap<>();
                    
                    List<String> movieIds = new ArrayList<>();
                    for (Node movie : movies) {
                        String movieId = movie.get("id").asString();
                        movieIds.add(movieId);
                    }
                    
                    List<String> costars = new ArrayList<>();
                    for(String movieId : movieIds) {
                    	List<String> actors = getStarredActors(dbConnection, movieId);
                    	for(String actor : actors) {
                    		if((!costars.contains(actor)) && (!actor.equals(actorId))) {
                    			costars.add(actor);
                    		}
                    	}
                    }
                    resultMap.put("costars", costars);
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
    
	/*
	 * Returns a list of actorIds of type String that represent the actors that have 
	 * starred in the movie specified by the movieId.
	 * 
	 * @return List of Strings of actorIds.
	 */
	public static List<String> getStarredActors(Neo4jConnection dbConnection, String movieId) throws Exception {
        try (Session session = dbConnection.driver.session()) {
            try (Transaction tx = session.beginTransaction()) {
                StatementResult node_result = tx.run("MATCH (m:Movie {id: $x})<-[r:ACTED_IN]-(a:Actor) RETURN a", parameters("x", movieId));

                List<String> actorIds = new ArrayList<>();

                while (node_result.hasNext()) {
                    Record record = node_result.next();
                    Node actorNode = record.get("a").asNode();
                    String actorId = actorNode.get("id").asString();
                    actorIds.add(actorId);
                }

                return actorIds;
            }
        } catch (Exception e) {
            throw e;
        }
    }
}
