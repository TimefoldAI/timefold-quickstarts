#!/bin/bash

# Expects the following environment variables to be set:
#   $NEW_VERSION                 (Example: "1.2.0")

# Finds all pom.xml files, identifies in them the property tag that defines solver version, and reads its value.
DETECTED_VERSION=$(find . -name pom.xml -exec grep "<version.ai.timefold.solver>" {} \;|tail -n 1|cut -d\> -f1 --complement|cut -d\< -f1)
echo "Current version: $DETECTED_VERSION"
echo "    New version: $NEW_VERSION"

# Replaces the detected version by the new version.
find . -name pom.xml | xargs sed -i "s/>$DETECTED_VERSION</>$NEW_VERSION</g"
find . -name build.gradle | xargs sed -i "s/\"$DETECTED_VERSION\"/\"$NEW_VERSION\"/g"
find . -name pom.xml | xargs git add
find . -name build.gradle | xargs git add
git commit -m "chore: switch to version $NEW_VERSION"
