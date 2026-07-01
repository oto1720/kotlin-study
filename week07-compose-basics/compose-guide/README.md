# Jetpack Compose 基礎ガイド（Flutter/Dart経験者向け）

Week 7「Compose基礎」の読み物。`ComposeBasis.kt` の演習の背景を、
Flutter/Dart のウィジェットモデルと対比しながら解説。

## 目次

- **00. 全体像**（このファイル）— Flutter → Compose の頭の切り替え
- [01. @Composable 関数](01-composable.md) — Widget が「関数」になる
- [02. Modifier](02-modifier.md) — Flutter の Container/Padding/SizedBox を1チェーンで
- [03. 状態と Recomposition](03-state-recomposition.md) — remember/mutableStateOf/setState の対応
- [04. State Hoisting](04-state-hoisting.md) — StatelessWidget/StatefulWidget の代わり

---

## 00. 全体像：Flutter → Compose の頭の切り替え

### Flutter / Dart との対応

| Flutter | Compose | メモ |
|---|---|---|
| `Widget` クラス | `@Composable fun` | クラスではなく**関数** |
| `build()` メソッド | Composable 関数の本体 | 呼ばれるたびにUIツリーを「宣言」 |
| `StatelessWidget` | 引数だけの `@Composable fun` | 状態なし→引数だけ受け取る |
| `StatefulWidget` + `State` | `remember { mutableStateOf() }` | 状態を関数内で保持 |
| `setState(() { ... })` | `mutableStateOf` の値を変える | **自動で**再描画（setState呼び出し不要） |
| `@override Widget build()` | Recomposition | 状態が変わると関数が再実行される |
| `Key` | `key()` | リスト内の同一性を保つ |
| `widget.xxx`（親から受けた値） | 関数の引数 | state hoisting で渡す |

### 一番大事な違い

**Compose の UI は「クラス」ではなく「関数」。**

Flutter では `class MyWidget extends StatelessWidget` と書くが、
Compose では `@Composable fun MyWidget()` と書く。クラスの継承階層を作らない。

この結果:
- ネストが浅くなる（クラス定義 + build メソッド → 関数1個）
- State はクラスのフィールドではなく、関数内の `remember` で保持
- ロジックの再利用は継承ではなく、関数の呼び出しや composition

### Recomposition = Flutter の build() 再実行

Flutter の `setState` は「build を再実行してUIを更新する」。
Compose の recomposition もまったく同じで、**状態が変わった Composable 関数だけ**が再実行される。

違いは:
- Flutter: `setState(() { _count++; })` と明示的に呼ぶ
- Compose: `count++` と書くだけ（mutableStateOf の値変更を Compose ランタイムが検知して自動で recomposition）

> Flutter 経験者へ：「build() が再実行される」という感覚はそのまま使える。
> ただし Compose は**関数レベルで部分的に**再実行できる（スキップ可能）ので、
> Flutter より細粒度で効率的な更新ができる。
