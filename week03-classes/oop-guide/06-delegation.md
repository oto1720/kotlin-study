# 06. 委譲（Delegation）by — 継承の代わりの再利用手段

OOPで「継承より委譲（composition over inheritance）」と言われる、その**委譲を言語が直接サポート**。
Dart にはない強力機能。

## 6-1. インターフェース委譲

```kotlin
interface Logger { fun log(msg: String) }
class ConsoleLogger : Logger {
    override fun log(msg: String) = println("[LOG] $msg")
}

// Logger の実装を base に「丸投げ」。自分は追加分だけ書く
class Service(base: Logger) : Logger by base {
    fun run() = log("started")   // 委譲先 ConsoleLogger.log が呼ばれる
}
```

`: Logger by base` は「Logger の全メソッドを `base` に転送するボイラープレートを自動生成」してくれる。
継承せずに実装を再利用でき、必要なメソッドだけ `override` で上書きできる。

Dart でこれをやると、全メソッドを手で `base.log(...)` と転送する必要がある。
Kotlin はその転送コードを言語が生成。

## 6-2. プロパティ委譲（重要）

プロパティの getter/setter のロジックを別オブジェクトに委譲できる。標準で便利なものが揃う：

```kotlin
import kotlin.properties.Delegates

class Example {
    val lazyValue: String by lazy {       // 初回アクセス時に1回だけ計算
        println("計算中…")
        "computed"
    }
    var name: String by Delegates.observable("初期") { _, old, new ->
        println("$old -> $new に変化")     // 変更を監視
    }
}
```

- `by lazy { }` … 遅延初期化。Dart の `late final` + 自前メモ化を1行で。
- `by Delegates.observable` … 変更フック。
- Android では `by viewModels()` や `by remember`（Compose）など、この仕組みの上に多くのAPIが乗る。
  Week 7〜9 で再会する。
</content>
