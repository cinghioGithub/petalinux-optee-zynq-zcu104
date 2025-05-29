require recipes-security/optee/optee-os.inc

DEPENDS += "dtc-native"

FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

# v4.4.0
#SRCREV = "8f645256efc0dc66bd5c118778b0b50c44469ae1"

#v4.5 torsec 
#SRCREV = "2ae999705c0ae94bd929289c13d13215640d62ea"

#v4.5.0 torsec pq
SRCREV = "bafcb251180c5670d36e5fafdb19280d30130cae"

SRC_URI += " \
    file://0003-optee-enable-clang-support.patch \
   "
