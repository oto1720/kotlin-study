# 03. 振り返り・README 整備

## 3-1. README テンプレート

ポートフォリオとして見せる場合の README。GitHub でリポジトリを開いたときに
**30秒で何のアプリか分かる**のが目標。

```markdown
# GitHub Search App

GitHub のリポジトリを検索・閲覧できる Android アプリ。
Kotlin + Jetpack Compose の学習プロジェクト。

## スクリーンショット

| 検索画面 | 詳細画面 | エラー画面 |
|---|---|---|
| ![search](screenshots/search.png) | ![detail](screenshots/detail.png) | ![error](screenshots/error.png) |

## 技術スタック

- **言語**: Kotlin 2.4.0
- **UI**: Jetpack Compose + Material3
- **アーキテクチャ**: MVVM (ViewModel + StateFlow)
- **ネットワーク**: Retrofit + OkHttp
- **JSON**: kotlinx.serialization
- **DI**: Hilt
- **テスト**: JUnit 5 + kotlinx-coroutines-test

## アーキテクチャ

UI (Compose) → ViewModel → Repository → API (Retrofit)

## セットアップ

1. Android Studio を開く
2. このプロジェクトを開く
3. Run ボタンでエミュレータ/実機に実行

## 学んだこと

- Flutter/Dart から Kotlin への移行で最も大きかった差分は...
- Riverpod → ViewModel + StateFlow の対応付けが...
- Compose の Modifier チェーンは Flutter の Widget ネストより...
```

## 3-2. スクリーンショットの撮り方

1. エミュレータでアプリを起動
2. 各画面（検索・結果・詳細・エラー・Loading）をキャプチャ
3. `screenshots/` ディレクトリに保存
4. README に貼る

Android Studio: `View` > `Tool Windows` > `Device File Explorer` > スクリーンショットボタン

## 3-3. 12週間の振り返り（docs/log.md の総まとめ）

`docs/log.md` に以下を追記する：

```markdown
## 総まとめ (Week 12)

### できるようになったこと
- [ ] let/run/apply/also/with を使い分けられる
- [ ] data class と sealed class で画面状態を設計できる
- [ ] suspend/コルーチン/Dispatchers で非同期を書ける
- [ ] StateFlow を Riverpod の状態と対応づけて説明できる
- [ ] Compose で state hoisting と ViewModel 連携ができる
- [ ] API連携アプリを1本、自力で完成させた
- [ ] Dart↔Kotlin の対応を cheatsheet にまとめた

### Dart → Kotlin で一番大きかった学び
（自分の言葉で1-3文）

### 今後やりたいこと
- Room でローカルキャッシュ
- Compose のアニメーション
- マルチモジュール化
- CI/CD（GitHub Actions）
```

## 3-4. ポートフォリオとして見せるコツ

### 面接で聞かれそうなこと

1. **「なぜ Kotlin/Android を学んだのですか？」**
   → Flutter の経験を活かしてネイティブ開発の幅を広げるため

2. **「Flutter との違いで最も苦労した点は？」**
   → （例）構造化並行性、Gradle のビルド設定、Modifier の順序依存

3. **「このアプリのアーキテクチャを説明してください」**
   → MVVM。ViewModel が StateFlow で状態を公開し、Compose が購読する。
     Riverpod の Notifier + ref.watch と同じ設計思想。

4. **「テストはどう書きましたか？」**
   → ViewModel の単体テスト。Fake Repository を注入し、
     Success/Error/空クエリのパターンをカバー。

### GitHub リポジトリの整え方

- [ ] `.gitignore` に不要ファイル（`.idea/`, `build/`, `*.iml`）を追加
- [ ] README.md をルートに置く
- [ ] スクリーンショットを貼る
- [ ] コミットメッセージを整理（`feat:`, `docs:`, `fix:` 等の prefix）
- [ ] 不要なデバッグコード（`println`）を削除
