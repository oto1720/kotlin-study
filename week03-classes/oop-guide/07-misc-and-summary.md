# 07. その他の要素 & まとめ

## 7-1. ネストクラスとインナークラス

```kotlin
class Outer {
    private val x = 1
    class Nested {                 // デフォルトは「静的ネスト」＝外側を参照しない
        // x にアクセスできない
    }
    inner class Inner {            // inner を付けると外側のインスタンスを参照
        fun show() = x             // 外側の x にアクセスできる
    }
}
```

Dart にこの区別はない。Kotlin の `class Nested`（デフォルト）は外側インスタンスへの参照を
**持たない**ので、コールバックでのメモリリークを避けたいときに安全。
`inner` を付けて初めて外側を掴む。

## 7-2. 知っておくべき周辺要素

- **`equals`/`hashCode`**：data class でない通常クラスで値等価が欲しいときは手書き。
  等価性を変えたら **`hashCode` も必ずセットで**変える（`HashMap`/`HashSet` が壊れる）。Dart と同じ鉄則。
- **`Any`**：全クラスのルート型（Dart の `Object`）。null許容版は `Any?`。
- **`Nothing`**：「値を返さない／インスタンスが存在しない」型。`UiState<Nothing>` で
  `Loading` をどんな `UiState<T>` の位置にも入れられる（共変 `<out T>` のおかげ）。Dart の `Never` 相当。
- **`value class`**：実行時オーバーヘッドなしの軽量ラッパ。ID型などに。

```kotlin
@JvmInline
value class UserId(val value: Int)   // 実行時は Int として扱われる
```

- **拡張関数**：クラスを継承せず外から振る舞いを足す（Week 2 既習）。

```kotlin
fun User.isAdult() = age >= 18   // User を書き換えずメソッド追加（実体は static 関数）
```

## 7-3. クラス設計の意思決定フロー

- **ただのデータの入れ物** → `data class`
- **状態が有限で各状態が違うデータを持つ**（Loading/Success/Error）→ `sealed interface` + `when`
- **値の有限集合で各要素が同形**（曜日・方角）→ `enum class`
- **アプリ全体で1個だけ**（設定・ロガー）→ `object`
- **クラスに紐づくファクトリ・定数**（`User.fromJson`）→ `companion object`
- **既存実装を継承せず再利用したい** → `by` 委譲
- **契約だけ定義（状態なし）** → `interface`
- **共通の状態 + 実装を継承で共有** → `abstract class`（最後の手段）

## 7-4. Dart脳からの乗り換えチェックリスト

- [ ] 「フィールド代入」に見える `obj.x = 1` は実は setter 呼び出しだと理解した
- [ ] 継承したいクラス/メソッドには `open` が要ると体に入れた
- [ ] `override` は付け忘れるとコンパイルエラー（Dart の `@override` と違う）
- [ ] `companion object` が Dart の named constructor / static の置き場だと分かった
- [ ] `sealed` + `when` の網羅チェックが状態管理の核だと納得した
- [ ] `by` 委譲が「継承より委譲」を言語で支援していると理解した
</content>
