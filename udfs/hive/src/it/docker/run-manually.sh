#!/bin/bash
#
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

SCRIPTDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
cd "${SCRIPTDIR}" || exit 1
# Start the Hive installation
docker-compose up -d

sleep 5s

# Figure out what the name of the actual Jar file is.
JARNAME=$( cd ../../../target/ || exit 1;  ls yauaa-hive-*-udf.jar )

# Store the jar file in HDFS
echo "==========================================="
echo "Installing Yauaa UDF on HDFS"
docker exec -t -i hive-server bash  hdfs dfs -mkdir '/udf/'
docker exec -t -i hive-server bash  hdfs dfs -put "/udf-target/${JARNAME}" '/udf/'
docker exec -t -i hive-server bash  hdfs dfs -ls '/udf/'

# Allow for manual commands
echo "==========================================="
echo "Console to try manually. First do 'use testdb;' !!"
echo "When you close this the test setup will be destroyed!!"
docker exec -t -i hive-server hive

# Shut it all down again.
echo "==========================================="
echo "Shutting down the test setup"
docker-compose down
