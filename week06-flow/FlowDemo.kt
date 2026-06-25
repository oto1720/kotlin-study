/**
 * Week 6 演習 — Flow
 * 実行: ./run.sh week06-flow/FlowDemo.kt
 *   （kotlinx-coroutines が必要。run.sh が libs/ を自動でクラスパスに入れる）
 *
 * ねらい: Riverpod 経験者の最大の武器。
 *   Dart Stream(cold) ≒ Kotlin Flow(cold)
 *   ★ StateFlow ≒ Riverpod の Notifier が持つ「状態」（Week9で本番投入）
 *   SharedFlow ≒ イベント用 Stream / collect ≒ listen
 */

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

fun main() = runBlocking {
    section1ColdFlow()
    section2Operators()
    section3StateFlow()
    section4SharedFlow()
}

// =====================================================================
// 1. flow{} は cold — collect されて初めて動く
// =====================================================================
// cold = 購読(collect)されるたびに最初から流れる。Dart の(broadcastでない)Stream と同じ。
fun numberFlow(): Flow<Int> = flow {
    for (i in 1..3) {
        delay(100)
        emit(i)        // Dart の yield / StreamController.add に相当
    }
}

suspend fun section1ColdFlow() {
    println("== 1. cold flow ==")
    println("（collect するまで何も起きない）")
    numberFlow().collect { println("collected: $it") }   // collect = Dart の listen/await for
    println()
}

// =====================================================================
// 2. 演算子 map / filter（Stream の変換と同じ感覚）
// =====================================================================
suspend fun section2Operators() {
    println("== 2. operators ==")
    val sum = numberFlow()
        .map { it * 10 }
        .filter { it >= 20 }
        .onEach { println("after ops: $it") }   // 副作用を挟む（also 的）
        .fold(0) { acc, v -> acc + v }           // 終端演算子で集計
    println("sum = $sum")                        // 20 + 30 = 50
    println()
}

// =====================================================================
// 3. StateFlow — 「現在値」を必ず持つ hot flow  ★Week9で再利用
// =====================================================================
// MutableStateFlow(初期値) を作り、.value で読み書き。最新値を常に保持する。
// = Riverpod の Notifier の state。Compose では collectAsStateWithLifecycle で購読する。
suspend fun section3StateFlow() = coroutineScope {
    println("== 3. StateFlow ==")
    val counter = MutableStateFlow(0)            // 初期値必須（常に現在値がある）
    println("initial value = ${counter.value}")

    // 別コルーチンで購読（最新値が流れてくる。distinct: 同じ値の連続は流れない）
    val job = launch {
        counter.take(3).collect { println("observed: $it") }
    }
    delay(50)
    counter.value = 1                            // 状態更新（Riverpod の state = 1 に相当）
    delay(50)
    counter.value = 2
    job.join()
    println()
}

// =====================================================================
// 4. SharedFlow — 一度きりのイベント用 hot flow
// =====================================================================
// 現在値を持たない。SnackBar 表示・画面遷移など「1回流したいイベント」に使う。
suspend fun section4SharedFlow() = coroutineScope {
    println("== 4. SharedFlow (events) ==")
    val events = MutableSharedFlow<String>()
    val job = launch {
        events.take(2).collect { println("event: $it") }
    }
    delay(50)                                     // 購読開始を待ってから emit（hotなので購読前のemitは届かない）
    events.emit("show snackbar")
    events.emit("navigate to detail")
    job.join()
    println()
    println("[cold/hot まとめ] flow{}=cold(購読毎に再生) / StateFlow,SharedFlow=hot(共有)")
}
