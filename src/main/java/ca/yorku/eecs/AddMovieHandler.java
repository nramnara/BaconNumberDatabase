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

public class AddMovieHandler implements RequestHandler {

    @Override
    public void handle(HttpExchange request) throws IOException {
        // Check if the requiredKeys are non-empty in the requestBody
        List<String> requiredKeys = Arrays.asList("name", "movieId");
        Map<String, String> bodyParams = Utils.getBodyParams(request, requiredKeys);
        if (bodyParams == null) {
        	System.out.println(requiredKeys.toString());
            Utils.sendResponse(request, 400, "Bad Request: Request Body is improperly formatted or missing required information.");
            return;
        }

        Neo4jConnection dbConnection = Neo4jConnection.getInstance();

        try {
            if (isMovieExists(dbConnection, bodyParams)) {
                Utils.sendResponse(request, 400, "Bad Request: Movie with id: " + bodyParams.get("movieId") + " already exists.");
                return;
            }

            if (addMovieToDatabase(dbConnection, bodyParams)) {
                Utils.sendResponse(request, 200, "Movie added successfully.");
            }
        } catch (Exception e) {
            // Catch any exception that occurs in the isMovieExists or addMovieToDatabase method
            e.printStackTrace(); // You can log the exception for debugging purposes
            Utils.sendResponse(request, 500, "Internal Server Error: An unexpected error occurred.");
        }
    }

    private boolean isMovieExists(Neo4jConnection dbConnection, Map<String, String> bodyParams) throws Exception {
        try (Session session = dbConnection.driver.session()) {
            try (Transaction tx = session.beginTransaction()) {
                StatementResult node_boolean = tx.run("MATCH (a:Movie {id: $x}) RETURN a",
                        parameters("x", bodyParams.get("movieId")));
                return node_boolean.hasNext();
            }
        } catch (Exception e) {
            throw e; // Throw the caught exception as is
        }
    }

    private boolean addMovieToDatabase(Neo4jConnection dbConnection, Map<String, String> bodyParams) throws Exception {
        try (Session session = dbConnection.driver.session()) {
            try (Transaction tx = session.beginTransaction()) {
                tx.run("MERGE (a:Movie {name: $x, id: $y})", parameters("x", bodyParams.get("name"), "y", bodyParams.get("movieId")));
                tx.success();
                return true;
            }
        } catch (Exception e) {
            throw e; // Throw the caught exception as is
        }
    }
}
