LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

define walk
	$(wildcard $(1)) $(foreach e, $(wildcard $(1)/*), $(call walk, $(e)))
endef

ALLFILES = $(call walk, $(LOCAL_PATH))
FILE_LIST := $(filter %.cpp %.c, $(ALLFILES))

LOCAL_MODULE    := vblock
LOCAL_SRC_FILES := $(FILE_LIST:$(LOCAL_PATH)/%=%)
LOCAL_LDLIBS := -L$(LOCAL_PATH)/$(TARGET_ARCH_ABI) -ldl -lsubstrate

TARGET_NO_UNDEFINED_LDFLAGS :=

include $(BUILD_SHARED_LIBRARY)
