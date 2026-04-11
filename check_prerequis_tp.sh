#!/usr/bin/env bash
set -euo pipefail

status=0

check_cmd() {
  local cmd="$1"
  if command -v "$cmd" >/dev/null 2>&1; then
    echo "[OK] $cmd: $(command -v "$cmd")"
  else
    echo "[KO] $cmd manquant"
    status=1
  fi
}

echo "=== Verification prerequis TP ==="
check_cmd java
check_cmd mvn
check_cmd python3
check_cmd cmake
check_cmd protoc

if command -v ninja >/dev/null 2>&1; then
  echo "[OK] ninja: $(command -v ninja)"
else
  echo "[INFO] ninja non detecte: CMake utilisera le generateur par defaut (Makefiles)"
fi

echo
if command -v protoc >/dev/null 2>&1; then
  pver="$(protoc --version || true)"
  echo "protoc version: $pver"
  if [[ "$pver" != "libprotoc 29.3" ]]; then
    echo "[KO] La version requise est libprotoc 29.3"
    status=1
  else
    echo "[OK] Version protobuf conforme"
  fi
fi

echo
if [[ -z "${VCPKG_ROOT:-}" ]]; then
  echo "[KO] VCPKG_ROOT non defini"
  echo "     Exemple: export VCPKG_ROOT=\"$HOME/vcpkg\""
  status=1
else
  echo "[OK] VCPKG_ROOT=$VCPKG_ROOT"
  if [[ -f "$VCPKG_ROOT/scripts/buildsystems/vcpkg.cmake" ]]; then
    echo "[OK] Toolchain vcpkg detecte"
  else
    echo "[KO] Toolchain introuvable: $VCPKG_ROOT/scripts/buildsystems/vcpkg.cmake"
    status=1
  fi
fi

echo
if [[ $status -eq 0 ]]; then
  echo "Tous les prerequis TP sont satisfaits."
else
  echo "Des prerequis manquent. Corriger avant la remise."
fi

exit $status
