# MicroProfile Metrics 5.0 Sample App


This app was initially created starting from the MicroProfile Starter.  The app helps illustrate the use of MicroProfile Metrics 5.0 with:
- Liberty server with application instrumented with MP Metrics
- Prometheus
- Grafana with a dashboard built to show the app's metrics

## Setup

1. set up a bridge network in docker to use for Liberty, Prometheus, Grafana

    ```
    docker network create -d bridge mynet
    ```

2. run Prometheus and Grafana on mynet network
    ```
    docker run -d --name prometheus --net mynet -p 9090:9090 -v $(pwd)/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml prom/prometheus:v2.39.1
    ```
    
    ```
    docker run -d --name grafana --net mynet -p 3000:3000 -v $(pwd)/prometheus/prometheus-datasource.yml:/etc/grafana/provisioning/datasources/prometheus-datasource.yml -v $(pwd)/grafana/grafana.ini:/etc/grafana/grafana.ini grafana/grafana:9.1.8-ubuntu
    ```

3. run Liberty in dev mode with container support on mynet network.  The Liberty Dockerfile builds FROM the daily Liberty build (openliberty/daily:full-java11-openj9)
    ```
    cd liberty
    # note: liberty dev mode with containers requires an internet connection 
    mvn liberty:devc -DserverStartTimeout=120 -DdockerRunOpts="--net=mynet --name=liberty1 -m 512MB"
    ```

4. import Grafana dashboard (use the dashboard import in the Grafana UI)
    ```
    open http://localhost:3000
    import dashboard from grafana/CarStats.json into Grafana
    ```

## Demo

Use the app while showing the effect on the metrics in the Grafana dashboard. 

1. open the app, Prometheus, Grafana from browser        
    ```
    # Liberty
    open http://localhost:9080
    open http://localhost:9080/metrics

    # Prometheus
    open http://localhost:9090

    # Grafana
    open http://localhost:3000
    ```

2. open the following files in an IDE to show the metrics instrumentation
    - CarController
    - CarMetrics

3. click links in the app. Suggested flow:

    1. unlock the car
    1. start the car
    1. start driving
    1. stop driving
    1. stop the car
    1. lock the car


## Uninstall

Uninstall the docker containers as follows:
        
    docker stop prometheus grafana liberty1
    docker rm   prometheus grafana liberty1
