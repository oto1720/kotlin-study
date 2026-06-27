# Kotlin オブジェクト指向ガイド（Flutter/Dart経験者向け）

Week 3「クラス設計」の読み物。`Classes.kt` の演習の背景にある **OOPの土台**を、
Dart と常に対比しながら体系化したもの。

## 目次

- **00. 全体像**（このファイル）— Dart と Kotlin の設計思想の違い
- [01. クラスとプロパティ](01-class-and-property.md) — フィールドではなくプロパティ、init、コンストラクタ
- [02. 可視性・継承・インターフェース・多態性](02-visibility-inheritance.md) — `open`/`override`/`abstract`/スマートキャスト
- [03. data class](03-data-class.md) — 値オブジェクトの自動生成（freezed相当）
- [04. sealed / enum](04-sealed-enum.md) — 取りうる状態を型で固定
- [05. object / companion object](05-object-companion.md) — シングルトンと static の代替
- [06. 委譲 by](06-delegation.md) — 継承の代わりの再利用
- [07. その他 & まとめ](07-misc-and-summary.md) — ネストクラス・Nothing・意思決定フロー

---

## 00. 全体像：DartとKotlinの「OOP設計思想」の違い

まず大局を押さえる。両者とも「クラスベースOOP + null安全 + ラムダ」で似ているが、
**デフォルトの方針**が決定的に違う。

| 観点 | Dart | Kotlin | 効いてくる場面 |
|---|---|---|---|
| クラスの継承可否 | デフォルトで**継承できる**（open） | デフォルトで**継承できない**（final） | 継承したい時 `open` が必須 |
| メソッドの上書き | `@override`（任意のアノテーション） | `override`（**必須キーワード**、文法） | 付け忘れはコンパイルエラー |
| フィールド | フィールドを直接持つ | **プロパティ**が基本（フィールドは隠れている） | getter/setter の扱い |
| シングルトン | 言語機能なし（手で書く） | `object` で言語機能 | 設定・ユーティリティ |
| static | `static` メンバ | `companion object`（厳密にはstaticではない） | ファクトリ・定数 |
| 可視性 | `_` プレフィックスで**ライブラリ**単位 | `private`/`protected`/`internal`/`public` キーワード | カプセル化 |
| union的な型 | `sealed`（Dart 3〜） | `sealed class`/`sealed interface` | 状態表現 |

## 一番大事な思想

**Kotlin は「デフォルトで閉じる（final・継承不可）」。**

これは「継承は設計者が明示的に許可した時だけ使ってよい」という Effective Java 由来の哲学。
Dart の「とりあえず全部 open」とは逆向きなので、ここで一度つまずく。

> OOPの3本柱「カプセル化・継承・多態性」のうち、Kotlin は**継承を最後の手段**とし、
> 委譲（`by`）・sealed・interface を優先する文化を言語機能で後押ししている。
</content>
</invoke>
