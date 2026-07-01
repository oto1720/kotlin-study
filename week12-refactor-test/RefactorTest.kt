/**
 * Week 12 演習 — リファクタ・テスト入門・振り返り
 * ※ Android Studio の Compose プロジェクト内で使う。run.sh では実行不可。
 *
 * ねらい: Week 11 で「動く」状態にしたアプリを、ポートフォリオ品質に仕上げる。
 *   - 命名・層分けの見直し
 *   - ViewModel の単体テストを1本書く（kotlinx-coroutines-test）
 *   - README にスクショ + 学びを整理
 *
 * このファイルは ViewModel テストのサンプルコード。
 */

package com.example.week12

// =====================================================================
// 1. テスト対象の ViewModel（Week11 から持ってくる）
// =====================================================================

/*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SearchUiState {
    data object Initial : SearchUiState
    data object Loading : SearchUiState
    data class Success(val repos: List<GitHubRepo>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

class SearchViewModel(
    private val repository: GitHubRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Initial)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun search(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            try {
                val repos = repository.searchRepos(query)
                _uiState.value = SearchUiState.Success(repos)
            } catch (e: Exception) {
                _uiState.value = SearchUiState.Error(e.message ?: "Unknown")
            }
        }
    }
}
*/

// =====================================================================
// 2. Fake Repository（テスト用）
// =====================================================================

/*
class FakeGitHubRepository : GitHubRepository {
    var result: Result<List<GitHubRepo>> = Result.success(emptyList())

    override suspend fun searchRepos(query: String): List<GitHubRepo> {
        return result.getOrThrow()
    }

    override suspend fun getRepo(owner: String, repo: String): GitHubRepo {
        throw NotImplementedError()
    }
}
*/

// =====================================================================
// 3. ViewModel の単体テスト（kotlinx-coroutines-test）
// =====================================================================
// テストファイルは src/test/ に置く（Android のテストディレクトリ）。
// JUnit 5 + kotlinx-coroutines-test + Turbine を使う。

/*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    // viewModelScope が Dispatchers.Main を使うので、テスト用に差し替える
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // -----------------------------------------------------------------
    // テスト 1: 初期状態は Initial
    // -----------------------------------------------------------------
    @Test
    fun `initial state is Initial`() {
        val repository = FakeGitHubRepository()
        val viewModel = SearchViewModel(repository)

        assertEquals(SearchUiState.Initial, viewModel.uiState.value)
    }

    // -----------------------------------------------------------------
    // テスト 2: 検索成功 → Success
    // -----------------------------------------------------------------
    @Test
    fun `search success updates state to Success`() = runTest {
        val fakeRepos = listOf(
            GitHubRepo(1, "kotlin", "JetBrains/kotlin", "Kotlin lang", 49000, "Kotlin", "", GitHubOwner("JetBrains", "")),
        )
        val repository = FakeGitHubRepository().apply {
            result = Result.success(fakeRepos)
        }
        val viewModel = SearchViewModel(repository)

        viewModel.search("kotlin")

        // UnconfinedTestDispatcher なので launch が即時実行される
        val state = viewModel.uiState.value
        assertTrue(state is SearchUiState.Success)
        assertEquals(1, (state as SearchUiState.Success).repos.size)
        assertEquals("kotlin", state.repos.first().name)
    }

    // -----------------------------------------------------------------
    // テスト 3: 検索失敗 → Error
    // -----------------------------------------------------------------
    @Test
    fun `search failure updates state to Error`() = runTest {
        val repository = FakeGitHubRepository().apply {
            result = Result.failure(java.io.IOException("Network error"))
        }
        val viewModel = SearchViewModel(repository)

        viewModel.search("kotlin")

        val state = viewModel.uiState.value
        assertTrue(state is SearchUiState.Error)
        assertEquals("Network error", (state as SearchUiState.Error).message)
    }

    // -----------------------------------------------------------------
    // テスト 4: 空クエリは検索しない
    // -----------------------------------------------------------------
    @Test
    fun `blank query does not trigger search`() = runTest {
        val repository = FakeGitHubRepository()
        val viewModel = SearchViewModel(repository)

        viewModel.search("   ")

        assertEquals(SearchUiState.Initial, viewModel.uiState.value)
    }
}
*/

// =====================================================================
// 4. Turbine を使った Flow テスト（より高度）
// =====================================================================
// Turbine は Flow のテストを簡潔に書くライブラリ。
// Riverpod のテストで ProviderContainer を使うのと同じ感覚。

/*
import app.cash.turbine.test

@Test
fun `search emits Loading then Success`() = runTest {
    val repository = FakeGitHubRepository().apply {
        result = Result.success(listOf(/* ... */))
    }
    val viewModel = SearchViewModel(repository)

    viewModel.uiState.test {
        assertEquals(SearchUiState.Initial, awaitItem())  // 初期値

        viewModel.search("kotlin")

        assertEquals(SearchUiState.Loading, awaitItem())  // Loading が流れる
        assertTrue(awaitItem() is SearchUiState.Success)  // Success が流れる
    }
}
*/

// =====================================================================
// 5. テストの依存関係（build.gradle.kts）
// =====================================================================
/*
dependencies {
    // JUnit 5
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")

    // コルーチンテスト
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")

    // Turbine（Flow テスト用）
    testImplementation("app.cash.turbine:turbine:1.2.0")

    // Compose UI テスト（UI テストを書く場合）
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
*/
