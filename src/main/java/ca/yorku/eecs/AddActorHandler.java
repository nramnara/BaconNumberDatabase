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

public class AddActorHandler implements RequestHandler {

    @Override
    public void handle(HttpExchange request) throws IOException {
        List<String> requiredKeys = Arrays.asList("name", "actorId");
        Map<String, String> bodyParams = Utils.getBodyParams(request, requiredKeys);

        if (bodyParams == null) {
            Utils.sendResponse(request, 400, "Bad Request: Request Body is improperly formatted or missing required information.");
            return;
        }

        Neo4jConnection dbConnection = Neo4jConnection.getInstance();

        try {
            if (isActorExists(dbConnection, bodyParams)) {
                Utils.sendResponse(request, 400, "Bad Request: Actor with id: " + bodyParams.get("actorId") + " already exists.");
                return;
            }

            if (addActorToDatabase(dbConnection, bodyParams)) {
                Utils.sendResponse(request, 200, "Actor added successfully.");
            }
        } catch (Exception e) {
            // Catch any exception that occurs in the isActorExists or addActorToDatabase method
            e.printStackTrace(); // You can log the exception for debugging purposes
            Utils.sendResponse(request, 500, "Internal Server Error: An unexpected error occurred.");
        }
    }

    private boolean isActorExists(Neo4jConnection dbConnection, Map<String, String> bodyParams) throws Exception {
        try (Session session = dbConnection.driver.session()) {
            try (Transaction tx = session.beginTransaction()) {
                StatementResult node_boolean = tx.run("MATCH (a:Actor {id: $x}) RETURN a",
                        parameters("x", bodyParams.get("actorId")));
                return node_boolean.hasNext();
            }
        } catch (Exception e) {
            throw e; // Throw the caught exception as is
        }
    }

    private boolean addActorToDatabase(Neo4jConnection dbConnection, Map<String, String> bodyParams) throws Exception {
        try (Session session = dbConnection.driver.session()) {
            try (Transaction tx = session.beginTransaction()) {
                tx.run("MERGE (a:Actor {name: $x, id: $y})", parameters("x", bodyParams.get("name"), "y", bodyParams.get("actorId")));
                tx.success();
                return true;
            }
        } catch (Exception e) {
            throw e; // Throw the caught exception as is
        }
    }
}
