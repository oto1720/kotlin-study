# リファクタ・テスト ガイド（Flutter/Dart経験者向け）

Week 12「リファクタ・テスト入門・振り返り」の読み物。
Week 11 で動くようにしたアプリを、ポートフォリオ品質に仕上げる。

## 目次

- **00. 全体像**（このファイル）— リファクタとテストの方針
- [01. リファクタ](01-refactor.md) — 命名・層分け・ファイル構成の見直し
- [02. ViewModel テスト](02-viewmodel-test.md) — 単体テストの書き方
- [03. 振り返り・README 整備](03-retrospective.md) — ポートフォリオとしてまとめる

---

## 00. 全体像

### Week 12 でやること

1. **リファクタ**: 命名/層分けの見直し、ファイル分割
2. **テスト**: ViewModel の単体テストを最低1本
3. **README 整備**: スクショ + 使い方 + 学びの整理

### Flutter テストとの対応

| Flutter | Android (Kotlin) | 説明 |
|---|---|---|
| `flutter test` | `./gradlew test` | ユニットテスト実行 |
| `test/` ディレクトリ | `src/test/` | テストファイル置き場 |
| `flutter_test` | JUnit 5 | テストフレームワーク |
| `mockito` / `mocktail` | Fake クラス / MockK | モック |
| `ProviderContainer` | `Dispatchers.setMain` | テスト環境セットアップ |
| `widget test` | Compose UI test | UIテスト |
| `integration_test` | Espresso / Compose test | E2Eテスト |

### テスト戦略

**まず ViewModel テストだけ書く。** UI テストは後回しでいい。

```
               UI テスト（Compose test）
              ━━━━━━━━━━━━━━━━━━━━━
           ViewModel テスト ★ ← ここだけやる
          ━━━━━━━━━━━━━━━━━━━━━━━━━
       Repository テスト（余裕があれば）
      ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

ViewModel テストが最もコスパが高い：
- ビジネスロジックの核をカバーできる
- Android フレームワークに依存しない（JVM で高速に動く）
- UI テストは壊れやすく、メンテコストが高い
