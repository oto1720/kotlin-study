# 02. LazyColumn / LazyRow — ListView.builder の代替

## 2-1. LazyColumn = ListView.builder

画面に見えるアイテムだけを compose する遅延リスト。
Flutter の `ListView.builder` とまったく同じ目的。

```kotlin
@Composable
fun UserList(users: List<User>) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),                // リスト外側の余白
        verticalArrangement = Arrangement.spacedBy(8.dp),     // アイテム間の余白
    ) {
        items(
            items = users,
            key = { it.id },        // Flutter の Key 相当。差分更新を効率化。
        ) { user ->
            UserCard(user)
        }
    }
}
```

### Flutter との対応

```dart
// Flutter
ListView.builder(
  padding: EdgeInsets.all(16),
  itemCount: users.length,
  itemBuilder: (context, index) => UserCard(user: users[index]),
)
```

## 2-2. items の書き方バリエーション

```kotlin
LazyColumn {
    // 1. List を渡す（最も一般的）
    items(users, key = { it.id }) { user -> UserCard(user) }

    // 2. 件数指定（Flutter の itemCount に近い）
    items(count = 100) { index -> Text("Item $index") }

    // 3. 単一アイテム（ヘッダーやフッター）
    item { Text("ヘッダー", style = MaterialTheme.typography.headlineSmall) }

    // 4. 組み合わせ
    item { Text("リポジトリ一覧") }
    items(repos, key = { it.id }) { repo -> RepoCard(repo) }
    item { Text("${repos.size} 件") }
}
```

## 2-3. LazyRow（横スクロール）

```kotlin
LazyRow(
    contentPadding = PaddingValues(horizontal = 16.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
) {
    items(categories, key = { it.id }) { category ->
        CategoryChip(category)
    }
}
```

Flutter の `ListView(scrollDirection: Axis.horizontal)` に相当。

## 2-4. LazyVerticalGrid（グリッド）

```kotlin
LazyVerticalGrid(
    columns = GridCells.Fixed(2),        // 2列固定（Flutter の crossAxisCount: 2）
    // or GridCells.Adaptive(minSize = 150.dp) // 画面幅に応じて列数が変わる
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
) {
    items(photos, key = { it.id }) { photo ->
        PhotoCard(photo)
    }
}
```

## 2-5. パフォーマンスの注意点

```kotlin
LazyColumn {
    items(users, key = { it.id }) { user ->
        // ✅ key を指定する → リストの変更時に効率的に差分更新
        // ❌ key なし → Flutter で Key を付けないのと同じ（再利用が不正確になりうる）
        UserCard(user)
    }
}
```

- `key` を必ず指定する（Flutter の `Key` と同じ重要性）
- LazyColumn の中で `Column` をネストしない（高さが決まらず無限ループの可能性）
- 大量データは `Paging` ライブラリ（Flutter の `infinite_scroll_pagination` 相当）を使う
