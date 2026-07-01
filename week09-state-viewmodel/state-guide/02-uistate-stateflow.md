# 02. sealed class + StateFlow で UiState

## 2-1. UiState の設計

Week 3 で作った `sealed interface UiState` を本番投入する。
Riverpod の `AsyncValue<T>` とまったく同じ構造：

```kotlin
// Riverpod
sealed class AsyncValue<T> {
    AsyncLoading         // 読み込み中
    AsyncData(T value)   // 成功
    AsyncError(error)    // 失敗
}

// Kotlin
sealed interface RepoUiState {
    data object Loading : RepoUiState
    data class Success(val repos: List<Repo>) : RepoUiState
    data class Error(val message: String) : RepoUiState
}
```

### なぜ sealed を使うか

`when` で全パターンを網羅でき、**新しい状態を追加するとコンパイルエラー**になる：

```kotlin
fun render(state: RepoUiState) = when (state) {
    is RepoUiState.Loading -> showLoading()
    is RepoUiState.Success -> showList(state.repos)
    is RepoUiState.Error -> showError(state.message)
    // 新しい状態を追加すると、ここに分岐を書くまでコンパイルが通らない
}
```

## 2-2. StateFlow で公開する

```kotlin
class RepoViewModel : ViewModel() {
    // 内部: MutableStateFlow（書き込み可能）
    private val _uiState = MutableStateFlow<RepoUiState>(RepoUiState.Loading)

    // 外部: StateFlow（読み取り専用）
    val uiState: StateFlow<RepoUiState> = _uiState.asStateFlow()
}
```

### なぜ Mutable と ReadOnly を分けるか

- UI 側が勝手に `_uiState.value = ...` で状態を書き換えるのを防ぐため。
- Riverpod でも `state` は Notifier 内部でのみ書き換え可能で、外部からは `ref.watch` で読むだけ。
- **単方向データフロー**（状態は ViewModel → UI の一方通行）を強制する。

## 2-3. 複雑な画面の UiState

画面に複数の状態が絡む場合は、data class のフィールドで管理：

```kotlin
data class SearchUiState(
    val query: String = "",
    val repos: List<Repo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
```

この「フラットな data class」スタイルと「sealed interface」スタイルの使い分け：

| スタイル | 使い所 |
|---|---|
| `sealed interface` | 状態が**排他的**（Loading/Success/Error は同時に起きない） |
| `data class` | 複数の状態が**共存**（検索中 + 結果表示 + エラー表示が同時にありうる） |

## 2-4. StateFlow の更新パターン

```kotlin
// 1. 単純な上書き
_uiState.value = RepoUiState.Success(repos)

// 2. data class の場合は copy で部分更新
_uiState.update { current ->
    current.copy(isLoading = false, repos = repos)
}
// update は「現在値を受け取って新しい値を返す」ラムダ。スレッドセーフ。
```

`update` は `MutableStateFlow` の拡張関数で、内部で CAS（compare-and-swap）を行う。
複数のコルーチンから同時に更新しても安全。
