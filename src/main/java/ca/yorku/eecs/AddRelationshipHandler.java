package ca.yorku.eecs;

import static org.neo4j.driver.v1.Values.parameters;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;

import com.sun.net.httpserver.HttpExchange;

public class AddRelationshipHandler implements RequestHandler{

	@Override
	public void handle(HttpExchange request) throws IOException {
		// TODO Auto-generated method stub
        List<String> requiredKeys = Arrays.asList("actorId", "movieId");
        Map<String, String> bodyParams = Utils.getBodyParams(request, requiredKeys);

        if (bodyParams == null) {
            Utils.sendResponse(request, 400, "Bad Request: Request Body is improperly formatted or missing required information.");
            return;
        }
        
        Neo4jConnection dbConnection = Neo4jConnection.getInstance();
        
        /*
         * 1. actor and movie exist
         * 2. they don't already have a relationship 
         */
        String actorId = bodyParams.get("actorId"); String movieId = bodyParams.get("movieId");
        try {
            if (!isActorExists(dbConnection,actorId) || !isMovieExists(dbConnection,movieId)) {
            	Utils.sendResponse(request, 404, "Actor or movie does not exist in the database.");
            	return;
            }
            
            if(relationshipExists(dbConnection,actorId,movieId)) {
            	Utils.sendResponse(request, 400, "Relationship already exists between the movie and actor.");
            	return;
            }
            
            if(castActor(dbConnection,actorId,movieId)) {
            	
            	Utils.sendResponse(request, 200, "Relationship made successfully.");
            }
        } catch (Exception e) {
        	e.printStackTrace();
            Utils.sendResponse(request, 500, "Internal Server Error: An unexpected error occurred.");
        }

                
	}
	
    private boolean isActorExists(Neo4jConnection dbConnection, String actorId) {
        try (Session session = dbConnection.driver.session()) {
            try (Transaction tx = session.beginTransaction()) {
                StatementResult node_boolean = tx.run("MATCH (a:Actor {id: $x}) RETURN a",
                        parameters("x", actorId));
                return node_boolean.hasNext();
            }
        } catch (Exception e) {
        	throw e;
        }
    }
    
    private boolean isMovieExists(Neo4jConnection dbConnection, String movieId) {
        try (Session session = dbConnection.driver.session()) {
            try (Transaction tx = session.beginTransaction()) {
                StatementResult node_boolean = tx.run("MATCH (a:Movie {id: $x}) RETURN a",
                        parameters("x", movieId));
                return node_boolean.hasNext();
            }
        } catch (Exception e) {
        	throw e;
        }
    }
    
    private boolean relationshipExists (Neo4jConnection dbConnection, String actorId, String movieId) {
        try (Session session = dbConnection.driver.session())
        {
        	try (Transaction tx = session.beginTransaction()) {
        		StatementResult node_boolean = tx.run("RETURN EXISTS( (:Actor {id: $x})"
        				+ "-[:ACTED_IN]-(:Movie {id: $y}) ) as bool"
						,parameters("x", actorId, "y", movieId) );
        		return node_boolean.single().get("bool").asBoolean();
        	}
        } catch (Exception e) {
        	throw e;
        }
    	
    }
    

			 
    private boolean castActor(Neo4jConnection dbConnection, String actorId, String movieId) {
        try (Session session = dbConnection.driver.session()) {
            try (Transaction tx = session.beginTransaction()) {
                tx.run("MATCH (a:Actor {id:$x}),"
            			+ "(m:Movie {id:$y})\n" + 
            			 "MERGE (a)-[r:ACTED_IN]->(m)\n" + 
            			 "RETURN r", parameters("x", actorId, "y", movieId));
                
                tx.success();
            	return true;
            }
        } catch (Exception e) {
        	throw e;
        }
    }
}
   
    
