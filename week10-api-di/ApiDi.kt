/**
 * Week 10 演習 — API連携・DI入門
 * ※ Android Studio の Compose プロジェクト内で使う。run.sh では実行不可。
 *
 * ねらい: Flutter の dio/http → Retrofit、json_serializable → kotlinx.serialization、
 *        get_it/Riverpod のDI → Hilt（まずは手動DI）。
 *
 * 成果物: GitHub API からリポジトリを取得し、ログ/簡易画面に表示する。
 *
 * 依存関係（build.gradle.kts に追加が必要）:
 *   - com.squareup.retrofit2:retrofit
 *   - org.jetbrains.kotlinx:kotlinx-serialization-json
 *   - com.squareup.retrofit2:converter-kotlinx-serialization (or converter-gson)
 *   - com.squareup.okhttp3:okhttp
 *   - com.google.dagger:hilt-android（DI を使う場合）
 */

package com.example.week10

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// =====================================================================
// 1. データモデル — kotlinx.serialization（json_serializable に相当）
// =====================================================================
// @Serializable を付けると JSON ↔ Kotlin の変換コードが自動生成される。
// Dart の json_serializable + @JsonKey に相当。

@Serializable
data class GitHubRepo(
    val id: Long,
    val name: String,
    @SerialName("full_name")           // JSON のキー名が Kotlin と違うとき
    val fullName: String,              // Dart: @JsonKey(name: 'full_name')
    val description: String?,          // null 許容（API のレスポンスが null の場合がある）
    @SerialName("stargazers_count")
    val stars: Int,
    val language: String?,
    @SerialName("html_url")
    val htmlUrl: String,
    val owner: GitHubOwner,
)

@Serializable
data class GitHubOwner(
    val login: String,
    @SerialName("avatar_url")
    val avatarUrl: String,
)

@Serializable
data class SearchResponse(
    @SerialName("total_count")
    val totalCount: Int,
    val items: List<GitHubRepo>,
)

// =====================================================================
// 2. Retrofit インターフェース — dio/http の代わり
// =====================================================================
// Dart: final response = await dio.get('/search/repositories', queryParameters: {...});
// Kotlin: Retrofit はインターフェースのメソッドに @GET を付けると、実装が自動生成される。

interface GitHubApiService {
    // GET /search/repositories?q=kotlin&sort=stars&per_page=20
    @GET("search/repositories")
    suspend fun searchRepos(
        @Query("q") query: String,
        @Query("sort") sort: String = "stars",
        @Query("per_page") perPage: Int = 20,
    ): SearchResponse

    // GET /repos/{owner}/{repo}
    @GET("repos/{owner}/{repo}")
    suspend fun getRepo(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
    ): GitHubRepo

    // GET /users/{username}/repos
    @GET("users/{username}/repos")
    suspend fun getUserRepos(
        @Path("username") username: String,
        @Query("sort") sort: String = "updated",
        @Query("per_page") perPage: Int = 20,
    ): List<GitHubRepo>
}

// =====================================================================
// 3. Retrofit インスタンス作成
// =====================================================================
// Dart の dio = Dio(BaseOptions(baseUrl: ...)) に相当。

/*
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object RetrofitClient {
    private val json = Json {
        ignoreUnknownKeys = true    // API に知らないキーがあっても無視（重要）
        coerceInputValues = true    // null → デフォルト値に変換
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC   // リクエスト/レスポンスのログ
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val apiService: GitHubApiService = retrofit.create(GitHubApiService::class.java)
}
*/

// =====================================================================
// 4. Repository パターン — データ取得を抽象化
// =====================================================================
// Dart/Flutter でも Repository パターンは Clean Architecture の基本。
// ViewModel は「どこからデータを取るか」を知らなくてよい。

interface GitHubRepository {
    suspend fun searchRepos(query: String): List<GitHubRepo>
    suspend fun getRepo(owner: String, repo: String): GitHubRepo
}

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

// =====================================================================
// 5. 手動 DI — Riverpod のプロバイダ定義に相当
// =====================================================================
// Riverpod: final repoProvider = Provider((ref) => GitHubRepository(ref.read(dioProvider)));
// 手動DI: オブジェクトグラフを自分で組み立てる（小規模なら十分）。

/*
object AppModule {
    // Riverpod の Provider に相当（シングルトン）
    val apiService: GitHubApiService = RetrofitClient.apiService
    val repository: GitHubRepository = GitHubRepositoryImpl(apiService)
}
*/

// =====================================================================
// 6. ViewModel（Week9 の ViewModel に Repository を注入）
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
    private val repository: GitHubRepository = AppModule.repository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Initial)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun search() {
        val q = _query.value.trim()
        if (q.isBlank()) return

        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            try {
                val repos = repository.searchRepos(q)
                _uiState.value = SearchUiState.Success(repos)
            } catch (e: Exception) {
                _uiState.value = SearchUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
*/

// =====================================================================
// 7. Hilt によるDI（手動DIの置き換え）
// =====================================================================
// Riverpod の「Provider 定義 + ref.read でインスタンス取得」を
// アノテーションベースで自動化したもの。
//
// @Module + @Provides = Riverpod の Provider 定義
// @Inject = ref.read でインスタンスを受け取る
// @HiltViewModel = ViewModel を Hilt 管理にする

/*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideApiService(): GitHubApiService = RetrofitClient.apiService

    @Provides
    @Singleton
    fun provideRepository(apiService: GitHubApiService): GitHubRepository {
        return GitHubRepositoryImpl(apiService)
    }
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: GitHubRepository,
) : ViewModel() {
    // ... 上と同じ実装
}

// Composable で使うとき
// val viewModel: SearchViewModel = hiltViewModel()
*/
