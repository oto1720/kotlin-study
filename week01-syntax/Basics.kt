/**
 * Week 1 スターター演習
 * Kotlin Playground (play.kotlinlang.org) か Android Studio で main() を実行して確認する。
 * Dart の感覚と比べながら埋めていく。
 */

fun main() {
    // --- 1. val / var ---
    // Dart: final name = "Oto";  var age = 27;
    // Kotlin: val=再代入不可(final相当) / var=再代入可。型は推論されるので String/Int は省略可。
    val personName = "Oto"   // val なので personName = ... は再代入できない（コンパイルエラー）
    var age = 27             // var は再代入できる
    age += 1                 // 28（誕生日が来た想定）
    println("name=$personName, age=$age")

    // --- 2. 文字列補間 ---
    val name = "Oto"
    println("Hello, $name! (${name.length} chars)") // Dartと同じ感覚

    // --- 3. null安全 ---
    val nickname: String? = null
    // Dart の ?? に相当するのが Kotlin のエルビス演算子 ?:
    // nickname が null なら右辺 "no nickname" が使われる
    println(nickname ?: "no nickname")
    // ?. (安全呼び出し) と組み合わせる例: null なら length も評価されず null になる
    println("nickname length = ${nickname?.length ?: 0}")

    // --- 4. when（式として値を返せる） ---
    val score = 82
    val grade = when {
        score >= 90 -> "A"
        score >= 80 -> "B"
        else -> "C"
    }
    println("grade = $grade")
    // when を使って曜日番号(1..7)を曜日名に変換（下の weekdayName 関数を呼ぶ）
    for (day in 1..7) {
        println("day $day = ${weekdayName(day)}")
    }

    // --- 5. range と for ---
    // 1 から 10 までの偶数だけを出力。
    // until は終端を含まない(1..<11 と同じ)、step で刻み幅を指定。
    print("evens (until/step): ")
    for (i in 2 until 11 step 2) print("$i ")   // 2 4 6 8 10
    println()
    // ちなみに 1..10 は終端を含む。条件で絞るなら filter でも書ける:
    print("evens (filter): ")
    (1..10).filter { it % 2 == 0 }.forEach { print("$it ") }
    println()

    // --- 6. スマートキャスト ---
    val any: Any = "I am a string"
    if (any is String) {
        // ここでは any は String として扱える（キャスト不要）= スマートキャスト
        println("length = ${any.length}")
    }
}

// when を式として使い、引数に応じた値を返す関数。
// Dart の switch 文と違い「式」なので、結果をそのまま return できる。
// 1..7 以外（else）も必ず網羅する必要がある（式として使うと網羅性が要求される）。
fun weekdayName(day: Int): String = when (day) {
    1 -> "Mon"
    2 -> "Tue"
    3 -> "Wed"
    4 -> "Thu"
    5 -> "Fri"
    6 -> "Sat"
    7 -> "Sun"
    else -> "invalid"
}

// 振り返り: このファイルで「Dartと一番違う」と感じた点を docs/log.md に1行で書く
