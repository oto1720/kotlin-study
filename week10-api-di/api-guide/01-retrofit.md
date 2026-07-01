# 01. Retrofit — dio → Retrofit

## 1-1. Retrofit とは

Flutter の `dio`（または `http`）に相当する HTTP クライアント。
大きな違いは：**インターフェースにアノテーションを付けるだけで実装が自動生成される**。

```dart
// Flutter (dio)
final response = await dio.get(
  '/search/repositories',
  queryParameters: {'q': 'kotlin', 'sort': 'stars'},
);
final data = SearchResponse.fromJson(response.data);
```

```kotlin
// Kotlin (Retrofit)
interface GitHubApiService {
    @GET("search/repositories")
    suspend fun searchRepos(
        @Query("q") query: String,
        @Query("sort") sort: String = "stars",
    ): SearchResponse      // ← JSON → Kotlin の変換も自動
}

// 呼び出し
val response = apiService.searchRepos("kotlin")
```

## 1-2. セットアップ

### build.gradle.kts に追加

```kotlin
dependencies {
    // Retrofit 本体
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    // JSON 変換（kotlinx.serialization を使う場合）
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:2.11.0")
    // OkHttp（Retrofit の通信エンジン）
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    // ログ出力（開発用）
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    // kotlinx.serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
}
```

### Retrofit インスタンス生成

```kotlin
val json = Json {
    ignoreUnknownKeys = true     // 知らないキーを無視（API に新フィールドが増えても壊れない）
    coerceInputValues = true     // null → デフォルト値
}

val retrofit = Retrofit.Builder()
    .baseUrl("https://api.github.com/")
    .client(
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            .build()
    )
    .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
    .build()

val apiService: GitHubApiService = retrofit.create(GitHubApiService::class.java)
```

## 1-3. アノテーション一覧

| アノテーション | 説明 | dio 相当 |
|---|---|---|
| `@GET("path")` | GET リクエスト | `dio.get('/path')` |
| `@POST("path")` | POST リクエスト | `dio.post('/path')` |
| `@PUT` / `@DELETE` / `@PATCH` | その他の HTTP メソッド | 同様 |
| `@Query("key")` | クエリパラメータ | `queryParameters: {'key': value}` |
| `@Path("name")` | パスパラメータ | URL に埋め込み |
| `@Body` | リクエストボディ | `data: body` |
| `@Header("name")` | ヘッダー | `options: Options(headers: ...)` |
| `@Headers(...)` | 固定ヘッダー | Interceptor でも可 |

## 1-4. Interceptor — dio.interceptors に相当

```kotlin
val authInterceptor = Interceptor { chain ->
    val request = chain.request().newBuilder()
        .addHeader("Authorization", "Bearer $token")
        .build()
    chain.proceed(request)
}

val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(authInterceptor)
    .addInterceptor(HttpLoggingInterceptor())
    .build()
```

Flutter の `dio.interceptors.add(InterceptorsWrapper(onRequest: ...))` と同じ仕組み。

## 1-5. エラーハンドリング

```kotlin
try {
    val repos = apiService.searchRepos("kotlin")
} catch (e: HttpException) {
    // 4xx / 5xx（DioException.badResponse に相当）
    val code = e.code()
    val body = e.response()?.errorBody()?.string()
} catch (e: IOException) {
    // ネットワークエラー（DioException.connectionError に相当）
}
```

Retrofit は HTTP エラーを `HttpException`、通信エラーを `IOException` で投げる。
dio の `DioExceptionType` で分岐するのと同じパターン。
