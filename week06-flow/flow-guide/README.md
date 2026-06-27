# Kotlin Flow ガイド（Flutter/Dart・Riverpod経験者向け）

Week 6「Flow」の読み物。`FlowDemo.kt` の演習の背景を、Dart の `Stream` と
Riverpod の状態管理に対比しながら体系化したもの。**Riverpod 経験者の最大の武器**になる章。

## 目次

- **00. 全体像**（このファイル）— Flow とは、Stream/Riverpod との対応、cold/hot
- [01. cold flow](01-cold-flow.md) — `flow{}` / `emit` / `collect`
- [02. 演算子](02-operators.md) — `map` / `filter` / `onEach` / 終端演算子
- [03. StateFlow](03-stateflow.md) — 現在値を持つ hot flow（Week9で本番投入）
- [04. SharedFlow](04-sharedflow.md) — 一度きりのイベント用 hot flow

---

## 00. 全体像：Flow とは

### Dart / Riverpod との対応

| Dart / Riverpod | Kotlin | メモ |
|---|---|---|
| `Stream<T>`（非broadcast） | `Flow<T>`（cold） | 購読するたびに最初から流れる |
| `yield` / `StreamController.add` | `emit(value)` | 値を流す |
| `stream.listen { }` / `await for` | `flow.collect { }` | 購読する（suspend） |
| `stream.map().where()` | `flow.map { }.filter { }` | 変換演算子 |
| Riverpod `Notifier` の **state** | **`StateFlow<T>`** | 現在値を必ず持つ ★最重要 |
| イベント用 Stream（SnackBar等） | `SharedFlow<T>` | 現在値を持たない |
| `ref.watch(provider)` | `collectAsStateWithLifecycle()` | Compose で購読（Week9） |

### Flow は「非同期に複数の値が流れてくる」型

`suspend fun` が「1個の値を後で返す」のに対し、`Flow<T>` は
「**0個以上の値が時間をかけて流れてくる**」。Dart の `Stream` とまったく同じ立ち位置。

```kotlin
suspend fun fetchOne(): Int          // 値1個（Future 相当）
fun fetchMany(): Flow<Int>           // 値が複数流れる（Stream 相当）
```

### cold と hot — Flow を理解する最重要の軸

| | cold | hot |
|---|---|---|
| 代表 | `flow { }` | `StateFlow` / `SharedFlow` |
| いつ動くか | **collect された瞬間に最初から**実行 | collect と無関係に存在し続ける |
| 購読者ごと | それぞれに最初から流れる（各自専用） | 全購読者で**1本を共有** |
| 現在値 | 持たない | StateFlow は持つ / SharedFlow は持たない |
| Dart 相当 | 非 broadcast Stream | broadcast Stream（に近い） |

- **cold**：水道の蛇口。`collect`（蛇口をひねる）して初めて水が流れる。
  ひねるたびに最初から流れる。誰も見ていなければ何も起きない。
- **hot**：ずっと流れている川。`collect` してもしなくても流れていて、
  購読したらその時点以降の流れに合流する。

```kotlin
// cold：collect するまで numberFlow{} の中身は1行も実行されない
fun numberFlow(): Flow<Int> = flow {
    for (i in 1..3) { delay(100); emit(i) }
}
```

> Riverpod 経験者へ：`StateFlow` は「`Notifier` が持つ state そのもの」、
> `collectAsStateWithLifecycle()` は「`ref.watch`」。この対応が腹落ちすれば、
> Week 9 の状態管理は「Riverpod を Kotlin で書き直すだけ」に感じられる。
</content>
