#!/bin/bash

SDK_VERSION=`grep compileSdkVersion app/build.gradle | awk '{ print $2 }'`
SDK_DIR=$ANDROID_HOME/platforms/android-$SDK_VERSION
TOOLS_VERSION=`grep buildToolsVersion app/build.gradle | cut -d \" -f 2`
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
