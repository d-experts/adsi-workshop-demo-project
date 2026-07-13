# アーキテクチャ概要 & 打刻メモ機能の変更計画

## アーキテクチャ概要

```
packages/
├── backend/   — Spring Boot 3.x (Java 21)
├── frontend/  — Next.js (TypeScript, App Router)
└── infra/     — AWS CDK (TypeScript)
```

### Backend（レイヤード + ライト DDD）

```
Controller → Service (interface + impl) → Repository (interface) → Entity
```

- **DI**: コンストラクタインジェクションのみ
- **DTO**: `record` で定義（Lombok `@Data` は Entity のみ）
- **DB**: Flyway マイグレーション（`ddl-auto` 禁止）、`@Version` 楽観ロック
- **パッケージ**: ドメイン分割（`attendance/`, `employee/`, `correction/` …）

### Frontend（フィーチャーベース）

```
src/
├── app/(authenticated)/   — ページ（App Router）
├── features/attendance/   — ドメイン単位で API・hooks・コンポーネントをまとめる
├── components/            — 共通UI
└── lib/                   — apiClient, utils
```

- **状態管理**: TanStack Query（サーバーステート）
- **スタイル**: Tailwind CSS
- **型安全**: `any` 禁止、API レスポンスは interface で定義
- **API 通信**: `apiClient`（`lib/api-client.ts`）経由、`withBasePath()` 必須

---

## 打刻メモ追加の変更計画

### Backend（12ファイル）

| # | ファイル | 変更内容 |
|---|---------|---------|
| 1 | `resources/db/migration/V5__add_attendance_memo.sql` | **新規**。`clock_in_memo VARCHAR(100)`, `clock_out_memo VARCHAR(100)` を追加 |
| 2 | `attendance/entity/AttendanceRecord.java` | `clockInMemo`, `clockOutMemo` フィールド追加 |
| 3 | `attendance/dto/ClockInRequest.java` | **新規** record。`employeeId` + `@Size(max=100) memo` |
| 4 | `attendance/dto/ClockOutRequest.java` | **新規** record。同上 |
| 5 | `attendance/dto/AttendanceRecordResponse.java` | `clockInMemo`, `clockOutMemo` をレスポンスに追加 |
| 6 | `attendance/service/AttendanceService.java` | `clockIn(UUID, String)`, `clockOut(UUID, String)` にシグネチャ変更 |
| 7 | `attendance/service/AttendanceServiceImpl.java` | memo を builder / setter で渡す |
| 8 | `attendance/controller/AttendanceController.java` | `@RequestParam` → `@RequestBody` に変更、リクエスト DTO 受け取り |
| 9 | `attendance/dto/MemoUpdateRequest.java` | **新規**。`@Size(max=100) clockInMemo`, `clockOutMemo` |
| 10 | `AttendanceController.java` | `PUT /api/attendance/{id}/memo` エンドポイント追加 |
| 11 | `AttendanceService` / `Impl` | `updateMemo(UUID recordId, ...)` メソッド追加 |
| 12 | `correction/service/CorrectionServiceImpl.java` | 承認時に `record.setClockInMemo(null); record.setClockOutMemo(null);` を追加 |

### Frontend（5ファイル）

| # | ファイル | 変更内容 |
|---|---------|---------|
| 1 | `features/attendance/attendance-api.ts` | `AttendanceRecordResponse` に memo 追加、`clockIn`/`clockOut` を body 送信に変更、`updateMemo` 関数追加 |
| 2 | `features/attendance/useAttendance.ts` | `useClockIn`/`useClockOut` の mutationFn に memo 引数追加、`useUpdateMemo` hook 新規 |
| 3 | `features/attendance/ClockButtons.tsx` | ボタン上にメモ入力欄（`<input maxLength={100}>`）追加 |
| 4 | `features/attendance/AttendanceTable.tsx` | メモ列追加 + 編集ボタン追加 |
| 5 | `features/attendance/MemoEditDialog.tsx` | **新規**。出勤メモ・退勤メモを編集するダイアログ |

### テスト（既存テストの修正 + 新規）

| ファイル | 変更 |
|---------|------|
| `AttendanceServiceTest.java` | `clockIn`/`clockOut` 呼び出しに memo 引数追加 |
| `AttendanceControllerTest.java` | POST を JSON body に変更、memo アサーション追加 |
| `AttendanceIntegrationTest.java` | 同上 |
| `CorrectionServiceTest.java` | 承認時のメモ削除を検証 |

---

### 変更の流れ（依存順）

```
1. Flyway V5（DB）
2. Entity
3. Request/Response DTO
4. Service interface → impl
5. Controller
6. Frontend API → hooks → UI
7. テスト修正
```
