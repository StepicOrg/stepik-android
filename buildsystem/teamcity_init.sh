#!/bin/bash

TOOLS_VERSION=`grep buildToolsVersion app/build.gradle | cut -d \" -f 2`
TOOLS_DIR=$ANDROID_HOME/build-tools/$TOOLS_VERSION

if [ ! -d "$TOOLS_DIR" ]; then
    echo "Installing Android build-tools version $TOOLS_VERSION"
    echo y | $ANDROID_HOME/tools/android update sdk --no-ui --all --filter \
    build-tools-$TOOLS_VERSION
fi
