This service uses http4s library to host simple HTTP service to serve records from hoteldb.csv based on city
The service only accepts requests containing (as bearer token) one of the pre-configured set of keys.
The api is rate limited per key via ratelimit.conf file (Number of requests/10 seconds)
If rate limit is exceeded for any particular key, that api key gets disabled for 5 minutes before allowing requests
again.


- compile and run tests:

mvn clean package

- running the server (runs on localhost:8080 by default if no arguments are provided):

java -cp /path/to/goho-1.0-SNAPSHOT-jar-with-dependencies.jar com.goho.TestMain (host) (port)

- Valid Request Example:

curl -i -H "Authorization: Bearer db78d85b7b27862779404c38abddd520" http://localhost:8080/getHotelsByCity/Bangkok

1. request to return records sorted by room price (ascending) :

curl -i -h "Authorization: bearer db78d85b7b27862779404c38abddd520" http://localhost:8080/gethotelsbycity/bangkok=sa

2. request to return records sorted by room price (descending) :

curl -i -h "Authorization: bearer db78d85b7b27862779404c38abddd520" http://localhost:8080/gethotelsbycity/bangkok=sd

3. Multiple requests:

ab -n 5 -v 4 -H "Authorization: Bearer db78d85b7b27862779404c38abddd520" http://127.0.0.1:8080/getHotelsByCity/Bangkok=sa
