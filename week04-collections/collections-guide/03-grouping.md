# 03. グルーピング — groupBy / associateBy / partition

「リストを Map に組み替える」操作。Dart には標準にないものが多く、**Kotlin の便利さが光る**領域。

## 3-1. groupBy — キーごとに List にまとめる

```kotlin
val byUser: Map<String, List<Order>> = orders.groupBy { it.user }
// {
//   "Oto" -> [Order(1,...), Order(2,...)],
//   "Aya" -> [Order(3,...), Order(4,...)],
//   "Ken" -> [Order(5,...)]
// }
byUser.forEach { (user, list) -> println("$user: ${list.size}件") }
```

- キーセレクタが返す値ごとに、**該当要素の List** を値とする Map を作る。
- Dart だと `Map` を手で組み立てる必要があったものが一発。
- 値側を加工したいなら `groupBy(keySelector, valueTransform)` か、後段で `mapValues`：

```kotlin
// カテゴリごとの売上合計（groupBy → mapValues の黄金パターン）
val byCategory = orders
    .groupBy { it.category }
    .mapValues { (_, list) -> list.sumOf { it.amount } }
// {book=4200, food=1300, gadget=2000}
```

`mapValues` は「Map の値だけ変換」。`(_, list)` の `_` は「キーは使わない」の意思表示。

## 3-2. associateBy — キー → 要素 の Map（1対1）

```kotlin
val byId: Map<Int, Order> = orders.associateBy { it.id }
byId[3]      // Order(id=3, ...)
```

- `groupBy` が `Map<K, List<V>>`（1対多）なのに対し、`associateBy` は `Map<K, V>`（1対1）。
- **キーが重複すると後勝ち**（上書き）。id のような一意キーで使う。
- 「リストを id 引きできる Map に変える」典型。API レスポンスの正規化でよく使う。

### associate 系のバリエーション

```kotlin
orders.associate { it.id to it.user }       // キーも値も指定 → Map<Int, String>
orders.associateWith { it.amount }          // 要素をキー、値を計算 → Map<Order, Int>
```

| 関数 | キー | 値 |
|---|---|---|
| `associateBy { }` | ラムダで指定 | 要素そのもの |
| `associateWith { }` | 要素そのもの | ラムダで指定 |
| `associate { k to v }` | ラムダ | ラムダ |

## 3-3. partition — 条件で2グループに分割

```kotlin
val (big, small) = orders.partition { it.amount >= 1000 }
// big   = 条件 true の要素
// small = 条件 false の要素
println("big=${big.size}, small=${small.size}")
```

- 戻り値は `Pair<List<T>, List<T>>`。分解宣言 `val (a, b) =` で2つに受けられる。
- `filter` を2回（条件と否定）書く代わりに**1回の走査で両方**得られる。Dart にはない。

## 3-4. その他の集約系

```kotlin
orders.groupingBy { it.user }.eachCount()   // ユーザーごとの件数 → Map<String, Int>
orders.chunked(2)                           // 2個ずつのリストに分割 → List<List<Order>>
orders.windowed(3)                          // 幅3のスライド窓
orders.distinctBy { it.user }               // user が重複しない最初の要素だけ
```

- `groupingBy { }.eachCount()` は「件数を数えるだけ」なら `groupBy` より効率的。
- `chunked` / `windowed` はページング処理や移動平均で便利。
</content>
