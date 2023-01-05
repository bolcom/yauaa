#!/bin/bash
#
# Yet Another UserAgent Analyzer
# Copyright (C) 2013-2023 Niels Basjes
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

VERSION=$1

[ -z "$VERSION" ] && echo "Usage: $0 <logstash version>" && exit 1

#https://wiki.archlinux.org/index.php/Color_Bash_Prompt
# Reset
export Color_Off='\e[0m'      # Text Reset

# High Intensity
export IRed='\e[0;91m'        # Red
export IYellow='\e[0;93m'     # Yellow
export IBlue='\e[0;94m'       # Blue
export IWhite='\e[0;97m'      # White

# Bold High Intensity
export BIRed='\e[1;91m'       # Red
export BIBlue='\e[1;94m'      # Blue

echo -e "${IWhite}[${BIRed}WARN${IWhite}] ${IYellow}/========================================================================\\"
echo -e "${IWhite}[${BIRed}WARN${IWhite}] ${IYellow}|                        ${BIRed}Running nasty workaround !${IYellow}                      |"
echo -e "${IWhite}[${BIRed}WARN${IWhite}] ${IYellow}| Waiting for https://github.com/elastic/logstash/issues/11002           |"
echo -e "${IWhite}[${BIRed}WARN${IWhite}] ${IYellow}|     \"${BIBlue}Publish Logstash Java dependencies to Maven central${IYellow}\"              |"
echo -e "${IWhite}[${BIRed}WARN${IWhite}] ${IYellow}| Downloading full logstash distribution to get the logstash-core.jar.   |"
echo -e "${IWhite}[${BIRed}WARN${IWhite}] ${IYellow}\\========================================================================/"
echo -e "${Color_Off}"


[ -f "${HOME}/.m2/repository/org/logstash/logstash-core/${VERSION}/logstash-core-${VERSION}.jar" ] && echo -e "${IYellow}Logstash ${VERSION} was already downloaded${Color_Off}" && exit 0;

cd /tmp || exit 1

echo -e "${IYellow}Logstash ${VERSION}: Downloading${Color_Off}"

curl "https://artifacts.elastic.co/downloads/logstash/logstash-${VERSION}-linux-x86_64.tar.gz" | tar xzf - --to-stdout "logstash-${VERSION}/logstash-core/lib/jars/logstash-core.jar" > logstash-core.jar

[ -s logstash-core.jar ] || ( echo -e "${IWhite}[${BIRed}FATAL${IWhite}] ${BIRed}Downloaded file is 0 bytes in size !!${Color_Off}" && exit 1 )

echo -e "${IYellow}Logstash ${VERSION}: Installing${Color_Off}"

mvn install:install-file        \
     -DgroupId=org.logstash     \
     -DartifactId=logstash-core \
     -Dpackaging=jar            \
     -Dversion="${VERSION}"     \
     -Dfile=logstash-core.jar

echo -e "${IYellow}Logstash ${VERSION}: Cleanup${Color_Off}"

rm -rf logstash-core.jar

echo -e "${IYellow}Logstash ${VERSION}: Done${Color_Off}"
