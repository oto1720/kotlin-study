# 01. 変換系 — map / filter / flatMap

コレクションの「形を変える」操作。すべて**新しい List を返す**（非破壊）。

## 1-1. map — 各要素を変換

```kotlin
val nums = listOf(1, 2, 3, 4, 5)
nums.map { it * it }        // [1, 4, 9, 16, 25]
```

- `it` は「ラムダの唯一の引数」の暗黙の名前（Dart にはない便利機能）。
  明示するなら `nums.map { n -> n * n }`。
- Dart の `nums.map((e) => e * e).toList()` と同じ。
  ただし Kotlin の `map` は**直接 List を返す**ので `.toList()` 不要（List に対して呼んだ場合）。

### 関連する変換

```kotlin
nums.mapIndexed { i, v -> "$i:$v" }     // インデックス付き → ["0:1", "1:2", ...]
nums.mapNotNull { if (it % 2 == 0) it else null }  // null を除外しつつ変換 → [2, 4]
```

`mapNotNull` は「変換 + null除去」を一度にやる頻出パターン。
Dart で `.map(...).where((e) => e != null)` と書いていたものが一発。

## 1-2. filter — 条件で絞る

```kotlin
nums.filter { it % 2 == 1 }       // [1, 3, 5]（Dart の where）
nums.filterNot { it % 2 == 1 }    // [2, 4]（条件の否定）
nums.filterIsInstance<String>()   // 型で絞る（混在リストから String だけ）
```

`filter` = Dart の `where`。`filterNot` と `filterIsInstance` は Kotlin の追加便利系。

## 1-3. flatMap — 展開して平坦化

各要素を**リストに展開**し、それらを1本に繋げる。Dart の `expand` に相当。

```kotlin
nums.flatMap { listOf(it, -it) }   // [1, -1, 2, -2, 3, -3, 4, -4, 5, -5]

// 実務例：ネストしたリストを平らに
val groups = listOf(listOf(1, 2), listOf(3, 4))
groups.flatten()                   // [1, 2, 3, 4]（変換不要ならこちら）
groups.flatMap { it.map { n -> n * 10 } }  // [10, 20, 30, 40]
```

- `flatten()` … 変換せずネストを潰すだけ。
- `flatMap { }` … 「各要素 → リスト」に変換しつつ潰す。

## 1-4. チェーンして読む

これらは数珠つなぎにでき、上から順に読める：

```kotlin
val result = orders
    .filter { it.amount >= 1000 }      // 1000円以上に絞り
    .map { it.user }                   // ユーザー名に変換し
    .distinct()                        // 重複を除く
println(result)                        // [Oto, Aya, Ken]
```

> 注意：List に対するチェーンは**各ステップで中間 List を作る**。
> 要素数が大きい/段数が多いときは `Sequence`（→ 04章）で遅延評価にすると速い。
</content>
