# 03. 完成チェックリスト

## アプリが「完成」と言える基準

### 必須（これができれば Week 11 は完了）

- [ ] 検索バーにキーワードを入力して検索ボタン（またはキーボードの検索）で結果が出る
- [ ] 結果が LazyColumn で一覧表示される（リポジトリ名・説明・スター数・言語）
- [ ] アイテムをタップすると詳細画面に遷移する
- [ ] 詳細画面で戻るボタンが動作する
- [ ] Loading 中に CircularProgressIndicator が表示される
- [ ] エラー時にエラーメッセージ + リトライボタンが表示される
- [ ] 結果が空のとき「見つかりませんでした」が表示される

### あると良い（余裕があれば）

- [ ] Pull-to-refresh で再検索
- [ ] 検索の debounce（入力後 500ms で自動検索）
- [ ] アバター画像の表示（Coil）
- [ ] Material3 テーマが適用されている
- [ ] ダークモード対応

### やらなくていい（Week 12 に回す）

- [ ] ファイル分割（1ファイルでも「動く」なら OK）
- [ ] テスト
- [ ] 命名の統一
- [ ] 完璧なエラーハンドリング

## build.gradle.kts の依存関係まとめ

```kotlin
dependencies {
    // Compose (BOM で一括管理)
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // ViewModel + Compose 連携
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    // Retrofit + OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // kotlinx.serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // 画像読み込み（任意）
    implementation("io.coil-kt.coil3:coil-compose:3.0.4")

    // Hilt（DI・任意）
    implementation("com.google.dagger:hilt-android:2.52")
    ksp("com.google.dagger:hilt-android-compiler:2.52")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
}
```

## トラブルシューティング

| 症状 | 原因 | 対処 |
|---|---|---|
| JSON パースエラー | 知らないフィールドがある | `Json { ignoreUnknownKeys = true }` |
| ネットワークエラー | AndroidManifest に INTERNET 権限がない | `<uses-permission android:name="android.permission.INTERNET"/>` |
| 画面回転で状態がリセット | `remember` だけで状態を保持している | ViewModel に移す |
| LazyColumn が表示されない | `fillMaxSize()` が抜けている | Modifier を確認 |
| ビルドエラー（serialization） | プラグイン未設定 | `kotlin("plugin.serialization")` を追加 |
