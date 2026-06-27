# 03. Dispatchers と withContext — スレッド切替

## 3-1. Dispatcher ＝「どのスレッド（プール）で動かすか」

コルーチンは「どのスレッドで実行するか」を **Dispatcher** で決める。

| Dispatcher | 用途 | スレッド数の目安 |
|---|---|---|
| `Dispatchers.Default` | **CPU負荷の高い計算**（ソート・パース・画像処理） | CPUコア数 |
| `Dispatchers.IO` | **I/O待ち**（ネットワーク・ファイル・DB） | 多め（待ちが多い前提） |
| `Dispatchers.Main` | **UIスレッド**（Android の画面更新） | 1（メインスレッド） |
| `Dispatchers.Unconfined` | 特殊用途（通常使わない） | — |

> Dart 対応：`Isolate`（別メモリの並列実行）に近い使い分けだが、
> コルーチンは**メモリを共有**しつつスレッドを切り替える点が違う。
> 「重い計算は Default、ネットワークは IO、UI更新は Main」と覚えれば実務は足りる。

## 3-2. withContext — このブロックだけ別スレッドで実行

```kotlin
suspend fun loadAndParse(): Int = withContext(Dispatchers.Default) {
    (1..1_000_000).sum()      // 重い計算は Default プールで
}   // ブロックを抜けると元のディスパッチャに自動で戻る
```

- `withContext(dispatcher) { }` は「**そのブロックだけ**指定スレッドで実行し、
  結果を返して、抜けたら元に戻る」。
- 戻り値はブロックの最後の式（上の例なら `Int`）。
- `async { }.await()` と違い、**新しいコルーチンを起こさず**現在のコルーチンの
  実行スレッドを切り替えるだけ。単純な「スレッド移動」にはこちらが正解。

### Android での典型パターン

```kotlin
// ViewModel など（Week 9 で本番投入）
suspend fun load() {
    val data = withContext(Dispatchers.IO) {   // ネットワークは IO で
        api.fetch()                            // ブロッキングI/Oをここに隔離
    }
    // ここは呼び出し元のディスパッチャ（通常 Main）に戻っている
    _state.value = data                        // UI状態の更新は Main で安全に
}
```

「**重い/待つ処理を `withContext` で隔離し、UI更新は Main に戻す**」が黄金パターン。

## 3-3. withContext と async の使い分け

| やりたいこと | 使うもの |
|---|---|
| スレッドを変えて**1つの結果**を得る（直列でOK） | `withContext` |
| **複数を並列**に走らせて後で合流 | `async` + `await` |

```kotlin
// 単に重い処理を別スレッドへ：withContext
val n = withContext(Dispatchers.Default) { heavyCompute() }

// 2つを並列に：async
val a = async(Dispatchers.IO) { fetchA() }
val b = async(Dispatchers.IO) { fetchB() }
val combined = a.await() + b.await()
```

> 注意：`async { }.await()` を**すぐ await する**だけなら、それは並列になっておらず
> `withContext` で十分（かつ意図が明確）。「並列にしたいときだけ async」を徹底すると読みやすい。
</content>
