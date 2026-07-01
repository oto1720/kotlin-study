# 03. Material3 テーマ — ThemeData → MaterialTheme

## 3-1. テーマの構造

Flutter の `ThemeData` → Compose の `MaterialTheme`。構成要素は3つ：

| Flutter | Compose | 役割 |
|---|---|---|
| `ColorScheme` | `ColorScheme` | 色 |
| `TextTheme` | `Typography` | 文字スタイル |
| `ShapeBorder` | `Shapes` | 角丸等 |

```kotlin
@Composable
fun MyApp() {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF6200EE),
            secondary = Color(0xFF03DAC6),
        ),
        typography = Typography(
            headlineMedium = TextStyle(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            ),
        ),
    ) {
        // この中の Composable はテーマを参照できる
        AppContent()
    }
}
```

## 3-2. テーマの参照

```kotlin
@Composable
fun ThemedText() {
    Text(
        text = "テーマの色を使う",
        // Flutter: Theme.of(context).colorScheme.primary
        // Compose: MaterialTheme.colorScheme.primary
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.bodyLarge,
    )
}
```

Flutter の `Theme.of(context)` → Compose の `MaterialTheme`（CompositionLocal で暗黙に渡る）。

## 3-3. Dynamic Color（Android 12+）

```kotlin
@Composable
fun MyApp() {
    val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        dynamicLightColorScheme(LocalContext.current)
    } else {
        lightColorScheme()
    }
    MaterialTheme(colorScheme = colorScheme) {
        AppContent()
    }
}
```

壁紙から色を自動生成する Android 12+ の機能。Flutter の `dynamic_color` パッケージ相当。

## 3-4. ダークモード対応

```kotlin
@Composable
fun MyApp() {
    val isDark = isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = if (isDark) darkColorScheme() else lightColorScheme(),
    ) {
        AppContent()
    }
}
```

Flutter の `ThemeData.dark()` / `ThemeMode.system` に相当。

## 3-5. Material3 のよく使うコンポーネント

| Flutter | Compose | 備考 |
|---|---|---|
| `ElevatedButton` | `Button` | デフォルトが Filled |
| `OutlinedButton` | `OutlinedButton` | |
| `TextButton` | `TextButton` | |
| `FloatingActionButton` | `FloatingActionButton` | |
| `Card` | `Card` | |
| `TextField` / `TextFormField` | `TextField` / `OutlinedTextField` | |
| `SnackBar` | `Snackbar` + `SnackbarHost` | |
| `BottomSheet` | `ModalBottomSheet` | |
| `AlertDialog` | `AlertDialog` | |
| `CircularProgressIndicator` | `CircularProgressIndicator` | |
| `Switch` / `Checkbox` | `Switch` / `Checkbox` | |
