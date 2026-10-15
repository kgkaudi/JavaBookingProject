# Running the project with Docker

Everything (backend, website, database connection) starts with one command.

| What | Address |
|------|---------|
| **Website** | http://localhost:5173 |
| API (the website uses it through `/api`; port published for debugging) | http://localhost:8080 |
| Database browser (optional) | http://localhost:8081 |

## Which database does it use?

Your backend always used the database **`JavaBookingProject`** (collections `users`, `rooms`, `bookings`,
`password_tokens`). The connection string is your `mongodb://localhost:27017/KostasEvents`, written as
`mongodb://host.docker.internal:27017/KostasEvents` because inside a container `localhost` is the container
itself. The app then reads the database named `MONGODB_DATABASE=JavaBookingProject` on that server.
If your data lives in a database called `KostasEvents`, change that one line in `.env`.

To check what is on your local server:

```bash
mongosh --quiet --eval 'db.getMongo().getDBNames()'
mongosh --quiet JavaBookingProject --eval 'db.getCollectionNames()'
```

## Quick start - use the MongoDB already running on this computer

Prerequisites: Docker (Desktop or Engine) with Compose, and your local MongoDB running on port 27017.
Stop `mvn spring-boot:run` and `npm run dev` first: the containers need ports 8080 and 5173.

**Step 1 - check that containers can reach your MongoDB** (a few seconds; downloads a small image once):

```bash
docker run --rm --add-host=host.docker.internal:host-gateway mongo:7 \
  mongosh "mongodb://host.docker.internal:27017" --quiet --eval "db.adminCommand('ping')"
```

You should see `{ ok: 1 }`. If it fails or hangs, skip to **Standalone mode** below - it always works.

**Step 2 - start everything:**

```bash
./scripts/init-env.sh              # once: creates .env with a random JWT secret
docker compose up --build -d
docker compose ps                  # backend becomes "healthy" after ~30 s
```

Open http://localhost:5173 and log in with your existing users - this is your live local database.

> **Docker Desktop vs. Docker Engine.** Docker Desktop (also on Linux) runs containers inside a small VM and
> provides `host.docker.internal` automatically. With plain Docker Engine on Linux that name points at the
> `docker0` bridge (172.17.0.1), and a MongoDB that only listens on `127.0.0.1` refuses it. Either make
> `mongod` also listen on that address (`bindIp: 127.0.0.1,172.17.0.1` in `mongod.conf` - never expose a
> MongoDB without authentication to a network) or use standalone mode.

## Plain Docker Engine on Linux (instead of Docker Desktop)

Docker Desktop runs everything in a virtual machine. If that VM does not work on your computer (see
"Docker Desktop crashes" below), install the normal **Docker Engine** instead: it runs containers directly on
Linux, with no VM. Follow https://docs.docker.com/engine/install/ubuntu/ and the "post-installation steps"
(`sudo usermod -aG docker $USER`, then log out and in). Both can be installed side by side; choose which one the
`docker` command talks to with `docker context use default` (Engine) or `docker context use desktop-linux`.

With Engine the backend can share your computer's network, so it reaches MongoDB on `localhost:27017` without
any `mongod.conf` changes:

```bash
./scripts/init-env.sh
docker compose -f docker-compose.yml -f docker-compose.hostnet.yml up --build -d
```

Open http://localhost:5173. (Add `--profile tools` for the database browser. Needs Compose 2.24+:
`docker compose version`.) Do not use `docker-compose.hostnet.yml` with Docker Desktop: there "the host" is the VM.

## See the database in a browser

```bash
docker compose --profile tools up -d
```

Open http://localhost:8081 (login `admin` and the password printed by `init-env.sh`, also stored in `.env` as
`MONGO_EXPRESS_PASSWORD`) and choose `JavaBookingProject`. It is only reachable from your own computer.
MongoDB Compass works too: connect to `mongodb://localhost:27017` (or `:27018` in standalone mode).

## Standalone mode - its own MongoDB in a container (works everywhere)

```bash
./scripts/init-env.sh
docker compose -f docker-compose.standalone.yml up --build -d
./scripts/import-local-db.sh       # optional: copies your current local database into the container
docker compose -f docker-compose.standalone.yml restart backend
```

The container's data lives in the `mongo-data` volume. `import-local-db.sh` only *reads* your local database;
it replaces the container's copy of `JavaBookingProject`. Without the import you start with an empty database
(set `SEED_ENABLED=true` and the two seed passwords in `.env` to create demo data).

## Everyday commands

| Task | Command (add `-f docker-compose.standalone.yml` for standalone mode) |
|------|---------|
| Follow the logs | `docker compose logs -f backend` |
| Rebuild after code changes | `docker compose up --build -d` |
| Stop | `docker compose down` |
| Stop and delete the standalone database | `docker compose -f docker-compose.standalone.yml down -v` |
| Run the backend tests | `cd backend/BookingProject && mvn test` |
| Run the frontend tests | `cd frontend && npm test` |

## Settings (`.env`)

| Variable | Meaning |
|----------|---------|
| `JWT_SECRET` | **Required.** Random, 32+ characters. `init-env.sh` generates it. Changing it logs everybody out. |
| `LOCAL_MONGODB_URI` / `MONGODB_DATABASE` | Server and database (see above). |
| `FRONTEND_PORT` | Port of the website (default 5173). |
| `SEED_ENABLED`, `SEED_ADMIN_PASSWORD`, `SEED_TEST_USER_PASSWORD` | Optional demo data. |

An older `.env` that still contains a `MONGODB_URI=` line is fine: that name is no longer used.
Add `.env` to your root `.gitignore`: it contains secrets.

## How it fits together

```
browser --> nginx (website, port 5173)
              |- /            static React build (unknown paths -> index.html)
              '- /api/...     forwarded to --> Spring Boot (8080) --> MongoDB
```

The website calls `/api/...` on its own address, so there is no CORS and no backend URL baked into the build.

## Replacing an older frontend Dockerfile

If your `frontend/` folder already had a `Dockerfile` and an `nginx.conf`, this setup replaces them:

- `frontend/Dockerfile` is overwritten (the old one copied `dist` from your computer instead of from the
  build stage, so the image only worked after a manual `npm run build`).
- `frontend/nginx.conf` is **no longer used** - the config is now `frontend/nginx/default.conf.template`.
  Delete the old file once you have copied any custom rules from it into the template.
- Make sure `frontend/package-lock.json` exists: the image installs with `npm ci`.

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| `failed to receive status: rpc error ... EOF` while building or pulling | The Docker engine itself dropped the connection (not a problem in these files). See "Docker Desktop crashes" and "Docker engine dropped out" below. |
| `JWT_SECRET is not set` | Run `./scripts/init-env.sh`. |
| Backend container restarts with `no main manifest attribute, in /app/app.jar` (or the build stops with "not an executable Spring Boot jar") | `pom.xml` is missing the `spring-boot-maven-plugin`, so `mvn package` builds a plain jar. Add it inside `<build><plugins>` (snippet below), then `docker compose up --build -d`. |
| `port is already allocated` / `address already in use` | Stop `mvn spring-boot:run` / `npm run dev`, or set another `FRONTEND_PORT`. |
| Backend log: cannot connect to `host.docker.internal:27017` | Run the Step 1 test above. If it fails, use standalone mode (or see the Docker Engine note). |
| Backend exits with `E11000 duplicate key` while creating indexes | Two users share an email or two rooms share a number. Remove the duplicates, then restart. |
| Website loads but every API call is `502` | The backend is still starting (wait ~30 s) or crashed: `docker compose logs backend`. |
| Logged out after switching database | Tokens are checked against the users in the database you are now using; log in again. |

### Missing `spring-boot-maven-plugin` in pom.xml

```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
</plugin>
```

### Docker Desktop crashes (`qemu: process terminated unexpectedly: signal: aborted`)

Docker Desktop for Linux starts a small virtual machine with QEMU/KVM. When QEMU aborts, every Docker command
fails - including the `EOF` errors during a build. Check these, in this order:

1. **Is KVM usable by you?**

   ```bash
   ls -l /dev/kvm                    # must exist; group should be "kvm"
   groups | tr ' ' '\n' | grep -x kvm   # you must be in the kvm group
   lsmod | grep kvm                  # kvm_intel or kvm_amd should be listed
   lscpu | grep -i virtualization    # VT-x (Intel) or AMD-V
   ```

   Fixes: `sudo usermod -aG kvm $USER` and log out/in; `sudo modprobe kvm_intel` (or `kvm_amd`); if
   virtualization is off, enable "Intel Virtualization Technology" / "SVM Mode" in the BIOS.
2. **Memory and disk.** `free -h` and `df -h ~`. The VM needs RAM that is actually free. Lower the memory and
   CPU limits in Docker Desktop -> Settings -> Resources (if it will not open, find the keys with
   `grep -i -E "memory|cpus|disk" ~/.docker/desktop/settings*.json`, edit them with Desktop closed, and start it again).
3. **Look at the log.** `grep -ri -E "kvm|qemu|memory" ~/.docker/desktop/log | tail -n 20` usually names the cause.
4. **Last resort for Desktop:** Docker Desktop -> Troubleshoot -> *Reset to factory defaults*, or reinstall it.
   This deletes images, containers and volumes *inside Docker*; your local MongoDB is not touched.
5. **Or skip the VM:** use plain Docker Engine (section above).

### Docker engine dropped out (`EOF`)

1. Is it still running? `docker info` - if that errors, restart Docker Desktop
   (`systemctl --user restart docker-desktop`, or quit and reopen it).
2. Enough room? `docker system df` and `df -h`. In Docker Desktop -> Settings -> Resources, give it at least
   4 GB memory and make sure the virtual disk is not full. Free space with `docker system prune`.
3. Download the base images one at a time, which shows exactly where a download fails (partly downloaded
   layers are kept, so retries continue where they stopped):

```bash
docker pull maven:3.9-eclipse-temurin-21
docker pull eclipse-temurin:21-jre
docker pull node:22-slim
docker pull nginx:1.27-alpine
```

4. Run `docker compose up --build -d` again.
