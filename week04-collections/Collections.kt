/**
 * Week 4 演習 — コレクション操作
 * 実行: ./run.sh week04-collections/Collections.kt
 *
 * ねらい: map/filter/fold 系を手足のように。Dart の Iterable とほぼ同じ思想だが、
 * groupBy/associateBy/partition/sumOf など Kotlin の方が便利な関数を押さえる。
 * 遅延評価は Dart の Iterable ≒ Kotlin の Sequence。
 */

fun main() {
    section1Basics()
    section2GroupingAndAssociate()
    section3Sequence()
    section4Aggregation()
}

data class Order(val id: Int, val user: String, val amount: Int, val category: String)

val orders = listOf(
    Order(1, "Oto", 1200, "book"),
    Order(2, "Oto", 800, "food"),
    Order(3, "Aya", 3000, "book"),
    Order(4, "Aya", 500, "food"),
    Order(5, "Ken", 2000, "gadget"),
)

// =====================================================================
// 1. map / filter / flatMap / fold / reduce
// =====================================================================
fun section1Basics() {
    println("== 1. map / filter / fold ==")
    val nums = listOf(1, 2, 3, 4, 5)
    println(nums.map { it * it })             // [1, 4, 9, 16, 25]
    println(nums.filter { it % 2 == 1 })      // [1, 3, 5]
    // flatMap: 各要素をリストに展開して平坦化（Dart の expand）
    println(nums.flatMap { listOf(it, -it) }) // [1, -1, 2, -2, ...]
    // fold: 初期値ありの畳み込み（Dart の fold）
    println(nums.fold(0) { acc, v -> acc + v })   // 15
    // reduce: 初期値なし（空リストで例外なので注意）
    println(nums.reduce { acc, v -> acc * v })    // 120
    println()
}

// =====================================================================
// 2. groupBy / associateBy / partition（Kotlin の便利系）
// =====================================================================
fun section2GroupingAndAssociate() {
    println("== 2. groupBy / associateBy / partition ==")
    // groupBy: キーごとに List にまとめる → Map<String, List<Order>>
    val byUser = orders.groupBy { it.user }
    byUser.forEach { (user, list) -> println("$user: ${list.size}件") }

    // associateBy: キー → 要素 の Map（重複キーは後勝ち）
    val byId = orders.associateBy { it.id }
    println("id=3 -> ${byId[3]}")

    // partition: 条件で2グループに分割（Dart にはない）
    val (big, small) = orders.partition { it.amount >= 1000 }
    println("big(>=1000)=${big.size}, small=${small.size}")
    println()
}

// =====================================================================
// 3. Sequence（遅延評価）— Dart の Iterable に相当
// =====================================================================
// List は各操作で中間リストを作る。Sequence は要素ごとに end まで流すので大きいデータで有利。
fun section3Sequence() {
    println("== 3. Sequence (lazy) ==")
    val result = (1..1_000_000).asSequence()
        .map { it * 2 }
        .filter { it % 3 == 0 }
        .take(5)            // 5個取れたら以降は評価しない
        .toList()
    println(result)         // [6, 12, 18, 24, 30]
    println()
}

// =====================================================================
// 4. 集計のお題（JSONっぽいリストを集計）
// =====================================================================
fun section4Aggregation() {
    println("== 4. aggregation ==")
    // sumOf: 数値プロパティの合計（Dart は fold で書きがち→Kotlin は一発）
    val total = orders.sumOf { it.amount }
    println("total amount = $total")                    // 7500

    // カテゴリごとの売上合計
    val byCategory = orders
        .groupBy { it.category }
        .mapValues { (_, list) -> list.sumOf { it.amount } }
    println("by category = $byCategory")                // {book=4200, food=1300, gadget=2000}

    // 最高額の注文（maxByOrNull は空なら null）
    val top = orders.maxByOrNull { it.amount }
    println("top order = $top")

    // 平均（averageを使う）
    println("avg = ${orders.map { it.amount }.average()}")  // 1500.0

    // ユーザーごとの利用額ランキング
    val ranking = orders
        .groupBy { it.user }
        .map { (user, list) -> user to list.sumOf { it.amount } }
        .sortedByDescending { it.second }
    println("ranking = $ranking")                       // [(Aya, 3500), (Oto, 2000), (Ken, 2000)]
    println()
}
