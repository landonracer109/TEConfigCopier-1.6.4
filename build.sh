#!/usr/bin/env bash
# Builds build/TEConfigCopier-<version>.jar. Needs Java 8 and, in tools/ (not in the repo):
#   ecj.jar                         Eclipse Java compiler (targets Java 6 like the rest of 1.6.4)
#   mc-1.6.4-srg.jar, forge-srg.jar Minecraft 1.6.4 and Forge 9.11.1.965 with SRG names
#   CoFHCore-2.0.0.5.jar, ThermalExpansion-3.0.0.6.jar   from the TechIt-ng pack
set -euo pipefail
cd "$(dirname "$0")"
T=tools
SEP=":"; case "$(uname -s)" in MINGW*|MSYS*|CYGWIN*) SEP=";";; esac
VERSION=$(sed -n 's/.*VERSION = "\(.*\)";.*/\1/p' src/main/java/techit/teconfigcopier/TEConfigCopier.java)
rm -rf build && mkdir -p build/classes build/tools
java -jar "$T/ecj.jar" -1.6 -nowarn -encoding UTF-8 \
    -cp "$T/mc-1.6.4-srg.jar${SEP}$T/forge-srg.jar${SEP}$T/CoFHCore-2.0.0.5.jar${SEP}$T/ThermalExpansion-3.0.0.6.jar" \
    -d build/classes $(find src/main/java -name '*.java')
cp src/main/resources/mcmod.info build/classes/
java -jar "$T/ecj.jar" -1.6 -nowarn -d build/tools buildtools/MakeJar.java
java -cp build/tools MakeJar "build/TEConfigCopier-$VERSION.jar" build/classes > /dev/null
echo "built build/TEConfigCopier-$VERSION.jar"
