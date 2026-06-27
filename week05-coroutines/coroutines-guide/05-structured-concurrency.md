# 05. 構造化並行性（Structured Concurrency）— Dart にない最重要概念

Week 5 で最も大事な概念。Dart にはこれに相当する仕組みがなく、Kotlin コルーチンの
安全性の核になっている。

## 5-1. 一言でいうと

> **「親スコープは、中の子コルーチンが全部終わるまで戻らない」**
> **「1つでも失敗すると兄弟もキャンセルされる」**

これにより「起動したコルーチンの解放を忘れてリークする」「親が死んだのに子が生き残る」
といった事故が**構造的に防がれる**。

```kotlin
suspend fun loadAll() {
    coroutineScope {
        repeat(3) { i ->
            launch {
                delay((i + 1) * 100L)
                println("child $i done")
            }
        }
        // ← ここで3つの child 全部の完了を「自動で」待つ。明示 join 不要。
    }
    println("all children finished (scope returned)")
}
```

`coroutineScope { }` のブロックは、中で `launch` した3つが全部終わるまで**抜けない**。
Dart で複数の `Future` を起動したら自分で `Future.wait` しないと待てないのと対照的に、
Kotlin はスコープが面倒を見てくれる。

## 5-2. なぜ重要か（3つのメリット）

1. **リークしない**：スコープを抜ける ＝ 中の全コルーチンが完了 or キャンセル済み。
   起動しっぱなしで宙に浮くコルーチンが原理的に生まれない。
2. **キャンセルが伝播する**：親をキャンセルすると、子も孫も芋づる式にキャンセルされる。
   Android で画面を閉じたら（`viewModelScope` が死ぬと）走っていた通信も自動で止まる。
3. **エラーが伝播する**：子が1つ失敗すると兄弟もキャンセルされ、例外が親に伝わる
   （「全部成功 or 全部やめる」。一部失敗を許したいなら `supervisorScope` → 04章）。

## 5-3. スコープの種類

| スコープ/ビルダー | 性質 | 使う場所 |
|---|---|---|
| `coroutineScope { }` | 子の完了を待つ。子の失敗で全キャンセル | suspend 関数内で複数を束ねる |
| `supervisorScope { }` | 子の失敗を親に伝播させない | 一部失敗を許す処理 |
| `runBlocking { }` | スレッドをブロックして待つ | main / テスト |
| `viewModelScope`（Android） | ViewModel の生存期間に紐づく | Week 9 の本番 |
| `lifecycleScope`（Android） | 画面の生存期間に紐づく | UI層 |
| `GlobalScope` | **どこにも紐づかない（非推奨）** | 基本使わない |

## 5-4. アンチパターン：GlobalScope

```kotlin
// ❌ GlobalScope はアプリ全体の寿命。親子関係から外れてリーク・キャンセル不能の温床
GlobalScope.launch { ... }

// ✅ 適切なスコープに紐づける
viewModelScope.launch { ... }    // 画面が消えたら自動キャンセル
coroutineScope { launch { ... } } // 親に紐づく
```

`GlobalScope` は構造化並行性の輪の外に出てしまうため、
「キャンセルされない」「リークする」原因になる。学習用以外では避ける。

## 5-5. Dart との対比でまとめ

| | Dart | Kotlin（構造化並行性） |
|---|---|---|
| 複数の非同期の待ち合わせ | 自分で `Future.wait` | スコープが自動で待つ |
| 親をやめたら子は | 個別にキャンセル管理が必要 | 自動で芋づるキャンセル |
| 起動しっぱなしのリスク | `unawaited` で容易にリーク | スコープが防ぐ |
| 失敗の伝播 | 自分で集約 | 親に伝播（or supervisor で遮断） |

> この「スコープが子の面倒を全部見る」発想に慣れると、非同期コードが圧倒的に安全になる。
> Week 9 で `viewModelScope` を使うとき、この章の理解がそのまま効いてくる。
</content>
