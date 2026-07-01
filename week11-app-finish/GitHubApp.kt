/**
 * Week 11 演習 — アプリ仕上げ（GitHub リポジトリ検索アプリ）
 * ※ Android Studio の Compose プロジェクト内で使う。run.sh では実行不可。
 *
 * ねらい: Week 7-10 で学んだ全てを組み合わせ、通しで動くアプリを「完成」させる。
 *   一覧→詳細を実APIで接続、ローディング/エラーUI、LazyColumn で表示、検索機能。
 *
 * 構成:
 *   - model/   GitHubRepo, SearchResponse（Week10 のモデルを流用）
 *   - data/    GitHubApiService, GitHubRepository（Week10）
 *   - ui/      SearchScreen, DetailScreen, 共通コンポーネント
 *   - viewmodel/ SearchViewModel（Week9 + Week10）
 *
 * このファイルは全体を1ファイルにまとめた学習用。実アプリでは層ごとにファイルを分ける（Week12）。
 */

package com.example.week11

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// =====================================================================
// 1. モデル（Week10 から流用）
// =====================================================================
@Serializable
data class GitHubRepo(
    val id: Long,
    val name: String,
    @SerialName("full_name") val fullName: String,
    val description: String?,
    @SerialName("stargazers_count") val stars: Int,
    val language: String?,
    @SerialName("html_url") val htmlUrl: String,
    val owner: GitHubOwner,
)

@Serializable
data class GitHubOwner(
    val login: String,
    @SerialName("avatar_url") val avatarUrl: String,
)

@Serializable
data class SearchResponse(
    @SerialName("total_count") val totalCount: Int,
    val items: List<GitHubRepo>,
)

// =====================================================================
// 2. UI State（sealed interface）
// =====================================================================
sealed interface SearchUiState {
    data object Initial : SearchUiState
    data object Loading : SearchUiState
    data class Success(val repos: List<GitHubRepo>, val totalCount: Int) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

// =====================================================================
// 3. ViewModel
// =====================================================================
class SearchViewModel : ViewModel() {
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
                // ★ 実際のアプリでは Repository 経由で API を叩く
                // val result = repository.searchRepos(q)
                // _uiState.value = SearchUiState.Success(result.items, result.totalCount)

                // 擬似データ（API を繋ぐまでのスタブ）
                kotlinx.coroutines.delay(1000)
                val fakeRepos = listOf(
                    GitHubRepo(1, "kotlin", "JetBrains/kotlin", "The Kotlin Programming Language", 49000, "Kotlin", "https://github.com/JetBrains/kotlin", GitHubOwner("JetBrains", "")),
                    GitHubRepo(2, "compose-samples", "android/compose-samples", "Official Compose samples", 18000, "Kotlin", "https://github.com/android/compose-samples", GitHubOwner("android", "")),
                    GitHubRepo(3, "nowinandroid", "android/nowinandroid", "Now in Android app", 16000, "Kotlin", "https://github.com/android/nowinandroid", GitHubOwner("android", "")),
                ).filter { it.name.contains(q, ignoreCase = true) || it.description?.contains(q, ignoreCase = true) == true }
                _uiState.value = SearchUiState.Success(fakeRepos, fakeRepos.size)
            } catch (e: Exception) {
                _uiState.value = SearchUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun refresh() = search()
}

// =====================================================================
// 4. 検索バー
// =====================================================================
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text("リポジトリを検索...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "クリア")
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

// =====================================================================
// 5. リポジトリカード
// =====================================================================
@Composable
fun RepoItem(
    repo: GitHubRepo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = repo.fullName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            repo.description?.let { desc ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                repo.language?.let { lang ->
                    Text(
                        text = lang,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text = "⭐ ${repo.stars}",
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

// =====================================================================
// 6. 検索画面（一覧）
// =====================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = viewModel(),
    onRepoClick: (GitHubRepo) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("GitHub Search") })
        },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            SearchBar(
                query = query,
                onQueryChange = viewModel::onQueryChange,
                onSearch = viewModel::search,
            )

            when (val state = uiState) {
                is SearchUiState.Initial -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "キーワードを入力して検索",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                is SearchUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is SearchUiState.Success -> {
                    if (state.repos.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("結果が見つかりませんでした")
                        }
                    } else {
                        PullToRefreshBox(
                            isRefreshing = false,
                            onRefresh = viewModel::refresh,
                        ) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                item {
                                    Text(
                                        "${state.totalCount} 件の結果",
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(bottom = 4.dp),
                                    )
                                }
                                items(state.repos, key = { it.id }) { repo ->
                                    RepoItem(
                                        repo = repo,
                                        onClick = { onRepoClick(repo) },
                                    )
                                }
                            }
                        }
                    }
                }

                is SearchUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "エラーが発生しました",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(state.message)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = viewModel::refresh) {
                                Text("リトライ")
                            }
                        }
                    }
                }
            }
        }
    }
}

// =====================================================================
// 7. 詳細画面
// =====================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    repo: GitHubRepo,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(repo.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ヘッダー
            Text(repo.fullName, style = MaterialTheme.typography.headlineMedium)
            HorizontalDivider()

            // 情報カード
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    InfoRow(label = "オーナー", value = repo.owner.login)
                    repo.description?.let {
                        InfoRow(label = "説明", value = it)
                    }
                    repo.language?.let {
                        InfoRow(label = "言語", value = it)
                    }
                    InfoRow(label = "スター", value = "${repo.stars}")
                }
            }

            // URL
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("URL", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text(repo.htmlUrl, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

// =====================================================================
// 8. ナビゲーション（Week8 の NavHost を使用）
// =====================================================================
@Composable
fun GitHubSearchApp() {
    val navController = rememberNavController()

    MaterialTheme {
        NavHost(navController = navController, startDestination = "search") {
            composable("search") {
                SearchScreen(
                    onRepoClick = { repo ->
                        navController.navigate("detail/${repo.id}")
                    },
                )
            }
            composable("detail/{repoId}") {
                // 実際のアプリでは ViewModel 経由で ID から取得する
                // ここではデモ用にダミーデータを表示
                DetailScreen(
                    repo = GitHubRepo(
                        0, "example", "user/example",
                        "Sample repository", 100, "Kotlin",
                        "https://github.com/user/example",
                        GitHubOwner("user", ""),
                    ),
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}

// =====================================================================
// プレビュー
// =====================================================================
@Preview(showBackground = true)
@Composable
fun RepoItemPreview() {
    MaterialTheme {
        RepoItem(
            repo = GitHubRepo(
                1, "kotlin", "JetBrains/kotlin",
                "The Kotlin Programming Language",
                49000, "Kotlin",
                "https://github.com/JetBrains/kotlin",
                GitHubOwner("JetBrains", ""),
            ),
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
