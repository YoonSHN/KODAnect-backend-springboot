#!/bin/bash

if [ -n "$CHANGE_TARGET" ]; then
  BASE_BRANCH="origin/${CHANGE_TARGET}"
else
  BASE_BRANCH="${BASE_BRANCH:-origin/main}"
fi

echo "[INFO] 코드 변경 여부 확인 중... (기준: $BASE_BRANCH)"

changed_code=$(git diff --name-only "$BASE_BRANCH"...HEAD | grep '^src/')

if [ -z "$changed_code" ]; then
  echo "[INFO] 코드 변경 없음 → 빌드 스킵"
  exit 0
fi

echo "[INFO] 코드 변경 감지됨 → 빌드 수행"
exit 1
