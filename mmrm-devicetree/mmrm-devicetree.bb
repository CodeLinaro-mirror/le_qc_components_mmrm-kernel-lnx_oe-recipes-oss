DESCRIPTION = "QTI MMRM ne wlkl"
LICENSE          = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/${LICENSE};md5=550794465ba0ec5312d6919e203a55f9"

inherit linux-kernel-base deploy

PR = "r0"

FILESPATH   =. "${WORKSPACE}:"
SRC_URI     =  "file://vendor/qcom/opensource/mmrm-devicetree/"

S = "${WORKDIR}/vendor/qcom/opensource/mmrm-devicetree"

do_configure[depends] = "virtual/kernel:do_shared_workdir"
do_compile[cleandirs] += "${WORKDIR}/out/${KERNEL_DEFCONFIG}"

KERNEL_VERSION = "${@get_kernelversion_headers('${STAGING_KERNEL_BUILDDIR}')}"

EXT_MODULES = "${@os.path.relpath("${S}", "${KERNEL_PLATFORM_PATH}")}"

do_configure () {
	:
}

do_compile() {
    cd ${KERNEL_PLATFORM_PATH}
    BUILD_CONFIG=msm-kernel/${KERNEL_CONFIG} \
    EXT_MODULES=${EXT_MODULES} \
    MODULE_OUT=${WORKDIR}/vendor/qcom/opensource/mmrm-devicetree \
    INPLACE_COMPILE=y \
    KERNEL_KIT=${KERNEL_PREBUILT_PATH} \
    OUT_DIR=${WORKDIR}/out/${KERNEL_DEFCONFIG} \
    ./build/build_module.sh
}

do_deploy() {
    install -d ${DEPLOYDIR}/tech_dtbs
    install -m 0644 ${WORKDIR}/vendor/qcom/opensource/mmrm-devicetree/${BASEMACHINE}/*.dtbo \
    ${DEPLOYDIR}/tech_dtbs
}

addtask do_deploy after do_install

FILES:${PN} += "${sysconfdir}/*"
FILES:${PN} += "${nonarch_base_libdir}/modules/${KERNEL_VERSION}/*"
ALLOW_EMPTY:${PN} = "1"
