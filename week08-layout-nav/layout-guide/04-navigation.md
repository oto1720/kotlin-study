# 04. Navigation Compose — Navigator → NavHost

## 4-1. 全体像

Flutter の画面遷移と Compose Navigation の対応：

| Flutter | Compose | 説明 |
|---|---|---|
| `Navigator.push()` | `navController.navigate("route")` | 画面遷移 |
| `Navigator.pop()` | `navController.popBackStack()` | 戻る |
| `MaterialPageRoute` | `composable("route") { }` | ルート定義 |
| `GoRouter` / `go_router` | `NavHost` | 宣言的ルーティング |
| `Navigator.pushNamed("/detail")` | `navController.navigate("detail")` | 名前付きルート |
| `RouteSettings.arguments` | パスパラメータ / `arguments` | 引数渡し |

## 4-2. セットアップ

```kotlin
// 1. NavController を作成（Flutter の NavigatorState 相当）
@Composable
fun MyApp() {
    val navController = rememberNavController()

    // 2. NavHost でルートを定義（GoRouter の routerConfig 相当）
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onNavigateToDetail = { id -> navController.navigate("detail/$id") }
            )
        }
        composable("detail/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            DetailScreen(id = id, onBack = { navController.popBackStack() })
        }
    }
}
```

## 4-3. 引数の渡し方

### パスパラメータ（必須）

```kotlin
// ルート定義
composable("detail/{userId}") { backStackEntry ->
    val userId = backStackEntry.arguments?.getString("userId")
    DetailScreen(userId = userId ?: "")
}

// 遷移
navController.navigate("detail/123")
```

Flutter の `GoRoute(path: '/detail/:id')` と同じ。

### クエリパラメータ（任意）

```kotlin
composable("search?query={query}") { backStackEntry ->
    val query = backStackEntry.arguments?.getString("query") ?: ""
    SearchScreen(query = query)
}

navController.navigate("search?query=kotlin")
```

## 4-4. 型安全なナビゲーション（推奨）

Navigation Compose 2.8+ では `@Serializable` を使った型安全ルーティングが可能：

```kotlin
@Serializable
data object Home

@Serializable
data class Detail(val id: Int)

@Composable
fun MyApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Home) {
        composable<Home> {
            HomeScreen(onNavigateToDetail = { id ->
                navController.navigate(Detail(id))
            })
        }
        composable<Detail> { backStackEntry ->
            val detail: Detail = backStackEntry.toRoute()
            DetailScreen(id = detail.id)
        }
    }
}
```

文字列ベースのルートより安全。Flutter の `GoRouter` でルートクラスを使うのに近い。

## 4-5. BottomNavigation との組み合わせ

```kotlin
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar {
                val currentRoute = navController.currentBackStackEntryAsState()
                    .value?.destination?.route
                NavigationBarItem(
                    selected = currentRoute == "home",
                    onClick = { navController.navigate("home") },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Home") },
                )
                NavigationBarItem(
                    selected = currentRoute == "settings",
                    onClick = { navController.navigate("settings") },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Settings") },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding),
        ) {
            composable("home") { HomeScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}
```

Flutter の `BottomNavigationBar` + `IndexedStack` のパターンに相当。
