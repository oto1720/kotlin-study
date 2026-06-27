# 02. launch と async — 撃ちっぱなし vs 結果を待つ

コルーチンを「起動する」2大ビルダー。役割がはっきり違う。

## 2-1. launch — 戻り値の要らない起動（fire and forget）

```kotlin
val job: Job = launch {
    delay(100)
    println("完了（戻り値なし）")
}
job.join()   // 終わるまで待ちたいときは join()
```

- 戻り値は `Job`（結果ではなく「実行の制御ハンドル」）。
- `Job` でできること：`join()`（完了待ち）・`cancel()`（キャンセル）・`isActive` の確認。
- **結果（値）は取れない**。「ログを出す」「状態を更新する」など副作用目的に使う。
- Dart に明確な対応はない（`unawaited(future)` がやや近い）。

## 2-2. async — 結果を待つ起動

```kotlin
val deferred: Deferred<String> = async { fetchUser() }
val user: String = deferred.await()   // .await() で結果を取り出す
```

- 戻り値は `Deferred<T>`（＝結果を持つ `Job`）。
- `.await()` で値を取り出す。これが Dart の `Future` + `await` に最も近い。
- `Deferred` も `Job` の一種なので `cancel()` などもできる。

| | 戻り値 | 結果の取得 | 主な用途 | Dart 相当 |
|---|---|---|---|---|
| `launch` | `Job` | 取れない | 副作用・発火 | `unawaited` 的 |
| `async` | `Deferred<T>` | `.await()` | 値を計算して使う | `Future` + `await` |

## 2-3. async の真価：並列化

`async` で複数を**同時に起動**し、後から `await` で合流すると並列になる。

```kotlin
suspend fun loadDashboard() = coroutineScope {
    val user  = async { fetchUser() }    // 300ms かかる処理を…
    val posts = async { fetchPosts() }   // …同時に開始（ここではまだ待たない）
    // 両方を起動してから await するのがポイント
    "${user.await()} / ${posts.await()}" // 合流。全体は max(300,500)=~500ms
}
```

### 直列 vs 並列の落とし穴

```kotlin
// ❌ これは「並列」にならない（直列 = 300 + 500 = 800ms）
val user = fetchUser()           // ここで300ms待ち切ってから
val posts = fetchPosts()         // 次を始めるので直列

// ✅ 並列（~500ms）：先に2つとも起動してから await
val u = async { fetchUser() }
val p = async { fetchPosts() }
val result = "${u.await()} / ${p.await()}"
```

- suspend 関数を**そのまま呼ぶと直列**（前のが終わってから次が始まる）。
- `async { }` で囲って**先に全部起動**してから `await` すると並列。
- Dart の `await f1(); await f2();`（直列）と `await Future.wait([f1(), f2()])`（並列）の違いと同じ。

### awaitAll — 複数をまとめて待つ

```kotlin
val results = listOf(
    async { fetchUser() },
    async { fetchUser() },
).awaitAll()   // List<String> でまとめて受け取る（Dart の Future.wait）
```

> 計測のヒント：`measureTimeMillis { }`（`kotlin.system`）でブロックの所要時間を測れる。
> 直列なら ~800ms、並列なら ~500ms になることを `Coroutines.kt` section2 で体感できる。
</content>
