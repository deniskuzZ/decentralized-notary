docker pull mongo

#start a mongo instance
docker run --name mongo -d mongo

./gradlew build docker

#connect to it from an application
docker run -p 8080:8080 
    -e mongodb.uri=mongodb://mongo/notary 
    -e nxt.url=192.168.33.11:6876 
    -e nxt.account=NXT-YTBB-LT9J-SRRR-7KLBQ 
    -e nxt.secret='bar fog safe think somebody finger feather create drink chill wish rub' 
    --name not-bot 
    --link mongo:mongo 
    -t nxt/not-bot
