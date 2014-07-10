#!/bin/bash - 

ADB=${ADB:-adb}
STAGE=${STAGE:-99stage}
STAGE_GUEST=${STAGE_GUEST:-${STAGE}/guest}
STAGE_HOST=${STAGE_HOST:-${STAGE}/host}

adb="${ADB} $@"

## guest side

${adb} remount

#${adb} push ${STAGE_GUEST}/tbnl.jar /system/framework/
#${adb} push ${STAGE_GUEST}/tbnl /system/bin/
#${adb} shell chmod 700 /system/bin/tbnl

${adb} push ${STAGE_GUEST}/figurehead.apk /sdcard/
${adb} push ${STAGE_GUEST}/figurehead /system/bin/
${adb} shell chmod 700 /system/bin/figurehead
#${adb} install -r ${STAGE_GUEST}/figurehead.apk

# test
#${adb} shell tbnl
${adb} shell figurehead -h

## host side
