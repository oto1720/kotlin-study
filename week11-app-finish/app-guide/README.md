# アプリ仕上げ ガイド（Flutter/Dart経験者向け）

Week 11「アプリ仕上げ」の読み物。Week 7-10 の知識を組み合わせて
GitHub リポジトリ検索アプリを完成させる。

## 目次

- **00. 全体像**（このファイル）— アーキテクチャと組み立て方
- [01. アプリ構成と画面設計](01-app-structure.md) — ファイル構成・画面遷移
- [02. 検索機能の実装](02-search.md) — 検索バー・debounce・pull-to-refresh
- [03. 完成チェックリスト](03-checklist.md) — 動くアプリにするためのチェック

---

## 00. 全体像

### このアプリでやること

1. GitHub API でリポジトリを**検索**
2. 結果を **LazyColumn** で一覧表示
3. タップで**詳細画面**に遷移
4. **Loading / Error / Empty** の各状態を出し分け
5. **Pull-to-refresh** で再読み込み

### Flutter アプリとの対応

```
Flutter                          →  Compose
─────────────────────────────────────────────
main.dart + MaterialApp          →  MainActivity + MaterialTheme
GoRouter / Navigator             →  NavHost + rememberNavController
SearchPage (StatefulWidget)      →  SearchScreen (@Composable)
SearchNotifier (Riverpod)        →  SearchViewModel
Repository + dio                 →  Repository + Retrofit
json_serializable model          →  @Serializable data class
ListView.builder                 →  LazyColumn
RefreshIndicator                 →  PullToRefreshBox
CircularProgressIndicator        →  CircularProgressIndicator
```

### アーキテクチャ図

```
┌─────────────────────────────────────────┐
│  UI Layer (Compose)                      │
│  SearchScreen ←→ DetailScreen            │
│       ↑ collectAsStateWithLifecycle      │
├─────────────────────────────────────────┤
│  ViewModel Layer                         │
│  SearchViewModel                         │
│       ↑ suspend fun                      │
├─────────────────────────────────────────┤
│  Data Layer                              │
│  GitHubRepository → GitHubApiService     │
│       ↑ Retrofit                         │
├─────────────────────────────────────────┤
│  Network                                 │
│  GitHub REST API                         │
└─────────────────────────────────────────┘
```

このアーキテクチャは Flutter の Clean Architecture と同じ。
Week 12 でファイル分割・命名整理・テストを行って仕上げる。
