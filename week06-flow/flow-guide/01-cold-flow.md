# 01. cold flow — flow{} / emit / collect

## 1-1. flow{} で作る

```kotlin
fun numberFlow(): Flow<Int> = flow {
    for (i in 1..3) {
        delay(100)
        emit(i)        // 値を1つ流す
    }
}
```

- `flow { }` ビルダーの中で `emit(value)` を呼ぶと、その値が購読者に届く。
- `emit` は **suspend 関数**。Dart の `yield`（async*）や `StreamController.add` に相当。
- `flow { }` の中では `delay` などの suspend 関数を自由に呼べる。

## 1-2. collect で購読する

```kotlin
suspend fun run() {
    println("（collect するまで何も起きない）")
    numberFlow().collect { value ->
        println("collected: $value")
    }
}
// collected: 1
// collected: 2
// collected: 3
```

- `collect { }` が**終端演算子**。これを呼んで初めて `flow { }` の中身が走る。
- `collect` は suspend 関数なので、コルーチン文脈（suspend 関数内など）でしか呼べない。
- Dart の `stream.listen { }` や `await for (v in stream)` に相当。

## 1-3. cold である、とはどういうことか

```kotlin
val f = numberFlow()     // ← この時点では flow{} の中身は1行も実行されない
f.collect { ... }        // ← 1回目の購読：1,2,3 が最初から流れる
f.collect { ... }        // ← 2回目の購読：また最初から 1,2,3 が流れる
```

- `numberFlow()` を呼んで `Flow` を**作っただけ**では何も起きない（遅延）。
- `collect` するたびに `flow { }` ブロックが**最初から実行される**。
  購読者ごとに独立した実行になる。
- これが「cold」。Dart の非 broadcast Stream と同じ性質。

> ポイント：Flow を変数に入れて持ち回っても、それは「処理のレシピ」を持っているだけ。
> 実際に走るのは `collect`（や `toList`/`first` などの終端演算子）の瞬間。
> Week 4 の `Sequence`（終端演算子で初めて流れる）と同じ「遅延評価」の発想。

## 1-4. 主な終端演算子

`collect` 以外にも「Flow を消費して値を確定させる」終端演算子がある：

```kotlin
numberFlow().toList()             // List<Int> に集める → [1, 2, 3]
numberFlow().first()              // 最初の1個 → 1
numberFlow().count()              // 個数 → 3
numberFlow().fold(0) { a, v -> a + v }  // 畳み込み → 6
```

終端演算子を呼ぶまで Flow は動かない、というのが cold flow の一貫したルール。
</content>
