package ca.yorku.eecs;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class Handler implements HttpHandler{	

	private HandlerFactory factory = new HandlerFactory();
	
	/*
	 * selects the appopriate handler based on the 
	 * path of the request param
	 */
	@Override
	public void handle(HttpExchange request) throws IOException {
		// TODO Auto-generated method stub
		
		try {
			String endpoint = this.getEndpoint(request.getRequestURI().getPath());
			
			RequestHandler handler = factory.getHandler(endpoint);
			if (handler != null) {
				handler.handle(request);
			} else {
                throw new IllegalArgumentException("Endpoint not found: " + endpoint);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		
	}
	
	/*
	 * returns the endpoint part of the path:
	 * ex. getEndpoint("/api/v1/example/?knock2=dashstar")
	 * = "example"
	 */
	private String getEndpoint(String path) {
	    String[] pathSegments = path.split("/");
	    if (pathSegments.length >= 4) {
	        return pathSegments[3];
	    } else {
	        throw new IllegalArgumentException("Invalid path format: " + path);
	    }
	}


}
