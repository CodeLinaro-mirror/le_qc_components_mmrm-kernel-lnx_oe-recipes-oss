inherit module deploy

DESCRIPTION = "QTI MMRM driver"
LICENSE = "GPL-2.0-with-Linux-syscall-note"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta-qti-bsp/files/common-licenses/\
GPLv2.0-with-linux-syscall-note;md5=8afb6abdac9a14cb18a0d6c9c151e9b4"


DEFAULT_PREFERENCE = "-1"

do_configure[depends] += "virtual/kernel:do_shared_workdir"

FILESPATH =+ "${WORKSPACE}:"
SRC_URI = "file://vendor/qcom/opensource/mmrm-driver/ \
file://${BASEMACHINE}/mmrm_load.conf"

S = "${WORKDIR}/vendor/qcom/opensource/mmrm-driver/"
 

RPROVIDES:${PN} += "kernel-module-msm-mmrm-${KERNEL_VERSION}"

DEPENDS += "virtual/kernel-toolchain-native"
PROVIDES = "kernel-module-mmrm-kernel"
EXTRA_OEMAKE += "M=${S}"
KERNEL_MODULES = "msm-mmrm"
MAKE_TARGETS = "modules"

KERNEL_CC = "${STAGING_BINDIR_NATIVE}/clang/bin/clang -target ${TARGET_ARCH}${TARGET_VENDOR}-${TARGET_OS}"

do_install() {
    install -d ${D}${base_libdir}/modules/${KERNEL_VERSION}/
    install -d ${D}${sysconfdir}/
    install -m 0755 ${WORKDIR}/${BASEMACHINE}/mmrm_load.conf -D ${D}${sysconfdir}/modules-load.d/mmrm_load.conf
    install -m 0755 ${S}/driver/msm-mmrm.ko -D ${D}${base_libdir}/modules/${KERNEL_VERSION}/msm-mmrm.ko
    install -m 0755 ${B}/Module.symvers -D ${D}${includedir}/kernel-module-mmrm-kernel/Module.symvers
}

FILES:${PN} = "${sysconfdir}/*"
FILES:${PN} += "${base_libdir}/*"
