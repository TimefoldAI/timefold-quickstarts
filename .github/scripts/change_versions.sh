#!/bin/bash

# Expects the following environment variables to be set:
#   $NEW_VERSION             (Example: "1.2.0")

find . -name pom.xml -exec sed -i -E 's/<version.ai.timefold.solver>[^<]+<\/version.ai.timefold.solver>/<version.ai.timefold.solver>'"$NEW_VERSION"'<\/version.ai.timefold.solver>/' {} \;
find . -name build.gradle -exec sed -i -E 's/def timefoldVersion = \"[^\"\\s]+\"/def timefoldVersion = \"'"$NEW_VERSION"'\"/' {} \;
find . -name pom.xml | xargs git add
find . -name build.gradle | xargs git add
git commit -m "chore: switch to version $NEW_VERSION"
