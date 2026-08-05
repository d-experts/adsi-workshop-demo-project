#!/bin/bash
# Gradle wrapper 呼び出しの OS 差を吸収する。
#
# npm scripts は Windows では cmd.exe 経由で実行されるため、
# "./gradlew" 形式が解釈できずエラーになる。Windows では gradlew.bat を、
# それ以外では ./gradlew を使う。
set -e

BACKEND_DIR="$(cd "$(dirname "$0")/../packages/backend" && pwd)"
cd "$BACKEND_DIR"

case "$(uname -s)" in
  MINGW* | MSYS* | CYGWIN*) exec ./gradlew.bat "$@" ;;
  *) exec ./gradlew "$@" ;;
esac
