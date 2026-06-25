/**
 * Week 3 演習 — クラス設計（data class / sealed class / enum / object / 委譲）
 * 実行: ./run.sh week03-classes/Classes.kt
 *
 * ねらい: Compose の「画面状態」を表現できる形でクラスを理解する。
 * 特に sealed class の UiState(Loading/Success/Error) は Week9 でそのまま再利用する。
 */

fun main() {
    section1DataClass()
    section2SealedClass()
    section3EnumAndObject()
    section4Delegation()
}

// =====================================================================
// 1. data class — Dart の手書き ==/copyWith/toString を自動生成
// =====================================================================
// equals/hashCode/toString/copy/分解宣言(componentN) が自動で付く。
data class User(val id: Int, val name: String, val age: Int)

fun section1DataClass() {
    println("== 1. data class ==")
    val u1 = User(1, "Oto", 28)
    println(u1)                                  // toString 自動: User(id=1, name=Oto, age=28)

    // copy: 一部だけ変えた新インスタンス（Dart freezed の copyWith 相当）
    val u2 = u1.copy(age = 29)
    println(u2)                                  // age だけ 29

    // 値で等価判定（Dart の手書き == 不要）
    println("u1 == u1.copy(): ${u1 == u1.copy()}")   // true

    // 分解宣言（destructuring）。componentN が自動生成されている
    val (id, name, age) = u2
    println("destructured -> id=$id, name=$name, age=$age")
    println()
}

// =====================================================================
// 2. sealed class — freezed の union / sealed に近い
// =====================================================================
// 「取りうる状態が有限」を型で表す。when で網羅すると else 不要になるのが強力。
// <out T> で共変にし、Success だけが値を持つ。
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

// when 式で網羅。全 sealed サブタイプを扱えば else を書かなくてよい（= 抜け漏れをコンパイラが検知）
fun render(state: UiState<String>): String = when (state) {
    is UiState.Loading -> "Loading..."
    is UiState.Success -> "OK: ${state.data}"   // ここで state は Success にスマートキャスト
    is UiState.Error -> "NG: ${state.message}"
}

fun section2SealedClass() {
    println("== 2. sealed class (UiState) ==")
    val states: List<UiState<String>> = listOf(
        UiState.Loading,
        UiState.Success("hello"),
        UiState.Error("network failed"),
    )
    states.forEach { println(render(it)) }
    println()
}

// =====================================================================
// 3. enum / object / companion object
// =====================================================================
// enum: 値と振る舞いを持てる。object: シングルトン(Dart にない)。companion: クラスの static 相当。
enum class Direction(val label: String) {
    NORTH("北"), SOUTH("南"), EAST("東"), WEST("西");

    fun opposite(): Direction = when (this) {
        NORTH -> SOUTH; SOUTH -> NORTH; EAST -> WEST; WEST -> EAST
    }
}

object AppConfig {                 // シングルトン: AppConfig.version でアクセス
    const val version = "1.0.0"
    fun describe() = "App v$version"
}

class Circle private constructor(val radius: Double) {
    companion object {             // ファクトリ等を置く場所（Java の static 相当）
        fun of(r: Double): Circle = Circle(r)
        const val PI = 3.14159
    }
    fun area(): Double = PI * radius * radius
}

fun section3EnumAndObject() {
    println("== 3. enum / object / companion ==")
    println("${Direction.NORTH.label} の逆は ${Direction.NORTH.opposite().label}")  // 北 の逆は 南
    println(AppConfig.describe())                       // App v1.0.0
    val c = Circle.of(2.0)                              // companion のファクトリ経由
    println("area = ${c.area()}")                       // 12.56636
    println()
}

// =====================================================================
// 4. 委譲 by — 実装を別オブジェクトに丸投げ
// =====================================================================
// インターフェース委譲: Logger の実装を委譲先に任せ、必要な所だけ override できる。
interface Logger {
    fun log(msg: String)
}
class ConsoleLogger : Logger {
    override fun log(msg: String) = println("[LOG] $msg")
}
// `by base` で Logger の実装を base に委譲。自前で書くのは追加分だけ。
class Service(base: Logger) : Logger by base {
    fun run() {
        log("service started")   // 委譲先 ConsoleLogger.log が呼ばれる
    }
}

fun section4Delegation() {
    println("== 4. delegation (by) ==")
    val service = Service(ConsoleLogger())
    service.run()                // [LOG] service started
    println()
}
