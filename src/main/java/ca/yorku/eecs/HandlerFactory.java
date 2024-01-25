package ca.yorku.eecs;

/*
 * HandlerFactory class is responsible for creating and providing appropriate RequestHandler instances
 * based on the given endpoint.
 */
public class HandlerFactory {
	// "PUT" handlers
	private static RequestHandler addActor = new AddActorHandler();
	private static RequestHandler addMovie = new AddMovieHandler();
	private static RequestHandler addRelationship = new AddRelationshipHandler();
	
	//"GET" handlers
	private static RequestHandler computeBaconNumber = new ComputeBaconNumberHandler();
	private static RequestHandler computeBaconPath = new ComputeBaconPathHandler();
	private static RequestHandler getActor = new GetActorHandler();
	private static RequestHandler getMovie = new GetMovieHandler();
	private static RequestHandler hasRelationship = new HasRelationshipHandler();
	private static RequestHandler getActorCostars = new GetActorCostars();
	
	
    /*
     * Gets the appropriate RequestHandler instance based on the given endpoint.
     *
     * @param endpoint The endpoint for which a RequestHandler is required.
     * @return The corresponding RequestHandler instance, or null if no match is found.
     */
	public RequestHandler getHandler(String endpoint) {
		switch (endpoint) {
		
		case "addActor":
			return addActor;
		case "addMovie":
			return addMovie;
		case "addRelationship":
			return addRelationship;
		case "getActor":
			return getActor;
		case "getMovie":
			return getMovie;
		case "hasRelationship":
			return hasRelationship;
		case "computeBaconNumber":
			return computeBaconNumber;
		case "computeBaconPath":
			return computeBaconPath;
		case "getActorCostars":
			return getActorCostars;
			
		default:
			return null;
		}
	}
}
