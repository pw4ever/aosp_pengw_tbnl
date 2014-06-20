#!/bin/bash - 

INPUT_ROOT=${1:-output}
OUTPUT_ROOT=${OUTPUT_ROOT:-viz}

SCP_CMD=${SCP_CMD:-"scp -P 20022 "}
SCP_TARGET=${SCP_TARGET:-"voidstar.info:/srv/http/dept/tmp/tbnl/"}

latest=$(ls ${INPUT_ROOT}_*.png | perl -wnl -e 'print $1 if /_(\d+)\./' | sort -n -r | head -1)

if [[ -z ${latest} ]]; then
    exit 1
fi

latest=${INPUT_ROOT}_${latest}

for suff in png pdf; do
    touch ${OUTPUT_ROOT}.${suff}
    if ! cmp -s ${latest}.${suff} ${OUTPUT_ROOT}.${suff}; then
        cp ${latest}.${suff} ${OUTPUT_ROOT}.${suff}
        ${SCP_CMD} ${OUTPUT_ROOT}.${suff} ${SCP_TARGET}
    fi
done
