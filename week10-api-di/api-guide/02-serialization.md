# 02. kotlinx.serialization — json_serializable → kotlinx.serialization

## 2-1. 対応表

| Dart (json_serializable) | Kotlin (kotlinx.serialization) |
|---|---|
| `@JsonSerializable()` | `@Serializable` |
| `@JsonKey(name: 'field_name')` | `@SerialName("field_name")` |
| `factory Foo.fromJson(Map json)` | 自動生成（コード不要） |
| `Map toJson()` | 自動生成（`Json.encodeToString(obj)`） |
| `build_runner` でコード生成 | **Kotlin コンパイラプラグイン**で自動 |

## 2-2. 基本的な使い方

```kotlin
@Serializable
data class User(
    val id: Int,
    val name: String,
    @SerialName("avatar_url")    // JSON キー名が違うとき
    val avatarUrl: String,
    val bio: String? = null,     // null 許容 + デフォルト値
)
```

Dart で書くと：

```dart
@JsonSerializable()
class User {
  final int id;
  final String name;
  @JsonKey(name: 'avatar_url')
  final String avatarUrl;
  final String? bio;

  User({required this.id, required this.name, required this.avatarUrl, this.bio});
  factory User.fromJson(Map<String, dynamic> json) => _$UserFromJson(json);
  Map<String, dynamic> toJson() => _$UserToJson(this);
}
```

### Kotlin の方が短い理由

- `data class` が `equals` / `toString` / `copy` を自動生成（freezed 不要）
- `@Serializable` で `fromJson` / `toJson` 相当が自動生成（`build_runner` 不要）
- Kotlin コンパイラプラグインなので、**ビルドのたびに自動で更新**される

## 2-3. JSON との変換

```kotlin
val json = Json {
    ignoreUnknownKeys = true     // API に知らないフィールドがあっても無視
    prettyPrint = true           // デバッグ用に整形
    coerceInputValues = true     // null → デフォルト値
}

// オブジェクト → JSON 文字列
val jsonString = json.encodeToString(user)

// JSON 文字列 → オブジェクト
val user = json.decodeFromString<User>(jsonString)
```

## 2-4. ネストしたオブジェクト

```kotlin
@Serializable
data class SearchResponse(
    @SerialName("total_count")
    val totalCount: Int,
    val items: List<GitHubRepo>,    // ネストも自動でパース
)

@Serializable
data class GitHubRepo(
    val id: Long,
    val name: String,
    val owner: Owner,               // さらにネスト
)

@Serializable
data class Owner(
    val login: String,
    @SerialName("avatar_url")
    val avatarUrl: String,
)
```

## 2-5. build.gradle.kts の設定

```kotlin
plugins {
    kotlin("plugin.serialization") version "2.4.0"
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
}
```

`build_runner` のように手動でコード生成を走らせる必要はない。
Kotlin コンパイラが自動で処理する。

## 2-6. Gson / Moshi との比較

| ライブラリ | 特徴 | Flutter 相当 |
|---|---|---|
| **kotlinx.serialization** | Kotlin 公式。マルチプラットフォーム対応。★推奨 | json_serializable |
| Gson | Google 製。リフレクションベース。古い | dart:convert（手書き） |
| Moshi | Square 製。kotlinx.serialization と似た思想 | json_serializable |

新規プロジェクトでは **kotlinx.serialization** 一択。
