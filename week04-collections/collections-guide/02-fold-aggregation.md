# 02. 畳み込み・集計 — fold / reduce / sumOf 系

「複数の要素を1つの値にまとめる」操作。

## 2-1. fold — 初期値ありの畳み込み

```kotlin
val nums = listOf(1, 2, 3, 4, 5)
nums.fold(0) { acc, v -> acc + v }      // 15
```

- 第1引数が**初期値（accumulator の初期状態）**。
- ラムダは `(これまでの累積, 今の要素) -> 新しい累積`。
- Dart の `nums.fold(0, (acc, v) => acc + v)` と完全に同じ思想。
- 初期値があるので**空リストでも安全**（初期値が返る）。

```kotlin
// 文字列の組み立てなど、型が変わる集計もできる
val csv = nums.fold("") { acc, v -> if (acc.isEmpty()) "$v" else "$acc,$v" }  // "1,2,3,4,5"
```

## 2-2. reduce — 初期値なしの畳み込み

```kotlin
nums.reduce { acc, v -> acc * v }       // 120（= 1*2*3*4*5）
```

- 初期値を取らず、**最初の要素を初期値として**始める。
- **空リストだと例外**（`UnsupportedOperationException`）。空がありうるなら `reduceOrNull` か `fold`。
- 累積と要素が**同じ型**である必要がある（`fold` は型を変えられる）。

| | 初期値 | 空リスト | 型変換 |
|---|---|---|---|
| `fold` | あり | 安全 | できる |
| `reduce` | なし（先頭を使う） | 例外 | できない |

## 2-3. 数値集計のショートカット（Kotlin の手厚い部分）

Dart では `fold` で書きがちな集計が、Kotlin は専用関数で一発：

```kotlin
orders.sumOf { it.amount }                  // 合計 → 7500
orders.map { it.amount }.average()          // 平均 → 1500.0
orders.maxByOrNull { it.amount }            // 最大の要素（Order全体）
orders.minByOrNull { it.amount }            // 最小の要素
orders.count { it.amount >= 1000 }          // 条件に合う件数
orders.any { it.category == "book" }        // 1つでも該当するか → true
orders.all { it.amount > 0 }                // 全部該当するか → true
orders.none { it.amount < 0 }               // 1つも該当しないか → true
```

- `sumOf { }` … セレクタが返す数値の合計。`Int`/`Long`/`Double` を返り値の型で自動判別。
- `maxByOrNull { }` … 「セレクタの値が最大の**要素そのもの**」を返す。
  最大値そのものが欲しいなら `maxOf { }`。空なら `OrNull` 版が `null`。
- `any`/`all`/`none` … Dart にもある述語系。short-circuit（早期打ち切り）する。

## 2-4. 並べ替え（集計の仕上げによく使う）

```kotlin
orders.sortedBy { it.amount }               // 昇順（新Listを返す＝非破壊）
orders.sortedByDescending { it.amount }     // 降順
orders.sortedWith(compareBy({ it.category }, { it.amount }))  // 複合キー
```

> ランキング作成の定番パターン（`Collections.kt` section4 と同じ）：
> ```kotlin
> val ranking = orders
>     .groupBy { it.user }                       // ユーザーごとに集約（→ 03章）
>     .map { (user, list) -> user to list.sumOf { it.amount } }  // 利用額を合計
>     .sortedByDescending { it.second }          // 多い順に並べる
> ```
</content>
