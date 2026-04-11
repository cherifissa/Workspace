#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/java-project/src/main/java/com/project/proto"
VERSION="$(protoc --version)"
echo "$VERSION"
case "$VERSION" in
	"libprotoc 29.3") ;;
	*)
		echo "ERROR: protoc 29.3 is required by the project specification." >&2
		exit 1
		;;
esac

protoc --java_out=../../../ --cpp_out=../../../../../../../cpp-project/generated filter.proto

echo "Protobuf files regenerated from $(pwd)"