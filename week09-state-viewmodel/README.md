# Week 9 — 状態管理・ViewModel・副作用

**今週の目標（約5時間）**
ViewModel+StateFlow・collectAsStateWithLifecycle・LaunchedEffect。Loading/Success/Error出し分け。

詳しいねらい・Flutter対応・成果物はルートの `README.md` を参照。

## ファイル構成
- `StateViewModel.kt` — 演習コード（ViewModel + sealed UiState + Loading/Success/Error 出し分け）※Android Studio で実行
- `state-guide/` — 状態管理ガイド（Riverpod 対応付き）
  - [00. 全体像](state-guide/README.md)
  - [01. ViewModel](state-guide/01-viewmodel.md)
  - [02. sealed class + StateFlow で UiState](state-guide/02-uistate-stateflow.md)
  - [03. collectAsStateWithLifecycle](state-guide/03-collect-lifecycle.md)
  - [04. 副作用（LaunchedEffect / DisposableEffect）](state-guide/04-side-effects.md)

## チェック
- [ ] インプット（読む・公式/Koans）
- [ ] 手を動かす（このフォルダにコード）
- [ ] `docs/log.md` に学びを記録
- [ ] コミット

## メモ

