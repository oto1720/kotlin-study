# API連携・DI ガイド（Flutter/Dart経験者向け）

Week 10「API連携・DI入門」の読み物。Flutter のネットワーク層・DI の知識を
Kotlin/Android の対応技術にマッピングする。

## 目次

- **00. 全体像**（このファイル）— Flutter → Android のネットワーク・DI 対応表
- [01. Retrofit](01-retrofit.md) — dio → Retrofit
- [02. kotlinx.serialization](02-serialization.md) — json_serializable → kotlinx.serialization
- [03. Repository パターン](03-repository.md) — データ取得の抽象化
- [04. DI（手動 → Hilt）](04-di.md) — get_it/Riverpod → Hilt

---

## 00. 全体像

### Flutter → Android 対応表

| Flutter (Dart) | Android (Kotlin) | 説明 |
|---|---|---|
| `dio` / `http` | **Retrofit** + OkHttp | HTTP クライアント |
| `json_serializable` | **kotlinx.serialization** | JSON パース |
| `@JsonKey(name: ...)` | `@SerialName(...)` | フィールド名マッピング |
| `freezed` のモデル | `@Serializable data class` | データモデル |
| `get_it` / Riverpod Provider | **Hilt**（or 手動DI） | DI |
| Repository パターン | Repository パターン | 同じ設計 |
| `dio.interceptors` | OkHttp Interceptor | ログ・認証ヘッダー |
| `dio.options.baseUrl` | `Retrofit.Builder().baseUrl()` | ベースURL |

### アーキテクチャ（層構成）

```
UI 層 (Compose)
  ↓ collectAsStateWithLifecycle
ViewModel 層
  ↓ suspend fun
Domain 層 (Repository interface)
  ↓
Data 層 (Repository impl + API Service)
  ↓ Retrofit
Network (GitHub API)
```

Flutter の Clean Architecture（UI → ViewModel/Cubit → UseCase → Repository → DataSource）
とまったく同じ構造。Week 12 でこの層分けを仕上げる。
