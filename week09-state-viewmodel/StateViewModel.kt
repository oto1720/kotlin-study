/**
 * Week 9 演習 — 状態管理・ViewModel・副作用
 * ※ Android Studio の Compose プロジェクト内で使う。run.sh では実行不可。
 *
 * ねらい: W3 の sealed class と W6 の StateFlow を UI に接続する。
 *   ChangeNotifierProvider/StateNotifierProvider → ViewModel + StateFlow
 *   ref.watch → collectAsStateWithLifecycle()
 *   initState/dispose → LaunchedEffect / DisposableEffect
 *
 * 成果物: ViewModel が状態を持ち、Loading/Success/Error を出し分ける画面
 */

package com.example.week09

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// =====================================================================
// 1. UI State（sealed interface）— Week3 の UiState を本番投入
// =====================================================================
// Riverpod の AsyncValue<T>（AsyncLoading/AsyncData/AsyncError）と同じ発想。
// sealed にすることで when の網羅性チェックが効く。

data class Repo(val id: Int, val name: String, val stars: Int)

sealed interface RepoUiState {
    data object Loading : RepoUiState
    data class Success(val repos: List<Repo>) : RepoUiState
    data class Error(val message: String) : RepoUiState
}

// =====================================================================
// 2. ViewModel — Riverpod の Notifier/StateNotifier に相当
// =====================================================================
// ViewModel はライフサイクルに紐づく（画面回転しても生き残る）。
// Riverpod の Notifier が state を持つように、ViewModel は StateFlow を公開する。

class RepoViewModel : ViewModel() {
    // _uiState: 内部でだけ書き換え可能な MutableStateFlow
    // uiState:  外部に公開する読み取り専用の StateFlow
    // Riverpod: state = Loading → state = AsyncData(repos) と同じ流れ
    private val _uiState = MutableStateFlow<RepoUiState>(RepoUiState.Loading)
    val uiState: StateFlow<RepoUiState> = _uiState.asStateFlow()

    init {
        loadRepos()
    }

    fun loadRepos() {
        // viewModelScope: ViewModel が破棄されると自動でキャンセル（structured concurrency）
        // Riverpod の ref.onDispose でキャンセルする必要がない。
        viewModelScope.launch {
            _uiState.value = RepoUiState.Loading
            try {
                val repos = fetchRepos()    // suspend fun（擬似API）
                _uiState.value = RepoUiState.Success(repos)
            } catch (e: Exception) {
                _uiState.value = RepoUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun retry() = loadRepos()
}

// 擬似的なAPI呼び出し（Week10 で実際の API に差し替える）
private suspend fun fetchRepos(): List<Repo> {
    delay(1500)     // ネットワーク遅延をシミュレート
    // エラーをシミュレートしたい場合は以下のコメントを外す:
    // throw java.io.IOException("Network error")
    return listOf(
        Repo(1, "kotlin", 49000),
        Repo(2, "compose-samples", 18000),
        Repo(3, "nowinandroid", 16000),
        Repo(4, "architecture-samples", 44000),
        Repo(5, "sunflower", 17000),
    )
}

// =====================================================================
// 3. collectAsStateWithLifecycle — Riverpod の ref.watch に相当
// =====================================================================
// StateFlow を Compose の State に変換する。ライフサイクルに従い、
// バックグラウンドでは自動で購読を停止する（Riverpod の autoDispose 的）。

@Composable
fun RepoScreen(
    viewModel: RepoViewModel = viewModel(),     // DI（Week10 で Hilt に差し替え可）
) {
    // ★ collectAsStateWithLifecycle = ref.watch(repoProvider)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // when で網羅的に出し分け（sealed なので else 不要）
    when (val state = uiState) {
        is RepoUiState.Loading -> LoadingContent()
        is RepoUiState.Success -> RepoList(repos = state.repos)
        is RepoUiState.Error -> ErrorContent(
            message = state.message,
            onRetry = viewModel::retry,
        )
    }
}

// =====================================================================
// 4. 各状態のUI
// =====================================================================

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("読み込み中...")
        }
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "エラーが発生しました",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = message, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("リトライ")
            }
        }
    }
}

@Composable
private fun RepoList(repos: List<Repo>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(repos, key = { it.id }) { repo ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(repo.name, style = MaterialTheme.typography.titleMedium)
                    Text("⭐ ${repo.stars}")
                }
            }
        }
    }
}

// =====================================================================
// 5. LaunchedEffect / DisposableEffect — Flutter の initState/dispose
// =====================================================================
// Composable 内で副作用（APIコール、タイマー、ログ）を安全に実行する仕組み。
// Recomposition のたびに副作用が再実行されないよう、key で制御する。

@Composable
fun LaunchedEffectDemo(userId: String) {
    var message by remember { mutableStateOf("Loading...") }

    // LaunchedEffect(key): key が変わるたびにブロックを再実行。
    // Flutter の initState + didUpdateWidget に相当。
    // key = userId → userId が変わったら再実行。
    LaunchedEffect(userId) {
        delay(1000)
        message = "User $userId loaded"
    }

    Text(message)
}

// DisposableEffect: リソース解放が必要な場合（onDispose = Dart の dispose()）
@Composable
fun DisposableEffectDemo() {
    DisposableEffect(Unit) {
        println("subscribe")
        onDispose {
            println("unsubscribe")   // Composable が画面から消えたとき
        }
    }
}

// =====================================================================
// 6. まとめ：Riverpod ↔ ViewModel+StateFlow 対応表
// =====================================================================
/*
  Riverpod                          → Compose / Android
  ─────────────────────────────────────────────────────
  Notifier / StateNotifier          → ViewModel
  state                             → _uiState.value (MutableStateFlow)
  ref.watch(provider)               → collectAsStateWithLifecycle()
  AsyncValue<T>                     → sealed interface UiState
    AsyncLoading                    →   UiState.Loading
    AsyncData(value)                →   UiState.Success(data)
    AsyncError(error, stackTrace)   →   UiState.Error(message)
  ref.read(provider.notifier).method → viewModel.method()
  autoDispose                       → viewModelScope（自動キャンセル）
  ref.onDispose                     → DisposableEffect の onDispose
  ref.listen                        → LaunchedEffect + collect
*/
