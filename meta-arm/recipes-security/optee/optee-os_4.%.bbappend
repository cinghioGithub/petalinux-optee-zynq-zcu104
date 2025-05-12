# Include Trusted Services Secure Partitions
require recipes-security/optee/optee-os-ts.inc

# Conditionally include platform specific Trusted Services related OPTEE build parameters
EXTRA_OEMAKE:append:qemuarm64-secureboot = "${@oe.utils.conditional('SP_PATHS', '', '', ' CFG_CORE_HEAP_SIZE=131072 CFG_TEE_BENCHMARK=n CFG_TEE_CORE_LOG_LEVEL=4 CFG_CORE_SEL1_SPMC=y ', d)}"

COMPATIBLE_MACHINE:zynqmp ?= ".*"

PLNX_DEPLOY_DIR ?= "${TOPDIR}/images/linux"

# If you want less verbose output decrese this value
EXTRA_OEMAKE += " CFG_TEE_CORE_LOG_LEVEL=3"
EXTRA_OEMAKE += " CFG_TEE_TA_LOG_LEVEL=3"
MACHINE_FEATURES += " optee-ftpm"

#EXTRA_OEMAKE += " CFG_REE_FS=y"

# to see if it's a heap allocation that fails
#EXTRA_OEMAKE += " CFG_CORE_DUMP_OOM=y"

#enable measured boot ftpm
EXTRA_OEMAKE += " CFG_CORE_TPM_EVENT_LOG=y"
EXTRA_OEMAKE += " MEASURED_BOOT=y"
EXTRA_OEMAKE += " MEASURED_BOOT_FTPM=y"

#EXTRA_OEMAKE += " CFG_DT=y"

EXTRA_OEMAKE += " CFG_TPM_MAX_LOG_SIZE=693"
EXTRA_OEMAKE += " CFG_TPM_LOG_BASE_ADDR=0x79640000"

# Solve a TEE_ERROR_OUT_OF_MEMORY
#EXTRA_OEMAKE += " CFG_TZDRAM_SIZE=0x01000000"
#EXTRA_OEMAKE += " CFG_TEE_RAM_VA_SIZE=0x00400000"

#EXTRA_OEMAKE += " CFG_TZDRAM_SIZE=0x10000000" 
#EXTRA_OEMAKE += " CFG_SHMEM_SIZE=0x10000000"

# Solve a TEE_ERROR_OUT_OF_MEMORY
#EXTRA_OEMAKE += " CFG_TZDRAM_SIZE=0x00500000"

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
