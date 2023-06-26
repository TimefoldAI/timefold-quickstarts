#!/bin/bash

# Expects the following environment variables to be set:
#   $NEW_VERSION             (Example: "1.2.0")

mvn versions:set-property -Dproperty=version.ai.timefold.solver -DnewVersion=$NEW_VERSION
find . -name build.gradle -exec sed -i -E 's/def optaplannerVersion = \"[^\"\\s]+\"/def optaplannerVersion = \"$NEW_VERSION\"/' {} \;
find . -name pom.xml | xargs git add
find . -name build.gradle | xargs git add
git commit -m "chore: switch to version $NEW_VERSION"
