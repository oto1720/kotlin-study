# 04. SharedFlow — 一度きりのイベント用 hot flow

## 4-1. StateFlow との違い：現在値を持たない

```kotlin
val events = MutableSharedFlow<String>()   // 初期値なし／現在値を持たない
events.emit("show snackbar")               // emit で流す（suspend）
```

- `StateFlow` が「**状態**（常に現在値がある）」なのに対し、
  `SharedFlow` は「**イベント**（流れた瞬間だけ意味がある）」。
- 現在値が無いので `.value` は無い。`emit()` で流し、`collect` で受ける。

## 4-2. なぜイベントに使うのか

SnackBar 表示・画面遷移・トースト・「保存しました」通知などは、
**状態ではなく一度きりの出来事**。これを `StateFlow` で扱うと不都合が出る：

- `StateFlow` は現在値を持つので、画面回転などで再購読すると
  「最後のイベント」をもう一度受け取ってしまう（SnackBar が二重表示）。
- `SharedFlow`（デフォルト設定）は現在値を保持しないので、
  購読開始**後**に emit されたものだけ届く＝再実行されない。

| | StateFlow | SharedFlow |
|---|---|---|
| 表すもの | 状態（current state） | イベント（one-shot event） |
| 現在値 | 持つ（初期値必須） | 持たない（デフォルト） |
| 用途 | カウント・UI state・ローディング | SnackBar・画面遷移・トースト |
| 再購読時 | 最新値が再度届く | 何も届かない（過去のイベントは消えている） |

## 4-3. hot ゆえの注意：購読前の emit は届かない

```kotlin
suspend fun demo() = coroutineScope {
    val events = MutableSharedFlow<String>()
    val job = launch {
        events.take(2).collect { println("event: $it") }
    }
    delay(50)                      // ★ 購読が始まるのを待ってから emit する
    events.emit("show snackbar")
    events.emit("navigate to detail")
    job.join()
}
```

- `SharedFlow` は hot なので、**購読が始まる前に emit した値は誰にも届かず消える**。
- 上の `delay(50)` は「`launch` した購読コルーチンが `collect` を始めるのを待つ」ため。
- これは「流れている川」モデルの自然な帰結。StateFlow なら現在値を保持するので
  この問題は起きない（後から購読しても現在値が届く）。

## 4-4. replay / buffer の調整（発展）

```kotlin
val events = MutableSharedFlow<String>(
    replay = 1,                    // 新規購読者に直近1件を再送する
    extraBufferCapacity = 8,       // emit がブロックしないようバッファを持つ
)
```

- `replay = 0`（デフォルト）が純粋なイベント用。`replay >= 1` にすると
  「直近 n 件を後から来た購読者にも配る」挙動になり、StateFlow に近づく。
- イベント用途では基本 `replay = 0`。

## 4-5. cold / hot のまとめ

```
flow{}      = cold（購読のたびに最初から再生／現在値なし）
StateFlow   = hot ＋ 現在値あり（状態の表現／distinct 内蔵）
SharedFlow  = hot ＋ 現在値なし（一度きりのイベント）
```

| Flutter / Riverpod | Kotlin |
|---|---|
| 非 broadcast Stream | `flow { }`（cold） |
| Notifier の state | `StateFlow` |
| イベント用 Stream | `SharedFlow` |

> 設計指針：**「画面に表示し続ける値」は StateFlow、「一回だけ起こす副作用」は SharedFlow**。
> この使い分けが Week 9〜11 のアプリ実装でそのまま判断軸になる。
</content>
