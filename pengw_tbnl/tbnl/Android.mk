# Copyright 2008 The Android Open Source Project
#
LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

# so that .jar will contain everything
LOCAL_DEX_PREOPT := false

LOCAL_SRC_FILES := $(call all-subdir-java-files)
LOCAL_MODULE := tbnl
include $(BUILD_JAVA_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := tbnl
LOCAL_SRC_FILES := tbnl
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_MODULE_TAGS := debug
include $(BUILD_PREBUILT)
