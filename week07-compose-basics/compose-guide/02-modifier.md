# 02. Modifier — Flutter の Container/Padding/SizedBox を1チェーンで

## 2-1. Modifier とは

Flutter ではレイアウト調整に `Padding`、`SizedBox`、`Container`、`Align` など
複数のウィジェットをネストする。Compose ではこれを **`Modifier` のチェーン**で表す。

```kotlin
Text(
    text = "Hello",
    modifier = Modifier
        .fillMaxWidth()       // Flutter: SizedBox(width: double.infinity)
        .padding(16.dp)       // Flutter: Padding(padding: EdgeInsets.all(16))
        .background(Color.LightGray)  // Flutter: Container(color: ...)
)
```

Flutter で同じことを書くと：

```dart
Container(
  width: double.infinity,
  color: Colors.grey[300],
  padding: const EdgeInsets.all(16),
  child: const Text('Hello'),
)
```

## 2-2. チェーンの順序が重要

Modifier は**上から順に**適用される。順序を変えると見た目が変わる：

```kotlin
// パターン A: padding → background（余白の外に背景色）
Modifier
    .padding(16.dp)
    .background(Color.Red)

// パターン B: background → padding（背景色の内側に余白）
Modifier
    .background(Color.Red)
    .padding(16.dp)
```

Flutter の `Container` は padding/color/margin を全部引数で受けるので順序を意識しにくいが、
Compose では**明示的にチェーンの順序で制御**する。最初は戸惑うが、慣れると自由度が高い。

## 2-3. よく使う Modifier

| Modifier | Flutter 相当 | 説明 |
|---|---|---|
| `.padding(16.dp)` | `Padding` / `EdgeInsets` | 余白 |
| `.fillMaxWidth()` | `double.infinity` | 横幅いっぱい |
| `.fillMaxSize()` | `SizedBox.expand()` | 全画面 |
| `.size(100.dp)` | `SizedBox(width: 100, height: 100)` | 固定サイズ |
| `.weight(1f)` | `Expanded(flex: 1)` | Row/Column 内での比率（★） |
| `.background(color)` | `Container(color: ...)` | 背景色 |
| `.clickable { }` | `GestureDetector(onTap: ...)` | タップ |
| `.border(...)` | `Container(decoration: ...)` | 枠線 |
| `.clip(RoundedCornerShape(8.dp))` | `ClipRRect` | 角丸 |

### ★ weight は Row/Column スコープ限定

```kotlin
Row {
    Text("A", modifier = Modifier.weight(1f))  // Expanded(flex: 1)
    Text("B", modifier = Modifier.weight(2f))  // Expanded(flex: 2)
}
```

`weight` は `RowScope`/`ColumnScope` のスコープ内でしか使えない。
これは Kotlin のレシーバスコープ（Week 2 のスコープ関数と同じ仕組み）で制御されている。

## 2-4. Modifier 引数のデフォルトパターン

再利用可能な Composable を作るときは、`modifier` 引数を受け取るのがベストプラクティス：

```kotlin
@Composable
fun UserCard(
    name: String,
    modifier: Modifier = Modifier,   // デフォルトは空の Modifier
) {
    Card(modifier = modifier) {       // 呼び出し側が外側の Modifier を差し込める
        Text(name, modifier = Modifier.padding(16.dp))
    }
}

// 使う側
UserCard("Oto", modifier = Modifier.fillMaxWidth().padding(8.dp))
```

Flutter で言うと、Widget のコンストラクタに `Key? key` を受け取るのと同じ「お作法」。
