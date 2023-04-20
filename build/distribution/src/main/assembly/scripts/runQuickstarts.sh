#!/bin/sh

# Change directory to the directory of the script
cd "$(dirname $0)" || exit

jvmOptions="-Xms128m -Xmx512m -Dstartup-open-browser=true"
mainJar="quickstarts/binaries/quarkus-app/quarkus-run.jar"

echo "Usage: ./runQuickstarts.sh"
echo "Notes:"
echo "- Java 11 or higher must be installed. Get the latest OpenJDK from (https://adoptium.net/)."
echo "- For JDK, the environment variable JAVA_HOME should be set to the JDK installation directory"
echo "  For example (Linux): export JAVA_HOME=/usr/lib/jvm/java-openjdk"
echo "  For example (macOS): export JAVA_HOME=/Library/Java/Home"
echo

if [ -f $JAVA_HOME/bin/java ]; then
    echo "Starting quickstarts app with JDK from environment variable JAVA_HOME ($JAVA_HOME)..."
    $JAVA_HOME/bin/java ${jvmOptions} -jar ${mainJar} "$*"
else
    echo "Starting quickstarts app with java from environment variable PATH..."
    java ${jvmOptions} -jar ${mainJar} "$*"
fi

if [ $? != 0 ]; then
    echo
    echo "ERROR Failed running the java command."
    echo "Maybe install the latest OpenJDK from (https://adoptium.net/) and check the environment variable JAVA_HOME ($JAVA_HOME)."
    # Prevent the terminal window to disappear before the user has seen the error message
    read -p "Press [Enter] key to close this window." dummyVar
fi
