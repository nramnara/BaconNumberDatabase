package ca.yorku.eecs;

import java.io.IOException;
import com.sun.net.httpserver.HttpExchange;

public interface RequestHandler {
	void handle(HttpExchange request) throws IOException;
}
