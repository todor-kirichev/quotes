# Quotes

A small full-stack application that serves a random quote and lets you add new ones.

The point of the project is practical: the same application deployed three different ways —
locally with Docker Compose, on a cloud VM, and on Kubernetes with a full GitOps cycle.

## Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 4.1, Spring Data JPA, Flyway |
| Frontend | Angular 21, Nginx |
| Database | PostgreSQL 16 |
| Containers | Docker, Docker Compose |
| Orchestration | Kubernetes, nginx-ingress |
| CI/CD | GitHub Actions, GitHub Container Registry |
| GitOps | Argo CD |

## Architecture

```
                    ┌──────────┐
   browser  ───────►│ gateway  │   single public entry point
                    └────┬─────┘
                         │
              ┌──────────┴──────────┐
              │                     │
         /    ▼                /api ▼
        ┌──────────┐          ┌──────────┐
        │ frontend │          │ backend  │
        │  Nginx   │          │  Spring  │
        └──────────┘          └────┬─────┘
                                   │
                              ┌────▼─────┐
                              │ postgres │
                              └──────────┘
```

The gateway is the only public entry point. It routes by path: `/api` to the backend,
everything else to the frontend. Because the browser sees a single origin,
**CORS never comes into play** — there is no `@CrossOrigin` in the code and no hardcoded hosts.

The gateway role is implemented differently per environment, but the principle is identical:

| Environment | Gateway |
|---|---|
| Local Compose | `nginx:alpine` container with `nginx/gateway.conf` |
| Cloud (Compose) | same container |
| Kubernetes | Ingress + nginx-ingress controller |

The frontend image is identical across all three — the Nginx inside it **only serves static
files** and has no knowledge of the backend.

## Configuration

The application reads everything from environment variables, with sensible defaults for
local development:

| Variable | Default | Description |
|---|---|---|
| `DB_HOST` | `localhost` | database host |
| `DB_PORT` | `5432` | port |
| `DB_NAME` | `quotes` | database name |
| `DB_USER` | `quotes` | user |
| `DB_PASSWORD` | *(empty)* | password |

This is exactly why one image works everywhere — only the wrapper changes.

## API

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/quotes/random` | random quote |
| `GET` | `/api/quotes` | all quotes |
| `POST` | `/api/quotes` | create a quote |
| `GET` | `/actuator/health` | health check |
| `GET` | `/actuator/health/readiness` | readiness probe |
| `GET` | `/actuator/health/liveness` | liveness probe |

Example:

```bash
curl -X POST http://localhost:8080/api/quotes \
  -H "Content-Type: application/json" \
  -d '{"text":"Simplicity is prerequisite for reliability.","author":"Dijkstra"}'
```

## Layout

```
.
├── backend/                  Spring Boot application
│   └── src/main/resources/db/migration/   Flyway migrations
├── frontend/                 Angular application
│   └── nginx/default.conf    serves static files only
├── nginx/gateway.conf        routing for the Compose environments
├── k8s/                      Kubernetes manifests (managed by Argo)
├── argocd/application.yaml   Argo CD Application (applied manually)
├── docker-compose.yml        local development (builds from source)
└── docker-compose.prod.yml   cloud (pulls images from ghcr)
```

---

## Running

### 1. Local development (no Docker)

Backend:

```bash
cd backend
./mvnw spring-boot:run
```

Frontend (separate terminal):

```bash
cd frontend
ng serve
```

Open `http://localhost:4200`. Requests to `/api` go through the dev proxy
(`frontend/proxy.conf.json`), so there is no CORS.

Requires a running database — e.g. `docker compose up -d postgres`.

### 2. Local with Docker Compose

```bash
docker compose up -d --build
```

Open `http://localhost:8080`.

Stopping:

```bash
docker compose down        # keeps the data
docker compose down -v     # drops the database too
```

### 3. Cloud (VM running Compose)

The server needs a `.env` file (not tracked in Git):

```bash
echo "DB_PASSWORD=your-password" > .env
chmod 600 .env
```

Then:

```bash
git pull
docker compose -f docker-compose.prod.yml pull
docker compose -f docker-compose.prod.yml up -d
```

Open the public IP. Images come prebuilt from ghcr — **nothing is built on the server**.

### 4. Kubernetes

Two things are not in Git and are created manually:

```bash
# Ingress controller
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.15.1/deploy/static/provider/cloud/deploy.yaml

# Secret holding the database password
kubectl create secret generic postgres-secret \
  --from-literal=POSTGRES_PASSWORD='your-password'
```

Then Argo CD:

```bash
kubectl create namespace argocd
kubectl apply -n argocd --server-side \
  -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
kubectl wait --for=condition=available --timeout=300s deployment/argocd-server -n argocd

kubectl apply -f argocd/application.yaml
```

From here Argo creates everything under `k8s/` on its own.

Open `http://localhost`.

Argo UI:

```bash
kubectl -n argocd get secret argocd-initial-admin-secret \
  -o jsonpath="{.data.password}" | base64 -d

kubectl port-forward svc/argocd-server -n argocd 8081:443
```

`https://localhost:8081`, user `admin`.

---

## CI/CD

```
git push (main)
   │
   ├─► CI   backend tests (against a real Postgres) + frontend build
   │
   └─► CD   build both images → ghcr
            tag with latest AND the commit SHA
            sed rewrites the tag in k8s/*-deployment.yaml
            bot commits back to Git ([skip ci])
                 │
                 └─► Argo CD picks up the new commit and syncs
                         │
                         └─► rolling update, no downtime
```

The double tagging is deliberate:

- **`latest`** — for the cloud Compose environment (`docker compose pull`)
- **commit SHA** — for Kubernetes, because Argo watches **Git, not the registry**.
  If the manifest doesn't change, Argo has nothing to apply.

Argo polls the repository roughly every 3 minutes. To speed it up: the **Refresh** button
in the UI.

---

## Useful commands

```bash
# status
kubectl get pods
kubectl get application quotes -n argocd

# logs
kubectl logs deployment/backend-deployment
kubectl logs deployment/backend-deployment | grep -i flyway

# why won't it start
kubectl describe pod <name>
kubectl get events --sort-by=.lastTimestamp

# does the Service have endpoints (empty = wrong selector, or pod not Ready)
kubectl describe service backend

# what port is the container actually listening on
kubectl exec -it <pod> -- sh -c "netstat -tlnp 2>/dev/null || ss -tlnp"
```

Demonstrating `selfHeal` — Argo reverts manual changes:

```bash
kubectl scale deployment frontend-deployment --replicas=5
kubectl get pods -w    # back to 2 after ~a minute
```
