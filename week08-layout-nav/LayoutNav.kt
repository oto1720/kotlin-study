/**
 * Week 8 演習 — レイアウト・ナビゲーション
 * ※ Android Studio の Compose プロジェクト内で使う。run.sh では実行不可。
 *
 * ねらい: Flutter のレイアウトウィジェットとの対応を掴み、2画面のナビゲーションを作る。
 *   Column/Row/Stack → Column/Row/Box
 *   Expanded → Modifier.weight()
 *   ListView.builder → LazyColumn
 *   Navigator → Navigation Compose
 *   ThemeData → Material3 MaterialTheme
 *
 * 成果物: 一覧→詳細の2画面（ダミーデータ）
 */

package com.example.week08

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*

// =====================================================================
// 0. ダミーデータ
// =====================================================================
data class Repo(
    val id: Int,
    val name: String,
    val description: String,
    val stars: Int,
    val language: String,
)

val sampleRepos = listOf(
    Repo(1, "kotlin", "The Kotlin Programming Language", 49000, "Kotlin"),
    Repo(2, "compose-samples", "Official Jetpack Compose samples", 18000, "Kotlin"),
    Repo(3, "nowinandroid", "Fully functional Android app (Now in Android)", 16000, "Kotlin"),
    Repo(4, "architecture-samples", "A collection of Android architecture samples", 44000, "Kotlin"),
    Repo(5, "sunflower", "A gardening app illustrating best practices", 17000, "Kotlin"),
)

// =====================================================================
// 1. Column / Row / Box — Flutter の Column/Row/Stack に相当
// =====================================================================
// Column: 縦に並べる（Flutter Column）
// Row: 横に並べる（Flutter Row）
// Box: 重ねる（Flutter Stack）
// Spacer: 余白（Flutter SizedBox / Expanded の空 child）

@Composable
fun RepoCard(
    repo: Repo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Row で横に並べる
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // weight で横幅を分配（Flutter の Expanded に相当）
                Text(
                    text = repo.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "⭐ ${repo.stars}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = repo.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = repo.language,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

// =====================================================================
// 2. LazyColumn — Flutter の ListView.builder に相当
// =====================================================================
// LazyColumn は画面に見えるアイテムだけ compose する（= Flutter の ListView.builder）。
// items() に List を渡し、key で一意なIDを指定する（Flutter の Key 相当）。

@Composable
fun RepoListScreen(
    repos: List<Repo>,
    onRepoClick: (Repo) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            items = repos,
            key = { it.id },    // Flutter の Key に相当。差分更新の効率化。
        ) { repo ->
            RepoCard(repo = repo, onClick = { onRepoClick(repo) })
        }
    }
}

// =====================================================================
// 3. 詳細画面
// =====================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepoDetailScreen(
    repo: Repo,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
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
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(repo.name, style = MaterialTheme.typography.headlineMedium)
            HorizontalDivider()
            DetailRow(label = "説明", value = repo.description)
            DetailRow(label = "言語", value = repo.language)
            DetailRow(label = "スター数", value = "${repo.stars}")
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

// =====================================================================
// 4. Navigation Compose — Flutter の Navigator に相当
// =====================================================================
// Flutter: Navigator.push(context, MaterialPageRoute(builder: (_) => DetailPage()))
// Compose: navController.navigate("detail/$id")
//
// NavHost でルートを定義し、navController で画面遷移する。
// Flutter の GoRouter に近い宣言的ルーティング。

@Composable
fun RepoApp(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = "list") {
        // 一覧画面
        composable("list") {
            RepoListScreen(
                repos = sampleRepos,
                onRepoClick = { repo ->
                    navController.navigate("detail/${repo.id}")
                },
            )
        }
        // 詳細画面（引数を URL パスで渡す）
        composable("detail/{repoId}") { backStackEntry ->
            val repoId = backStackEntry.arguments?.getString("repoId")?.toIntOrNull()
            val repo = sampleRepos.find { it.id == repoId }
            if (repo != null) {
                RepoDetailScreen(
                    repo = repo,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}

// =====================================================================
// 5. Material3 テーマ — Flutter の ThemeData に相当
// =====================================================================
// MaterialTheme は Flutter の Theme(data: ThemeData(...)) に対応。
// colorScheme / typography / shapes の3軸で構成される。

@Composable
fun Week08App() {
    MaterialTheme(
        colorScheme = dynamicLightColorScheme(),    // または lightColorScheme()
        typography = Typography(),                   // デフォルトの Material3 タイポ
    ) {
        RepoApp()
    }
}

// dynamicLightColorScheme が使えない環境用のフォールバック
@Composable
private fun dynamicLightColorScheme(): ColorScheme {
    return lightColorScheme()
}

// =====================================================================
// プレビュー
// =====================================================================
@Preview(showBackground = true)
@Composable
fun RepoCardPreview() {
    MaterialTheme {
        RepoCard(
            repo = sampleRepos.first(),
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RepoListPreview() {
    MaterialTheme {
        RepoListScreen(repos = sampleRepos, onRepoClick = {})
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RepoDetailPreview() {
    MaterialTheme {
        RepoDetailScreen(repo = sampleRepos.first(), onBack = {})
    }
}
