/**
 * Week 2 演習 — 関数・ラムダ・スコープ関数
 * 実行: ./run.sh week02-functions-scope/Functions.kt
 *
 * Week2 の肝は「Dart に明確な対応がない」スコープ関数(let/run/apply/also/with)。
 * Flutter 経験者でも一番つまずきやすいので、戻り値とレシーバ(it か this か)を意識する。
 */

fun main() {
    section1TopLevelAndArgs()
    section2Lambdas()
    section3Extensions()
    section4ScopeFunctions()
}

// =====================================================================
// 1. トップレベル関数 / デフォルト引数 / 名前付き引数
// =====================================================================
// Dart: クラス外の関数もそのまま書ける点は同じ。デフォルト引数も似ているが、
//       Kotlin は呼び出し側で「名前付き引数」を自由に使えるのが強力。

// 式本体(= で1行)。Dart の `int add(a, b) => a + b;` に相当
fun add(a: Int, b: Int): Int = a + b

// デフォルト引数つき。Dart のように [] や {} で囲む必要はない
fun greet(name: String, greeting: String = "Hello", excited: Boolean = false): String {
    val mark = if (excited) "!!" else "."
    return "$greeting, $name$mark"
}

fun section1TopLevelAndArgs() {
    println("== 1. functions / args ==")
    println(add(2, 3))                                   // 5
    println(greet("Oto"))                                // Hello, Oto.
    // 名前付き引数: 順番を無視して、欲しい引数だけ指定できる（Dartの名前付きに近い）
    println(greet("Oto", excited = true))                // Hello, Oto!!
    println(greet(name = "Oto", greeting = "Hi"))        // Hi, Oto.
    println()
}

// =====================================================================
// 2. ラムダと it / 高階関数
// =====================================================================
// Dart: list.map((e) => e * 2)  →  Kotlin: list.map { it * 2 }
// 引数が1つのラムダは暗黙の `it` が使える。最後の引数がラムダなら () の外に出せる。

// 関数を引数に取る高階関数。(Int) -> Int は「Intを受けてIntを返す関数」型
fun applyTwice(x: Int, op: (Int) -> Int): Int = op(op(x))

fun section2Lambdas() {
    println("== 2. lambdas / higher-order ==")
    val nums = listOf(1, 2, 3, 4)
    println(nums.map { it * 2 })            // [2, 4, 6, 8]  it = 各要素
    println(nums.filter { it % 2 == 0 })    // [2, 4]
    // trailing lambda: 最後の引数がラムダなので () の外に書ける
    println(applyTwice(3) { it + 1 })       // 5  (3→4→5)
    // 複数引数のラムダは名前を付ける
    println(nums.foldIndexed(0) { index, acc, v -> acc + index * v }) // 0+0+2+6+12=20
    println()
}

// =====================================================================
// 3. 拡張関数 (Dart には標準ではない概念)
// =====================================================================
// 既存クラスにメソッドを「外から」生やせる。Dart の extension に近いが Kotlin の方が手軽。

fun String.shout(): String = uppercase() + "!"
// レシーバは this。null許容レシーバにも定義できる
fun Int.isEven(): Boolean = this % 2 == 0

fun section3Extensions() {
    println("== 3. extension functions ==")
    println("hello".shout())   // HELLO!
    println(4.isEven())        // true
    println(7.isEven())        // false
    println()
}

// =====================================================================
// 4. スコープ関数 let / run / apply / also / with  ← Week2 の本丸
// =====================================================================
// 覚え方: 「ブロック内でのオブジェクトの呼び方(it か this)」と「戻り値(ラムダ結果かオブジェクト自身)」の2軸。
//
//   関数   | ブロック内の参照 | 戻り値           | 典型用途
//   -------|------------------|------------------|--------------------------------
//   let    | it               | ラムダの結果     | null チェック後の処理 / 変換
//   run    | this             | ラムダの結果     | 計算してまとめて返す
//   with   | this             | ラムダの結果     | 同一オブジェクトに連続アクセス
//   apply  | this             | オブジェクト自身 | 初期設定(ビルダー的)
//   also   | it               | オブジェクト自身 | 副作用(ログ等)、チェーンの途中差し込み

class Person(var name: String = "", var age: Int = 0) {
    override fun toString() = "Person(name=$name, age=$age)"
}

fun section4ScopeFunctions() {
    println("== 4. scope functions ==")

    // let: nullable を安全に処理。?.let { } は null のときブロックごとスキップ
    val nickname: String? = "oto"
    val upper = nickname?.let { it.uppercase() } ?: "(none)"
    println("let     -> $upper")             // OTO

    // run: this でアクセスし、計算結果を返す
    val area = run {
        val w = 3; val h = 4
        w * h
    }
    println("run     -> $area")              // 12

    // apply: this を設定して「オブジェクト自身」を返す = 初期化に最適
    val p = Person().apply {
        name = "Oto"     // this.name
        age = 28
    }
    println("apply   -> $p")                 // Person(name=Oto, age=28)

    // also: it で受け、対象をそのまま返す = ログなどの副作用をチェーンに挟む
    val doubled = listOf(1, 2, 3)
        .map { it * 2 }
        .also { println("also    -> (debug) $it") } // [2, 4, 6]
        .sum()
    println("also sum-> $doubled")           // 12

    // with: レシーバを引数で渡し、this で連続アクセス（戻り値はラムダ結果）
    val summary = with(p) {
        "$name is $age years old"
    }
    println("with    -> $summary")           // Oto is 28 years old
    println()
}
