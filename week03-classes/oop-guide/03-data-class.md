# 03. data class — 値オブジェクトの自動生成（Dart freezed 相当）

```kotlin
data class User(val id: Int, val name: String)
```

`data` を付けるとコンパイラが自動生成するもの：

- `equals()` / `hashCode()` … **値の等価判定**（Dart のデフォルトは参照等価で手書きが必要だった）
- `toString()` … `User(id=1, name=Oto)`
- `copy()` … freezed の `copyWith` 相当
- `componentN()` … 分解宣言 `val (id, name) = user` を可能にする

```kotlin
val u1 = User(1, "Oto")
val u2 = u1.copy(name = "Kotaro")   // 一部だけ変えた新インスタンス
println(u1 == u1.copy())            // true（値で等価判定）
val (id, name) = u2                 // 分解宣言
```

## 制約と落とし穴

- プライマリコンストラクタに**最低1つ** `val`/`var` パラメータが必要。
- `equals`/`hashCode`/`toString` は**プライマリコンストラクタのプロパティだけ**を対象にする。
  本体に書いた `val`/`var` は無視される：

```kotlin
data class Foo(val a: Int) {
    var b: Int = 0   // equals/hashCode/toString に含まれない！
}
Foo(1).apply { b = 9 } == Foo(1).apply { b = 5 }  // true（b は無視される）
```

- `copy()` は**シャローコピー**。ネストした可変オブジェクトは共有される。
- `data class` は `open` にできない（継承前提の設計と相性が悪い）。

> Flutter で freezed を使っていたなら、`data class` は「コード生成なし・即時・標準機能で
> freezed の8割」をやってくれる存在。`build_runner` を回す必要がないのが快感。
</content>
