# 02. 検索機能の実装

## 2-1. 検索バー

```kotlin
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        placeholder = { Text("リポジトリを検索...") },
        leadingIcon = { Icon(Icons.Default.Search, null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, "クリア")
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = {
            onSearch()
            keyboardController?.hide()
        }),
    )
}
```

Flutter の `TextField` + `TextEditingController` に相当。
state hoisting で親が `query` と `onQueryChange` を管理する。

## 2-2. Debounce（入力中の連続リクエスト防止）

```kotlin
class SearchViewModel @Inject constructor(
    private val repository: GitHubRepository,
) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    init {
        // query が変わって 500ms 経過したら自動検索
        viewModelScope.launch {
            _query
                .debounce(500)                // 500ms 待つ
                .filter { it.isNotBlank() }   // 空なら無視
                .distinctUntilChanged()       // 同じクエリなら無視
                .collect { q -> performSearch(q) }
        }
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }
}
```

Flutter/Riverpod で `Timer` や `debounce` パッケージを使うのと同じ。
Flow の `debounce` オペレータがコルーチンに統合されているので簡潔。

## 2-3. Pull-to-Refresh

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultList(
    repos: List<GitHubRepo>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onRepoClick: (GitHubRepo) -> Unit,
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(repos, key = { it.id }) { repo ->
                RepoItem(repo = repo, onClick = { onRepoClick(repo) })
            }
        }
    }
}
```

Flutter の `RefreshIndicator` に相当。
Material3 の `PullToRefreshBox` でラップするだけ。

## 2-4. 空の状態・エラーの状態

```kotlin
when (val state = uiState) {
    is SearchUiState.Initial -> {
        // 初期状態：何も検索していない
        EmptyMessage("キーワードを入力して検索")
    }
    is SearchUiState.Loading -> {
        CenterContent { CircularProgressIndicator() }
    }
    is SearchUiState.Success -> {
        if (state.repos.isEmpty()) {
            EmptyMessage("結果が見つかりませんでした")
        } else {
            SearchResultList(repos = state.repos, ...)
        }
    }
    is SearchUiState.Error -> {
        ErrorContent(message = state.message, onRetry = viewModel::retry)
    }
}
```

sealed interface で全パターンを網羅。新しい状態を追加し忘れるとコンパイルエラー。
Riverpod の `AsyncValue.when(loading: ..., data: ..., error: ...)` と同じ発想。

## 2-5. 画像読み込み（Coil）

リポジトリオーナーのアバターを表示するには **Coil**（Flutter の `cached_network_image` 相当）を使う：

```kotlin
// build.gradle.kts
implementation("io.coil-kt.coil3:coil-compose:3.0.4")

// Composable
AsyncImage(
    model = repo.owner.avatarUrl,
    contentDescription = "${repo.owner.login} のアバター",
    modifier = Modifier.size(40.dp).clip(CircleShape),
)
```
