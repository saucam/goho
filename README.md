compile and run tests:

mvn clean package

running the server (runs on localhost:8080 for now):

java -cp /path/to/goho-1.0-SNAPSHOT-jar-with-dependencies.jar com.goho.TestMain


Valid Request Example:

curl -i -H "Authorization: Bearer db78d85b7b27862779404c38abddd520" http://localhost:8080/getHotelsByCity/Bangkok

request to return records sorted by room price (ascending) :

curl -i -h "authorization: bearer db78d85b7b27862779404c38abddd520" http://localhost:8080/gethotelsbycity/bangkok=sa

request to return records sorted by room price (descending) :

curl -i -h "authorization: bearer db78d85b7b27862779404c38abddd520" http://localhost:8080/gethotelsbycity/bangkok=sd


