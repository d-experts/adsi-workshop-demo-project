# Claude Code ハーネス（ワークショップ用）

このリポジトリの `.claude/` に **必要最低限** の rules / skills / agents をすべて同梱している。
外部プラグイン（ECC 等）は不要。

## 構成

```
.claude/
├── README.md           # 本ファイル
├── settings.json       # モデル設定
├── agents/             # subagent 定義 (model: sonnet)
├── rules/
│   ├── common/         # 開発プロセス・共通規約
│   ├── java-spring-boot.md
│   ├── typescript-frontend.md
│   ├── security.md
│   └── testing.md
└── skills/
    ├── dev-environment/        # 起動・アクセス
    ├── requirements/           # 要求仕様（Inception）
    ├── design/                 # 設計（Construction 入口）
    ├── work-decomposition/     # UoW/domain 分割
    ├── tdd-implementation/     # Plan → 承認 → TDD 実装
    ├── multi-agent-review/     # 並列 subagent レビュー
    └── verify/                 # 検証コマンド
```

## レイヤー

| レイヤー | 役割 | 例 |
|---------|------|-----|
| `CLAUDE.md` | 入口（最小） | パッケージ構成、スキルへのリンク |
| `rules/` | 何を守るか | TDD、SB4 互換、セキュリティ |
| `skills/` | どう進めるか | 要求 / 設計 / 分割 / TDD / レビュー手順 |
| `agents/` | subagent の専門性 | java-reviewer 等 |

## ワークショップの流れ（SDD ベース）

看板は **SDD（仕様駆動開発）**＝仕様（要求 → 設計）を先に固めてから実装する。
参照モデルは AWS の AI-DLC。全体像は `rules/common/development-process.md`。

**進め方**: 前半は参加者が**自分でプロンプトを入力**して体験し、後半で**スキルを明示的に呼び出して**効率化する。各工程に対応するスキル:

1. **要求仕様** — `skills/requirements/SKILL.md`（ユーザーストーリー、[Question]/[Answer]）
2. **設計** — `skills/design/SKILL.md`（ドメイン / DB / API）
3. **作業分割** — `skills/work-decomposition/SKILL.md`（UoW/domain 分割。大機能のみ）
4. **TDD 実装** — `skills/tdd-implementation/SKILL.md`（Plan → 承認 → TDD）
5. **検証** — `skills/verify/SKILL.md`
6. **レビュー** — `skills/multi-agent-review/SKILL.md`（subagent デモ）

> 「要求 → 設計 → (分割) → 実装」は**ズームレベルで再帰する**。
> プロダクト全体 / 大機能では全工程を、各 Unit では軽量な要求・設計 + 実装を回す。

## Agents（subagent）

`.claude/agents/` — すべて **`model: sonnet`**

| Agent | 用途 |
|-------|------|
| java-reviewer | Backend Java / Spring Boot |
| typescript-reviewer | Frontend Next.js |
| security-reviewer | セキュリティ |
| test-reviewer | テスト品質 |

`multi-agent-review` スキルが変更内容に応じて並列起動する。

## 起動

```bash
npm run dev    # backend(:8080) + frontend(:3000)
```

詳細: `skills/dev-environment/SKILL.md`
