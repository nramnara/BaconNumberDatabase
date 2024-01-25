*** Settings ***
Library    RequestsLibrary

Suite Setup    Create Session    localhost    http://localhost:8080      

*** Test Cases ***

addActorPassBacon
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    actorId=nm0000102    name=Kevin Bacon
    ${resp}=    PUT On Session    localhost    /api/v1/addActor/    json=${params}    headers=${headers}    expected_status=200

addActorPass
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    actorId=100    name=al pacino
    ${resp}=    PUT On Session    localhost    /api/v1/addActor/    json=${params}    headers=${headers}    expected_status=200
    
addActorFail
	${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    actorId=100    name=al pacino
    ${resp}=    PUT On Session    localhost    /api/v1/addActor/    json=${params}    headers=${headers}    expected_status=400
    
addMoviePass
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    movieId=10    name=a few good men
    ${resp}=    PUT On Session    localhost    /api/v1/addMovie/    json=${params}    headers=${headers}    expected_status=200
    
addMovieFail
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    movieId=10    name=a few good men
    ${resp}=    PUT On Session    localhost    /api/v1/addMovie/    json=${params}    headers=${headers}    expected_status=400

addRelationshipPass
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    movieId=10    actorId=100
    ${resp}=    PUT On Session    localhost    /api/v1/addRelationship/    json=${params}    headers=${headers}    expected_status=200
    
addRelationshipFail
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    movieId=10    actorId=100
    ${resp}=    PUT On Session    localhost    /api/v1/addRelationship/    json=${params}    headers=${headers}    expected_status=400



getActorPass
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    actorId=100
    ${resp}=    GET On Session    localhost    /api/v1/getActor/    params=${params}    headers=${headers}    expected_status=200

getActorFail
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    actorId=101
    ${resp}=    GET On Session    localhost    /api/v1/getActor/    params=${params}    headers=${headers}    expected_status=404

getMoviePass
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    movieId=10
    ${resp}=    GET On Session    localhost    /api/v1/getMovie/    params=${params}    headers=${headers}    expected_status=200

getMovieFail
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    movieId=11
    ${resp}=    GET On Session    localhost    /api/v1/getMovie/    params=${params}    headers=${headers}    expected_status=404

hasRelationshipPass
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    movieId=10    actorId=100
    ${resp}=    GET On Session    localhost    /api/v1/hasRelationship/    params=${params}    headers=${headers}    expected_status=200
    
hasRelationshipFail
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    movieId=11    actorId=100
    ${resp}=    GET On Session    localhost    /api/v1/hasRelationship/    params=${params}    headers=${headers}    expected_status=404



computeBaconNumberSetupAddActorPass
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    actorId=150    name=keanu reeves
    ${resp}=    PUT On Session    localhost    /api/v1/addActor/    json=${params}    headers=${headers}    expected_status=200

computeBaconNumberSetupAddRelationshipPass
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    movieId=10    actorId=nm0000102
    ${resp}=    PUT On Session    localhost    /api/v1/addRelationship/    json=${params}    headers=${headers}    expected_status=200

computeBaconNumberPass
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    actorId=100
    ${resp}=    GET On Session    localhost    /api/v1/computeBaconNumber/    params=${params}    headers=${headers}    expected_status=200
    
computeBaconNumberFail
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    actorId=150
    ${resp}=    GET On Session    localhost    /api/v1/computeBaconNumber/    params=${params}    headers=${headers}    expected_status=404
    
computeBaconPathPass
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    actorId=100
    ${resp}=    GET On Session    localhost    /api/v1/computeBaconPath/    params=${params}    headers=${headers}    expected_status=200

computeBaconPathFail
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    actorId=151
    ${resp}=    GET On Session    localhost    /api/v1/computeBaconPath/    params=${params}    headers=${headers}    expected_status=404



getActorCostarsPass
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    actorId=100
    ${resp}=    GET On Session    localhost    /api/v1/getActorCostars/    params=${params}    headers=${headers}    expected_status=200
    
getActorCostarsFail
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    actorId=300
    ${resp}=    GET On Session    localhost    /api/v1/getActorCostars/    params=${params}    headers=${headers}    expected_status=404
