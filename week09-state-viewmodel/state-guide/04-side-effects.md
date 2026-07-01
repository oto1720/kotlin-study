# 04. 副作用 — LaunchedEffect / DisposableEffect

## 4-1. なぜ副作用を特別扱いするか

Composable 関数は recomposition のたびに再実行される。
だから「APIを呼ぶ」「タイマーを開始する」「ログを送る」などの副作用を
直接書くと**何度も実行されてしまう**。

```kotlin
// ❌ NG: recomposition のたびに API を叩く
@Composable
fun BadExample() {
    fetchData()   // recomposition で何度も呼ばれる！
}
```

Flutter でも `build()` の中で API を叩かないのと同じ原則。
`initState` / `dispose` に相当する仕組みが Compose にも用意されている。

## 4-2. LaunchedEffect — initState + didUpdateWidget 相当

```kotlin
@Composable
fun UserProfile(userId: String) {
    var user by remember { mutableStateOf<User?>(null) }

    // key = userId: userId が変わるたびにブロックを再実行
    LaunchedEffect(userId) {
        user = fetchUser(userId)     // suspend fun を安全に呼べる
    }

    user?.let { Text(it.name) }
}
```

### key の仕組み

| パターン | 動作 |
|---|---|
| `LaunchedEffect(Unit)` | **一度だけ**実行（initState 相当） |
| `LaunchedEffect(userId)` | `userId` が変わるたびに再実行（didUpdateWidget 相当） |
| `LaunchedEffect(a, b)` | `a` か `b` が変わったら再実行 |

key が変わると：
1. 前回の LaunchedEffect がキャンセルされる
2. 新しいブロックが起動する

Flutter の `didUpdateWidget` で古いリスナーを解除して新しいのを登録する、と同じ。

## 4-3. DisposableEffect — dispose() 相当

リソースの解放が必要な場合：

```kotlin
@Composable
fun LifecycleLogger() {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            println("lifecycle: $event")
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        // onDispose = Flutter の dispose()。Composable が画面から消えたとき実行。
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
```

### LaunchedEffect vs DisposableEffect

| | LaunchedEffect | DisposableEffect |
|---|---|---|
| ブロック内 | suspend 関数を呼べる（コルーチン） | 通常のコード（非 suspend） |
| 終了処理 | コルーチンがキャンセルされる | `onDispose { }` で明示的に解放 |
| 用途 | API コール、タイマー、アニメーション | リスナー登録/解除、センサー購読 |

## 4-4. SideEffect — 毎回の recomposition で実行

```kotlin
@Composable
fun AnalyticsTracker(screenName: String) {
    SideEffect {
        // 毎回の successful recomposition で呼ばれる
        analytics.trackScreen(screenName)
    }
}
```

Compose のコードから非 Compose のコードに値を同期するとき（Firebase Analytics 等）に使う。
Flutter の `build()` の最後にログを仕込むようなケース。

## 4-5. まとめ：Flutter → Compose の副作用対応

| Flutter | Compose | タイミング |
|---|---|---|
| `initState()` | `LaunchedEffect(Unit) { }` | 最初の1回 |
| `didUpdateWidget()` | `LaunchedEffect(key) { }` | key 変更時 |
| `dispose()` | `DisposableEffect(key) { onDispose { } }` | 画面離脱時 |
| `build()` 内のログ等 | `SideEffect { }` | 毎回の recomposition |
| `addListener` / `removeListener` | `DisposableEffect` | リスナー管理 |
