# 04. 例外処理 — coroutineScope vs supervisorScope

コルーチンの例外は「親子の伝播」が絡むため、Dart の素朴な try/catch より一段ややこしい。
ここが Week 5 で一番ハマるポイント。

## 4-1. 基本：suspend 関数の例外は普通に try/catch できる

```kotlin
suspend fun safeFetch(): String = try {
    fetchUser()                       // 中で例外が出れば
} catch (e: Exception) {
    "fallback"                        // ここで捕まえられる
}
```

直列に呼ぶ suspend 関数の例外は、Dart の `try { await f(); }` と同じ感覚で捕まえられる。

## 4-2. 落とし穴：async + coroutineScope の例外伝播

```kotlin
// ❌ 期待通りにならない例
suspend fun broken() = coroutineScope {
    try {
        async {
            throw IllegalStateException("API error")
        }.await()                     // await で再スローされるが…
    } catch (e: Exception) {
        "caught"                      // ここで捕まえても、
    }
    // 例外は「親スコープ」にも伝播し、全体がクラッシュしてしまう
}
```

- `coroutineScope` の中で `async` が例外を投げると、`await` で再スローされて
  catch できる**ように見える**が、例外は同時に**親スコープにも伝播**する。
- 結果、catch したのに親（兄弟コルーチンを含むスコープ全体）がキャンセル／クラッシュする。
- これは「子の失敗は親に伝わり、兄弟も巻き添えにキャンセルする」という
  **構造化並行性のルール**（→ 05章）の副作用。

## 4-3. 解決：supervisorScope で子の失敗を親に伝播させない

```kotlin
// ✅ supervisorScope なら子の失敗が親に伝播しない
suspend fun fixed() = supervisorScope {
    val result = try {
        async {
            delay(50)
            throw IllegalStateException("API error")
        }.await()                     // ここで再スローされたものを
    } catch (e: IllegalStateException) {
        "caught: ${e.message}"        // catch すれば完結（親は無事）
    }
    println(result)
}
```

| スコープ | 子の失敗が… | 用途 |
|---|---|---|
| `coroutineScope` | 親と兄弟に伝播（全員キャンセル） | 「全部成功 or 全部やめる」処理 |
| `supervisorScope` | 親に伝播しない（その子だけ失敗） | 「一部失敗しても他は続ける」処理 |

`Coroutines.kt` section4 が `supervisorScope` を使っているのはこの理由。
「await の例外を自分で握って完結させたい」ときは `supervisorScope`。

## 4-4. launch の例外と CoroutineExceptionHandler

- `async` の例外は `await()` 時に投げられる（捕まえる責任は await 側）。
- `launch` の例外は**即座に親に伝播**する。await が無いので try/catch では取りにくく、
  `CoroutineExceptionHandler` を scope に渡して受ける：

```kotlin
val handler = CoroutineExceptionHandler { _, e -> println("ハンドラ: ${e.message}") }
scope.launch(handler) { throw RuntimeException("boom") }
```

## 4-5. キャンセルと CancellationException（重要）

```kotlin
try {
    delay(1000)
} catch (e: CancellationException) {
    throw e        // ★ キャンセル例外は握りつぶさず再スローする
}
```

- コルーチンのキャンセルは `CancellationException` を投げて伝わる。
- これを `catch (e: Exception)` で**握りつぶすとキャンセルが効かなくなる**。
  `catch (e: CancellationException) { throw e }` で必ず再スローするか、
  そもそも `CancellationException` を捕まえない書き方にする。
- Dart の `Future` キャンセルが弱いのに対し、コルーチンは**協調的キャンセル**が
  仕組みとして入っている（構造化並行性の一部）。
</content>
