---
name: dev-environment
description: ローカル開発での起動・アクセス方法。アプリ起動、環境確認、新規セットアップ時に使う。
---

# 開発環境 — 起動とアクセス

## 初回セットアップ

```bash
npm run setup    # backend Gradle + frontend npm + infra npm
```

frontend 単体で作業する場合も **`cd packages/frontend && npm install`** が必要。

## ローカル開発

| 目的 | コマンド | アクセス先 |
|------|---------|-----------|
| DB (PostgreSQL) 起動 / 停止 | `npm run db:up` / `npm run db:down` | localhost:5432 |
| Backend | `npm run boot` | http://localhost:8080 |
| Backend (H2, Docker 不要) | `npm run boot:workshop` | http://localhost:8080 |
| Frontend dev | `npm run dev` | http://localhost:3000 |
| Backend チェック | `npm run check:backend` | — |
| Frontend lint | `npm run lint:frontend` | — |

Frontend の dev server は **ホットリロードが効く**（`next dev` / Turbopack）。
コード変更のたびにビルドし直す必要はない。`/api/*` は `next.config.ts` の rewrites で
backend (`:8080`) に転送される。

## 環境の見分け

| 環境 | Frontend 起動 | Backend DB |
|------|----------------|-----------|
| ローカル dev | `npm run dev` | PostgreSQL (`db:up`) or H2 (`boot:workshop`) |
| 本番 | static export (`npm run build:frontend`) | RDS 等 |
