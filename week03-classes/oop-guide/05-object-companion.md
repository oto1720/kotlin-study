# 05. object と companion object — シングルトンと「staticの代替」

Dart にない Kotlin の目玉機能。

## 5-1. object 宣言 ＝ 言語レベルのシングルトン

```kotlin
object AppConfig {
    const val version = "1.0.0"
    fun describe() = "App v$version"
}
// 使う側：インスタンス化せず、そのまま
AppConfig.describe()
```

`object` はクラス定義とインスタンス生成を**同時に1回だけ**行う。
スレッドセーフに遅延初期化されるシングルトンが1行で手に入る。
Dart だと `factory Singleton()` + static フィールドで自作していたものが言語機能に。

## 5-2. companion object ＝ クラスに紐づく唯一のオブジェクト

Kotlin に Java/Dart の `static` はない。代わりに「クラスに1つだけ付随する `object`」を書く。

```kotlin
class Circle private constructor(val radius: Double) {
    companion object {
        const val PI = 3.14159
        fun of(r: Double) = Circle(r)   // ファクトリ（private コンストラクタを呼べる）
    }
}
Circle.of(2.0)      // Circle.PI も Circle.of も「クラス名.メンバ」で呼べる
```

### Dart の static との違い（重要）

見た目は `Circle.of(...)` で static そっくりだが、`companion object` は**実体を持つオブジェクト**。
だから**インターフェースを実装したり、変数に代入したりできる**：

```kotlin
interface Factory<T> { fun create(): T }
class Widget {
    companion object : Factory<Widget> {  // companion がインターフェースを実装！
        override fun create() = Widget()
    }
}
```

- 「static メソッドを差し替え不能」という Java/Dart の制約から解放される。テストでのモック化で効く。
- ファクトリパターン（`of`/`create`/`from`）の置き場として定番。
  Dart の named constructor（`Circle.fromRadius`）の役割をここが担う。
</content>
