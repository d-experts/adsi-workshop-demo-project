---
description: Git コミットのルール。コミット時に適用する。push / PR は行わない。
---

# Git ワークフロー

> 実装前の Plan → TDD → レビューの流れは [development-process.md](./development-process.md) を参照。

## コミットメッセージ形式

```
<type>: <description>

<optional body>
```

**type**: `feat`, `fix`, `refactor`, `docs`, `test`, `chore`, `perf`, `ci`

- 1–2 文で「なぜ」を説明する（「何を」だけにしない）
- ユーザーが明示的に依頼したときだけコミットする
- Issue に対応するコミットは番号を付ける（例: `fix: 退勤打刻の時刻ずれを修正 (#3)`）

## push / Pull Request は行わない

作業環境は GitHub に未ログインのため、作業はローカルの編集と `git commit` で完結させる。

- `git push`、`gh` コマンド、Pull Request の作成は行わない
- PR の代わりに AI レビューを行う（変更内容を Issue ファイルに追記し、新しいセッションの Claude にレビューさせる）
- 手順は [issue-workflow.md](./issue-workflow.md)

## 安全な Git 操作

- `git config` は変更しない
- `--force`, `hard reset` 等の破壊的操作はユーザー明示指示がない限り行わない
- `--no-verify` 等でフックをスキップしない
