#!/usr/bin/env bash
# STANDALONE MODE ONLY.
# Copies the database from the MongoDB on this computer into the MongoDB container, so the
# dockerised app shows the same users, rooms and bookings. It REPLACES the container's copy of
# that database; your original database is only read, never changed.
set -euo pipefail
cd "$(dirname "$0")/.."

# read a value from .env without executing the file
env_value() { grep -E "^$1=" .env 2>/dev/null | head -n1 | cut -d= -f2- || true; }

SOURCE_URI="${SOURCE_MONGODB_URI:-$(env_value SOURCE_MONGODB_URI)}"
DB="${MONGODB_DATABASE:-$(env_value MONGODB_DATABASE)}"
IMAGE="${MONGO_IMAGE:-$(env_value MONGO_IMAGE)}"

DB="${DB:-JavaBookingProject}"
IMAGE="${IMAGE:-mongo:7}"

COMPOSE=(docker compose -f docker-compose.standalone.yml)

echo "Starting the MongoDB container..."
"${COMPOSE[@]}" up -d --wait mongo

if command -v mongodump >/dev/null 2>&1; then
  # fastest and most reliable: use the tool installed on this computer
  SOURCE_URI="${SOURCE_URI:-mongodb://localhost:27017}"
  echo "Copying database '${DB}' from ${SOURCE_URI} (mongodump on this computer) ..."
  mongodump --uri="${SOURCE_URI}" --db="${DB}" --archive \
    | "${COMPOSE[@]}" exec -T mongo mongorestore --archive --drop --nsInclude="${DB}.*"
else
  # no mongodump here: run it inside a container and reach this computer as host.docker.internal
  SOURCE_URI="${SOURCE_URI:-mongodb://host.docker.internal:27017}"
  echo "Copying database '${DB}' from ${SOURCE_URI} (mongodump in a container) ..."
  docker run --rm --add-host=host.docker.internal:host-gateway "${IMAGE}" \
    mongodump --uri="${SOURCE_URI}" --db="${DB}" --archive \
    | "${COMPOSE[@]}" exec -T mongo mongorestore --archive --drop --nsInclude="${DB}.*"
fi

echo "Done. Restart the backend so it re-reads the data:"
echo "  docker compose -f docker-compose.standalone.yml restart backend"
