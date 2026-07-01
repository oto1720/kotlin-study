# 04. State Hoisting — StatelessWidget/StatefulWidget の代わり

## 4-1. State Hoisting とは

Flutter には `StatelessWidget` と `StatefulWidget` という**明確な分類**がある。
Compose にはこの区別がない。代わりに **state hoisting（状態の引き上げ）** という設計パターンで
「状態をどこに置くか」を決める。

### 原則

- **子 Composable は状態を持たない**（引数で値とコールバックを受け取るだけ）
- **親 Composable が状態を持つ**（remember/ViewModel で管理）
- 子は「表示」と「イベントの通知」だけに責任を持つ

これは Flutter でも推奨される設計だが、Compose では**言語レベルで自然にこう書ける**。

## 4-2. Before（Hoisting なし）

```kotlin
// 状態と表示が一体化 — 再利用しにくい
@Composable
fun Counter() {
    var count by remember { mutableStateOf(0) }
    Column {
        Text("Count: $count")
        Button(onClick = { count++ }) { Text("+1") }
    }
}
```

## 4-3. After（Hoisting あり）

```kotlin
// 1. 子: 状態を持たない（Stateless）
@Composable
fun CounterDisplay(
    count: Int,              // 現在値
    onIncrement: () -> Unit, // イベント通知
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text("Count: $count")
        Button(onClick = onIncrement) { Text("+1") }
    }
}

// 2. 親: 状態を持つ（Stateful）
@Composable
fun CounterScreen() {
    var count by remember { mutableStateOf(0) }
    CounterDisplay(
        count = count,
        onIncrement = { count++ },
    )
}
```

### メリット

| メリット | 説明 |
|---|---|
| 再利用性 | `CounterDisplay` はどこでも使える（状態の管理方法に依存しない） |
| テスタビリティ | `CounterDisplay` は引数を渡すだけでテストできる |
| プレビュー | `@Preview` で任意の状態を簡単に確認できる |

### Flutter との対応

```
Flutter:                        Compose:
StatelessWidget                 引数だけ受け取る @Composable fun
  ├ final int count;            ├ count: Int（引数）
  └ final VoidCallback onTap;   └ onIncrement: () -> Unit（引数）

StatefulWidget + State          remember { mutableStateOf() } を持つ親
  └ State._count                └ var count by remember { ... }
```

## 4-4. 状態をどこまで引き上げるか

引き上げ先は「その状態を必要とする最も近い共通の祖先」：

```kotlin
@Composable
fun App() {
    // 複数の画面で共有する状態 → ここに置く（or ViewModel）
    var user by remember { mutableStateOf<User?>(null) }

    if (user == null) {
        LoginScreen(onLogin = { user = it })
    } else {
        HomeScreen(user = user!!)
    }
}
```

> Week 9 では、`remember` の代わりに **ViewModel** に状態を持たせる。
> ViewModel は画面回転やプロセス再生成をまたいで状態を保持できるため、
> 実アプリでは「画面レベルの状態は ViewModel」「UIの一時的な状態は remember」と使い分ける。
