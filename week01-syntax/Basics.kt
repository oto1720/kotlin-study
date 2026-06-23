/**
 * Week 1 スターター演習
 * Kotlin Playground (play.kotlinlang.org) か Android Studio で main() を実行して確認する。
 * Dart の感覚と比べながら埋めていく。
 */

fun main() {
    // --- 1. val / var ---
    // TODO: val で名前、var で年齢を宣言して出力する（Dartの final/var に対応）

    // --- 2. 文字列補間 ---
    val name = "Oto"
    println("Hello, $name! (${name.length} chars)") // Dartと同じ感覚

    // --- 3. null安全 ---
    val nickname: String? = null
    // TODO: エルビス演算子 ?: を使い、null のときは "no nickname" を出力する
    //       （Dart の ?? に相当）

    // --- 4. when（式として値を返せる） ---
    val score = 82
    val grade = when {
        score >= 90 -> "A"
        score >= 80 -> "B"
        else -> "C"
    }
    println("grade = $grade")
    // TODO: when を使って曜日番号(1..7)を曜日名に変換する関数を書いてみる

    // --- 5. range と for ---
    // TODO: 1 から 10 までの偶数だけを出力する（until / step を使ってみる）

    // --- 6. スマートキャスト ---
    val any: Any = "I am a string"
    if (any is String) {
        // ここでは any は String として扱える（キャスト不要）= スマートキャスト
        println("length = ${any.length}")
    }
}

// 振り返り: このファイルで「Dartと一番違う」と感じた点を docs/log.md に1行で書く
