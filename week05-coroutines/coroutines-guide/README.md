# Kotlin コルーチン基礎ガイド（Flutter/Dart経験者向け）

Week 5「コルーチン基礎」の読み物。`Coroutines.kt` の演習の背景を、
Dart の `Future`/`async`/`await`/`Isolate` と対比しながら体系化したもの。

## 目次

- **00. 全体像**（このファイル）— suspend とは何か、Dart 非同期との対応
- [01. suspend と runBlocking](01-suspend-runblocking.md) — 中断可能関数、スコープへの入り口
- [02. launch と async](02-launch-async.md) — 撃ちっぱなし vs 結果を待つ、並列化
- [03. Dispatchers と withContext](03-dispatchers.md) — スレッド切替、IO/Default/Main
- [04. 例外処理](04-error-handling.md) — coroutineScope vs supervisorScope、キャンセル
- [05. 構造化並行性](05-structured-concurrency.md) — Dart にない最重要概念

---

## 00. 全体像：コルーチンとは

### Dart 非同期との対応

| Dart | Kotlin | メモ |
|---|---|---|
| `Future<T>` | `suspend fun(): T` | 「待てる処理」。Kotlin は型ではなく**関数の修飾子** |
| `async` / `await` | `suspend` + 呼び出し / `await()` | await は suspend 関数内でそのまま呼ぶだけ |
| `Future.wait([...])` | `async { }` を並べて `awaitAll()` | 並列実行 |
| `Isolate`（別メモリ） | `Dispatchers.Default` / `IO`（スレッド切替） | Kotlin はメモリ共有・スレッド切替 |
| `Stream<T>` | `Flow<T>` | → Week 6 |
| （概念なし） | **構造化並行性** | コルーチンの親子関係とライフサイクル管理 |

### suspend fun ＝「中断できる関数」

```kotlin
suspend fun fetchUser(): String {
    delay(300)            // 中断ポイント：スレッドをブロックせず「待つ」
    return "User(Oto)"
}
```

- `suspend` が付くと「**途中で中断・再開できる関数**」になる。
- `delay(300)` は Dart の `Future.delayed` 相当だが、決定的に違うのは
  **スレッドをブロックしない**点。`Thread.sleep` はスレッドを占有して止めるが、
  `delay` は「そのスレッドを他の処理に明け渡して、300ms 後に再開」する。
  だから少ないスレッドで大量の非同期を捌ける（Dart のイベントループと似た効率）。

### suspend 関数の呼び出しルール

**suspend 関数は、別の suspend 関数か、コルーチンビルダーの中からしか呼べない。**

```kotlin
fun normal() {
    fetchUser()   // ❌ コンパイルエラー：通常関数からは呼べない
}
suspend fun another() {
    val u = fetchUser()   // ✅ suspend 関数の中ならOK
}
```

これは Dart で「`await` は `async` 関数の中でしか使えない」のと同じ制約。
「非同期は非同期の文脈からしか呼べない」という色（カラーリング）が伝播する。

### コルーチンの世界への「入り口」

通常のコードはコルーチンの外にいる。中に入るには**ビルダー**が要る：

| ビルダー | 用途 | 戻り値 |
|---|---|---|
| `runBlocking { }` | main/テストで橋渡し（**スレッドをブロック**） | ブロックの結果 |
| `launch { }` | 撃ちっぱなしの起動 | `Job` |
| `async { }` | 結果を待つ起動 | `Deferred<T>` |
| `coroutineScope { }` | スコープを作る（構造化並行性） | ブロックの結果 |

詳細は各章で。まずは「`suspend` は中断可能、呼ぶにはコルーチンの文脈が要る」を押さえる。
</content>
