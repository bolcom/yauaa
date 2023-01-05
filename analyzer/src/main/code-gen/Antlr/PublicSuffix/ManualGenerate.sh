#!/bin/bash
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
SCRIPTDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
TARGETDIR="${SCRIPTDIR}"

INPUT="${SCRIPTDIR}/public_suffix_list.dat"
OUTPUT="${TARGETDIR}/PublicSuffix.g4.fragment"

if [ ! -f "${INPUT}" ]; then
    cd "${SCRIPTDIR}" || exit 1
    curl https://publicsuffix.org/list/public_suffix_list.dat | sed 's@TODO:@TO_DO:@g' > public_suffix_list.dat
fi

if [ "Generate.sh" -ot "${OUTPUT}" ]; then
    if [ "${INPUT}" -ot "${OUTPUT}" ]; then
        echo "Up to date: ${OUTPUT}";
        exit;
    fi
fi

echo "Generating: ${OUTPUT}";

(


echo "// The TLDs extracted from https://publicsuffix.org/list/public_suffix_list.dat on $(date)"
echo -n "fragment TLD : "
grep -F -v '//' "${INPUT}" | grep . | sed 's@.*\.\([a-z]\+\)$@\1@g' | grep -E '^[a-z]+$' | sort -u | \
while read -r suffix
do
  echo -n " '${suffix}' |"
done
echo ';'

echo "//"
echo "//"
echo "//"
echo "// ============================================="
echo "// THIS FILE WAS GENERATED; DO NOT EDIT MANUALLY"
echo "// ============================================="
echo "//"
echo "// Yet Another UserAgent Analyzer"
echo "// Copyright (C) 2013-2023 Niels Basjes"
echo "//"
echo "// Licensed under the Apache License, Version 2.0 (the \"License\");"
echo "// you may not use this file except in compliance with the License."
echo "// You may obtain a copy of the License at"
echo "//"
echo "// https://www.apache.org/licenses/LICENSE-2.0"
echo "//"
echo "// Unless required by applicable law or agreed to in writing, software"
echo "// distributed under the License is distributed on an \"AS IS\" BASIS,"
echo "// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied."
echo "// See the License for the specific language governing permissions and"
echo "// limitations under the License."
echo "//"



) | sed 's@|;@;@g'> "${OUTPUT}"


echo "";
echo "=============================================================";
echo "Now copy/paste the content of ${OUTPUT} into the UserAgent.g4";
echo "=============================================================";
echo "";
echo "";
