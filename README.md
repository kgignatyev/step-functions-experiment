Read List of Tasks
---

Start exection

    ./gradlew run --args='start partnerAA group123 3'
    
    
Read tasks    

    ./gradlew run --args='read' |tee tasks.txt
    

Now we can respond to the tasks by sending response

    ./gradlew run --args='send <token> <response-file>'
    
    
for file waiting

    ./gradlew run --args='send <token> src/responses/file-arrived.json'
    

after sending all or some responses we can read list of current tasks     

    ./gradlew run --args='read' |tee tasks2.txt  
    
    
for file validated

    ./gradlew run --args='send <token> src/responses/validated1.json'     
