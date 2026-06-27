# 02. 可視性・継承・インターフェース・多態性

## 2-1. 可視性修飾子 — Dart の `_` より細かい

Dart は `_foo` で「ライブラリ（ファイル）外から見えない」の一段階だけ。Kotlin は4段階。

| 修飾子 | 範囲 | Dart 相当 |
|---|---|---|
| `public`（デフォルト） | どこからでも | 修飾子なし |
| `private` | **同じクラス内**（トップレベルなら同じファイル内） | （`_` に近いが厳密に違う） |
| `protected` | そのクラスとサブクラス | （Dart にない） |
| `internal` | **同じモジュール内**（同じコンパイル単位） | `_`（ライブラリ単位）に近い |

```kotlin
class BankAccount {
    private var balance = 0          // 外から触れない（カプセル化の核）
    fun deposit(amount: Int) { balance += amount }
    fun getBalance() = balance       // 読み取りだけ公開
}
```

> 移行で効くのは `internal`。「モジュールの中では使うが外には見せない」公開範囲で、
> Dart の `_` の感覚に一番近い。逆に Kotlin の `private` は Dart の `_` より**狭い**（クラス単位）。

## 2-2. 継承 — `open` / `override` / 抽象クラス

### デフォルトは final、継承には `open`

```kotlin
open class Animal(val name: String) {     // open がないと継承不可
    open fun sound(): String = "..."      // open がないと override 不可
    fun describe() = "$name says ${sound()}"  // これは override 禁止（final）
}

class Dog(name: String) : Animal(name) {
    override fun sound(): String = "ワン"  // override は必須キーワード
}
```

Dart との3つの違い：

1. **`open` が必須**：Kotlin は class と fun の両方に `open` が要る。Dart は何もせず継承・上書き可。
2. **`override` は文法**：消すとコンパイルエラー。Dart の `@override` は単なる注意喚起で消しても動く。
3. **`extends` ではなく `:`**：親クラスはコンストラクタ呼び出し `Animal(name)` の形で `super` を兼ねる。

### 抽象クラス `abstract`

```kotlin
abstract class Shape {
    abstract fun area(): Double          // 実装なし＝サブクラスが必ず実装
    fun describe() = "面積は ${area()}"   // 共通実装も持てる
}
class Circle(val r: Double) : Shape() {
    override fun area() = Math.PI * r * r
}
```

`abstract` メンバは自動的に `open`（明示不要）。インスタンス化不可は Dart と同じ。

### super でのアクセス

```kotlin
open class Base { open fun greet() = "Base" }
class Derived : Base() {
    override fun greet() = super.greet() + " + Derived"
}
```

## 2-3. インターフェース — Dart の暗黙インターフェースとの違い

Dart は「すべてのクラスが暗黙にインターフェース」。Kotlin は明示的に `interface` を宣言。

```kotlin
interface Clickable {
    fun click()                          // 抽象メソッド
    fun showOff() = println("クリックできるよ")  // デフォルト実装OK
    val label: String                    // 抽象プロパティ（バッキングフィールド不可）
        get() = "default"                // getter は持てる
}
interface Focusable { fun click() }

class Button : Clickable, Focusable {    // 複数実装OK（多重継承の代替）
    override val label = "送信"
    override fun click() = println("押された")
}
```

### interface vs abstract class の使い分け

| | interface | abstract class |
|---|---|---|
| 多重実装 | ○（いくつでも） | ×（クラスは1つだけ） |
| 状態（バッキングフィールド） | **×** | ○ |
| コンストラクタ | × | ○ |
| デフォルト実装 | ○ | ○ |

> 覚え方：「**振る舞いの契約だけ → interface**」「**状態 + 共通実装を共有 → abstract class**」。
> Android では interface 中心（特にリポジトリの契約定義）。

### ダイヤモンド問題の解決

```kotlin
interface A { fun f() = "A" }
interface B { fun f() = "B" }
class C : A, B {
    override fun f() = super<A>.f() + super<B>.f()  // どちらを呼ぶか明示
}
```

## 2-4. ポリモーフィズム（多態性） — `when` + スマートキャスト

```kotlin
fun describe(animal: Animal): String = when (animal) {
    is Dog -> "犬: ${animal.sound()}"   // is でチェックするとブロック内で Dog 型にスマートキャスト
    is Cat -> "猫: ${animal.sound()}"   // animal as Dog のキャスト不要
    else -> "不明な動物"
}
```

Kotlin のスマートキャストは Dart の type promotion より広く効く（`&&`/`||` をまたいでも効く）：

```kotlin
val obj: Any = "hello"
if (obj is String && obj.length > 3) {   // is の後、obj は String として扱える
    println(obj.uppercase())             // キャスト不要
}
```

**注意**：`var` プロパティや別スレッドから変わりうるものはスマートキャストできない。
`sealed class` + `when` の組み合わせ（→ 04章）が、多態性を**網羅性チェック付き**で安全にやる定番。
</content>
