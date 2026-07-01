# 03. Repository パターン — データ取得の抽象化

## 3-1. なぜ Repository を挟むか

Flutter でも Android でも同じ理由：

- **ViewModel が「どこからデータを取るか」を知らなくてよい**（API? キャッシュ? ローカルDB?）
- テスト時に fake を差し替えやすい
- データソースの切り替えが ViewModel に影響しない

```
ViewModel → Repository (interface) → API / DB / Cache
```

## 3-2. 実装パターン

```kotlin
// 1. インターフェース（ViewModel が知る型）
interface GitHubRepository {
    suspend fun searchRepos(query: String): List<GitHubRepo>
    suspend fun getRepo(owner: String, repo: String): GitHubRepo
}

// 2. 実装（API を叩く）
class GitHubRepositoryImpl(
    private val apiService: GitHubApiService,
) : GitHubRepository {
    override suspend fun searchRepos(query: String): List<GitHubRepo> {
        return apiService.searchRepos(query).items
    }

    override suspend fun getRepo(owner: String, repo: String): GitHubRepo {
        return apiService.getRepo(owner, repo)
    }
}

// 3. ViewModel はインターフェースに依存
class SearchViewModel(
    private val repository: GitHubRepository,    // 実装を知らない
) : ViewModel() {
    fun search(query: String) {
        viewModelScope.launch {
            val repos = repository.searchRepos(query)
            // ...
        }
    }
}
```

### Dart での同じ構造

```dart
// Riverpod
final repoProvider = Provider<GitHubRepository>((ref) {
  return GitHubRepositoryImpl(ref.read(dioProvider));
});

class SearchNotifier extends AsyncNotifier<List<Repo>> {
  @override
  Future<List<Repo>> build() async {
    return ref.read(repoProvider).searchRepos('kotlin');
  }
}
```

## 3-3. テスト用の Fake Repository

```kotlin
class FakeGitHubRepository : GitHubRepository {
    var shouldFail = false

    override suspend fun searchRepos(query: String): List<GitHubRepo> {
        if (shouldFail) throw IOException("Fake error")
        return listOf(
            GitHubRepo(id = 1, name = "fake-repo", /* ... */)
        )
    }

    override suspend fun getRepo(owner: String, repo: String): GitHubRepo {
        return GitHubRepo(id = 1, name = repo, /* ... */)
    }
}

// テストで使う
val viewModel = SearchViewModel(repository = FakeGitHubRepository())
```

Riverpod の `overrideWithValue` / `overrideWith` でプロバイダを差し替えるのと同じ発想。

## 3-4. Repository でキャッシュする例

```kotlin
class GitHubRepositoryImpl(
    private val apiService: GitHubApiService,
) : GitHubRepository {
    private var cache: Map<String, List<GitHubRepo>> = emptyMap()

    override suspend fun searchRepos(query: String): List<GitHubRepo> {
        cache[query]?.let { return it }     // キャッシュヒット
        val repos = apiService.searchRepos(query).items
        cache = cache + (query to repos)    // キャッシュに保存
        return repos
    }
}
```

実際のアプリでは Room（ローカルDB）をキャッシュに使うことが多い（Flutter の drift/sqflite 相当）。
