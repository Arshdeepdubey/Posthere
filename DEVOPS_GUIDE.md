# DevOps Setup Guide: ArgoCD, Docker & Kubernetes Deployment

This guide walks through deploying the Posthere Spring Boot microservice using Docker, ArgoCD, and Kubernetes with health checks.

## Prerequisites

- Docker & Docker Compose installed
- Kubernetes cluster (minikube, kind, or cloud provider)
- kubectl configured to access your cluster
- Git CLI

## Part 1: Running ArgoCD in Docker Container

### Step 1: Start ArgoCD and Posthere with Docker Compose

```bash
docker-compose up -d
```

This starts:
- **posthere-microservice**: Spring Boot app on `http://localhost:8080`
- **argocd-server**: ArgoCD on `https://localhost:8443` and `http://localhost:8080`

### Step 2: Access ArgoCD UI

```bash
# Get initial ArgoCD admin password
docker exec argocd-server argocd admin initial-password -n argocd

# Access ArgoCD
# URL: https://localhost:8443
# Username: admin
# Password: <from above command>
```

### Step 3: Verify Microservice Health

```bash
# Check liveness probe
curl http://localhost:8080/actuator/health/liveness

# Check readiness probe
curl http://localhost:8080/actuator/health/readiness

# Get all health details
curl http://localhost:8080/actuator/health
```

## Part 2: Building Docker Image

### Step 1: Build the Docker Image

```bash
docker build -t posthere:latest .
```

The Dockerfile uses multi-stage build:
- **Builder stage**: Maven builds the application
- **Runtime stage**: Eclipse Temurin JRE runs the JAR
- **Health check**: Built-in liveness probe

### Step 2: Verify Image

```bash
docker images | grep posthere
docker run -p 8080:8080 posthere:latest
```

## Part 3: Deploying to Kubernetes with ArgoCD

### Step 1: Install ArgoCD on Kubernetes Cluster

```bash
# Create argocd namespace
kubectl create namespace argocd

# Install ArgoCD
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

# Wait for ArgoCD to be ready
kubectl rollout status deployment/argocd-server -n argocd

# Get ArgoCD initial password
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d
```

### Step 2: Access ArgoCD UI

```bash
# Port forward ArgoCD UI
kubectl port-forward svc/argocd-server -n argocd 8443:443

# URL: https://localhost:8443
# Username: admin
# Password: <from above>
```

### Step 3: Add Git Repository to ArgoCD

Using ArgoCD CLI:

```bash
argocd repo add https://github.com/Arshdeepdubey/Posthere \
  --username <github-username> \
  --password <github-token>
```

Or through ArgoCD UI:
- Settings → Repositories → Connect Repo

### Step 4: Deploy Application with ArgoCD

```bash
# Apply ArgoCD Application manifest
kubectl apply -f k8s/argocd-app.yaml

# Or create via CLI
argocd app create posthere-app \
  --repo https://github.com/Arshdeepdubey/Posthere \
  --revision devops/argocd-k8s-deployment \
  --path k8s \
  --dest-server https://kubernetes.default.svc \
  --dest-namespace posthere
```

### Step 5: Monitor Deployment

```bash
# Watch ArgoCD application sync status
argocd app get posthere-app
argocd app wait posthere-app

# Or check via kubectl
kubectl get all -n posthere
kubectl get deployment -n posthere -o wide
kubectl get pods -n posthere -o wide
```

## Part 4: Checking Health, Liveness & Readiness Probes

### Monitor Pod Health

```bash
# Check pod status and health
kubectl get pods -n posthere

# Describe a pod to see probe status
kubectl describe pod <pod-name> -n posthere

# View pod logs for health issues
kubectl logs <pod-name> -n posthere
kubectl logs <pod-name> -n posthere --previous  # If crashed
```

### Test Health Endpoints

```bash
# Port forward to the service
kubectl port-forward svc/posthere-service -n posthere 8080:80

# Liveness probe endpoint
curl http://localhost:8080/actuator/health/liveness

# Readiness probe endpoint  
curl http://localhost:8080/actuator/health/readiness

# Full health details
curl http://localhost:8080/actuator/health
```

### Probe Configuration Details

The deployment includes three types of probes:

**1. Liveness Probe** (Restarts stuck containers)
- Path: `/actuator/health/liveness`
- Initial Delay: 30s
- Period: 10s
- Timeout: 5s
- Failure Threshold: 3

**2. Readiness Probe** (Stops routing traffic to unhealthy pods)
- Path: `/actuator/health/readiness`
- Initial Delay: 20s
- Period: 5s
- Timeout: 3s
- Failure Threshold: 3

**3. Startup Probe** (Waits for app to start before other probes)
- Path: `/actuator/health/liveness`
- Initial Delay: 0s
- Period: 10s
- Timeout: 3s
- Failure Threshold: 30 (30 attempts = ~5 min startup window)

### Monitor Horizontal Pod Autoscaler

```bash
# Check HPA status
kubectl get hpa -n posthere

# Detailed HPA info
kubectl describe hpa posthere-hpa -n posthere

# Watch HPA in action
kubectl get hpa -n posthere --watch
```

## Part 5: Troubleshooting

### Pod Stuck in CrashLoopBackOff

```bash
# Check logs
kubectl logs <pod-name> -n posthere --tail=50
kubectl logs <pod-name> -n posthere --previous

# Describe pod for error details
kubectl describe pod <pod-name> -n posthere
```

### Readiness Probe Failing

```bash
# Verify service is accessible from pod
kubectl exec <pod-name> -n posthere -- curl localhost:8080/actuator/health/readiness

# Check if port is listening
kubectl exec <pod-name> -n posthere -- netstat -tuln | grep 8080
```

### Service Not Getting Traffic

```bash
# Check service endpoints
kubectl get endpoints posthere-service -n posthere

# Verify pods are in Running and Ready state
kubectl get pods -n posthere -o wide
```

## Part 6: Clean Up

```bash
# Delete ArgoCD application
argocd app delete posthere-app

# Delete namespace and all resources
kubectl delete namespace posthere

# Stop Docker containers
docker-compose down

# Remove Docker image
docker rmi posthere:latest
```

## Key Files

- **Dockerfile**: Multi-stage build with health check
- **docker-compose.yml**: Local testing with ArgoCD
- **application-docker.properties**: Docker-specific Spring config with Actuator endpoints
- **k8s/deployment.yaml**: Kubernetes manifests with probes and HPA
- **k8s/argocd-app.yaml**: ArgoCD Application for GitOps sync

## API Endpoints

- `GET /api/details`: Retrieve all details
- `GET /api/details/{id}`: Retrieve details by ID
- `POST /api/details`: Create new details
- `GET /actuator/health`: Overall health status
- `GET /actuator/health/liveness`: Liveness probe
- `GET /actuator/health/readiness`: Readiness probe
