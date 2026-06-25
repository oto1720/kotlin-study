# Kotlin 3ヶ月ロードマップ（Android / Compose 中心）

Flutter / Dart の実戦経験を踏み台にして、**12週間で Kotlin + Jetpack Compose の実アプリを1本完成させる**ための学習リポジトリ。

- 想定読者: Flutter で実務〜ハッカソンレベルの開発経験がある人（Dart / 非同期 / 状態管理を理解済み）
- 学習ペース: **週5時間 × 12週 ≒ 60時間**
- ゴール: API連携・ViewModel・StateFlow を使った Compose アプリを自力で書けて、Dart↔Kotlin の対応関係を説明できる状態
- 進め方: 「3h インプット + 2h 手を動かす」を毎週、**週末に最低1コミット**

> 環境は **Kotlin 2.4.0（2026年6月時点の最新安定版）** / 最新の **Android Studio**（Kotlin プラグイン同梱）を前提。Compose は Material3 を使う。

---

## 進め方のルール

1. 各週フォルダ（`week01-*` 〜 `week12-*`）の `README.md` にその週のタスクが書いてある。
2. 学んだことは `docs/log.md` に1行でいいので記録する（後で ES や面接で「何を学んだか」を言語化する材料になる）。
3. 詰まったら無理に全部やらない。**60時間は最低ラインではなく上限**。週10〜11は実装なので、足りなければ週12を予備に回す。
4. Dart との違いに気づいたら `docs/dart-kotlin-cheatsheet.md` に追記する。これが自分専用の一番の教材になる。

### コミット運用（おすすめ）
```
feat(week03): data class と sealed class の練習
docs(log): 週3の学びを記録
```
週ごとにブランチを切ってもいいが、学習用なので `main` に直接積んでいくので十分。

---

## 環境構築（Week 0 / 1時間）

1. **Android Studio** 最新版をインストール（Kotlin プラグインは同梱なので別途不要）。
2. JDK は Android Studio 同梱の JBR でOK。
3. 序盤（Week 1〜4）の言語練習は **Kotlin Playground**（play.kotlinlang.org）か、Android Studio の `.kt` ファイル + `main()` 実行でも可。重い Android プロジェクトを毎回立ち上げなくていい。
4. Week 7 で初めて空の Compose プロジェクト（Empty Activity）を新規作成する。

### ローカルCLIで `.kt` を実行する（任意・おすすめ）

Playground を開かず、ターミナルから直接練習ファイルを動かしたい場合:

```bash
brew install kotlin          # kotlinc / kotlin を導入（Kotlin 2.4.0）
./run.sh week01-syntax/Basics.kt
```

`run.sh` は指定した `.kt` をコンパイルして `main()` を実行するだけのヘルパー。
複数ファイルもまとめて渡せる（例: `./run.sh week02-functions-scope/*.kt`）。

---

## 全体像

| フェーズ | 週 | テーマ | アウトプット |
|---|---|---|---|
| **Phase 1: 言語の差分** | W1–W3 | Dartとの構文差・関数・クラス設計 | 言語練習用の `.kt` 群 |
| **Phase 2: Kotlinらしさ** | W4–W6 | コレクション・コルーチン・Flow | 非同期の小さな課題 |
| **Phase 3: Compose** | W7–W9 | UI・レイアウト・状態管理 | 動く画面 |
| **Phase 4: 実践** | W10–W12 | API連携アプリを1本完成 | ポートフォリオ級アプリ |

---

## Phase 1 — 言語の差分（W1–W3）

退屈な基礎は飛ばし、**「Dart と違うところ」と「Kotlin らしい書き方」だけ**を高速で押さえる。

### Week 1 — 構文の差分
- **ねらい**: 変数・型・制御構文の Dart との違いを体に入れる
- **Flutter対応**: `var`/`final` → `var`/`val`、`String interpolation` の `$` は同じ、`switch` → `when`(式として返せる)、`int?` の null安全は両者似てるが Kotlin は `?.` `?:` `!!` `let` が中心
- **やること**: null安全（`?.` / `?:` / `!!`）、`when`、`for` と range（`1..10`, `until`, `step`）、スマートキャスト
- **成果物**: `week01` に練習 `.kt`、cheatsheet に「Dart→Kotlin 構文対応」を追記

### Week 2 — 関数・ラムダ・スコープ関数
- **ねらい**: Kotlin で一番つまずきやすい**スコープ関数**を使いこなす
- **Flutter対応**: 高階関数・ラムダは Dart と同じ感覚。だが `let`/`run`/`with`/`apply`/`also` は Dart に明確な対応がなく、Kotlin らしさの核
- **やること**: トップレベル関数、デフォルト引数・名前付き引数、ラムダと `it`、拡張関数、`let`/`run`/`apply`/`also`/`with` の使い分け
- **成果物**: スコープ関数5種の使い分けを自分の言葉で log にまとめる

### Week 3 — クラス設計
- **ねらい**: `data class` と `sealed class` を Compose の状態表現に使える形で理解
- **Flutter対応**: `data class` ≒ Dart の手書き `==`/`copyWith`/`toString` を自動生成。`sealed class` は freezed の union/sealed に近い。`object`(シングルトン) は Dart にない便利機能
- **やること**: `data class`(`copy`/分解宣言)、`sealed class`/`sealed interface`、`enum`、`object`/`companion object`、委譲 `by`
- **成果物**: 「画面状態を sealed class で表す」例（Loading/Success/Error）を書く ← W9で再利用

---

## Phase 2 — Kotlinらしさ（W4–W6）

### Week 4 — コレクション操作
- **ねらい**: `map`/`filter`/`fold` 系を手足のように使う
- **Flutter対応**: Dart の `Iterable` メソッドとほぼ同じ思想。`groupBy`/`associate`/`partition`/`sumOf` など Kotlin の方が便利。遅延評価は Dart の `Iterable` ≒ Kotlin の `Sequence`
- **やること**: `map`/`filter`/`flatMap`/`fold`/`reduce`/`groupBy`/`associateBy`、`Sequence` の使いどころ
- **成果物**: 簡単なデータ集計のお題を1問（例: JSONっぽいリストを集計）

### Week 5 — コルーチン基礎
- **ねらい**: 非同期を Dart の知識に接続する
- **Flutter対応**: `Future`/`async`/`await` → `suspend`/`launch`/`async`/`await`。`Isolate` ≒ `Dispatchers.Default/IO` でのスレッド切り替え。**構造化並行性(structured concurrency)** は Dart にない重要概念
- **やること**: `suspend fun`、`CoroutineScope`、`launch` vs `async`、`Dispatchers`、`withContext`、例外と `try/catch`
- **成果物**: 擬似的な「2つのAPIを並列取得して合成」を `runBlocking` で書く

### Week 6 — Flow
- **ねらい**: ここが Riverpod 経験者の最大の武器になる
- **Flutter対応**: Dart `Stream` ≒ Kotlin `Flow`(cold)。**`StateFlow` ≒ Riverpod の `StateNotifier`/`Notifier` が持つ状態**、`SharedFlow` ≒ イベント用 Stream。`collect` ≒ `listen`
- **やること**: `flow{}`、`map`/`filter`、`StateFlow`/`MutableStateFlow`、`SharedFlow`、cold/hot の違い
- **成果物**: cheatsheet に「Riverpod ↔ ViewModel+StateFlow 対応表」を作る ← W9で本番投入

---

## Phase 3 — Jetpack Compose（W7–W9）

ここから Android プロジェクトで実機/エミュレータを使う。Flutter 経験者は**宣言的UIの考え方をそのまま転用できる**ので速い。

### Week 7 — Compose基礎
- **ねらい**: 宣言的UIの対応関係を掴む
- **Flutter対応**: `Widget` → `@Composable fun`、`build()` → Composable 関数本体、`setState` → `remember { mutableStateOf() }` + recomposition、`StatelessWidget`/`StatefulWidget` の区別は **state hoisting** という設計判断に置き換わる
- **やること**: `@Composable`、`Text`/`Button`/`Modifier`、`remember`/`mutableStateOf`、recomposition、state hoisting、`@Preview`
- **成果物**: カウンター + 簡単なフォーム画面（状態を親に持ち上げる練習）

### Week 8 — レイアウトとテーマ・ナビゲーション
- **Flutter対応**: `Column`/`Row`/`Stack` → `Column`/`Row`/`Box`、`Expanded` → `Modifier.weight()`、`Padding`/`Container` → `Modifier`、`ListView.builder` → `LazyColumn`、`Navigator` → Navigation Compose、`ThemeData` → Material3 `MaterialTheme`
- **やること**: `Column`/`Row`/`Box`、`LazyColumn`、`Modifier` チェーン、Material3 テーマ、Navigation Compose で2画面遷移
- **成果物**: 一覧→詳細の2画面（ダミーデータでOK）

### Week 9 — 状態管理・ViewModel・副作用
- **ねらい**: W3/W6 の sealed class と StateFlow をUIに接続
- **Flutter対応**: `ChangeNotifierProvider`/`StateNotifierProvider` → `ViewModel` + `StateFlow`、`ref.watch` → `collectAsStateWithLifecycle()`、`initState`/`dispose` の副作用 → `LaunchedEffect`/`DisposableEffect`
- **やること**: `ViewModel`、`viewModelScope`、UI state を sealed class + `StateFlow` で公開、`collectAsStateWithLifecycle`、`LaunchedEffect`
- **成果物**: ViewModel が状態を持ち、画面が Loading/Success/Error を出し分ける

---

## Phase 4 — 実践アプリ（W10–W12）

**お題例**: 公開API（例: GitHubユーザー検索 / 任意のフリーAPI）を叩いて一覧→詳細を表示するアプリ。Clean Architecture の経験があるので、`data / domain / ui` の薄い層分けで設計する。

### Week 10 — API連携・DI入門
- **Flutter対応**: `dio`/`http` → **Retrofit + OkHttp**(または Ktor Client)、`json_serializable` → **kotlinx.serialization**、`get_it`/Riverpod のDI → **Hilt**（まずは手動DIでも可）
- **やること**: Retrofit or Ktor Client で GET、`kotlinx.serialization` でパース、Repository を1枚、`suspend` で取得
- **成果物**: API から取得したデータをログ/簡易画面に出す

### Week 11 — アプリ仕上げ
- **やること**: 一覧→詳細を実APIで接続、ローディング/エラーUI、`LazyColumn` で表示、簡単な検索 or pull-to-refresh
- **成果物**: 通しで動くアプリ（雑でいいので「完成」させる）

### Week 12 — リファクタ・テスト入門・振り返り
- **やること**: 命名/層分けの見直し、ViewModel の簡単な単体テスト（`kotlinx-coroutines-test`）を1本、README にスクショ + 学びを整理
- **成果物**: ポートフォリオに載せられる状態の README、`docs/log.md` の総まとめ

---

## 完成の基準（自己評価チェック）

- [ ] `let`/`run`/`apply`/`also`/`with` を使い分けられる
- [ ] `data class` と `sealed class` で画面状態を設計できる
- [ ] `suspend`/コルーチン/`Dispatchers` で非同期を書ける
- [ ] `StateFlow` を Riverpod の状態と対応づけて説明できる
- [ ] Compose で state hoisting と ViewModel 連携ができる
- [ ] API連携アプリを1本、自力で完成させた
- [ ] Dart↔Kotlin の対応を自分の cheatsheet にまとめた

---

## 参考リンク

- Kotlin 公式ドキュメント: https://kotlinlang.org/docs/home.html
- Kotlin Koans（手を動かす公式演習）: https://play.kotlinlang.org/koans
- Android Compose 公式: https://developer.android.com/jetpack/compose/documentation
- Now in Android（Google公式サンプルアプリ）: https://github.com/android/nowinandroid
- コルーチン公式ガイド: https://kotlinlang.org/docs/coroutines-guide.html
