DESCRIPTION = "QTI MMRM drivers"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-2.0-only;md5=801f80980d171dd6425610833a22dbe6"

inherit linux-kernel-base deploy

PR = "r0"
DEPENDS = "bc-native"

do_configure[depends] += "virtual/kernel:do_shared_workdir"
do_compile[cleandirs] += "${WORKDIR}/out/${KERNEL_DEFCONFIG}"

FILESPATH =. "${WORKSPACE}:"
SRC_URI  = "file://vendor/qcom/opensource/mmrm-driver/"
SRC_URI += "file://${BASEMACHINE}/mmrm_load.conf"

S = "${UNPACKDIR}/vendor/qcom/opensource/mmrm-driver"

EXTRA_OEMAKE += "TARGET_SUPPORT=${BASEMACHINE}"
KERNEL_VERSION = "${@get_kernelversion_file("${STAGING_KERNEL_BUILDDIR}")}"

EXT_MODULES = "${@os.path.relpath("${S}", "${KERNEL_PLATFORM_PATH}")}"

do_compile() {
    cd ${KERNEL_PLATFORM_PATH}
    BUILD_CONFIG=msm-kernel/${KERNEL_CONFIG} \
    EXT_MODULES=${EXT_MODULES} \
    MODULE_OUT=${WORKDIR}/vendor/qcom/opensource/mmrm-driver \
    INPLACE_COMPILE=y \
    KERNEL_KIT=${KERNEL_PREBUILT_PATH} \
    OUT_DIR=${WORKDIR}/out/${KERNEL_DEFCONFIG} \
    KERNEL_UAPI_HEADERS_DIR=${STAGING_KERNEL_BUILDDIR} \
    ./build/build_module.sh
}

do_install() {
    install -m 0644 ${WORKDIR}/${BASEMACHINE}/mmrm_load.conf -D ${D}${sysconfdir}/modules-load.d/mmrm_load.conf
    install -m 0644 ${WORKDIR}/vendor/qcom/opensource/mmrm-driver/driver/msm-mmrm.ko -D ${D}${nonarch_base_libdir}/modules/${KERNEL_VERSION}/msm-mmrm.ko
    install -m 0644 ${WORKDIR}/vendor/qcom/opensource/mmrm-driver/Module.symvers -D ${D}${nonarch_base_libdir}/modules/${KERNEL_VERSION}/Module.symvers
}

do_deploy() {
# Deploy unstripped kernel modules into ${DEPLOYDIR}/kernel_modules for debugging purposes
    install -d ${DEPLOYDIR}/kernel_modules
    for kmod in $(find ${D} -name "*.ko") ; do
        install -m 0644 $kmod ${DEPLOYDIR}/kernel_modules
    done
}

addtask deploy after do_install before do_package

FILES:${PN} += "${nonarch_base_libdir}/modules/${KERNEL_VERSION}/*"
