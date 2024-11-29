# Build OP-TEE with Petalinux for Xilinx zcu104

Tested with Petalinux v2024.2 and OP-TEE v4.1.0

Download the BSP file for the zcu104 from [here](https://www.xilinx.com/support/download/index.html/content/xilinx/en/downloadNav/embedded-design-tools.html).

## Create the project:

      $ petalinux-create -t project -s /path/of/bsp/file -n <name-of-the-project>

```-t project``` is deprecated. It is better to use the new argument project:

	$ petalinux-create project -s /path/of/bsp/file -n <name-of-the-project>
 
## Add the preference to use the Xilinx TF-A in the user configuration:

Add in the ```project-spec/meta-user/conf/layer.conf```
	
	PREFERRED_PROVIDER_virtual/arm-trusted-firmware = "arm-trusted-firmware"
	PREFERRED_VERSION_arm-trusted-firmware = "2.8-xilinx-v2023.2+git"
	
## Build the SDK to populate the components/yocto directory:

      $ petalinux-build --sdk

## Append lines to the ``arm-trust-firmware_%.bbappend``:

      $ cat meta-xilinx/meta-xilinx-core/recipes-bsp/arm-trusted-firmware/arm-trusted-firmware_%.bbappend >> components/yocto/layers/meta-xilinx/meta-xilinx-core/recipes-bsp/arm-trust-firmware/arm-trusted-firmware_%.bbappend

## Build the TF-A

	  $ petalinux-build -c arm-trusted-firmware
	
## Append lines in ```optee-os.4.%.bbappend``` file:

	$ cat meta-arm/recipes-security/optee/optee-os_4.%.bbappend >> components/yocto/layers/meta-arm/recipes-security/optee/optee-os_4.%.bbappend
	
## Build ```optee-os```:

	$ petalinux-build -c optee-os
	
## Build the project:

	$ petalinux-build

## Package the BOOT.bin image:

Copy the file ```bootgen.bif``` in the project main directory and package the BOOT.bin:

	$ petalinux-package boot --bif bootgen.bif --force
	
## Prepare the SD card with two partitions:

	1. One FAT32 for the boot image
	2. One ext4 for the rootfs
	
## Flash the SD card:

1. Copy the boot image:
	
	    $ cp images/linux/{BOOT.BIN,boot.scr,image.ub} /path/to/the/sd/boot/partition
	
2. Copy the rootfs:
	
	    $ sudo rm -rf /path/to/sd/rootfs/partition/*
	    $ sudo tar -xvf images/linux/rootfs.tar.gz --directory /path/to/sd/rootfs/partition/
