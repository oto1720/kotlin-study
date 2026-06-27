# Kotlin コレクション操作ガイド（Flutter/Dart経験者向け）

Week 4「コレクション操作」の読み物。`Collections.kt` の演習の背景を、
Dart の `Iterable` と常に対比しながら体系化したもの。

## 目次

- **00. 全体像**（このファイル）— コレクションの種類、可変/不変、Dartとの対応
- [01. 変換系](01-transform.md) — `map` / `filter` / `flatMap` / `mapNotNull`
- [02. 畳み込み・集計](02-fold-aggregation.md) — `fold` / `reduce` / `sumOf` / `average` / `maxByOrNull`
- [03. グルーピング](03-grouping.md) — `groupBy` / `associateBy` / `partition`
- [04. Sequence（遅延評価）](04-sequence.md) — `List` との違いと使いどころ

---

## 00. 全体像：Kotlin コレクションの地図

### 3つの基本型

| 型 | 中身 | Dart 相当 | リテラル |
|---|---|---|---|
| `List<T>` | 順序あり・重複OK | `List<T>` | `listOf(1, 2, 3)` |
| `Set<T>` | 重複なし・順序保証は実装次第 | `Set<T>` | `setOf(1, 2)` |
| `Map<K, V>` | キー→値 | `Map<K, V>` | `mapOf("a" to 1)` |

`"a" to 1` の `to` は **`Pair` を作る中置関数**。`mapOf` はその `Pair` を並べて Map を作る。

### 可変 / 不変の区別（Dart にない明確な分離）

Kotlin は**読み取り専用インターフェース**と**可変インターフェース**を型レベルで分けている。

| 読み取り専用 | 可変 |
|---|---|
| `List<T>`（`listOf`） | `MutableList<T>`（`mutableListOf`） |
| `Set<T>`（`setOf`） | `MutableSet<T>`（`mutableSetOf`） |
| `Map<K,V>`（`mapOf`） | `MutableMap<K,V>`（`mutableMapOf`） |

```kotlin
val nums = listOf(1, 2, 3)          // List<Int>：add できない（コンパイルエラー）
val buf  = mutableListOf(1, 2, 3)   // MutableList<Int>：buf.add(4) できる
```

- `val nums` の `val` は「**変数 nums の再代入**」を禁止するだけ。
  中身を変えられるかどうかは**型**（`List` か `MutableList` か）で決まる。ここを混同しやすい。
- Dart は `List` 1種類で、不変にしたいときは `const` や `List.unmodifiable` を使う。
  Kotlin は「読み取り専用を**デフォルト**にして、必要な時だけ可変」という思想。

### 関数型操作は「新しいコレクションを返す」

`map`/`filter` などは元のコレクションを変更せず**新しい List を返す**（Dart と同じ非破壊）。
`.sort()`（破壊的・MutableListのみ）と `.sorted()`（非破壊・新List）の命名規則を覚えると迷わない：

| 破壊的（その場で変更） | 非破壊（新コレクションを返す） |
|---|---|
| `sort()` | `sorted()` / `sortedBy {}` |
| `reverse()` | `reversed()` |
| `shuffle()` | `shuffled()` |

> 思想：Dart の `Iterable` メソッド群とほぼ同じ感覚で書ける。
> 違いは「**可変/不変が型で分かれる**」点と「`groupBy`/`associateBy`/`partition`/`sumOf` など
> Kotlin 標準が手厚い」点。この2つを押さえれば移行はスムーズ。

### null を返す系の命名規則

空コレクションに対して「最初の要素」などを取ると、Kotlin は2系統を用意する：

| 例外を投げる版 | null を返す版 |
|---|---|
| `first()` | `firstOrNull()` |
| `last()` | `lastOrNull()` |
| `max()`（旧） | `maxByOrNull {}` / `maxOrNull()` |
| `single()` | `singleOrNull()` |

`OrNull` が付く方は空でも安全（`null` が返る）。null安全と相性が良いので実務では `OrNull` を多用する。
</content>
