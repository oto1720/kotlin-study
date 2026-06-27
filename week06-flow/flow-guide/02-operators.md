# 02. 演算子 — map / filter / onEach / 終端

Flow の演算子は2種類に分かれる。この区別が Flow 理解の肝。

## 2-1. 中間演算子 vs 終端演算子

| | 中間演算子 | 終端演算子 |
|---|---|---|
| 例 | `map` / `filter` / `onEach` / `take` | `collect` / `toList` / `first` / `fold` |
| 戻り値 | `Flow<T>`（また Flow） | 確定値（`List`/`Int` など）または `Unit` |
| 実行 | **遅延**（まだ流れない） | ここで**初めて流れる** |

Dart の `Iterable`/`Sequence` と同じく、「中間演算子をいくら繋いでも、
終端演算子を呼ぶまで何も実行されない」。

## 2-2. 主な中間演算子

```kotlin
numberFlow()
    .map { it * 10 }            // 各値を変換 → 10, 20, 30
    .filter { it >= 20 }        // 条件で絞る → 20, 30
    .onEach { println("after ops: $it") }  // 副作用を挟む（値は変えない）
    .collect { }                // ← ここで初めて流れる
```

- `map` / `filter` … Dart の Stream/Iterable と同じ感覚。
- `onEach { }` … 値を変えずに**副作用だけ**挟む（ログ・デバッグ）。スコープ関数の `also` に近い。
- `take(n)` … 先頭 n 個で打ち切る。`drop(n)` … 先頭 n 個を捨てる。

### 非同期変換ができるのが Stream/Sequence との違い

Flow の `map` の中では **suspend 関数を呼べる**（コレクションの `map` では呼べない）：

```kotlin
flowOf(1, 2, 3)
    .map { id -> fetchUserName(id) }   // map の中で suspend 関数を await できる
    .collect { println(it) }
```

これが「コレクション操作」と「Flow」の決定的な違い。非同期な変換を演算子チェーンに書ける。

## 2-3. 終端演算子で集計

```kotlin
val sum = numberFlow()
    .map { it * 10 }
    .filter { it >= 20 }
    .fold(0) { acc, v -> acc + v }   // 20 + 30 = 50
```

`fold` / `reduce` / `count` / `toList` / `first` などは Week 4 のコレクションと同名・同思想。
違いは「非同期に流れてくる値」を畳み込む点。

## 2-4. よく使う合成・制御演算子（発展）

| 演算子 | 何をする | Dart/Rx 的な対応 |
|---|---|---|
| `combine(a, b) { x, y -> }` | 複数 Flow の最新値を合成 | `combineLatest` |
| `zip(other) { a, b -> }` | 2つの Flow を1対1で組む | `zip` |
| `flatMapLatest { }` | 新しい値が来たら前の内側 Flow をキャンセル | 検索のdebounce的処理で頻出 |
| `debounce(ms)` | 一定時間値が来なければ最後の1つを流す | 検索入力の間引き |
| `distinctUntilChanged()` | 同じ値の連続を除く | StateFlow が内部でこれをやる |
| `catch { }` | 上流の例外を捕まえる | try/catch 相当 |

```kotlin
// 検索ボックスの典型：入力を間引いて、最新のクエリだけ検索する
queryFlow
    .debounce(300)                  // 300ms 入力が止まったら
    .distinctUntilChanged()         // 同じ語は無視
    .flatMapLatest { q -> search(q) }  // 新しい入力が来たら前の検索を捨てる
    .collect { showResults(it) }
```

> Riverpod で `ref.watch` を組み合わせて派生状態を作っていた感覚が、
> `combine` / `flatMapLatest` にそのまま対応する。Week 10〜11 の検索機能で効いてくる。
</content>
