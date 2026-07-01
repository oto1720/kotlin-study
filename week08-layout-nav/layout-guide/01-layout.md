# 01. Column / Row / Box — 基本レイアウト

## 1-1. Column（縦並び）

Flutter の `Column` とほぼ同じ。子を縦に並べる。

```kotlin
Column(
    modifier = Modifier.fillMaxWidth().padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),      // 子同士の間隔
    horizontalAlignment = Alignment.CenterHorizontally,    // 横方向の揃え
) {
    Text("1行目")
    Text("2行目")
    Text("3行目")
}
```

### Flutter との比較

| Flutter | Compose |
|---|---|
| `mainAxisAlignment: MainAxisAlignment.center` | `verticalArrangement = Arrangement.Center` |
| `crossAxisAlignment: CrossAxisAlignment.start` | `horizontalAlignment = Alignment.Start` |
| `MainAxisSize.min` | `Modifier`（デフォルトで min。`fillMaxHeight` で max） |

## 1-2. Row（横並び）

```kotlin
Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
) {
    Text("左")
    Text("右")
}
```

## 1-3. weight — Flutter の Expanded/Flexible

```kotlin
Row(modifier = Modifier.fillMaxWidth()) {
    // Expanded(flex: 1) 相当
    Text("A", modifier = Modifier.weight(1f))
    // Expanded(flex: 2) 相当
    Text("B", modifier = Modifier.weight(2f))
    // weight なし = 固定幅（Flutter の wrap_content）
    Text("C")
}
```

`weight` は `RowScope` / `ColumnScope` 内でのみ使える（スコープ限定拡張関数）。

## 1-4. Box（重ねる）— Flutter の Stack

```kotlin
Box(
    modifier = Modifier.size(200.dp),
    contentAlignment = Alignment.Center,        // 全子要素のデフォルト配置
) {
    // 下から順に重なる（Flutter の Stack と同じ）
    Text("背面")
    Text(
        "前面（右下）",
        modifier = Modifier.align(Alignment.BottomEnd),   // 個別に配置
    )
}
```

Flutter の `Positioned` → Compose の `Modifier.align()` / `Modifier.offset()`。

## 1-5. Spacer — 余白

```kotlin
Column {
    Text("上")
    Spacer(modifier = Modifier.height(16.dp))   // SizedBox(height: 16) 相当
    Text("下")
}

Row {
    Text("左")
    Spacer(modifier = Modifier.weight(1f))      // Expanded() 相当（余白を埋める）
    Text("右")
}
```

## 1-6. Scaffold — 画面の土台

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyScreen() {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("タイトル") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { }) { Text("+") }
        },
    ) { innerPadding ->
        // ★ innerPadding を必ず content に適用する（AppBar 等との重なり防止）
        Column(modifier = Modifier.padding(innerPadding)) {
            Text("本文")
        }
    }
}
```

Flutter の `Scaffold` とほぼ同じ。`body` → `content`（trailing lambda）、
`innerPadding` を適用するのが Compose 特有の作法。
