inherit linux-kernel-base deploy

DESCRIPTION = "QTI MMRM driver"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-2.0-only;md5=801f80980d171dd6425610833a22dbe6"

FILESEXTRAPATHS:prepend := "${WORKSPACE}:"
SRC_URI  =  "file://vendor/qcom/opensource/mmrm-driver/"
SRC_URI +=  "file://${BASEMACHINE}/mmrm_load.conf"
S = "${WORKDIR}/vendor/qcom/opensource/mmrm-driver"
DEPENDS += "virtual/kernel bc-native rsync-native"


KERNEL_VERSION = "${@get_kernelversion_file("${STAGING_KERNEL_BUILDDIR}")}"
EXT_MODULES = "${@os.path.relpath("${S}", "${KERNEL_PLATFORM_PATH}")}"
INTERMEDIATE_KERNEL_PATH = "${WORKDIR}/out/${KERNEL_DEFCONFIG}"

do_configure[noexec] = "1"

do_compile[depends]   += "virtual/kernel:do_shared_workdir"
do_compile[cleandirs] += "${INTERMEDIATE_KERNEL_PATH}"
do_compile() {
    cd ${KERNEL_PLATFORM_PATH}

    BUILD_CONFIG=msm-kernel/${KERNEL_CONFIG} \
    EXT_MODULES=${EXT_MODULES} \
    KERNEL_KIT=${KERNEL_PREBUILT_PATH} \
    OUT_DIR=${INTERMEDIATE_KERNEL_PATH} \
    INPLACE_COMPILE=y \
    MODULE_OUT=${S} \
    ./build/build_module.sh
}

do_install() {
    ## copy *all* the generated KOs to ${D}$/${libdir}/modules/${KERNEL_VERSION}
    ## please notice all the output layout maybe different, please modify as per the actual situation
    install -d ${D}${nonarch_base_libdir}/modules/${KERNEL_VERSION}
    install -m 0644 ${WORKDIR}/${BASEMACHINE}/mmrm_load.conf -D ${D}${sysconfdir}/modules-load.d/mmrm_load.conf
    install -m 0755 ${B}/driver/msm-mmrm.ko -D ${D}${nonarch_base_libdir}/modules/${KERNEL_VERSION}

    ## copy Module.symvers so that other DLKM can make use of symbol of current DLKM
    install -m 0644 ${B}/Module.symvers -D ${D}/${libdir}/modules/${KERNEL_VERSION}/mmrm-kernel/Module.symvers
}
do_deploy() {
# Deploy unstripped kernel modules into ${DEPLOYDIR}/kernel_modules for debugging purposes
    install -d ${DEPLOYDIR}/kernel_modules
    cp -rp ${B}/driver/msm-mmrm.ko ${DEPLOYDIR}/kernel_modules
}

addtask deploy after do_install before do_package

FILES:${PN} += "${base_libdir}/modules/${KERNEL_VERSION}/*"
FILES:${PN} += "${nonarch_base_libdir}/modules/${KERNEL_VERSION}/*"
