#!/usr/bin/env bash
#
# Kotlin 練習ファイルをローカルで手軽に実行するヘルパー。
#
# 使い方:
#   ./run.sh week01-syntax/Basics.kt          # 1ファイルを実行
#   ./run.sh week02-functions-scope/*.kt      # 複数ファイルをまとめてコンパイルして実行
#
# 前提: kotlinc / java が PATH にあること（brew install kotlin で導入済み）。
# 各ファイルに fun main() があれば実行されます（Kotlin Playground の代わり）。

set -euo pipefail

if [ "$#" -eq 0 ]; then
  echo "使い方: ./run.sh <file.kt> [file2.kt ...]" >&2
  exit 1
fi

tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT

# libs/*.jar があればクラスパスに追加（Week5/6 の kotlinx-coroutines など）
cp=""
if compgen -G "libs/*.jar" > /dev/null; then
  cp="$(printf '%s:' libs/*.jar)"
fi

echo "→ コンパイル中: $*"
kotlinc "$@" ${cp:+-classpath "$cp"} -include-runtime -d "$tmp/app.jar"

echo "→ 実行:"
echo "----------------------------------------"
if [ -n "$cp" ]; then
  # 外部 jar が必要なときは java -jar が使えないので、Main-Class を読んで -cp で起動
  main="$(unzip -p "$tmp/app.jar" META-INF/MANIFEST.MF | awk -F': ' '/Main-Class/{gsub(/\r/,"",$2); print $2}')"
  java -cp "$tmp/app.jar:$cp" "$main"
else
  java -jar "$tmp/app.jar"
fi
