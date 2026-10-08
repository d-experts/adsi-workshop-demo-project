# mermaid の書き方（visual-report 用）

`template.html` の `<pre class="mermaid">` に書くときの約束ごと。色・フォント・ズームはテンプレート側が面倒を見るので、ここでは**図の中身と構文**だけを扱う。

## 図の選び方

| 伝えたいこと | 記法 | 目安 |
|-------------|------|------|
| 部品のつながり（全体構成・パッケージ間依存） | `flowchart LR` / `TB` | パッケージやレイヤーは `subgraph` で囲む |
| 呼び出しの順番（画面操作 → API → DB） | `sequenceDiagram` | 参加者 6 個まで。1 図 1 ユースケース |
| テーブルと関連 | `erDiagram` | 主キー・外部キー・業務上重要な列だけ書く |
| 状態の移り変わり（申請中 → 承認 / 却下） | `stateDiagram-v2` | enum や CHECK 制約を根拠にする |
| 分岐のある処理・バリデーション | `flowchart TD` | 判定はひし形 `{"…?"}`、エラー出口を必ず描く |
| 変更前 / 変更後 | 同じ記法の図を 2 つ | `.pair` で並べる。ノード ID・向き・並び順をそろえる |

迷ったら「読み手が最初に知りたい問い」に答える図を 1 つ選ぶ。種類を増やすより、1 つの図の主張をはっきりさせる。

## 描き方のルール

- **1 図 1 主張**。figcaption に「この図で言いたいこと」を一文で書き、図はそれだけを描く
- **ノードは 12 個まで**。超えるなら全体図（5〜8 ノード）＋詳細図に分ける
- **横一列は 6 ノードまで**。それ以上つながるなら `flowchart TB` にして `subgraph` の中で `direction LR` を使う（横に長い図は縮小されて文字が小さくなる）
- **subgraph の外とつなぐ矢印は subgraph の ID に向ける**（`fe -->|"HTTP"| be`）。中のノードを外と直接つなぐと `direction` が無視され、縦に長い図になる
- ノード同士のつながり自体が主題の図（Service → Repository の依存など）は subgraph を使わず `flowchart LR` で 2 列に並べる。矢印が 15 本を超えるなら図ではなく表（行: 使う側、列: 使われる側）にする
- **矢印には動詞のラベル**を付ける: `-->|"呼び出す"|`、`-->|"SQL"|`、`-->|"HTTP /api/*"|`。ただし「どのクラスがどれを使うか」のような多対多の依存図（矢印 8 本超）はラベルを省き、矢印の意味をキャプションに書く
- **形で種類を表す**: 画面・サービスは `["…"]`、DB は `[("…")]`、利用者・外部は `(["…"])`、判定は `{"…?"}`
- ラベルはコード上の名前（クラス名・パス・テーブル名）を使い、2 行目に補足を `<br/>` で足す: `svc["AttendanceServiceImpl<br/>打刻の業務ロジック"]`
- **色は直接指定しない**（`style` / `classDef` / `linkStyle` に色コードを書かない）。強調は次の 4 クラスだけ

## 強調クラス

テンプレートがライト / ダークに合わせて色を付ける。凡例（`.legend`）も同じ名前で用意してある。

| クラス | 意味 | 書き方 |
|--------|------|--------|
| `key` | この図で一番見てほしい要素（1 図に 1〜2 個） | `class ctl key` |
| `added` | 変更で追加された要素 | `class chk added` |
| `changed` | 変更で中身が変わった要素 | `class svc changed` |
| `removed` | 変更で削除された要素（点線） | `class old removed` |

- 複数指定は `class a,b added`
- flowchart のノードに使う。sequence / ER では使えないので、変更点は `Note over` や figcaption で示す

## 構文エラーを防ぐ

1. **ラベルは必ず `"` で囲む**: `A["打刻(出勤)"]`、`A -->|"GET /api/x"| B`。`()` `[]` `{}` `:` `/` `#` `;` `<` `>` を含むと高確率で壊れる
2. ラベルの中に `"` を書かない（必要なら `'` か「」を使う）
3. 改行は `<br/>`。`\n` は使わない
4. **ノード ID は英数字だけ**（`ctl`、`attSvc`）。日本語や `-` `.` を ID にしない。`end` `graph` `class` `style` `default` は ID に使えない
5. `subgraph` は `subgraph id["表示名"]` … `end` の形。ID と表示名を分ける
6. `sequenceDiagram` の参加者は `participant FE as フロントエンド` のように英数字 ID + 表示名。メッセージ文に `;` `#` を入れない
7. `erDiagram` の属性は `型 名前 [PK|FK|UK] ["コメント"]`。型に `()` やスペースを入れない（`varchar(255)` → `varchar`）。テーブル名は英数字と `_` のみ
8. `erDiagram` の関連ラベルは必ず `"` で囲む: `employees ||--o{ attendance_records : "打刻する"`
9. `stateDiagram-v2` の状態名は英数字 ID にして、表示名は `state "申請中" as PENDING` で付ける
10. HTML の中なので `&` は `&amp;` と書く。`<` `>` はラベルで使わない（`<br/>` だけは可）
11. コメントは `%%` で始まる行だけ。行末コメントは書かない
12. 書き終えたら、ラベルの `"` の対応と `subgraph` / `end` の数を見直す

描画に失敗した図は、テンプレートが赤枠のエラーメッセージとソースを表示する（ページ全体は壊れない）。

## ひな形

### 全体構成（subgraph で境界を描く）

```
flowchart TB
  user(["利用者<br/>ブラウザ"])
  subgraph fe["packages/frontend（Next.js）"]
    direction LR
    page["画面<br/>src/app/**/page.tsx"] -->|"呼び出す"| api["api-client.ts"]
  end
  subgraph be["packages/backend（Spring Boot）"]
    direction LR
    ctl["Controller"] -->|"委譲"| svc["Service"] -->|"読み書き"| repo["Repository"]
  end
  db[("PostgreSQL / H2")]
  user -->|"操作"| fe
  fe -->|"HTTP /api/*"| be
  be -->|"SQL"| db
  class ctl key
```

### シーケンス（1 ユースケース）

```
sequenceDiagram
  actor U as 利用者
  participant FE as 打刻画面
  participant API as AttendanceController
  participant SVC as AttendanceServiceImpl
  participant DB as attendance_records
  U->>FE: 出勤ボタンを押す
  FE->>API: POST /api/attendance/clock-in
  API->>SVC: clockIn(employeeId)
  SVC->>DB: INSERT
  SVC-->>API: AttendanceRecord
  API-->>FE: 201 Created
  Note over SVC,DB: @Transactional
```

### ER

```
erDiagram
  departments ||--o{ employees : "所属"
  employees ||--o{ attendance_records : "打刻"
  employees {
    uuid id PK
    uuid department_id FK
    varchar email UK
  }
```

### 状態遷移

```
stateDiagram-v2
  state "申請中" as PENDING
  state "承認" as APPROVED
  state "却下" as REJECTED
  [*] --> PENDING : 申請する
  PENDING --> APPROVED : 承認する
  PENDING --> REJECTED : 却下する
  APPROVED --> [*]
  REJECTED --> [*]
```
