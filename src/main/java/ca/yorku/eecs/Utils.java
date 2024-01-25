package ca.yorku.eecs;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.soap.Node;

import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Value;
import com.sun.net.httpserver.HttpExchange;

class Utils {        
    // use for extracting query params
    public static Map<String, String> splitQuery(String query) throws UnsupportedEncodingException {
        Map<String, String> query_pairs = new LinkedHashMap<String, String>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        return query_pairs;
    }

    // one possible option for extracting JSON body as String
    public static String convert(InputStream inputStream) throws IOException {
                
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            return br.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }

    // another option for extracting JSON body as String
    public static String getBody(HttpExchange he) throws IOException {
            InputStreamReader isr = new InputStreamReader(he.getRequestBody(), "utf-8");
            BufferedReader br = new BufferedReader(isr);
            
            int b;
            StringBuilder buf = new StringBuilder();
            while ((b = br.read()) != -1) {
                buf.append((char) b);
            }

            br.close();
            isr.close();
	    
        return buf.toString();
        }
    
    // Get the query parameters and check that each key from the list has a non-empty value
    public static Map<String, String> getQueryParams(HttpExchange he, List<String> requiredKeys) throws UnsupportedEncodingException {
        String query = he.getRequestURI().getQuery();
        Map<String, String> queryParams = splitQuery(query);

        for (String key : requiredKeys) {
            if (!queryParams.containsKey(key) || queryParams.get(key).isEmpty()) {
                return null;
            }
        }

        return queryParams;
    }
    
    public static Map<String,String> getBodyParams(HttpExchange he, List<String> requiredKeys) throws IOException {
    	String requestBody = Utils.getBody(he);
        Map<String,String> body = Utils.jsonStringToMap(requestBody);
        
        for (String key : requiredKeys) {
            if (!body.containsKey(key) || body.get(key).isEmpty()) {
                return null;
            }
        }

        return body;
    }
    
    public static Map<String, String> jsonStringToMap(String jsonString) {
        Map<String, String> resultMap = new HashMap<>();

        String[] keyValuePairs = jsonString.replaceAll("[{}\"]", "").split(",");
        for (String pair : keyValuePairs) {
            String[] entry = pair.split(":");
            if (entry.length == 2) {
                String key = entry[0].trim();
                String value = entry[1].trim();
                resultMap.put(key, value);
            }
        }

        return resultMap;
    }
    
    public static JSONObject createJsonObjectFromMap(Map<String, Object> inputMap) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<String, Object> entry : inputMap.entrySet()) {
            jsonObject.put(entry.getKey(), entry.getValue());
        }
        return jsonObject;
    }

    
    public static void sendResponse(HttpExchange exchange, int statusCode, String responseMessage) throws IOException {
        byte[] responseBytes = responseMessage.getBytes("UTF-8");
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(responseBytes);
        os.close();
    }
    
    /*
     * returns:
     * List<String> where each string is an actorId that has a `ACTED_IN` relationship with the movie
     */
//    public static List<String> getStarredActors(Neo4jConnection dbConnection, String movieId) throws Exception {
//        try (Session session = dbConnection.driver.session()) {
//            try (Transaction tx = session.beginTransaction()) {
//                StatementResult node_result = tx.run("MATCH (m:Movie {id: $x})<-[r:ACTED_IN]-(a:Actor) RETURN a", parameters("x", movieId));
//
//                List<String> actorIds = new ArrayList<>();
//
//                while (node_result.hasNext()) {
//                    Record record = node_result.next();
//                    Node actorNode = record.get("a").asNode();
//                    String actorId = actorNode.get("id").asString();
//                    actorIds.add(actorId);
//                }
//
//                return actorIds;
//            }
//        } catch (Exception e) {
//            throw e;
//        }
//    }
}