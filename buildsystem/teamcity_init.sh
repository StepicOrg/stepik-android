#!/bin/bash

SDK_VERSION=`grep compileSdk dependencies.gradle | awk '{ print $3+0}'`
SDK_DIR=$ANDROID_HOME/platforms/android-$SDK_VERSION
TOOLS_VERSION=`grep buildTools dependencies.gradle | cut -d \' -f 2`
TOOLS_DIR=$ANDROID_HOME/build-tools/$TOOLS_VERSION

if [ ! -d "$SDK_DIR" ]; then
    echo "Installing Android SDK version $SDK_VERSION"
    echo y | $ANDROID_HOME/tools/android update sdk --no-ui --all --filter \
    android-$SDK_VERSION
fi

if [ ! -d "$TOOLS_DIR" ]; then
    echo "Installing Android build-tools version $TOOLS_VERSION"
    echo y | $ANDROID_HOME/tools/android update sdk --no-ui --all --filter \
    build-tools-$TOOLS_VERSION
fi
