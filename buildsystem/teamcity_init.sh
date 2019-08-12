#!/bin/bash

rm -rf /home/buildagent/.gradle/caches/transforms-1
rm -rf /home/buildagent/.gradle/caches/modules-2
rm -rf /home/buildagent/.gradle/caches/build-cache-1

SDK_VERSION=`grep compileSdk dependencies.gradle | awk '{ print $3+0}'`
SDK_DIR=$ANDROID_HOME/platforms/android-$SDK_VERSION
TOOLS_VERSION=`grep buildTools dependencies.gradle | cut -d \' -f 2`
TOOLS_DIR=$ANDROID_HOME/build-tools/$TOOLS_VERSION

if [ ! -d "$SDK_DIR" ]; then
    echo "Installing Android SDK version $SDK_VERSION"
    yes | $ANDROID_HOME/tools/bin/sdkmanager "platforms;android-$SDK_VERSION" --verbose
fi

if [ ! -d "$TOOLS_DIR" ]; then
    echo "Installing Android build-tools version $TOOLS_VERSION"
    yes | $ANDROID_HOME/tools/bin/sdkmanager "build-tools;$TOOLS_VERSION" --verbose
fi
