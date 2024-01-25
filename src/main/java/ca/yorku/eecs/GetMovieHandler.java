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

public class GetMovieHandler implements RequestHandler{

	@Override
	public void handle(HttpExchange request) throws IOException {
		// TODO Auto-generated method stub
        List<String> requiredKeys = Arrays.asList("movieId");
        Map<String, String> queryParams = Utils.getQueryParams(request, requiredKeys);

        //missing parameters
        if (queryParams == null) {
            // If any required key is missing or has an empty value, return 400 Bad Request
            Utils.sendResponse(request, 400, "Bad Request: Missing or empty required parameters.");
            return;
        }
        
        Neo4jConnection dbConnection = Neo4jConnection.getInstance();
        String movieId = queryParams.get("movieId");
        /*
         * boilerplate for querying a node with some matching credentials
         */
        try {
        	JSONObject queryResult = queryMovie(dbConnection,movieId);
        	
        	if(queryResult != null) {
        		/*
        		 * return response, with specified response body
        		 */
        		Utils.sendResponse(request, 200, queryResult.toString());
        	} else {
        		Utils.sendResponse(request, 404, "No movie in the database exists with id: " + movieId);
        	}
        	
        } catch (Exception E) {
        	Utils.sendResponse(request, 500, "Internal Server Error: An unexpected error occurred.");
        }
	}
	
	/*
	 * Checks to see if a movie exists within the database, and returns the Id, 
	 * name, and list of actors in the movie as a response.
	 * If the movieId does not exist within the database, then null is returned.
	 * If the movieId exists, then the response will be returned as a JSONObject.
	 * 
	 * @return JSONObject that contains the name, Id, and list of actors for the movieId.
	 */
	private JSONObject queryMovie(Neo4jConnection dbConnection, String movieId) throws Exception {
    	try (Session session = dbConnection.driver.session()) {
    		try (Transaction tx = session.beginTransaction()) {
                StatementResult node_result = tx.run(
                        "MATCH (a:Movie {id: $x}) OPTIONAL MATCH (a)-[:STARRING]->(m:Actors) RETURN a, COLLECT(m) AS actors",
                        parameters("x", movieId)
                    );
    			
    			if (node_result.hasNext()) {
                	/*
                	 * get the id, name and list of actors in the movie
                	 * 
                	 */
    			    Record record = node_result.single();
    			    Node movieNode = record.get("a").asNode();
                    String movieName = movieNode.get("name").asString();
                    String id = movieNode.get("id").asString();
                    List<String> actors = getStarredActors(dbConnection, movieId);
    			    
    			    //map the name of the key to the value, and call the json utility class
                    Map<String, Object> resultMap = new HashMap<>();
                    
                    resultMap.put("actors", actors);
                    resultMap.put("name", movieName);
                    resultMap.put("movieId", id);

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
