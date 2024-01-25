*** Settings ***
Library    RequestsLibrary

Suite Setup    Create Session    localhost    http://localhost:8080      

*** Test Cases ***

getActorPass
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    actorId=4
    ${resp}=    GET On Session    localhost    /api/v1/getActor/    params=${params}    headers=${headers}    expected_status=200

addActorPass
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    actorId=99    name=adam devine
    ${resp}=    PUT On Session    localhost    /api/v1/addActor/    json=${params}    headers=${headers}    expected_status=200
