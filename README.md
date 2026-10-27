# HTTP Geolocation Service

A Scala-based HTTP service providing geolocation data for IP addresses. Built with http4s, cats-effect, and circe, the service integrates with external geolocation providers and includes production-ready observability configurations.

## Architecture & Stack

* **Language:** Scala / Java 17
* **Framework:** Cats Effect 3, Http4s (Ember), Circe, Ciris
* **Observability:** OpenTelemetry (Java Agent), Prometheus, Jaeger, Grafana, Logback
* **Testing:** Weaver

## Quick Start

The easiest way to run the service with its full observability stack is via Docker Compose.

    docker-compose up --build -d

This provisions the following services:
* **Application:** http://localhost:8080
* **Prometheus:** http://localhost:9090
* **Jaeger (Tracing):** http://localhost:16686
* **Grafana:** http://localhost:3000 (Credentials: admin / admin)

## API Endpoints

Method: GET 
Endpoint: /api/info
Description: Returns the country code for the caller's IP.

Method: GET
Endpoint: /api/info/{ip}
Description: Returns the country code for the specified IP.

Method: GET
Endpoint: /metrics
Description: Exposes Prometheus-compatible JVM and HTTP metrics.

Example Response:

    {
      "ip": "8.8.8.8",
      "country": "US"
    }

## Configuration

The application is configured via environment variables.

* API_HOST (Default: https://api.external.com) - Upstream geolocation API base URL. Overridden to https://api.country.is/ in docker-compose.
* PORT (Default: 8080) - Port for the HTTP server to bind to.

## Observability

This service is configured for zero-code instrumentation via the OpenTelemetry Java Agent.
* **Tracing:** HTTP requests are automatically traced and exported via OTLP to Jaeger.
* **Logging:** logback.xml is configured to inject the active trace_id into application logs for trace-to-log correlation.
* **Metrics:** JVM and HTTP metrics are collected via the Prometheus Java client and exposed on the /metrics endpoint.

## Development

To run the application locally without Docker:

    sbt run

To execute the test suite:

    sbt test
