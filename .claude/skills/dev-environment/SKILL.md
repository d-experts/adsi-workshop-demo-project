---
name: dev-environment
description: ローカル開発の起動・アクセス方法。アプリ起動、環境確認、新規セットアップ時に使う。
---

# 開発環境 — 起動とアクセス

## 初回セットアップ

```bash
npm run setup    # backend Gradle + frontend npm + infra npm
```

## 起動

```bash
npm run dev      # backend(:8080) + frontend(:3000) を同時起動。Ctrl+C で両方停止
```

ブラウザで **http://localhost:3000** を開く。ログインは `tanaka@example.com` / `password123`
（他のシードユーザーは `packages/backend/src/main/resources/db/seed/V1000__seed_data.sql`）。

- frontend は `next dev`。**ファイルを保存すればそのまま反映される**（ビルド・再起動不要）
- backend は H2 インメモリ。**Docker / PostgreSQL は不要**。再起動でシード状態に戻る
- Java コードを変えたときは backend の再起動が必要（`Ctrl+C` → `npm run dev`）

## 個別起動 / その他

| 目的 | コマンド | アクセス先 |
|------|---------|-----------|
| backend のみ (H2) | `npm run dev:backend` | http://localhost:8080 |
| frontend のみ | `npm run dev:frontend` | http://localhost:3000 |
| backend (PostgreSQL) | `npm run db:up` → `npm run boot` | http://localhost:8080 |
| backend チェック | `npm run check:backend` | — |
| frontend lint | `npm run lint:frontend` | — |
| テスト | `npm run test:backend` / `npm run test:frontend` | — |

## 構成

```
ブラウザ :3000 ─ Next.js dev ─ /api/* を rewrites で転送 ─ Spring Boot :8080 ─ H2 (in-memory)
```

frontend の fetch は `/api/...` の相対パスで書く（`packages/frontend/next.config.ts` の
rewrites が backend に転送する）。CORS 設定は `application-workshop.yaml`。

## 環境の違い

| 環境 | Frontend | Backend DB |
|------|----------|------------|
| ローカル | `npm run dev`（next dev） | H2 インメモリ |
| 本番 | static export（S3 + CloudFront） | RDS 等 |

## つまずき

| 症状 | 原因 / 対処 |
|------|------------|
| ポートが使用中で起動しない | 前回のプロセスが残存。`:3000` / `:8080` の PID を kill してから再実行 |
| `./gradlew` が見つからない | npm scripts は `scripts/gradlew.sh` 経由で OS 差を吸収済み。直接叩くなら Windows は `gradlew.bat` |
| API が 404 / 繋がらない | backend が起動しているか確認（`curl http://localhost:8080/actuator/health` → `{"status":"UP"}`） |
