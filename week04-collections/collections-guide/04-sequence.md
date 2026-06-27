# 04. Sequence（遅延評価）— Dart の Iterable に相当

## 4-1. List と Sequence の決定的な違い

```kotlin
// List：各操作で「中間リスト」を毎回作る（即時評価 / eager）
val r1 = (1..1_000_000)
    .map { it * 2 }       // ← 100万件の List を作る
    .filter { it % 3 == 0 } // ← さらに別の List を作る
    .take(5)              // ← そこから5件
    .toList()

// Sequence：要素を1個ずつ末端まで流す（遅延評価 / lazy）
val r2 = (1..1_000_000).asSequence()
    .map { it * 2 }       // ← まだ計算しない
    .filter { it % 3 == 0 } // ← まだ計算しない
    .take(5)              // ← 5個取れたら止める
    .toList()             // ← ここ（終端演算子）で初めて流れる
// [6, 12, 18, 24, 30]
```

### 何が違うのか

- **List（eager）**：`map` の時点で100万件すべてを変換した中間リストを作り、
  次の `filter` でまた全件走査…と、各段で全要素 × 中間リストを生成する。
- **Sequence（lazy）**：要素1個を `map → filter → take` と**縦に**通す。
  `take(5)` が5個集めた時点で**残り99万9995件は一切計算されない**。

> Dart の `Iterable`（`.map().where()` は遅延、`.toList()` で確定）とまったく同じ思想。
> Kotlin は List がデフォルトで eager なので、遅延が欲しいときは明示的に `asSequence()` する。

## 4-2. 即時評価 / 遅延評価の対応表

| | List（eager） | Sequence（lazy） |
|---|---|---|
| 評価タイミング | 各操作で即実行 | 終端演算子まで遅延 |
| 中間オブジェクト | 各段で中間 List | 作らない |
| Dart 相当 | `.toList()` 済み | `Iterable`（未確定） |
| 起点 | `listOf(...)` | `.asSequence()` / `generateSequence` / `sequence{}` |
| 確定（終端） | （常に確定） | `toList()` / `first()` / `sum()` / `forEach` など |

## 4-3. どちらを使うべきか

**Sequence が有利な場面：**
- 要素数が多い（数千〜）
- 操作の段数が多い（`map`→`filter`→`map`…）
- `take` / `first` / `find` で**途中で打ち切れる**（全件見なくていい）

**List のままで良い場面：**
- 要素数が小さい（数十〜数百）。Sequence はイテレータのオーバーヘッドがあるので、
  小さいデータでは List の方が速いことすらある。
- 一度きりの単純な `map` や `filter`。

```kotlin
// 無限シーケンスも作れる（List では不可能）
val firstFive = generateSequence(1) { it + 1 }   // 1, 2, 3, ... 無限
    .map { it * it }
    .take(5)
    .toList()                                     // [1, 4, 9, 16, 25]
```

> 結論：**「大量データ × 多段 × 途中打ち切り」のときだけ `asSequence()`**。
> 迷ったら List のままで良い。早すぎる最適化は不要。
</content>
