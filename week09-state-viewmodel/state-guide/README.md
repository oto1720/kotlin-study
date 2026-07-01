# 状態管理・ViewModel ガイド（Flutter/Riverpod経験者向け）

Week 9「状態管理・ViewModel・副作用」の読み物。Week 3 の sealed class と
Week 6 の StateFlow を Compose の UI に接続する。Riverpod の知識が最大の武器になる章。

## 目次

- **00. 全体像**（このファイル）— Riverpod → ViewModel + StateFlow の頭の切り替え
- [01. ViewModel](01-viewmodel.md) — Notifier → ViewModel
- [02. sealed class + StateFlow で UiState](02-uistate-stateflow.md) — AsyncValue → sealed interface
- [03. collectAsStateWithLifecycle](03-collect-lifecycle.md) — ref.watch → collectAsState
- [04. 副作用（LaunchedEffect / DisposableEffect）](04-side-effects.md) — initState/dispose の代替

---

## 00. 全体像：Riverpod → ViewModel + StateFlow

### 対応表（★最重要）

| Riverpod | Android (Compose) | 説明 |
|---|---|---|
| `Notifier` / `StateNotifier` | `ViewModel` | ビジネスロジック + 状態保持 |
| `state` | `MutableStateFlow.value` | 現在の状態を読み書き |
| `ref.watch(provider)` | `collectAsStateWithLifecycle()` | 状態を購読してUIに反映 |
| `AsyncValue<T>` | `sealed interface UiState` | Loading/Success/Error を表す型 |
| `autoDispose` | `viewModelScope`（自動キャンセル） | ViewModel 破棄時にコルーチン終了 |
| `ref.listen` | `LaunchedEffect` + `collect` | 副作用（SnackBar 表示等） |
| `ref.read(p.notifier).method()` | `viewModel.method()` | アクション呼び出し |
| `ProviderScope` | `ViewModelStoreOwner`（Activity等） | DI のスコープ |

### データの流れ

```
[ViewModel]                        [Composable]
  MutableStateFlow                    collectAsStateWithLifecycle
       │                                  │
       │  .value = Success(data)          │  State<UiState> として受け取る
       │──────── StateFlow ──────────────▶│
                                          │
                                    when (state) {
                                      Loading -> ...
                                      Success -> ...
                                      Error   -> ...
                                    }
```

Riverpod で `state = AsyncData(data)` → `ref.watch` で購読するのとまったく同じ流れ。

### 一番大事なポイント

**Riverpod の経験がそのまま使える。**
名前が違うだけで、設計思想（状態を一方向に流す / UI は状態の関数）は同じ。
Week 6 で学んだ `StateFlow` が「ViewModel の出力」として使われ、
Compose が `collectAsStateWithLifecycle` で購読する。
