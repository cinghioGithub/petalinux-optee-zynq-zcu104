SUMMARY = "My Custom IMA Policy"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

SRC_URI = "file://my_custom_policy"

inherit features_check
REQUIRED_DISTRO_FEATURES = "ima"

do_install() {
    install -d ${D}${sysconfdir}/ima
    install -m 0644 ${WORKDIR}/my_custom_policy ${D}${sysconfdir}/ima/ima-policy
}

FILES:${PN} += "${sysconfdir}/ima/ima-policy"
#RDEPENDS:${PN} = "ima-evm-utils"
