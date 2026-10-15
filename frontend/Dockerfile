# ---------- build stage ----------
# Debian-based (glibc) image: matches the platform package-lock.json was created on,
# so the right native build tools (Vite/rolldown, esbuild) are installed.
FROM node:22-slim AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund

COPY . .

# Empty = the browser calls the API on the same address as the page ("/api/..."),
# and nginx forwards it to the backend. No CORS and no hard-coded backend URL.
ARG VITE_API_URL=""
ENV VITE_API_URL=$VITE_API_URL

RUN npm run build

# ---------- runtime stage ----------
FROM nginx:1.27-alpine

# nginx renders /etc/nginx/templates/*.template with the container's environment variables
COPY nginx/default.conf.template /etc/nginx/templates/default.conf.template
COPY nginx/security-headers.conf /etc/nginx/snippets/security-headers.conf
COPY --from=build /app/dist /usr/share/nginx/html

# where /api/ requests are forwarded (compose overrides this)
ENV BACKEND_URL=http://backend:8080

EXPOSE 80

HEALTHCHECK --interval=15s --timeout=3s --start-period=5s --retries=3 \
    CMD ["wget", "-q", "--spider", "http://127.0.0.1/"]
