# Portfolio 2.0 Production Runtime

## Final Topology

The production path intentionally contains two Nginx layers:

- **Host Nginx** owns public ports `80/443`, TLS, Certbot, HTTP-to-HTTPS behavior, and domain virtual hosts.
- **Docker `portfolio-nginx`** owns Portfolio routing on container port `80`, bound on the VPS as `127.0.0.1:8081`.

The Docker gateway routes `/api/*` to `portfolio:8080` and all other public routes to `frontend:4000`. Spring Boot and Angular SSR are not directly Internet-exposed. Angular SSR uses `API_ORIGIN=http://portfolio:8080`; browser API requests remain same-origin through the two Nginx layers.

## First Cutover

The repository does not mutate the host Nginx automatically. After the release is committed, pushed, and deployed to the VPS:

1. Start the Docker topology with the default gateway `127.0.0.1:8081`.
2. From the VPS, run `SMOKE_BASE_URL=http://127.0.0.1:8081 ./scripts/smoke.sh`.
3. Update only the Portfolio host virtual host upstream from `http://localhost:8080` to `http://127.0.0.1:8081`, preserving `server_name`, TLS, Certbot directives, HTTPS, and proxy headers.
4. Run `sudo nginx -t`. If it fails, do not reload; keep the existing public configuration active.
5. After successful validation, run `sudo systemctl reload nginx`.
6. Validate `https://ludovic-brot.fr` and `https://www.ludovic-brot.fr` externally.

The Docker gateway must be healthy before the host upstream changes. The host Nginx remains the only Internet-facing proxy; Docker Nginx must never bind VPS ports `80` or `443`.

## Normal Deployment

GitHub Actions validates Spring tests, frontend tests, the Angular SSR build, and the application container builds. The existing SSH deployment then runs `scripts/deploy-production.sh`, which records the current Git revision, pulls `main`, rebuilds Compose, and smoke-tests `127.0.0.1:8081`. No privileged host-Nginx mutation is automated.

## Rollback

Application rollback restores the recorded Git revision, rebuilds the Docker topology, and never runs `docker compose down -v`. PostgreSQL, MongoDB, and uploads volumes are preserved. Image pruning remains disabled.

During the first cutover, rollback has two layers: restore the previous Git/Docker application revision, then restore the host Nginx upstream to `http://localhost:8080` if the previous production application still expects the historical direct Spring endpoint. Validate with `sudo nginx -t` before `sudo systemctl reload nginx`. If the host Nginx was already permanently aligned to `127.0.0.1:8081`, only the application rollback is required.

There is no database/schema migration in this release: `NO_DATABASE_MIGRATION_REQUIRED`.
