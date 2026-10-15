#!/usr/bin/env bash
# Creates .env from .env.example with freshly generated random secrets.
# Safe to run twice: an existing .env is never overwritten.
set -euo pipefail
cd "$(dirname "$0")/.."

if [ -f .env ]; then
  echo ".env already exists - leaving it untouched."
  exit 0
fi

cp .env.example .env

jwt_secret="$(openssl rand -base64 48 | tr -d '\n')"
express_password="$(openssl rand -hex 12)"

sed -i "s|^JWT_SECRET=.*|JWT_SECRET=${jwt_secret}|" .env
sed -i "s|^MONGO_EXPRESS_PASSWORD=.*|MONGO_EXPRESS_PASSWORD=${express_password}|" .env

echo "Created .env with a new JWT secret."
echo "Database browser login (if you start the 'tools' profile): admin / ${express_password}"
