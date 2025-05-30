SUMMARY = "Keylime agent for remote attestation"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://../LICENSE;md5=2b42edef8fa55315f34f2370b4715ca9"

SRC_URI = " file://keylime_agent.service \
            file://keylime-agent.conf \
            file://var-lib-keylime-secure.mount \
            file://keylime_agent \
            file://LICENSE \
            file://setup_keylime.sh \
          "

DEPENDS = " \
    openssl \
    tpm2-tss \
"

inherit systemd

SYSTEMD_SERVICE:${PN} = "keylime_agent.service"

do_install() {
    # Create directories
    install -d ${D}/etc/keylime/
    install -d ${D}/etc/keylime/agent.conf.d
    install -d ${D}/usr/bin
    install -d ${D}${systemd_system_unitdir}

    # Copy configuration file
    install -m 644 ${WORKDIR}/keylime-agent.conf ${D}/etc/keylime/agent.conf

    # Install the keylime agent binary
    install -m 755 ${WORKDIR}/keylime_agent ${D}/usr/bin/keylime_agent

    # Install systemd service and mount unit
    install -m 644 ${WORKDIR}/keylime_agent.service ${D}${systemd_system_unitdir}/keylime_agent.service
    install -m 644 ${WORKDIR}/var-lib-keylime-secure.mount ${D}${systemd_system_unitdir}/var-lib-keylime-secure.mount

    # Install the setup script and make it executable
    install -m 755 ${WORKDIR}/setup_keylime.sh ${D}/usr/bin/setup_keylime.sh
}

FILES:${PN} += " \
    /etc/keylime \
    /usr/bin/keylime_agent \
    /usr/bin/setup_keylime.sh \
    ${systemd_system_unitdir}/keylime_agent.service \
    ${systemd_system_unitdir}/var-lib-keylime-secure.mount \
"

RDEPENDS:${PN} += " \
    openssl \
    libssl \
    libcrypto \
    tpm2-tss \
"



