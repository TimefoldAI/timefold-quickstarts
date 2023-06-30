#!/bin/bash

# Expects the following environment variables to be set:
#   $OLD_VERSION                 (Example: "1.1.0")
#   $NEW_VERSION                 (Example: "1.2.0")

echo "Old version: $OLD_VERSION"
echo "New version: $NEW_VERSION"

# Replaces the old version by the new version.
find . -name pom.xml | xargs sed -i "s/>$OLD_VERSION</>$NEW_VERSION</g"
find . -name build.gradle | xargs sed -i "s/\"$OLD_VERSION\"/\"$NEW_VERSION\"/g"
