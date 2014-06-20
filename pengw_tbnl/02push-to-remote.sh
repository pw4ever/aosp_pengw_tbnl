# !!! this file needs to be sourced from lunched-AOSP shell (for printconfig)

SSH_CMD=${SSH_CMD:-ssh -p 20022}
SCP_CMD=${SCP_CMD:-scp -P 20022}

REMOTE=${REMOTE:-voidstar.info}

AOSP_BUILD_ID=${AOSP_BUILD_ID:-$(printconfig 2> /dev/null | perl -wnl -e 'print $1 if /BUILD_ID=(\S+)/' 2> /dev/null)}
REMOTE_PATH_PREFIX=${REMOTE_PATH_PREFIX:-/srv/http/dept/tmp/tbnl}
REMOTE_PATH=${REMOTE_PATH:-${REMOTE_PATH_PREFIX}/${AOSP_BUILD_ID}}

STAGE=${STAGE:-99stage}

${SSH_CMD} ${REMOTE} "mkdir -p ${REMOTE_PATH}"

${SCP_CMD} -r ${STAGE}/* ${REMOTE}:${REMOTE_PATH}/

${SSH_CMD} ${REMOTE} "cd ${REMOTE_PATH_PREFIX}; tar cf ${AOSP_BUILD_ID}.tar ${AOSP_BUILD_ID}"

unset SSH_CMD SCP_CMD
unset REMOTE AOSP_BUILD_ID REMOTE_PATH_PREFIX REMOTE_PATH
unset STAGE STAGE_GUEST STAGE_HOST
