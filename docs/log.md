# 学習ログ

毎週1〜数行でOK。「何をやったか」より「何が分かったか / 何に詰まったか」を残すと、面接やESで効く。

## テンプレ
```
## Week N (YYYY/MM/DD)
- やったこと:
- 分かったこと / Dartとの違い:
- 詰まったこと:
- 次にやること:
```

---

## Week 1 (2026/06/23)
- やったこと: Basics.kt の演習（val/var、文字列補間、null安全 ?:/?.、when式、range+until/step、スマートキャスト）。ローカルCLI（kotlinc 2.4.0）+ run.sh で実行確認。
- 分かったこと / Dartとの違い: when が「式」で値を返せる（switch文との最大の差）。Dart の ?? は Kotlin の ?:。until は終端を含まない（Dart の `..<` 相当）、step で刻み幅指定。`is` チェック後はスマートキャストでキャスト不要。
- 詰まったこと: when を式として使うと else（網羅性）が必須になる点。
- 次にやること: Week 2（関数・ラムダ・スコープ関数）。

## Week 2 (2026/06/25)
- やったこと: Functions.kt を作成。トップレベル関数/デフォルト・名前付き引数、ラムダと it、高階関数、拡張関数、スコープ関数5種(let/run/apply/also/with)。run.sh で実行確認。
- 分かったこと / Dartとの違い: スコープ関数は「ブロック内の参照(it か this)」×「戻り値(ラムダ結果かオブジェクト自身)」の2軸で整理できる。apply/also はオブジェクト自身を返す→初期化やチェーン途中のログに便利。拡張関数は Dart の extension より手軽。trailing lambda で () の外にラムダを出せる。
- 詰まったこと: let/run と apply/also の戻り値の違い（前者=ラムダ結果、後者=対象自身）を最初混同しがち。
- 次にやること: Week 3（data class / sealed class）。

## Week 3 (2026/06/25)
- やったこと: Classes.kt 作成。data class(copy/分解宣言/値等価)、sealed interface で UiState(Loading/Success/Error)、enum(振る舞い付き)、object(シングルトン)/companion object、委譲 by。
- 分かったこと / Dartとの違い: data class が freezed の copyWith/== を自動生成。sealed を when で網羅すると else 不要＝抜け漏れをコンパイラが検知。object は Dart にないシングルトン。委譲 by で実装を丸投げできる。UiState は Week9 でそのまま使う。
- 詰まったこと: sealed の共変 <out T> と Nothing（Loading/Error が値を持たない表現）。
- 次にやること: Week 4（コレクション）。

## Week 4 (2026/06/25)
- やったこと: Collections.kt 作成。map/filter/flatMap/fold/reduce、groupBy/associateBy/partition、Sequence(遅延)、sumOf/average/maxByOrNull で集計のお題（カテゴリ別売上・ランキング）。
- 分かったこと / Dartとの違い: 思想は Dart の Iterable と同じ。groupBy/associateBy/partition/sumOf が Kotlin の便利どころ。Sequence は Dart の Iterable 相当で take と組むと巨大データでも無駄なく評価。
- 詰まったこと: reduce は空リストで例外（初期値が要るなら fold）。
- 次にやること: Week 5（コルーチン）。

## Week 5 (2026/06/25)
- やったこと: Coroutines.kt 作成。suspend/delay、launch vs async、2API並列取得(measureTimeMillis で~500ms確認)、Dispatchers/withContext、例外処理、構造化並行性。kotlinx-coroutines を libs/ に入れ run.sh をクラスパス対応に。
- 分かったこと / Dartとの違い: Future/async/await → suspend/launch/async/await。delay は非ブロッキング。★落とし穴: coroutineScope 内の async 例外は await で catch しても親に伝播してクラッシュ→ supervisorScope で隔離する。構造化並行性は Dart にない概念で、scope を抜ける＝子の完了を自動で待つ。
- 詰まったこと: 上記 async + 例外伝播。supervisorScope で解決。
- 次にやること: Week 6（Flow）。

## Week 6 (2026/06/25)
- やったこと: FlowDemo.kt 作成。flow{}(cold)・collect、map/filter/onEach/fold、StateFlow(現在値あり)、SharedFlow(イベント)。
- 分かったこと / Dartとの違い: Dart Stream(cold) ≒ Flow。★StateFlow ≒ Riverpod Notifier の状態（.value で読み書き、常に現在値）→ Week9 で collectAsStateWithLifecycle で購読。SharedFlow はイベント用で hot＝購読前の emit は届かない。
- 詰まったこと: hot な SharedFlow は購読開始前の emit を取りこぼす（delay で購読を待ってから emit）。
- 次にやること: Week 7（Compose基礎）。※ここから Android Studio 必須でCLI実行は不可。
