# 01. リファクタ — 命名・層分け・ファイル構成の見直し

## 1-1. リファクタのチェックリスト

Week 11 で「動く」状態にしたコードを見直す。完璧を目指す必要はなく、
**ポートフォリオとして見せたときに「設計を意識している」と伝わるレベル**にする。

### 命名

- [ ] クラス名: PascalCase（`SearchViewModel`, `GitHubRepo`）
- [ ] 関数名: camelCase（`searchRepos`, `onQueryChange`）
- [ ] 定数: UPPER_SNAKE_CASE または `const val`
- [ ] パッケージ: lowercase（`com.example.githubsearch.data`）
- [ ] 略語を避ける（`repo` はOK、`rp` はNG）

### ファイル分割

1ファイルに詰め込んだコードを適切に分割する：

```
// Before（Week 11）
GitHubApp.kt  ← モデル + ViewModel + 全画面がここに

// After（Week 12）
data/model/GitHubRepo.kt
data/GitHubApiService.kt
data/GitHubRepository.kt
ui/search/SearchScreen.kt
ui/search/SearchViewModel.kt
ui/search/SearchUiState.kt
ui/detail/DetailScreen.kt
ui/components/RepoItem.kt
```

## 1-2. 層分けの原則

```
UI層 ──→ ViewModel層 ──→ Data層
 │          │              │
 │          │              └─ API, DB, Repository
 │          └─ UiState, ビジネスロジック
 └─ Composable, Navigation
```

### 依存の向き

- **UI → ViewModel**: OK（画面が ViewModel を使う）
- **ViewModel → Data**: OK（ViewModel が Repository を使う）
- **Data → UI**: NG（Repository が Composable を知ってはいけない）
- **ViewModel → UI**: NG（ViewModel が Composable を知ってはいけない）

Flutter の Clean Architecture と同じ原則。

## 1-3. よくあるリファクタパターン

### UiState を別ファイルに

```kotlin
// ui/search/SearchUiState.kt
sealed interface SearchUiState {
    data object Initial : SearchUiState
    data object Loading : SearchUiState
    data class Success(val repos: List<GitHubRepo>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}
```

### 共通コンポーネントの抽出

```kotlin
// ui/components/ErrorContent.kt
@Composable
fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 検索画面でも詳細画面でも再利用できる
}
```

### Modifier 引数の追加

再利用可能な Composable には `modifier: Modifier = Modifier` を引数に追加：

```kotlin
// Before
@Composable
fun RepoItem(repo: GitHubRepo, onClick: () -> Unit) { ... }

// After
@Composable
fun RepoItem(repo: GitHubRepo, onClick: () -> Unit, modifier: Modifier = Modifier) { ... }
```

## 1-4. Android Studio のリファクタ機能

| 操作 | ショートカット | 説明 |
|---|---|---|
| Rename | `Shift + F6` | 変数・クラス・ファイルの一括リネーム |
| Extract Function | `Cmd + Opt + M` | 選択範囲を関数に切り出す |
| Move | `F6` | ファイルを別パッケージに移動 |
| Inline | `Cmd + Opt + N` | 不要な中間変数を削除 |
| Extract Interface | メニューから | Repository の interface 抽出 |

Flutter の VS Code リファクタと同じ感覚で使える。
