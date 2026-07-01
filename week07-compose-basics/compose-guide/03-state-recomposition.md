# 03. 状態と Recomposition — remember / mutableStateOf

## 3-1. Recomposition とは

Flutter の `setState` → `build()` 再実行に相当するのが **recomposition**。
状態が変わると、その状態を読んでいる Composable 関数が**再実行**される。

```kotlin
@Composable
fun Counter() {
    var count by remember { mutableStateOf(0) }
    Button(onClick = { count++ }) {   // count を変更 → recomposition
        Text("Count: $count")         // count を読んでいるので再描画
    }
}
```

### Flutter との比較

```dart
// Flutter: 明示的に setState を呼ぶ
class _CounterState extends State<Counter> {
  int _count = 0;
  @override
  Widget build(BuildContext context) {
    return ElevatedButton(
      onPressed: () => setState(() => _count++),
      child: Text('Count: $_count'),
    );
  }
}
```

```kotlin
// Compose: 値を変えるだけ。setState は不要。
@Composable
fun Counter() {
    var count by remember { mutableStateOf(0) }
    Button(onClick = { count++ }) {
        Text("Count: $count")
    }
}
```

**違い**: Compose は `mutableStateOf` の値が変わると**自動で** recomposition する。
`setState` のような「再描画を明示的にリクエストする」ステップが要らない。

## 3-2. remember — recomposition をまたいで値を保持

`remember` がないと、recomposition のたびに変数が初期化されてしまう：

```kotlin
// ❌ NG: recomposition のたびに 0 にリセットされる
@Composable
fun BrokenCounter() {
    var count = mutableStateOf(0)  // remember なし → 毎回 0
    // ...
}

// ✅ OK: remember で recomposition をまたいで保持
@Composable
fun Counter() {
    var count by remember { mutableStateOf(0) }
    // ...
}
```

- `remember { }` は **最初の composition でだけ**ブロックを実行し、結果をキャッシュする。
- Recomposition 時はキャッシュから取り出すだけ。
- Flutter の `State` クラスのフィールドに値を持つのと同じ役割。

## 3-3. mutableStateOf — 「観察可能な値」

```kotlin
val count = mutableStateOf(0)      // MutableState<Int> を返す
count.value++                       // .value で読み書き

// by 委譲（Week 3 の by と同じ仕組み）で .value を省略：
var count by mutableStateOf(0)
count++                             // .value なしで直接アクセス
```

`mutableStateOf` は Compose ランタイムに「この値を読んでいる Composable」を登録し、
値が変わると該当する Composable を再実行する。これが recomposition の仕組み。

> Riverpod 経験者へ：`mutableStateOf` ≒ `StateProvider` や `Notifier` の `state`。
> 値を読むと購読が自動登録され、値の変更で再描画が走る。ref.watch と同じ感覚。

## 3-4. rememberSaveable — 画面回転でも生き残る

`remember` は **プロセス破棄（画面回転など）で失われる**。
`Bundle` に保存したいときは `rememberSaveable` を使う：

```kotlin
var count by rememberSaveable { mutableStateOf(0) }
```

- Flutter で言うと `RestorableInt` や `RestorationMixin` に相当。
- 基本型（Int, String, Boolean）は自動で保存される。
- カスタム型は `Saver` を書くか、ViewModel に持つ（→ Week 9）。
