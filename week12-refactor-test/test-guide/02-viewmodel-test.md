# 02. ViewModel テスト — 単体テストの書き方

## 2-1. テスト環境のセットアップ

### build.gradle.kts

```kotlin
dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("app.cash.turbine:turbine:1.2.0")  // Flow テスト用（任意）
}

tasks.withType<Test> {
    useJUnitPlatform()
}
```

### Dispatchers.Main の差し替え

ViewModel の `viewModelScope` は `Dispatchers.Main` を使う。
テスト環境には `Main` ディスパッチャがないので、テスト用に差し替える：

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }
}
```

Flutter の `ProviderContainer` でテスト環境を作るのと同じ発想。

## 2-2. Fake Repository を用意

モック（MockK）ではなく **Fake**（手書きの偽実装）を使うのが Android の推奨：

```kotlin
class FakeGitHubRepository : GitHubRepository {
    var searchResult: Result<List<GitHubRepo>> = Result.success(emptyList())

    override suspend fun searchRepos(query: String): List<GitHubRepo> {
        return searchResult.getOrThrow()
    }

    override suspend fun getRepo(owner: String, repo: String): GitHubRepo {
        throw NotImplementedError()
    }
}
```

Flutter の `MockRepoRepository extends Mock implements GitHubRepository` と同じ。
Fake の方がテストが読みやすく、壊れにくい。

## 2-3. テストを書く

### テスト 1: 初期状態

```kotlin
@Test
fun `initial state is Initial`() {
    val viewModel = SearchViewModel(FakeGitHubRepository())
    assertEquals(SearchUiState.Initial, viewModel.uiState.value)
}
```

### テスト 2: 検索成功

```kotlin
@Test
fun `search with valid query updates state to Success`() = runTest {
    val fakeRepos = listOf(sampleRepo())
    val repository = FakeGitHubRepository().apply {
        searchResult = Result.success(fakeRepos)
    }
    val viewModel = SearchViewModel(repository)

    viewModel.search("kotlin")

    val state = viewModel.uiState.value
    assertInstanceOf(SearchUiState.Success::class.java, state)
    assertEquals(1, (state as SearchUiState.Success).repos.size)
}
```

### テスト 3: 検索失敗

```kotlin
@Test
fun `search failure updates state to Error`() = runTest {
    val repository = FakeGitHubRepository().apply {
        searchResult = Result.failure(IOException("timeout"))
    }
    val viewModel = SearchViewModel(repository)

    viewModel.search("kotlin")

    val state = viewModel.uiState.value
    assertInstanceOf(SearchUiState.Error::class.java, state)
    assertEquals("timeout", (state as SearchUiState.Error).message)
}
```

### テスト 4: 空クエリ

```kotlin
@Test
fun `blank query does not change state`() = runTest {
    val viewModel = SearchViewModel(FakeGitHubRepository())

    viewModel.search("")
    viewModel.search("   ")

    assertEquals(SearchUiState.Initial, viewModel.uiState.value)
}
```

## 2-4. runTest とは

```kotlin
@Test
fun `test with coroutines`() = runTest {
    // この中は TestScope で実行される
    // delay() が即座にスキップされる（テストが高速）
    // viewModelScope.launch { } も即座に実行される（UnconfinedTestDispatcher の場合）
}
```

Flutter の `test('description', () async { ... })` で `Future` をテストするのと同じ。
`runTest` はコルーチンのテスト用スコープを提供する。

## 2-5. Turbine で Flow のテスト（発展）

```kotlin
@Test
fun `search emits Loading then Success`() = runTest {
    val repository = FakeGitHubRepository().apply {
        searchResult = Result.success(listOf(sampleRepo()))
    }
    val viewModel = SearchViewModel(repository)

    viewModel.uiState.test {
        assertEquals(SearchUiState.Initial, awaitItem())

        viewModel.search("kotlin")

        assertEquals(SearchUiState.Loading, awaitItem())
        assertInstanceOf(SearchUiState.Success::class.java, awaitItem())
    }
}
```

Turbine は Flow から emit された値を順番に取り出してアサートできる。
Riverpod のテストで `container.listen` して状態遷移を確認するのと同じ。

## 2-6. テストの実行

```bash
# CLI から
./gradlew test

# 特定のテストだけ
./gradlew test --tests "com.example.SearchViewModelTest"

# Android Studio から
# テストクラスの横の ▶ ボタンをクリック
```
