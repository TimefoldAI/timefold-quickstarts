#!/bin/bash

# Change directory to the directory of the script
cd "$(dirname $0)" || exit

cd build
./mvnw -f quickstarts-showcase quarkus:dev -Dstartup-open-browser=true
cd ..
