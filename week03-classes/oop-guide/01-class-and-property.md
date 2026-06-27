# 01. クラスとプロパティ — 「フィールド」ではなく「プロパティ」で考える

## 1-1. 最小のクラス

```kotlin
class Person(val name: String, var age: Int)
```

これ1行で、Dart の以下と**ほぼ等価**：

```dart
class Person {
  final String name;
  int age;
  Person(this.name, this.age);
}
```

Kotlin の `class Person(...)` の `( )` が**プライマリコンストラクタ**。
ここに `val`/`var` を付けると「コンストラクタ引数」と「プロパティ宣言」を**同時に**行う。
Dart の `this.name` ショートハンドの強化版。

- `val name` → 読み取り専用プロパティ（Dart の `final`）
- `var age` → 読み書き可能プロパティ

`val`/`var` を**付けない**と、ただのコンストラクタ引数で本体に保存されない：

```kotlin
class Person(name: String) {            // name はプロパティにならない
    val displayName = name.uppercase()  // init 時に使うだけ
}
```

## 1-2. プロパティ＝「getter/setter + 隠れたフィールド」

ここが Dart と最も考え方が違う。Kotlin に「フィールド」を直接書く構文はない。
**すべてプロパティ**で、内部に**バッキングフィールド `field`** を隠し持つ。

```kotlin
class Temperature {
    var celsius: Double = 0.0
        get() = field                      // field = 隠れた実フィールド
        set(value) {
            require(value >= -273.15) { "絶対零度より下は不可" }
            field = value                  // ここで field に代入
        }

    // 計算プロパティ（バッキングフィールドを持たない）
    val fahrenheit: Double
        get() = celsius * 9 / 5 + 32       // 毎回計算。Dart の getter と同じ
}
```

Dart で書くと：

```dart
class Temperature {
  double _celsius = 0.0;                    // 手書きのバッキングフィールド
  double get celsius => _celsius;
  set celsius(double value) {
    if (value < -273.15) throw ArgumentError();
    _celsius = value;
  }
  double get fahrenheit => _celsius * 9 / 5 + 32;
}
```

### 違いのポイント

- Dart は `_celsius` という別フィールドを**自分で**用意する必要がある。
  Kotlin は `field` キーワードが自動で提供。
- だから Kotlin は `var celsius = 0.0` と書くだけでデフォルト getter/setter が自動生成済み。
  `person.age = 30` という「フィールド代入っぽい」記法は、実は**setter 呼び出し**。
- `fahrenheit` のように `get()` だけ書いて `field` を使わなければ、
  バッキングフィールドは**生成されない**（＝計算プロパティ）。

> 実務的含意：Kotlin では「とりあえず public な var」でも、後から getter/setter に
> ロジックを足せる。**API を壊さずカプセル化を後付けできる**のが言語の基本動作。

## 1-3. 初期化のタイミング：`init` ブロックと初期化順序

プライマリコンストラクタには本体（`{ }`）が書けない。処理を走らせたいときは `init`：

```kotlin
class User(val name: String, age: Int) {
    val isAdult: Boolean
    init {
        // プロパティ初期化子と init は「上から書いた順」に実行される
        require(age >= 0) { "age は0以上" }
        isAdult = age >= 18
        println("User($name) を作成")
    }
}
```

実行順序は「**プロパティ初期化子と init ブロックを、ソースコードに書いた順に上から**」。試験頻出。

## 1-4. セカンダリコンストラクタ

複数の作り方を提供したいとき。ただし Kotlin では**デフォルト引数**で済むことが多く、
Dart の named constructor ほど多用しない。

```kotlin
class Rectangle(val width: Int, val height: Int) {
    constructor(side: Int) : this(side, side)   // 必ず primary を this(...) で呼ぶ
}

// Kotlin らしいのはむしろデフォルト引数：
class Box(val width: Int = 1, val height: Int = width)
val b = Box(height = 5)   // 名前付き引数（Dart と同じ感覚）
```

Dart の `Rectangle.square(int side)` のような named constructor に相当するのは、
Kotlin では **companion object のファクトリ関数**（→ 05章）がより一般的。
</content>
