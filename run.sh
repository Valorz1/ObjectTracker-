#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
mkdir -p "$ROOT_DIR/bin"

OPENCV_JAR="${OPENCV_JAR:-}"
if [[ -z "$OPENCV_JAR" ]]; then
  OPENCV_JAR="$(find /usr/share/java /usr/local/share/java /opt/opencv /opt /usr/local /usr -name 'opencv-*.jar' 2>/dev/null | head -n 1)"
fi

if [[ -z "$OPENCV_JAR" || ! -f "$OPENCV_JAR" ]]; then
  echo "OpenCV Java jar not found. Install OpenCV or set OPENCV_JAR to the jar path." >&2
  exit 1
fi

OPENCV_LIB_DIR="${OPENCV_LIB_DIR:-}"
if [[ -z "$OPENCV_LIB_DIR" ]]; then
  OPENCV_LIB_DIR="$(dirname "$(find /usr/lib /usr/local/lib /opt/opencv /opt -name 'libopencv_java*.so*' 2>/dev/null | head -n 1)")"
fi

if [[ -z "$OPENCV_LIB_DIR" ]]; then
  echo "OpenCV native library directory not found. Set OPENCV_LIB_DIR to the directory containing libopencv_java*.so." >&2
  exit 1
fi

javac -cp "$OPENCV_JAR" -d "$ROOT_DIR/bin" $(find "$ROOT_DIR/src" -name '*.java')
java -Djava.library.path="$OPENCV_LIB_DIR" -cp "$ROOT_DIR/bin:$OPENCV_JAR" com.objecttracker.Main "$@"
