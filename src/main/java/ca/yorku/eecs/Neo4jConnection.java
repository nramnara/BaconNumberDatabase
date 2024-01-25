package ca.yorku.eecs;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;


public class Neo4jConnection {

	private static Neo4jConnection instance;
	Driver driver;
	private String uriDb;

	private Neo4jConnection() {
		uriDb = "bolt://localhost:7687";
		Config config = Config.builder().withoutEncryption().build();
		driver = GraphDatabase.driver(uriDb, AuthTokens.basic("neo4j","12345678"), config);

	}
	
	public static Neo4jConnection getInstance() {
		if (instance == null) {
			instance = new Neo4jConnection();
		}
		return instance;
	}
	
	public void close() {
		driver.close();
	}
}
