/**
 * Week 5 演習 — コルーチン基礎
 * 実行: ./run.sh week05-coroutines/Coroutines.kt
 *   （kotlinx-coroutines が必要。libs/ に jar があり run.sh が自動でクラスパスに入れる）
 *
 * ねらい: 非同期を Dart の知識に接続する。
 *   Future/async/await → suspend/launch/async/await
 *   Isolate → Dispatchers.Default / IO でのスレッド切替
 *   ★ structured concurrency（構造化並行性）は Dart にない重要概念
 */

import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

// suspend fun: 中断可能な関数。Dart の「Future を返す async 関数」に相当。
// delay は Thread.sleep と違いスレッドをブロックしない（中断するだけ）。
suspend fun fetchUser(): String {
    delay(300)            // 擬似的なネットワーク待ち
    return "User(Oto)"
}

suspend fun fetchPosts(): List<String> {
    delay(500)
    return listOf("post1", "post2")
}

// runBlocking: main をコルーチンの世界に橋渡し（テストや main で使う。実アプリでは使わない）
fun main() = runBlocking {
    section1LaunchVsAsync()
    section2Parallel()
    section3Dispatchers()
    section4ErrorHandling()
    section5StructuredConcurrency()
}

// =====================================================================
// 1. launch（撃ちっぱなし） vs async（結果を待つ）
// =====================================================================
suspend fun section1LaunchVsAsync() = coroutineScope {
    println("== 1. launch vs async ==")
    // launch: Job を返す。戻り値は要らない「発火」用。Dart に明確な対応なし。
    val job = launch {
        delay(100)
        println("launch: 完了（戻り値なし）")
    }
    job.join()  // 終わるまで待つ

    // async: Deferred<T> を返す。.await() で結果を取る（Dart の Future + await に近い）
    val deferred = async { fetchUser() }
    println("async: ${deferred.await()}")
    println()
}

// =====================================================================
// 2. 2つのAPIを並列取得して合成（成果物のお題）
// =====================================================================
suspend fun section2Parallel() = coroutineScope {
    println("== 2. parallel fetch & combine ==")
    val elapsed = measureTimeMillis {
        // 2つを async で同時に開始 → await で合流。直列(300+500)ではなく並列(~500)になる。
        val user = async { fetchUser() }
        val posts = async { fetchPosts() }
        println("combined: ${user.await()} / posts=${posts.await()}")
    }
    println("elapsed = ${elapsed}ms (直列なら~800ms、並列なので~500ms)")
    println()
}

// =====================================================================
// 3. Dispatchers と withContext（スレッド切替）
// =====================================================================
suspend fun section3Dispatchers() = coroutineScope {
    println("== 3. dispatchers / withContext ==")
    // withContext: そのブロックだけ別ディスパッチャで実行して結果を返す。
    // IO=ネットワーク/ファイル, Default=CPU負荷の高い計算。Dart の Isolate 的な使い分け。
    val onIo = withContext(Dispatchers.Default) {
        (1..1_000_000).sum()    // 重めの計算は Default で
    }
    println("computed on Default = $onIo")
    println()
}

// =====================================================================
// 4. 例外と try/catch
// =====================================================================
// ★落とし穴: 通常の coroutineScope 内で async が例外を投げると、await で catch しても
//   例外は「親スコープ」にも伝播して全体がクラッシュする。
//   await だけで握りたいときは supervisorScope を使い、子の失敗を親に伝播させない。
suspend fun section4ErrorHandling() = supervisorScope {
    println("== 4. error handling ==")
    val result = try {
        async {
            delay(50)
            throw IllegalStateException("API error")
        }.await()
    } catch (e: IllegalStateException) {
        "caught: ${e.message}"   // supervisorScope なので await の再スローを catch すれば完結
    }
    println(result)
    println()
}

// =====================================================================
// 5. 構造化並行性（Dart にない概念）
// =====================================================================
// coroutineScope は「中の子コルーチンが全部終わるまで戻らない」。
// 1つでも失敗すると兄弟もキャンセルされる（= リーク防止）。これが structured concurrency。
suspend fun section5StructuredConcurrency() {
    println("== 5. structured concurrency ==")
    coroutineScope {
        repeat(3) { i ->
            launch {
                delay((i + 1) * 100L)
                println("child $i done")
            }
        }
        // ここで全 child の完了を自動で待つ。明示 join 不要。
    }
    println("all children finished (scope returned)")
    println()
}
