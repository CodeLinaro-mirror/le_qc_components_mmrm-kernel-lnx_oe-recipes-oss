inherit linux-kernel-base deploy

DESCRIPTION = "QTI Video driver"
LICENSE = "GPL-2.0-with-Linux-syscall-note"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-2.0;md5=801f80980d171dd6425610833a22dbe6"

PR = "r0"

DEPENDS = "bc-native rsync-native"

do_configure[depends] += "virtual/kernel:do_shared_workdir"

# TODO: Remove this local definition once available via machine.conf
KERNEL_DEFCONFIG ?= "neo_le-defconfig"
KERNEL_DEFCONFIG_qti-distro-debug ?= "neo_le-debug_defconfig"

FILESPATH =+ "${WORKSPACE}:"
SRC_URI = "file://vendor/qcom/opensource/mmrm-driver/ \
file://${BASEMACHINE}/mmrm_load.conf"

SRC_URI    +=  "file://kernel-5.10/kernel_platform"
SRC_URI    +=  "file://kernel-5.10/out/${KERNEL_DEFCONFIG}"


S = "${WORKDIR}/vendor/qcom/opensource/mmrm-driver/"

EXTRA_OEMAKE += "TARGET_SUPPORT=${BASEMACHINE}"
KERNEL_VERSION = "${@get_kernelversion_file("${STAGING_KERNEL_BUILDDIR}")}"

# Disable parallel make
PARALLEL_MAKE = "-j1"

do_compile() {
    cd ${WORKDIR}/kernel-5.10/kernel_platform  && \
    BUILD_CONFIG=msm-kernel/${KERNEL_CONFIG} \
    EXT_MODULES=../../vendor/qcom/opensource/mmrm-driver/ \
    ROOTDIR=${WORKDIR}/ \
    MODULE_OUT=${WORKDIR}/vendor/qcom/opensource/mmrm-driver \
    OUT_DIR=${WORKDIR}/kernel-5.10/out/${KERNEL_DEFCONFIG} \
    KERNEL_UAPI_HEADERS_DIR=${STAGING_KERNEL_BUILDDIR} \
    ./build/build_module.sh
}

do_install() {
    install -m 0755 ${WORKDIR}/${BASEMACHINE}/mmrm_load.conf -D ${D}${sysconfdir}/modules-load.d/mmrm_load.conf
    install -m 0755 ${WORKDIR}/vendor/qcom/opensource/mmrm-driver/driver/msm-mmrm.ko -D ${D}${base_libdir}/modules/${KERNEL_VERSION}/msm-mmrm.ko
    install -m 0755 ${WORKDIR}/vendor/qcom/opensource/mmrm-driver/Module.symvers -D ${D}${base_libdir}/modules/${KERNEL_VERSION}/Module.symvers
}

do_deploy() {
# Deploy unstripped kernel modules into ${DEPLOYDIR}/kernel_modules for debugging purposes
    install -d ${DEPLOYDIR}/kernel_modules
    for kmod in $(find ${D} -name "*.ko") ; do
        install -m 0644 $kmod ${DEPLOYDIR}/kernel_modules
    done
}

addtask deploy after do_install before do_package

FILES:${PN} += "${base_libdir}/modules/*/msm-mmrm.ko"
