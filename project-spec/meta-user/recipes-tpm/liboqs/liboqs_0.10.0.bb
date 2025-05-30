SUMMARY = "Post-quantum cryptographic library with OP-TEE support"
DESCRIPTION = "liboqs statically compiled and installed into TA_DEV_KIT_DIR for OP-TEE TA usage"
HOMEPAGE = "https://openquantumsafe.org"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=4b93ef2da47496727a4e8a59f443844e"

SRC_URI = "git://github.com/torsec/liboqs.git;branch=master;protocol=https"
SRCREV = "16c6f8436c3d712ae4e56cb03b68d66e8ad00a8e"

S = "${WORKDIR}/git"
B = "${S}/build"

DEPENDS = "openssl cmake ninja"

inherit pkgconfig cmake

AARCH32_CROSS_COMPILE = "${TARGET_PREFIX}"
SYSROOT = "${STAGING_DIR_TARGET}"
#TA_DEV_KIT_DIR_TARGET = "${STAGING_DIR_TARGET}${includedir}/optee/export-user_ta"

do_configure:prepend() {
    mkdir -p ${B}
    cat > ${S}/toolchain-arm.cmake <<EOF
SET(CMAKE_SYSTEM_NAME Linux)
SET(CMAKE_SYSTEM_PROCESSOR aarch64)
SET(CMAKE_C_COMPILER "${AARCH32_CROSS_COMPILE}gcc")
SET(CMAKE_CXX_COMPILER "${AARCH32_CROSS_COMPILE}g++")
SET(CMAKE_FIND_ROOT_PATH "${SYSROOT}")
SET(CMAKE_FIND_ROOT_PATH_MODE_PROGRAM NEVER)
SET(CMAKE_FIND_ROOT_PATH_MODE_LIBRARY ONLY)
SET(CMAKE_FIND_ROOT_PATH_MODE_INCLUDE ONLY)
EOF
}

EXTRA_OECMAKE = " \
    -DCMAKE_TOOLCHAIN_FILE=${S}/toolchain-arm.cmake \
    -DOQS_PERMIT_UNSUPPORTED_ARCHITECTURE=ON \
    -DOQS_ENABLE_KEM_CLASSIC_MCELIECE=OFF \
    -DOQS_USE_OPENSSL=OFF \
    -DOQS_BUILD_ONLY_LIB=ON \
    -DOQS_ENABLE_CPU_EXTENSIONS=OFF \
    -DOQS_DISABLE_RUNTIME_CPU_FLAGS=ON \
    -DOQS_DIST_BUILD=ON \
    -DCMAKE_SYSROOT=${SYSROOT} \
    -DCMAKE_INSTALL_PREFIX=${prefix} \
"

EXTRA_OECMAKE += "-DBUILD_SHARED_LIBS=OFF -DOQS_BUILD_STATIC=ON"

do_compile() {
    cmake --build ${B} --target oqs
}

do_install() {
    install -d ${D}${includedir}/oqs
    cp -r ${B}/include/oqs/* ${D}${includedir}/oqs/

    install -d ${D}${libdir}
    install -m 0644 ${B}/lib/liboqs.a ${D}${libdir}/
}

FILES_${PN}-dev += "\
    ${includedir}/oqs \
"

FILES_${PN}-staticdev += "\
    ${libdir}/liboqs.a \
"

INSANE_SKIP_${PN} += "staticdev"
INSANE_SKIP_${PN}-dev += "staticdev"


