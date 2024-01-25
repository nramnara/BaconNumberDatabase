# 3311project


## Before Installation
1. open up neo4j, navigate into an empty project folder. add a local dbms use version 4.4.23
2. set name: neo4j
3. set password: 12345678
4. make sure it listens on port 7678
  - ctrl + f  `# Bolt Connector` in the setting file, the port number should appear under that heading

## Installation Guide using eclipse
1. run ` git clone "https://github.com/nramnara/BaconNumberDatabase.git" ` in your terminal
2. import into eclipse:
  - File -> Open Projects from File System... -> Import (choose directory option) -> select the cloned folder
3. right click on the folder in the project explorer.
4. run it as a java application
5. select App.java file

## Project Structure
- each handler has its own file with the same naming scheme: <endpointName>Handler.java
- actual endpoints extend from 'http:://localhost8080/api/v1/' + the end point names specified in HandlerFactory.java
  - useful if you want to test manually
- Utils.java contains some methods you may find useful when developing the handlers, here are a few I found useful:
  - getBodyParams(...) : get the key-value pairs from a request body
  - getQueryParams(...) : get the key-value pairs from request query parameters
  - sendResponse(...) : send an http response
  - createJsonObjectFromMap(...) : creating response body's
  <br>
  see how i used them in these files: GetActorHandler.java, AddActorHandler.java, AddRelationshipHandler.java
 
## Testing
- open terminal, navigate to directory of .robot file (relocate it to desktop make it easier)
- make sure robotframework & robotframework-requests are installed in this directory
- run robot `api_test.robot` to run all the test cases
- run robot -t "<testCaseName>" api_test.robot to run only one case

## How it works
1. App.java : main entry point, listens on port 8080 for requests, handles it with a Handler defined in Handler.java
2. Handler.java: passes the request path to its HandlerFactory object, where it gets back the appropriate handler
3. HandlerFactory.java: HandlerFactory reads the path and returns the appropriate concrete handler
4. Back to Handler.java: it invokes the returned concrete handler, in which the response is sent
