---
name: verify
description: 実装後の検証コマンドと完了チェック。Unit 完了前・コミット前・Issue をレビュー待ちにする前に使う。
---

# 検証

## クイックチェック

```bash
npm run check:backend
npm run lint:frontend
```

## 完了チェックリスト

- [ ] Backend チェック通過
- [ ] Frontend lint 通過
- [ ] 新機能にテストあり（カバレッジ 80% 目標）
- [ ] 未回答の `[Answer]` が残っていない
- [ ] Issue 対応なら、確認したテストを Issue ファイルの「変更内容 > 検証」に書いた

## 失敗時

1. エラーメッセージを確認
2. **テスト以外**の実装を修正（テスト変更は最終手段）
