---
name: requirements
description: 要求仕様（要件定義）フェーズ。ユーザーストーリーの詳述と [Question]/[Answer] による曖昧さ解消。新規プロダクト・新機能の着手時、要件が曖昧なときに使う。
---

# 要求仕様フェーズ（SDD）

SDD（仕様駆動開発）の最初の工程（参照モデル AI-DLC の Inception に対応）。
**「何を作るか」** をユーザーストーリーと Q&A で確定する。

## いつ使う

- 新規プロダクト / 大機能の要求を固めるとき
- 各 Unit 着手時の軽量な要求確認
- 仕様が曖昧で AI が仮定してしまいそうなとき

## ズームレベル（再帰）

同じ「要求 → 設計 → 実装」ループをスコープ別に繰り返す。要求の重さはスコープに比例する。

| レベル | 要求の粒度 | 出力先 |
|--------|-----------|--------|
| プロダクト全体 | フルのユーザーストーリー群 | `docs/requirements/` |
| 機能追加 | ストーリー + [Q]/[A] + 確定仕様 | `docs/feature-N/qa.md` |
| 各 Unit | ストーリー数点 + [Q]/[A] 確認のみ | `docs/units/unit_*.md` 内 |
| 小修正 | ほぼスキップ | — |

## 手順

1. 既存コード・`docs/` を読んでコンテキストを作る（AI に土台を理解させる）
2. ユーザーストーリーで意図を記述する（「〜として、〜したい」）
3. 機能ごとのフォルダ `docs/feature-N/` を作り（N は既存の最大番号 + 1）、曖昧点を `docs/feature-N/qa.md` に `[Question]` / `[Answer]` で洗い出す。Issue があれば冒頭に `- Issue: #番号（issues/NNNN-*.md）` を書く
4. すべての `[Answer]` が埋まったら、`qa.md` 末尾に `## 確定仕様` をまとめる（プロダクト全体に関わる決定は `docs/requirements/` にも反映）

## [Question] / [Answer]

AI が仮定してはいけない仕様は明示的に質問する。

```
[Question] 社員IDは自動採番ですか？それとも手動入力？
[Answer]   自動採番（UUID）
```

未回答の `[Answer]` がある限り、その仕様に依存する設計・実装に進まない。

`[Q]/[A]` の壁打ちは **`docs/feature-N/qa.md` で行う**（Issue ファイルに往復を残さないため）。
Issue ファイル（`issues/NNNN-*.md`）には `## 仕様` として **`qa.md` へのリンクと確定した決定だけ**を追記する（→ `.claude/rules/common/issue-workflow.md`）。

> `docs/working/requirements/` はアプリ初期構築時の Q&A 記録。新しい Q&A は置かない。

## 完了条件

- [ ] 未回答の `[Answer]` が残っていない
- [ ] 確定仕様が `docs/feature-N/qa.md`（プロダクト全体なら `docs/requirements/`）にまとまっている

## 次のステップ

→ `design` スキル（どう作るかの設計へ）

## 参照

- `.claude/rules/common/development-process.md`
