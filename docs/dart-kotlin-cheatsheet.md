# Dart ↔ Kotlin 対応チートシート（自分用に追記していく）

Flutter経験を踏み台にするための対応表。学習中に気づいた違いをどんどん足していく。

## 変数・null安全
| Dart | Kotlin | メモ |
|---|---|---|
| `final x = 1` | `val x = 1` | 再代入不可 |
| `var x = 1` | `var x = 1` | 再代入可 |
| `String? s` | `val s: String?` | nullable |
| `s?.length` | `s?.length` | 同じ |
| `s ?? "x"` | `s ?: "x"` | Kotlinは `?:`（エルビス） |
| `s!` | `s!!` | 非null断言 |
| `"hi $name"` | `"hi $name"` | 文字列補間は同じ |

## 制御構文
| Dart | Kotlin |
|---|---|
| `switch` | `when`（式として値を返せる） |
| `for (var i in 0..<10)` | `for (i in 0 until 10)` |
| 三項 `a ? b : c` | `if (a) b else c`（ifが式） |

## 関数・ラムダ
| Dart | Kotlin |
|---|---|
| `int add(int a, int b) => a + b;` | `fun add(a: Int, b: Int) = a + b` |
| `list.map((e) => e * 2)` | `list.map { it * 2 }` |
| 拡張なし | 拡張関数 `fun String.shout() = uppercase()` |

### スコープ関数（Dartに明確な対応なし／Kotlinの肝）
- `let`  : null チェック後の処理、変換　`s?.let { use(it) }`
- `run`  : 計算してまとめて返す
- `apply`: オブジェクト設定（`this` を返す）　`Person().apply { name = "a" }`
- `also` : 副作用（ログなど、対象をそのまま返す）
- `with` : 同一オブジェクトに連続アクセス

## コレクション
| Dart | Kotlin |
|---|---|
| `list.where((e) => ...)` | `list.filter { ... }` |
| `list.map(...)` | `list.map { ... }` |
| `list.fold(0, (a, b) => a+b)` | `list.fold(0) { a, b -> a + b }` |
| なし | `groupBy` / `associateBy` / `partition` / `sumOf` |
| `Iterable`(遅延) | `Sequence`(遅延) |

## 非同期
| Dart | Kotlin |
|---|---|
| `Future<T>` | `suspend fun(): T` |
| `async`/`await` | `suspend` + `withContext` / `await()` |
| `Future.wait([...])` | `coroutineScope { async ... }` |
| `Isolate` | `Dispatchers.Default` / `IO` でスレッド切替 |
| `Stream<T>` | `Flow<T>`（cold） |
| `StreamController.broadcast` | `SharedFlow`（hot） |

## 状態管理（Riverpod ↔ Compose）
| Riverpod / Flutter | Compose / Android |
|---|---|
| `StateNotifier` / `Notifier` の状態 | `ViewModel` + `StateFlow` |
| `ref.watch(provider)` | `viewModel.state.collectAsStateWithLifecycle()` |
| `state = newState` | `_state.value = newState` |
| イベント1回（SnackBar等） | `SharedFlow` |

## UI（Flutter Widget ↔ Composable）
| Flutter | Compose |
|---|---|
| `StatelessWidget.build()` | `@Composable fun`（引数で状態を受ける） |
| `setState` | `remember { mutableStateOf(...) }` + recomposition |
| `Column` / `Row` / `Stack` | `Column` / `Row` / `Box` |
| `Expanded(flex:)` | `Modifier.weight()` |
| `Padding` / `Container` | `Modifier.padding()` / 各種 Modifier |
| `ListView.builder` | `LazyColumn { items(...) }` |
| `Navigator.push` | Navigation Compose の `navController.navigate()` |
| `ThemeData` | `MaterialTheme`（Material3） |
| `initState` / `dispose` | `LaunchedEffect` / `DisposableEffect` |
