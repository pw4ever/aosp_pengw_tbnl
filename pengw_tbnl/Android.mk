LOCAL_PATH := $(call mydir)
include $(CLEAR_VARS)

ifneq ($(filter pengw_tbnl,$(TARGET_DEVICE)),)
    include $(call all-makefiles-under,$(LOCAL_PATH))
endif
