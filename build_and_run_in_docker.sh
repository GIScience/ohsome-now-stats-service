
./gradlew build

docker build -t heigit/hot-api .
docker run -p 8080:8080 heigit/hot-api