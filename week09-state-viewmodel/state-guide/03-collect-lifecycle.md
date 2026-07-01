# 03. collectAsStateWithLifecycle — ref.watch → collectAsState

## 3-1. StateFlow を Compose で購読する

```kotlin
@Composable
fun RepoScreen(viewModel: RepoViewModel = viewModel()) {
    // ★ Riverpod の ref.watch(repoProvider) に相当
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is RepoUiState.Loading -> CircularProgressIndicator()
        is RepoUiState.Success -> RepoList(state.repos)
        is RepoUiState.Error -> ErrorText(state.message)
    }
}
```

## 3-2. collectAsStateWithLifecycle vs collectAsState

| 関数 | ライフサイクル対応 | 使い所 |
|---|---|---|
| `collectAsStateWithLifecycle()` | あり（バックグラウンドで停止） | **常にこちらを使う** |
| `collectAsState()` | なし（バックグラウンドでも動き続ける） | テストや非 Android 環境 |

`WithLifecycle` 版は Activity/Fragment が `STOPPED` になると自動で購読を停止し、
`STARTED` に戻ると再開する。バッテリー消費やクラッシュを防ぐ。

Riverpod の `autoDispose` に近い仕組み。

## 3-3. by 委譲 vs = 代入

```kotlin
// パターン A: by 委譲（推奨）
val uiState by viewModel.uiState.collectAsStateWithLifecycle()
// → uiState は RepoUiState 型で直接使える

// パターン B: = 代入
val uiState = viewModel.uiState.collectAsStateWithLifecycle()
// → uiState は State<RepoUiState> 型。使うときに .value が必要
```

`by` 委譲を使うと `.value` が省略できる（Week 3 の委譲 by と同じ仕組み）。

## 3-4. 複数の StateFlow を購読

```kotlin
@Composable
fun SearchScreen(viewModel: SearchViewModel = viewModel()) {
    val repos by viewModel.repos.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    Column {
        SearchBar(query = query, onQueryChange = viewModel::onQueryChange)
        if (isLoading) CircularProgressIndicator()
        RepoList(repos)
    }
}
```

Riverpod で複数の Provider を `ref.watch` するのと同じ感覚。

## 3-5. イベント（one-shot）の購読

StateFlow は「現在値」を保持するので、SnackBar 表示のような一回きりのイベントには不向き。
イベントには `SharedFlow`（Week 6）+ `LaunchedEffect` を使う：

```kotlin
// ViewModel
private val _event = MutableSharedFlow<String>()
val event: SharedFlow<String> = _event.asSharedFlow()

// Composable
LaunchedEffect(Unit) {
    viewModel.event.collect { message ->
        snackbarHostState.showSnackbar(message)
    }
}
```

Riverpod の `ref.listen` でイベントを受け取るパターンに相当。
