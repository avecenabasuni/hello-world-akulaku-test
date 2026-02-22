# Hello World - Akulaku SRE Technical Test

Spring Boot application deployed on Kubernetes with Jenkins CI/CD pipeline.

## Tech Stack

| Component     | Technology                                   |
| ------------- | -------------------------------------------- |
| Application   | Spring Boot 4.0.3, Java 17                   |
| Build         | Maven                                        |
| Container     | Docker (multi-stage build)                   |
| Orchestration | Kubernetes (Docker Desktop)                  |
| CI/CD         | Jenkins Pipeline                             |
| Observability | Spring Boot Actuator + Micrometer Prometheus |

## Project Structure

```
hello-world/
├── src/main/java/com/akulaku/helloworld/
│   ├── HelloWorldApplication.java    # Main application
│   └── HelloController.java         # REST controller (/hello)
├── src/test/java/com/akulaku/helloworld/
│   ├── HelloWorldApplicationTests.java  # Context load test
│   └── HelloControllerTest.java         # Controller unit tests
├── src/main/resources/
│   └── application.properties        # Actuator + graceful shutdown config
├── k8s/
│   ├── deployment.yaml               # 2 replicas, security context, probes
│   └── service.yaml                  # NodePort service (30080)
├── Dockerfile                        # Multi-stage, non-root user
├── Dockerfile.jenkins                # Custom Jenkins with Maven + kubectl
├── Jenkinsfile                       # 6-stage CI/CD pipeline
└── pom.xml                           # Dependencies + build config
```

## Prerequisites

- Java 17
- Maven 3.9+
- Docker Desktop with Kubernetes enabled
- Jenkins (optional, for CI/CD)

## Quick Start

### 1. Run Locally

```bash
mvn spring-boot:run
```

Endpoints:

- <http://localhost:8080/hello> — Hello World response
- <http://localhost:8080/actuator/health> — Health check (Actuator)
- <http://localhost:8080/actuator/prometheus> — Prometheus metrics

### 2. Run Tests

```bash
mvn clean verify
```

### 3. Build Docker Image

```bash
docker build -t hello-world-app:latest .
```

### 4. Deploy to Kubernetes

```bash
# Make sure Docker Desktop Kubernetes is active
kubectl config use-context docker-desktop

# Deploy
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml

# Wait for rollout
kubectl rollout status deployment/hello-world-deployment --timeout=120s

# Verify
kubectl get pods -l app=hello-world
```

Access via:

- `kubectl port-forward service/hello-world-service 8080:80`
- Or directly: <http://localhost:30080/hello> (NodePort)

### 5. Jenkins CI/CD Setup

```bash
# Build custom Jenkins image with Maven + kubectl
docker build -t jenkins-custom -f Dockerfile.jenkins .

# Run Jenkins (port 9090 to avoid conflict with Spring Boot)
docker run -d --name jenkins \
  -p 9090:8080 -p 50000:50000 \
  -u root \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v $HOME/.kube:/root/.kube \
  jenkins-custom
```

Access Jenkins at <http://localhost:9090>, create a Pipeline job pointing to this repository.

## CI/CD Pipeline Stages

```
Checkout → Build & Test → Package → Docker Build → Deploy to K8s → Verify
```

| Stage            | Description                                           |
| ---------------- | ----------------------------------------------------- |
| **Checkout**     | Clone from Git repository                             |
| **Build & Test** | `mvn clean verify` — compile + unit tests             |
| **Package**      | `mvn package` — create JAR artifact                   |
| **Docker Build** | Build image with version tag (`BUILD_NUMBER-GIT_SHA`) |
| **Deploy**       | Apply K8s manifests, wait for rollout                 |
| **Verify**       | Confirm pods and services are running                 |
| **On Failure**   | Auto-rollback via `kubectl rollout undo`              |

## SRE Features

### Security

- Container runs as **non-root user** (`appuser`)
- Kubernetes `securityContext`: `readOnlyRootFilesystem`, `allowPrivilegeEscalation: false`, drop all capabilities
- Writable `/tmp` via `emptyDir` volume (required for JVM)

### Observability

- **Health checks** via Spring Boot Actuator (`/actuator/health`)
- **Prometheus metrics** via Micrometer (`/actuator/prometheus`)
- Kubernetes probes:
  - `startupProbe` — tolerates JVM cold start (up to 120s)
  - `readinessProbe` — controls traffic routing
  - `livenessProbe` — detects deadlocks

### Reliability

- **2 replicas** for high availability
- **Graceful shutdown** (30s drain period)
- **Resource limits** — CPU: 100m-500m, Memory: 128Mi-256Mi
- **Auto-rollback** on pipeline failure

## Useful Commands

```bash
# Check pod status
kubectl get pods -l app=hello-world

# View pod logs
kubectl logs -f deployment/hello-world-deployment

# Check health from inside cluster
kubectl exec deployment/hello-world-deployment -- wget -qO- http://localhost:8080/actuator/health

# Manually rollback
kubectl rollout undo deployment/hello-world-deployment

# Scale replicas
kubectl scale deployment/hello-world-deployment --replicas=3
```

## Author

Avecena Basuni - SRE Technical Test for PT Akulaku Silvrr Indonesia
