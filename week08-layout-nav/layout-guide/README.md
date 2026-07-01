# レイアウト・ナビゲーション ガイド（Flutter/Dart経験者向け）

Week 8「レイアウト・ナビゲーション」の読み物。Flutter のレイアウトウィジェットとの対応を
体系化し、Navigation Compose で画面遷移を実現する。

## 目次

- **00. 全体像**（このファイル）— Flutter レイアウト → Compose レイアウトの対応表
- [01. Column / Row / Box](01-layout.md) — 基本レイアウト
- [02. LazyColumn / LazyRow](02-lazy-lists.md) — ListView.builder の代替
- [03. Material3 テーマ](03-material3.md) — ThemeData → MaterialTheme
- [04. Navigation Compose](04-navigation.md) — Navigator → NavHost

---

## 00. 全体像：Flutter → Compose レイアウトの対応表

| Flutter | Compose | メモ |
|---|---|---|
| `Column` | `Column` | そのまま |
| `Row` | `Row` | そのまま |
| `Stack` | `Box` | 名前が違うだけ |
| `Expanded(flex: 2)` | `Modifier.weight(2f)` | Modifier で指定 |
| `SizedBox(height: 8)` | `Spacer(Modifier.height(8.dp))` | |
| `Padding` | `Modifier.padding()` | ウィジェットではなく Modifier |
| `Container` | `Box` + `Modifier` | 分解して書く |
| `ListView.builder` | `LazyColumn` | 遅延描画 |
| `GridView.builder` | `LazyVerticalGrid` | |
| `Scaffold` | `Scaffold` | そのまま |
| `AppBar` | `TopAppBar` | |
| `BottomNavigationBar` | `NavigationBar` | M3 名称 |
| `Card` | `Card` | そのまま |
| `Navigator.push` | `navController.navigate()` | |
| `Navigator.pop` | `navController.popBackStack()` | |
| `GoRouter` | `NavHost` + `composable()` | 宣言的ルーティング |

### 一番大事な違い

**Flutter は「すべてが Widget」だが、Compose は「レイアウト = Composable + Modifier」の2層構造。**

Flutter では `Padding`、`Center`、`SizedBox` も Widget だが、
Compose ではこれらを **Modifier チェーン**で表す。この結果ネストが浅くなる。

```dart
// Flutter: Widget のネスト
Padding(
  padding: EdgeInsets.all(16),
  child: Center(
    child: SizedBox(width: 200, child: Text('Hello')),
  ),
)
```

```kotlin
// Compose: Modifier で平坦に
Text(
    "Hello",
    modifier = Modifier.padding(16.dp).width(200.dp).align(Alignment.CenterHorizontally)
)
```
