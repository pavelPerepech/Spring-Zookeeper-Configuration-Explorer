echo Zookeeper Spring Config is starting from Docker image...
docker run -it --net=host -e DISPLAY --env "ZOO_HOST=localhost" --env "ZOO_PORT=2181" --env "ZOO_USER=" --env "ZOO_PASSWORD=" pavelperepech/zsc:latest