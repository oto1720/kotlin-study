# 03. StateFlow — 現在値を持つ hot flow ★Week9で本番投入

Week 6 で最も重要。Riverpod の `Notifier` が持つ **state** に対応する。

## 3-1. 基本：現在値を常に持つ

```kotlin
val counter = MutableStateFlow(0)        // 初期値必須（常に「現在値」がある）
println(counter.value)                   // 0 ← .value でいつでも今の値を読める

counter.value = 1                        // 状態を更新（Riverpod の state = 1 に相当）
println(counter.value)                   // 1
```

- `MutableStateFlow(初期値)` で作る。**初期値が必須**＝「値が無い瞬間が存在しない」。
- `.value` で**いつでも同期的に**現在値を読める／書ける。これが cold flow との最大の違い。
- Riverpod の `state` プロパティと完全に同じ感覚。

## 3-2. 購読すると「最新値 → 以降の変化」が流れる

```kotlin
suspend fun demo() = coroutineScope {
    val counter = MutableStateFlow(0)

    val job = launch {
        counter.take(3).collect { println("observed: $it") }
        // 購読開始時の現在値 0 がまず流れ、その後の変化が流れる
    }
    delay(50)
    counter.value = 1     // observed: 1
    delay(50)
    counter.value = 2     // observed: 2
    job.join()
}
// observed: 0  ← 購読した瞬間に現在値が届く
// observed: 1
// observed: 2
```

- 購読を始めると、**まず現在値**が届き、その後は値が変わるたびに流れる。
- 途中から購読しても「今の状態」が分かる。これが画面の状態管理に最適な理由。

## 3-3. StateFlow の重要な性質

1. **hot**：collect の有無に関わらず存在し、値を保持し続ける。
2. **現在値を持つ**：`.value` で同期取得できる。初期値必須。
3. **distinctUntilChanged 内蔵**：`value` に**同じ値**を入れても購読者には流れない
   （変化があったときだけ通知）。無駄な再描画を防ぐ。
4. **conflated（最新優先）**：購読者が遅いと中間値は飛ばされ、最新値だけ届く。

```kotlin
val s = MutableStateFlow(0)
s.value = 1
s.value = 1      // 同じ値なので購読者には流れない（distinct）
```

## 3-4. 公開は読み取り専用にする（カプセル化の定番）

```kotlin
class CounterViewModel {
    private val _state = MutableStateFlow(0)        // 書けるのは内部だけ
    val state: StateFlow<Int> = _state.asStateFlow() // 外には読み取り専用で公開

    fun increment() { _state.value++ }              // 更新は公開メソッド経由
}
```

- `_state`（Mutable・private）と `state`（読み取り専用・public）の**2本立て**が定番。
- 外部から勝手に `value` を書き換えられないようにする。Riverpod の Notifier と同じ設計。
- `_` プレフィックスは「内部用」を表す慣習（Dart の `_` と同じ気分だが、Kotlin では
  あくまで命名慣習で、可視性は `private` キーワードで担保する）。

## 3-5. Compose との接続（Week 9 の予告）

```kotlin
// Compose 画面側（Week 9 で本番）
@Composable
fun CounterScreen(vm: CounterViewModel) {
    val count by vm.state.collectAsStateWithLifecycle()  // ref.watch 相当
    Text("$count")
    Button(onClick = { vm.increment() }) { Text("+1") }
}
```

| Riverpod / Flutter | Compose / Kotlin |
|---|---|
| `Notifier` の `state` | `StateFlow`（ViewModel が公開） |
| `state = newValue` | `_state.value = newValue` |
| `ref.watch(provider)` | `collectAsStateWithLifecycle()` |

> Week 3 の `sealed class UiState`（Loading/Success/Error）を `StateFlow<UiState<T>>` として
> 公開すれば、「現在の画面状態」を型安全に流せる。これが Week 9 の完成形。
</content>
