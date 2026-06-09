#!/bin/sh
#
# Gradle start up script for POSIX compatible shells
#

# Attempt to set APP_HOME
SAVED="`pwd`"
cd "`dirname \"$0\"`/" >/dev/null
APP_HOME="`pwd -P`"
cd "$SAVED" >/dev/null

APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`

DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

die() {
    echo
    echo "$*"
    echo
    exit 1
} >&2

warn() {
    echo "$*"
} >&2

# OS specific support (must be 'true' or 'false').
cygwin=false
msys=false
darwin=false
nonstop=false
case "`uname`" in
  CYGWIN* )
    cygwin=true
    ;;
  Darwin* )
    darwin=true
    ;;
  MSYS* | MINGW* )
    msys=true
    ;;
  NONSTOP* )
    nonstop=true
    ;;
esac

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

JAVA_EXE=java
JAVA_HOME_CANDIDATES=(
  "$JAVA_HOME"
  /usr/lib/jvm/java-17-openjdk-amd64
  /usr/lib/jvm/java-17-openjdk
  /usr/lib/jvm/java-17
)

for candidate in "${JAVA_HOME_CANDIDATES[@]}"; do
  if [ -n "$candidate" ] && [ -x "$candidate/bin/java" ]; then
    JAVA_EXE="$candidate/bin/java"
    break
  fi
done

exec "$JAVA_EXE" $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS \
  "-Dorg.gradle.appname=$APP_BASE_NAME" \
  -classpath "$CLASSPATH" \
  org.gradle.wrapper.GradleWrapperMain \
  "$@"
