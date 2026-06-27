# 04. sealed class / enum — 取りうる状態を型で固定

## 4-1. sealed — 「実装はここに書いたものが全て」

Dart 3 の `sealed` とほぼ同じ思想。コンパイラに実装の有限集合を教える。

```kotlin
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

fun render(s: UiState<String>) = when (s) {   // else が要らない！
    is UiState.Loading -> "読込中"
    is UiState.Success -> s.data               // スマートキャストで s.data にアクセス
    is UiState.Error   -> s.message
}
```

### なぜ強力か

- `when` で全サブタイプを書けば `else` 不要。
  **新しい状態を追加すると、書き漏れの `when` が全部コンパイルエラーになる**。
  これが「状態の追加忘れ」を撲滅する。
- Dart の `sealed` + `switch` の網羅チェックと完全に同じ発想。
  Riverpod の `AsyncValue`（loading/data/error）を自前の型で表現するイメージ。
- Week 9 の ViewModel + StateFlow で、この `UiState` をそのまま画面状態として流す（README記載の通り再利用）。

## 4-2. enum — 値の有限集合

```kotlin
enum class Direction(val label: String) {
    NORTH("北"), SOUTH("南"), EAST("東"), WEST("西");

    fun opposite(): Direction = when (this) {
        NORTH -> SOUTH; SOUTH -> NORTH; EAST -> WEST; WEST -> EAST
    }
}
```

## 4-3. sealed と enum の違い

| | enum | sealed |
|---|---|---|
| 各要素 | 同じ形（同じプロパティ） | 違うデータを持てる |
| 例 | 曜日・方角 | Loading / Success(data) / Error(message) |
| 表すもの | **値**の有限集合 | **型**の有限集合 |

`Success` だけが `data` を持てるのが sealed の価値。「各状態で持つ情報が違う」なら sealed。
</content>
