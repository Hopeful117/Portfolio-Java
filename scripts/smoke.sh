#!/usr/bin/env sh
set -eu

base_url="${SMOKE_BASE_URL:-http://127.0.0.1:8081}"

check_contains() {
  url="$1"
  expected="$2"
  body=$(curl --fail --silent --show-error "$base_url$url")
  printf '%s' "$body" | grep -F "$expected" >/dev/null
}

check_contains / "Ludovic Brot"
check_contains /skills "Compétences"
check_contains /blog "Articles"
check_contains /api/public/articles '['
check_contains /api/public/projects '['

echo "Production smoke checks passed: homepage, nested route, articles, public API."
