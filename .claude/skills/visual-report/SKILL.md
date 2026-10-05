---
name: visual-report
description: コードベースや変更内容を mermaid 図・表・サマリーカード入りの単一 HTML レポートにまとめる。全体構成の可視化、Issue 対応の変更内容の説明、機能の処理フローの図解を頼まれたときに使う。
---

# ビジュアルレポート（HTML）

コードを読んで、**図と表で説明する単一 HTML** を `reports/` に書き出す。
ブラウザで開くだけで見られる（ビルド不要・1 ファイル完結）。

## いつ使う

| 用途 | 例 | 主な図 |
|------|----|--------|
| 全体構成の可視化 | 「このアプリの構成を図で説明して」 | アーキテクチャ図（flowchart）、ER 図、パッケージ構成表 |
| 変更内容のレポート | 「Issue #3 の対応内容をレポートにして」 | 変更前後のシーケンス図、変更ファイル表 |
| 機能の処理フロー | 「打刻の処理の流れを図にして」 | シーケンス図、状態遷移図、フローチャート |

## 手順

1. **対象を決める**: 依頼が曖昧なら、対象（全体 / 機能名 / 変更）を 1 回だけ確認する
2. **コードを読む**: 図に出すものはすべてコードで確認する
   - 全体構成: `packages/*/`、`docs/design/`、`build.gradle.kts`、`package.json`
   - 変更内容: `git diff` / `git log -p`（未コミットなら `git diff HEAD`）、対応する Issue ファイル
   - 処理フロー: 画面（`packages/frontend/src/app/`）→ API クライアント → Controller → Service → Repository → テーブル
3. **HTML を書く**: 下の構成とテンプレートに沿って 1 ファイルで書き出す
4. **報告する**: 出力パス・開き方・図の一覧を伝える

## 事実確認（必須）

- **コードに存在しないコンポーネント・テーブル・API を図に描かない**。推測で補わない
- クラス名・エンドポイント・テーブル名・カラム名はコードの表記どおりに書く
- 図の各要素には根拠のファイルパスを表やキャプションで添える
- 確認できなかった点は「未確認」と明記する

## 出力先

```
reports/<YYYYMMDD-HHMM>-<slug>.html
```

- 例: `reports/20261005-1430-architecture.html`、`reports/20261005-1500-issue-3-change.html`
- `<slug>` は英小文字のケバブケース（Windows でも扱えるよう ASCII のみ）
- 日時は Git Bash で `date +%Y%m%d-%H%M`
- `reports/` は `.gitignore` 済み（生成物のためコミットしない）。残したいときは Issue ファイル等に要点を書く

## レポート構成

1. **ヘッダ**: タイトル、対象（全体 / 機能 / 変更）、生成日時
2. **サマリーカード**: 3〜4 枚（例: 変更ファイル数、関係するテーブル数、API 数、要点 1 行）
3. **図**: 用途に合うものを 2〜4 個。各図に見出しと 1〜2 文の説明を付ける
4. **表**: 関連ファイル（パス・役割）、API（メソッド・パス・説明）、テーブル（名前・主なカラム）など
5. **補足**: 注意点・未確認事項

文章はすべて日本語。1 図に詰め込みすぎない（ノード 15 個程度まで。超えるなら分割）。

## mermaid の使い分け

| 図 | 記法 | 向いている内容 |
|----|------|---------------|
| アーキテクチャ | `flowchart LR` | Frontend → Backend → DB、パッケージ間の依存 |
| シーケンス | `sequenceDiagram` | 画面操作から DB までの呼び出し順 |
| ER | `erDiagram` | テーブルと関連（Flyway の `V*.sql` を根拠にする） |
| 状態遷移 | `stateDiagram-v2` | 申請の状態（申請中 → 承認 / 却下）など |
| フロー | `flowchart TD` | 分岐のある処理・バリデーション |

構文エラーを避けるコツ:

- ノードのラベルに `()` `[]` `:` `"` を含むときは `A["ラベル(補足)"]` のように `"` で囲む
- `erDiagram` の属性は `型 名前` の順（例: `uuid id PK`）。型に `()` を使わない
- `sequenceDiagram` の参加者名は英数字の ID にし、表示名は `participant FE as フロントエンド` で付ける

## HTML テンプレート

この骨組みを元に書く（スタイルは調整してよい）。

```html
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>タイトル</title>
<style>
  :root {
    color-scheme: light dark;
    --bg: #ffffff; --fg: #1f2328; --muted: #59636e;
    --card: #f6f8fa; --line: #d1d9e0; --accent: #0969da;
  }
  @media (prefers-color-scheme: dark) {
    :root { --bg: #0d1117; --fg: #e6edf3; --muted: #9198a1;
            --card: #161b22; --line: #3d444d; --accent: #4493f8; }
  }
  body { margin: 0; background: var(--bg); color: var(--fg);
         font-family: "Segoe UI", "Yu Gothic UI", "Meiryo", sans-serif; line-height: 1.7; }
  main { max-width: 1100px; margin: 0 auto; padding: 32px 24px; }
  h1 { margin: 0 0 4px; }
  .meta { color: var(--muted); font-size: 14px; }
  .cards { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 12px; margin: 24px 0; }
  .card { background: var(--card); border: 1px solid var(--line); border-radius: 8px; padding: 16px; }
  .card .value { font-size: 28px; font-weight: 600; }
  .card .label { color: var(--muted); font-size: 13px; }
  section { margin: 32px 0; }
  h2 { padding-bottom: 6px; border-bottom: 1px solid var(--line); }
  .diagram { background: var(--card); border: 1px solid var(--line); border-radius: 8px; padding: 16px; overflow-x: auto; }
  table { width: 100%; border-collapse: collapse; font-size: 14px; }
  th, td { border: 1px solid var(--line); padding: 6px 10px; text-align: left; vertical-align: top; }
  th { background: var(--card); }
  code { font-family: Consolas, monospace; font-size: 13px; }
  .note { border-left: 4px solid var(--accent); padding: 8px 12px; background: var(--card); }
</style>
</head>
<body>
<main>
  <h1>タイトル</h1>
  <div class="meta">対象: … / 生成: YYYY-MM-DD HH:MM</div>

  <div class="cards">
    <div class="card"><div class="value">12</div><div class="label">変更ファイル</div></div>
  </div>

  <section>
    <h2>全体構成</h2>
    <p>説明 1〜2 文。</p>
    <div class="diagram"><pre class="mermaid">
flowchart LR
  FE["Frontend (Next.js)"] --> BE["Backend (Spring Boot)"] --> DB[(PostgreSQL / H2)]
    </pre></div>
  </section>

  <section>
    <h2>関連ファイル</h2>
    <table>
      <tr><th>パス</th><th>役割</th></tr>
      <tr><td><code>packages/...</code></td><td>…</td></tr>
    </table>
  </section>

  <section>
    <h2>補足・未確認事項</h2>
    <p class="note">…</p>
  </section>
</main>
<script type="module">
  import mermaid from 'https://cdn.jsdelivr.net/npm/mermaid@11/dist/mermaid.esm.min.mjs';
  const dark = window.matchMedia('(prefers-color-scheme: dark)').matches;
  mermaid.initialize({ startOnLoad: true, theme: dark ? 'dark' : 'default', securityLevel: 'strict' });
</script>
</body>
</html>
```

### スタイルの決まりごと

- ライト / ダーク両対応にする（CSS 変数 + `prefers-color-scheme`）
- **角丸と一辺だけのボーダーを同じ要素に組み合わせない**
  - 一辺だけのボーダー（例: `.note` の左線、`h2` の下線）を使う要素は角を丸めない
  - 角丸を使う要素（`.card`、`.diagram`）は全周の枠線や背景色で区切る
- 外部から読み込むのは mermaid（CDN）だけにする。インターネットに出られない環境では図が表示されない旨を補足に書く

## 開き方（Windows）

- **VS Code**: Explorer で HTML を右クリック →「Reveal in File Explorer」→ ダブルクリック（Edge で開く）
- **PowerShell**: `start reports\20261005-1430-architecture.html`
- **Git Bash**: `explorer.exe "$(cygpath -w reports/20261005-1430-architecture.html)"`（成功しても終了コード 1 が返るが問題ない）

## 完了報告

- 出力パス（リポジトリルートからの相対パス）
- 上記の開き方
- 含めた図の一覧（種類と見出し）
- 未確認事項があればその内容
