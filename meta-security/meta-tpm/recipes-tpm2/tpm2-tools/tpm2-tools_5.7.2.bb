SUMMARY = "Tools for TPM2."
DESCRIPTION = "tpm2-tools"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://docs/LICENSE;md5=a846608d090aa64494c45fc147cc12e3"
SECTION = "tpm"

DEPENDS = "tpm2-tss openssl curl"

# original 5.7
# SRC_URI = "https://github.com/tpm2-software/${BPN}/releases/download/${PV}/${BPN}-${PV}.tar.gz"
# SRC_URI[sha256sum] = "3810d36b5079256f4f2f7ce552e22213d43b1031c131538df8a2dbc3c570983a"
# UPSTREAM_CHECK_URI = "https://github.com/tpm2-software/${BPN}/releases"

# tpm2-tools torsec 5.7.2
SRC_URI = "https://github.com/torsec/${BPN}/releases/download/${PV}/${BPN}-${PV}.tar.gz"
SRC_URI[sha256sum] = "2d7012a1f4edefb3cfe0a8f8211e8da0b62fd5257845ebc86e5b970d23465908"
UPSTREAM_CHECK_URI = "https://github.com/torsec/${BPN}/releases"

inherit autotools pkgconfig bash-completion
