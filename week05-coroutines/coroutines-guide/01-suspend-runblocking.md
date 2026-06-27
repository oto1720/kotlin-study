# 01. suspend と runBlocking

## 1-1. suspend fun の正体

```kotlin
suspend fun fetchUser(): String {
    delay(300)
    return "User(Oto)"
}
```

`suspend` は「この関数は**中断ポイント**（`delay` や他の suspend 呼び出し）で
一時停止し、後で再開できる」ことを表す。

- **中断中はスレッドを手放す**：300ms の `delay` の間、そのスレッドは他の仕事をできる。
  Dart のシングルスレッド・イベントループに近い効率を、スレッドプール上で実現する。
- 戻り値は普通の `String`。Dart の `Future<String>` のように型でラップしない。
  「待てること」は型ではなく `suspend` という**関数の性質**で表現する。

### 中断ポイント（suspension point）とは

`delay`・他の `suspend` 関数呼び出し・`withContext`・`await` などが中断ポイント。
ここで初めて「中断・再開」が起こりうる。中断ポイントが無ければ普通の関数と同じく一気に走る。

## 1-2. runBlocking — main をコルーチンの世界に橋渡し

```kotlin
fun main() = runBlocking {        // ここから中は suspend 文脈
    val user = fetchUser()        // suspend 関数を呼べる
    println(user)
}
```

- `runBlocking` は「**現在のスレッドをブロックして**、中のコルーチンが終わるまで待つ」ビルダー。
- 名前のとおり**ブロックする**ので、実アプリの本番コードでは使わない。
  使うのは **main 関数・単体テスト・学習用スクリプト**だけ。
- Android 実機では代わりに `viewModelScope.launch { }`（Week 9）や
  `lifecycleScope` を使う。これらはブロックしない。

> 対応関係：Dart の `void main() async { await ...; }` の `async main` に近い役割。
> ただし Dart の main はイベントループに乗るのに対し、`runBlocking` は
> 「呼んだスレッドを占有して待つ」点が違う。

## 1-3. なぜ main で直接 suspend を呼べないのか

```kotlin
fun main() {
    fetchUser()   // ❌ Suspend function 'fetchUser' should be called only from a coroutine
}
```

`main` は通常関数なので suspend 文脈がない。`runBlocking` で囲うか、
`fun main() = runBlocking { }` の形にして初めて呼べる。
これは「非同期の色（async/await のカラーリング）」が伝播する Dart と同じ制約。
</content>
