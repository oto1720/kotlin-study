# 01. ViewModel — Notifier → ViewModel

## 1-1. ViewModel とは

Flutter/Riverpod の `Notifier` に相当するクラス。
画面の状態を保持し、ビジネスロジックを実行する。

**Riverpod の Notifier との最大の違い**：ViewModel は Android のライフサイクルに紐づく。
画面回転（Configuration Change）でも**破棄されず状態を保持**する。
Flutter では画面回転でウィジェットツリーが再構築されるが State は残る — それと同じ。

```kotlin
class RepoViewModel : ViewModel() {
    // Riverpod: late final state = AsyncLoading();
    // Android:  MutableStateFlow で初期値を設定
    private val _uiState = MutableStateFlow<RepoUiState>(RepoUiState.Loading)
    val uiState: StateFlow<RepoUiState> = _uiState.asStateFlow()

    init {
        loadRepos()    // Riverpod の build() 相当
    }

    fun loadRepos() {
        viewModelScope.launch {
            _uiState.value = RepoUiState.Loading
            try {
                val repos = repository.getRepos()
                _uiState.value = RepoUiState.Success(repos)
            } catch (e: Exception) {
                _uiState.value = RepoUiState.Error(e.message ?: "Unknown")
            }
        }
    }
}
```

## 1-2. Riverpod Notifier との対比

```dart
// Riverpod (Dart)
class RepoNotifier extends AsyncNotifier<List<Repo>> {
  @override
  Future<List<Repo>> build() async {
    return await ref.read(repoRepositoryProvider).getRepos();
  }

  Future<void> refresh() async {
    state = const AsyncLoading();
    state = await AsyncValue.guard(() => ref.read(repoRepositoryProvider).getRepos());
  }
}
```

```kotlin
// ViewModel (Kotlin)
class RepoViewModel(private val repository: RepoRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<RepoUiState>(RepoUiState.Loading)
    val uiState: StateFlow<RepoUiState> = _uiState.asStateFlow()

    init { loadRepos() }

    fun loadRepos() {
        viewModelScope.launch {
            _uiState.value = RepoUiState.Loading
            try {
                _uiState.value = RepoUiState.Success(repository.getRepos())
            } catch (e: Exception) {
                _uiState.value = RepoUiState.Error(e.message ?: "Unknown")
            }
        }
    }
}
```

## 1-3. viewModelScope

```kotlin
viewModelScope.launch {
    // ViewModel が onCleared() されると自動でキャンセル
    val data = fetchData()
}
```

- `viewModelScope` は ViewModel に内蔵されたコルーチンスコープ。
- ViewModel が破棄されるとスコープもキャンセルされる（= structured concurrency）。
- Riverpod の `ref.onDispose(() => subscription.cancel())` を書く必要がない。

## 1-4. Composable から ViewModel を取得

```kotlin
@Composable
fun RepoScreen(
    viewModel: RepoViewModel = viewModel(),   // デフォルトで Activity スコープ
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // ...
}
```

- `viewModel()` は `ViewModelProvider` から取得する Compose 用ヘルパー。
- Riverpod の `ref.watch(repoNotifierProvider)` に相当。
- Week 10 で Hilt を導入すると `hiltViewModel()` に差し替わる。

## 1-5. ViewModel に引数を渡す

Riverpod では `family` で引数付き Provider を作るが、ViewModel では `Factory` を使う：

```kotlin
class DetailViewModel(private val repoId: Int) : ViewModel() {
    // ...

    class Factory(private val repoId: Int) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DetailViewModel(repoId) as T
        }
    }
}

// 使用
@Composable
fun DetailScreen(repoId: Int) {
    val viewModel: DetailViewModel = viewModel(factory = DetailViewModel.Factory(repoId))
}
```

Hilt を使えばもっと簡潔に書ける（→ Week 10）。
