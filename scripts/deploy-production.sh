#!/usr/bin/env sh
set -eu

app_root="${APP_ROOT:-/var/www/Portfolio-java}"
state_dir="${DEPLOY_STATE_DIR:-$app_root/.deployment-state}"
smoke_base_url="${SMOKE_BASE_URL:-http://127.0.0.1:${NGINX_PORT:-8081}}"

cd "$app_root"
mkdir -p "$state_dir"

git checkout main
previous_revision="${PREVIOUS_REVISION:-$(git rev-parse HEAD)}"
printf '%s\n' "$previous_revision" > "$state_dir/previous-revision"

if [ "${SKIP_PULL:-0}" != "1" ]; then
  git pull --ff-only origin main
fi

rollback() {
  echo "Restoring Git revision $previous_revision."
  git checkout "$previous_revision"
  docker compose up -d --build --remove-orphans
}

if ! docker compose up -d --build --remove-orphans; then
  rollback
  exit 1
fi

if SMOKE_BASE_URL="$smoke_base_url" ./scripts/smoke.sh; then
  echo "Deployment completed at $(git rev-parse HEAD)."
  exit 0
fi

echo "Smoke validation failed; rolling back."
rollback
echo "Rollback restored the previous application revision without removing database volumes."
exit 1
