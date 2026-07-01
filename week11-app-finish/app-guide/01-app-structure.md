# 01. アプリ構成と画面設計

## 1-1. ファイル構成（推奨）

```
app/src/main/java/com/example/githubsearch/
├── GitHubSearchApp.kt          // NavHost + テーマ
├── MainActivity.kt             // @AndroidEntryPoint
├── data/
│   ├── GitHubApiService.kt     // Retrofit インターフェース
│   ├── GitHubRepository.kt     // Repository interface + impl
│   └── model/
│       ├── GitHubRepo.kt       // @Serializable データモデル
│       └── SearchResponse.kt
├── di/
│   └── NetworkModule.kt        // Hilt Module（@Provides）
└── ui/
    ├── search/
    │   ├── SearchScreen.kt     // 検索画面 Composable
    │   └── SearchViewModel.kt  // ViewModel + UiState
    ├── detail/
    │   └── DetailScreen.kt     // 詳細画面 Composable
    └── components/
        ├── RepoItem.kt         // リポジトリカード
        └── ErrorContent.kt     // エラー表示
```

### Flutter との対応

```
lib/
├── main.dart                   →  MainActivity.kt
├── app.dart                    →  GitHubSearchApp.kt
├── data/
│   ├── github_api.dart         →  GitHubApiService.kt
│   ├── github_repository.dart  →  GitHubRepository.kt
│   └── models/
│       └── github_repo.dart    →  GitHubRepo.kt
├── providers/                  →  di/
│   └── providers.dart          →  NetworkModule.kt
└── ui/
    ├── search_page.dart        →  SearchScreen.kt + SearchViewModel.kt
    ├── detail_page.dart        →  DetailScreen.kt
    └── widgets/
        └── repo_tile.dart      →  RepoItem.kt
```

## 1-2. 画面遷移

```
SearchScreen ──(タップ)──→ DetailScreen
     ↑                         │
     └────(戻るボタン)──────────┘
```

### Navigation の設定

```kotlin
@Composable
fun GitHubSearchApp() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "search") {
        composable("search") {
            SearchScreen(onRepoClick = { repo ->
                navController.navigate("detail/${repo.id}")
            })
        }
        composable("detail/{repoId}") { entry ->
            val repoId = entry.arguments?.getString("repoId")?.toLongOrNull() ?: return@composable
            DetailScreen(repoId = repoId, onBack = { navController.popBackStack() })
        }
    }
}
```

## 1-3. MainActivity（最小）

```kotlin
@AndroidEntryPoint   // Hilt を使う場合
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                GitHubSearchApp()
            }
        }
    }
}
```

Flutter の `main()` + `runApp(MyApp())` に相当。
