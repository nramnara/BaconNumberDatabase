package ca.yorku.eecs;

import java.io.IOException;

import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;

public class App 
{
    static int PORT = 8080;
    public static void main(String[] args) throws IOException
    {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
        // TODO: two lines of code are expected to be added here
        // please refer to the HTML server example 
        
        /*
         * accept requests starting with 'api/v1'
         * handle them with the handler specified in second parameter
         */
        
        /*
         * handle get & put requests using the handle method in ./Handler.java
         * on this endpoint: `http://localhost:8080/api/v1`
         */
        Handler handler = new Handler();
        server.createContext("/api/v1",handler::handle);
        
        server.start();
        System.out.printf("Server started on port %d...\n", PORT);
    }
}
