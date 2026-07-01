/**
 * Week 7 演習 — Compose基礎
 * ※ このファイルは Android Studio の Compose プロジェクト内で使う。run.sh では実行不可。
 *
 * ねらい: 宣言的UIの対応関係を掴む。
 *   Widget → @Composable fun
 *   build() → Composable 関数本体
 *   setState → remember { mutableStateOf() } + recomposition
 *   StatelessWidget/StatefulWidget → state hoisting という設計判断に置き換わる
 *
 * 成果物: カウンター + 簡単なフォーム画面（状態を親に持ち上げる練習）
 */

package com.example.week07

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// =====================================================================
// 1. 最小の Composable — Flutter の StatelessWidget に相当
// =====================================================================
// @Composable を付けた関数が UI の構成要素。Flutter の Widget クラスではなく「関数」。
// Dart: class Greeting extends StatelessWidget { Widget build(ctx) => Text("Hello"); }
// Kotlin: @Composable fun Greeting() { Text("Hello") }

@Composable
fun Greeting(name: String) {
    // Text = Flutter の Text ウィジェットに相当。Modifier で装飾する。
    Text(
        text = "Hello, $name!",
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.padding(16.dp),
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Greeting("Oto")
}

// =====================================================================
// 2. remember + mutableStateOf — Flutter の setState に相当
// =====================================================================
// remember: recomposition をまたいで値を保持（Flutter の State フィールドに相当）
// mutableStateOf: 値が変わると recomposition をトリガー（setState 相当）
//
// Flutter:
//   int _count = 0;
//   void _increment() => setState(() => _count++);
//
// Compose:
//   var count by remember { mutableStateOf(0) }
//   count++  ← これだけで recomposition が走る（setState 不要）

@Composable
fun Counter() {
    // by 委譲で .value の読み書きを省略。内部で count を読む Composable が自動で再描画される。
    var count by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Count: $count",
            style = MaterialTheme.typography.displaySmall,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { count++ }) {
                Text("+1")
            }
            OutlinedButton(onClick = { count = 0 }) {
                Text("Reset")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CounterPreview() {
    Counter()
}

// =====================================================================
// 3. State Hoisting — Flutter の「親が状態を持つ」パターン
// =====================================================================
// Compose では StatelessWidget/StatefulWidget の区別がない。代わりに
// 「状態をどこに置くか」を設計判断として選ぶ。これが state hoisting。
//
// Flutter:
//   親 Widget が state を持ち、子 Widget にコールバックを渡す
//   → Compose でもまったく同じ。状態を「引き上げ(hoist)」て、子は値+コールバックだけ受け取る。

// Stateless な子: 状態を持たず、値とイベントだけ受け取る（Flutter の StatelessWidget 相当）
@Composable
fun CounterDisplay(
    count: Int,
    onIncrement: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Count: $count", style = MaterialTheme.typography.displaySmall)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onIncrement) { Text("+1") }
            OutlinedButton(onClick = onReset) { Text("Reset") }
        }
    }
}

// 親が状態を持つ（hoisted state）
@Composable
fun HoistedCounterScreen() {
    var count by remember { mutableStateOf(0) }
    // 子に「現在値」と「変更関数」を渡す。Riverpod の ref.watch + ref.read に近い構図。
    CounterDisplay(
        count = count,
        onIncrement = { count++ },
        onReset = { count = 0 },
    )
}

// =====================================================================
// 4. 簡単なフォーム画面 — テキスト入力 + state hoisting
// =====================================================================
// Flutter の TextEditingController → Compose では remember { mutableStateOf("") }
// onChanged → onValueChange コールバック

@Composable
fun NameInput(
    name: String,
    onNameChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        label = { Text("名前を入力") },
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
fun FormScreen() {
    var name by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("ユーザー登録", style = MaterialTheme.typography.headlineSmall)
        // NameInput は状態を持たない（hoisted）
        NameInput(name = name, onNameChange = { name = it })
        Button(
            onClick = { submitted = true },
            enabled = name.isNotBlank(),
        ) {
            Text("送信")
        }
        if (submitted) {
            Text(
                text = "こんにちは、${name}さん！",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FormScreenPreview() {
    FormScreen()
}

// =====================================================================
// 5. Modifier チェーン — Flutter の Container/Padding/SizedBox を1つに集約
// =====================================================================
// Flutter では Padding(child: Container(color: ...)) のようにネストが深くなるが、
// Compose では Modifier のチェーンで横に書ける。適用順は「外→内」（上から順に評価）。

@Composable
fun ModifierDemo() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)            // 外側の余白（Flutter の Padding）
            .padding(8.dp),            // さらに内側の余白
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Modifier チェーンで装飾",
            modifier = Modifier.padding(4.dp),
        )
    }
}

// =====================================================================
// 6. メイン画面（すべてを組み合わせ）
// =====================================================================
@Composable
fun Week07Screen() {
    Column(modifier = Modifier.fillMaxSize()) {
        Greeting("Compose World")
        HorizontalDivider()
        HoistedCounterScreen()
        HorizontalDivider()
        FormScreen()
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun Week07ScreenPreview() {
    MaterialTheme {
        Week07Screen()
    }
}
