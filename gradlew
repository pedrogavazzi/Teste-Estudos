#!/bin/sh

##############################################################################
# Gradle start up script for POSIX shells (simplified wrapper).
#
# Locates this script's directory as APP_HOME, builds the classpath pointing
# at gradle/wrapper/gradle-wrapper.jar, and delegates to GradleWrapperMain.
##############################################################################

APP_HOME=$(cd "$(dirname "$0")" >/dev/null 2>&1 && pwd -P)

CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ -n "$JAVA_HOME" ]; then
  if [ -x "$JAVA_HOME/jre/sh/java" ]; then
    JAVACMD="$JAVA_HOME/jre/sh/java"
  else
    JAVACMD="$JAVA_HOME/bin/java"
  fi
  if [ ! -x "$JAVACMD" ]; then
    echo "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME" >&2
    exit 1
  fi
else
  JAVACMD="java"
  if ! command -v java >/dev/null 2>&1; then
    echo "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH." >&2
    exit 1
  fi
fi

if [ ! -f "$CLASSPATH" ]; then
  echo "ERROR: $CLASSPATH not found." >&2
  echo "Open this project in Android Studio once (it regenerates the wrapper jar automatically)," >&2
  echo "or run: gradle wrapper --gradle-version 8.7" >&2
  exit 1
fi

exec "$JAVACMD" $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS \
  "-Dorg.gradle.appname=$(basename "$0")" \
  -classpath "$CLASSPATH" \
  org.gradle.wrapper.GradleWrapperMain "$@"
