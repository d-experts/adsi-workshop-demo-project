#!/bin/bash
# ローカル開発サーバーの起動。
#
#   bash scripts/dev.sh            backend + frontend を同時起動
#   bash scripts/dev.sh backend    backend のみ
#   bash scripts/dev.sh frontend   frontend のみ
#
# backend は H2 インメモリ（workshop プロファイル）で起動するため
# Docker / PostgreSQL は不要。frontend は next dev なので
# ファイル保存でそのままブラウザに反映される（ビルド不要）。
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
FRONTEND_DIR="$PROJECT_DIR/packages/frontend"

BACKEND_PORT=8080
FRONTEND_PORT=3000

start_backend() {
  echo "=== backend (H2, :$BACKEND_PORT) ==="
  bash "$SCRIPT_DIR/gradlew.sh" bootRun --args='--spring.profiles.active=workshop'
}

start_frontend() {
  echo "=== frontend (next dev, :$FRONTEND_PORT) ==="
  cd "$FRONTEND_DIR"
  npm run dev
}

# frontend の依存が未インストールだと next dev が失敗するため先に確認する
ensure_frontend_deps() {
  if [ ! -d "$FRONTEND_DIR/node_modules" ]; then
    echo "=== installing frontend dependencies ==="
    (cd "$FRONTEND_DIR" && npm install)
  fi
}

case "${1:-all}" in
  backend)
    start_backend
    ;;
  frontend)
    ensure_frontend_deps
    start_frontend
    ;;
  all)
    ensure_frontend_deps

    # 片方を Ctrl+C で止めたら両方止める
    trap 'kill 0' EXIT INT TERM

    start_backend &
    start_frontend &

    echo ""
    echo "=================================================="
    echo "  frontend  http://localhost:$FRONTEND_PORT"
    echo "  backend   http://localhost:$BACKEND_PORT"
    echo "  login     tanaka@example.com / password123"
    echo "  Ctrl+C for stop"
    echo "=================================================="
    echo ""

    wait
    ;;
  *)
    echo "Usage: $0 [all|backend|frontend]" >&2
    exit 1
    ;;
esac
