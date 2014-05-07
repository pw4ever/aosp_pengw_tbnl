#
# Copyright (C) 2014 Wei Peng (write.to.peng.wei@gmail.com)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# adapted from $(gettop)/build/target/product/full.mk

# This is a build configuration for a full-featured build of the
# Open-Source part of the tree. It's geared toward a US-centric
# build quite specifically for the emulator, and might not be
# entirely appropriate to inherit from for on-device configurations.


$(call inherit-product, $(SRC_TARGET_DIR)/product/languages_full.mk)
$(call inherit-product, $(SRC_TARGET_DIR)/product/aosp_base_telephony.mk)
$(call inherit-product, $(SRC_TARGET_DIR)/board/generic/device.mk)

include $(SRC_TARGET_DIR)/product/emulator.mk

# Overrides
# To Be Named Later
PRODUCT_NAME := aosp_pengw_tbnl
PRODUCT_DEVICE := pengw_tbnl
# To Be Branded Later
PRODUCT_BRAND := TBBL
PRODUCT_MODEL := TBNL on ARM Emulator

DEVICE_PACKAGE_OVERLAYS :=

PRODUCT_PACKAGES += \
		    my_hello

PRODUCT_COPY_FILES +=
