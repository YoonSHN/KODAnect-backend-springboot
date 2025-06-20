#!/bin/bash

if [ -n "$CHANGE_TARGET" ]; then
  BASE_BRANCH="origin/${CHANGE_TARGET}"
else
  BASE_BRANCH="${BASE_BRANCH:-origin/main}"
fi

echo "[INFO] 변경된 테스트 클래스만 실행합니다 (기준 브랜치는: ${BASE_BRANCH})"

test_files=$(git diff --name-only "$BASE_BRANCH"...HEAD \
  | grep '^src/test/java/' \
  | grep '\.java$')

if [ -z "$test_files" ]; then
  echo "[INFO] 변경된 테스트 클래스 없음. 테스트 스킵할게요"
  exit 0
fi

test_classes=$(echo "$test_files" \
  | sed 's#^src/test/java/##' \
  | sed 's#/#.#g' \
  | sed 's#\.java$##' \
  | paste -sd, -)

echo "[INFO] 실행할 테스트 클래스:"
echo "$test_classes" | tr ',' '\n'

echo "[INFO] Maven 테스트 실행 중 (변경된 테스트 대상)"
./mvnw test -Dtest="$test_classes"

exit_code=$?
if [ $exit_code -ne 0 ]; then
  echo "[ERROR] 일부 테스트 실패"
  exit $exit_code
fi

echo "[INFO] 테스트 성공"
