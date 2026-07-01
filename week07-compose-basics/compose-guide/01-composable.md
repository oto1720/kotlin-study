# 01. @Composable 関数 — Widget が「関数」になる

## 1-1. 最小の Composable

```kotlin
@Composable
fun Greeting(name: String) {
    Text("Hello, $name!")
}
```

Flutter で書くと：

```dart
class Greeting extends StatelessWidget {
  final String name;
  const Greeting({required this.name});
  @override
  Widget build(BuildContext context) => Text('Hello, $name!');
}
```

- Compose: **関数1個**。`@Composable` アノテーションを付けるだけ。
- Flutter: クラス定義 + コンストラクタ + build メソッドの3点セット。

### 命名規則

- Composable 関数は**大文字始まり**（PascalCase）。`Greeting()`、`UserCard()`。
- Flutter の Widget クラスと同じ命名規則なので、移行しやすい。
- 戻り値は `Unit`（何も返さない）。UI は「戻り値」ではなく「関数内で呼び出す」ことで構築する。

## 1-2. Composable の中で Composable を呼ぶ

```kotlin
@Composable
fun UserCard(name: String, age: Int) {
    Column {
        Text(name)
        Text("$age 歳")
    }
}
```

Flutter の `Column(children: [Text(name), Text('$age 歳')])` に相当。
Compose では `children` 引数ではなく、`Column { }` のブロック内に**直接呼び出す**。

これは Kotlin の **trailing lambda**（Week 2 で学んだ）が活きる場面：
`Column(content = { Text("a"); Text("b") })` → `Column { Text("a"); Text("b") }`

## 1-3. @Preview — ホットリロード的な確認

```kotlin
@Preview(showBackground = true)
@Composable
fun UserCardPreview() {
    UserCard(name = "Oto", age = 28)
}
```

- Android Studio のプレビューペインに表示される（Flutter の Hot Reload に近い体験）。
- `@Preview` は**引数なしの Composable** に付ける（引数があるとプレビューできない）。
- `showBackground = true` で白背景を付ける。`showSystemUi = true` でステータスバー付き。

## 1-4. Composable 関数の制約

Composable 関数の中では普通の Kotlin コードが書けるが、いくつか制約がある：

```kotlin
@Composable
fun Example() {
    // ✅ OK: 条件分岐
    if (condition) { Text("A") } else { Text("B") }

    // ✅ OK: ループ
    for (item in items) { Text(item) }

    // ❌ NG: 副作用を直接実行（recomposition のたびに再実行されてしまう）
    // println("recompose!")    // ← ログが何度も出る
    // fetchData()              // ← API を何度も叩く

    // ✅ 副作用は LaunchedEffect 等で制御する（→ Week 9）
}
```

> ポイント：Composable 関数は**何度でも再実行される可能性がある**（recomposition）。
> だから「副作用のないピュアな関数」として書くのが基本。
> Flutter の build() メソッドで副作用を書かないのと同じ原則。
