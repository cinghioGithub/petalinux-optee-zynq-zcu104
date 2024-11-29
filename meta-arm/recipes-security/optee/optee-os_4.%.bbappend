COMPATIBLE_MACHINE:zynqmp ?= ".*"

PLNX_DEPLOY_DIR ?= "${TOPDIR}/images/linux"

EXTRA_OEMAKE += " CFG_TEE_CORE_LOG_LEVEL=4"
EXTRA_OEMAKE += " CFG_TEE_CORE_DEBUG=y"

do_compile:append() {
	${S}/scripts/gen_tee_bin.py --input ${B}/core/tee.elf --out_tee_raw_bin ${B}/core/tee_raw.bin
}

do_install:append() {
        install -m 644 ${B}/core/tee.elf ${D}${nonarch_base_libdir}/firmware/tee.elf
}

deploy_optee() {
	install -d ${PLNX_DEPLOY_DIR}
	install -m 644 ${DEPLOYDIR}/optee/tee_raw.bin ${PLNX_DEPLOY_DIR}/tee_raw.bin
	install -m 644 ${DEPLOYDIR}/optee/tee.elf ${PLNX_DEPLOY_DIR}/bl32.elf
}

do_deploy[postfuncs] += " deploy_optee"
do_deploy_setscene[postfuncs] += " deploy_optee"

