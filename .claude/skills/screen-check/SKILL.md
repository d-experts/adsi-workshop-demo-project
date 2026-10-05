---
name: screen-check
description: 起動中のアプリを playwright-cli でブラウザ操作して画面・操作フローを確認し、スクリーンショットを証跡として保存する。「画面で確認して」「動作確認して」「スクショを証跡に残して」と頼まれたとき、Issue 対応の検証に使う。
---

# 画面確認（playwright-cli）

起動中のアプリを `playwright-cli` で操作し、確認した画面のスクリーンショットを残す。
Issue 対応なら、Issue ファイルの「検証」欄に画像へのリンクを追記する。

## 前提

| 項目 | 値 |
|------|----|
| Frontend | http://localhost:3000（`npm run dev`） |
| Backend | http://localhost:8080（`npm run boot:workshop` で H2、または `npm run boot`） |
| ログイン画面 | http://localhost:3000/login |
| 管理者 | `admin@example.com` / `demo1234` |
| 一般社員 | `tanaka@example.com` / `password123` |

- アカウントはシードデータ（`packages/backend/src/main/resources/db/seed/V1000__seed_data.sql`）のデモ用。管理画面（`/admin/*`）や承認（`/approvals`）は管理者、打刻・修正申請は一般社員で確認する
- 主な画面: `/dashboard`、`/attendance`、`/history`、`/corrections`、`/corrections/new`、`/approvals`、`/team`、`/admin/employees`、`/admin/departments`、`/admin/reports`
- 起動手順は `dev-environment` スキル

## 手順

### 1. 準備

```bash
playwright-cli --version || npm install -g @playwright/cli@latest
curl -s -o /dev/null -w '%{http_code}\n' http://localhost:3000/login   # 200 なら起動済み
```

アプリが起動していなければ、ユーザーに起動を依頼するか、バックグラウンドで起動して待つ。

### 2. 確認観点を決める

操作する前に、確認する観点を箇条書きにする。Issue 対応なら「期待する動作」や受け入れ条件から作る。

### 3. ブラウザを開いてログイン

```bash
playwright-cli open http://localhost:3000/login --browser=msedge --headed
playwright-cli snapshot
playwright-cli fill <メール欄の ref> "admin@example.com"
playwright-cli fill <パスワード欄の ref> "demo1234"
playwright-cli click <ログインボタンの ref>
playwright-cli snapshot
```

- `<ref>` は `snapshot` の出力にある `e15` のような要素 ID
- ワークショップの Windows 環境には Edge が入っているので `--browser=msedge` を指定する（Chrome がなくても動く）
- `--headed` を付けると参加者もブラウザの動きを見られる

### 4. 操作して証跡を残す

観点ごとに「操作 → `snapshot` で状態確認 → `screenshot`」を繰り返す。

```bash
playwright-cli goto http://localhost:3000/attendance
playwright-cli snapshot
playwright-cli click <ref>
playwright-cli screenshot --filename=issues/assets/0003-01-clock-in.png
```

| 状況 | 保存先 |
|------|--------|
| Issue 番号がわかっている | `issues/assets/NNNN-<連番>-<観点>.png`（例: `issues/assets/0003-01-clock-in.png`） |
| Issue がない | `reports/screenshots/<YYYYMMDD-HHMM>-<観点>.png` |

- `--filename` は **リポジトリルートからの相対パス**で、区切りは `/` にする（PowerShell・Git Bash どちらでも動く）。保存先フォルダがなければ先に `mkdir -p issues/assets` で作る
- `issues/` 直下は Issue の `.md` だけにする。画像は必ず `issues/assets/` に置く
- ファイル名は英小文字・数字・ハイフンのみ（日本語や空白を使わない）
- 画面全体が必要なら `--full-page` を付ける

### 5. 後片付け

```bash
playwright-cli close
```

## Issue への記録

対応中の Issue ファイルがあれば、その「検証」欄（なければ末尾）に結果と画像リンクを追記する。
リンクは Issue ファイルからの相対パス（`assets/...`）で書く。

```markdown
### 画面確認（YYYY-MM-DD、admin@example.com でログイン）

| 観点 | 結果 |
|------|------|
| 出勤ボタンを押すと出勤時刻が表示される | OK |

![出勤打刻後の画面](assets/0003-01-clock-in.png)
```

`issues/assets/` の画像は Issue ファイルと一緒にコミットする。`reports/screenshots/` は `.gitignore` 済み。

## 注意

- 確認対象（`localhost:3000` / `localhost:8080`）以外のサイトへ移動しない
- NG があれば証跡を添えて報告し、修正するかどうかはユーザーに確認する
- `&` を含む URL は PowerShell では `playwright-cli --% goto "http://localhost:3000/?a=1&b=2"` のように `--%` を付ける
- `snapshot` のたびに `.playwright-cli/` にファイルが溜まる（`.gitignore` 済み）

## 完了報告

- 確認した観点と結果（OK / NG）
- 保存したスクリーンショットのパス
- 追記した Issue ファイル（あれば）
