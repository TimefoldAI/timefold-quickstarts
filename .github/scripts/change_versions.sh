#!/bin/bash

# Expects the following environment variables to be set:
#   $CURRENT_VERSION             (Example: "1.1.0")
#   $NEW_VERSION                 (Example: "1.2.0")
#
# The current version is necessary, because at this point, the script has no way of knowing what the current version is.
# It cannot be read from the POM, as the POM would have to be resolved and the upstream SNAPSHOT is not available.

find . -name pom.xml | xargs sed -i "s/$CURRENT_VERSION/$NEW_VERSION/g"
find . -name build.gradle | xargs sed -i "s/$CURRENT_VERSION/$NEW_VERSION/g"
find . -name pom.xml | xargs git add
find . -name build.gradle | xargs git add
git commit -m "chore: switch to version $NEW_VERSION"
