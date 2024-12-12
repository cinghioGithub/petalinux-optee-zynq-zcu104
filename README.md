# Build OP-TEE with Petalinux for Xilinx zcu104

This is currently a work in progress.

Testing with Petalinux v2024.2 and OP-TEE v4.1.0

Download the BSP file for the zcu104 from [here](https://www.xilinx.com/support/download/index.html/content/xilinx/en/downloadNav/embedded-design-tools.html).

## Create the project:

```text
$ petalinux-create -t project -s /path/of/bsp/file -n <name-of-the-project>
```

```-t project``` is deprecated. It is better to use the new argument project:

```text
$ petalinux-create project -s /path/of/bsp/file -n <name-of-the-project>
```

## Add the preference to use the Xilinx TF-A in the user:

Add in the ```<project-root>/project-spec/meta-user/conf/layer.conf```

```text
PREFERRED_PROVIDER_virtual/arm-trusted-firmware = "arm-trusted-firmware"
PREFERRED_VERSION_arm-trusted-firmware = "2.8-xilinx-v2023.2+git"
```

## Build the SDK to populate the components/yocto directory:

```text
$ petalinux-build --sdk
```

## Chenage the machine name:

```text
$ petalinux-config
```

Navigate to ```Yocto Settings -> Yocto Machine Name``` and change it to ```zynqmp-zcu104```.

## Append lines to the ``arm-trust-firmware_%.bbappend``:

```text
$ cat meta-xilinx/meta-xilinx-core/recipes-bsp/arm-trusted-firmware/arm-trusted-firmware_%.bbappend >> <project-root>/components/yocto/layers/meta-xilinx/meta-xilinx-core/recipes-bsp/arm-trust-firmware/arm-trusted-firmware_%.bbappend
```

## Build the TF-A

```text
$ petalinux-build -c arm-trusted-firmware
```

## Append lines in ```meta-arm/recipes-security/optee``` files:

```text
$ cat meta-arm/recipes-security/optee/optee-os_4.%.bbappend >> <project-root>/components/yocto/layers/meta-arm/recipes-security/optee/optee-os_4.%.bbappend

$ cat meta-arm/recipes-security/optee/optee-examples_4.%.bbappend >> <project-root>/components/yocto/layers/meta-arm/recipes-security/optee/optee-examples_4.%.bbappend

$ cat meta-arm/recipes-security/optee/optee-os-tadevkit_4.%.bbappend >> <project-root>/components/yocto/layers/meta-arm/recipes-security/optee/optee-os-tadevkit_4.%.bbappend

$ cat meta-arm/recipes-security/optee/optee-test_4.%.bbappend >> <project-root>/components/yocto/layers/meta-arm/recipes-security/optee/optee-test_4.%.bbappend
```

## Append lines in ```meta-arm/recipes-security/optee-ftpm``` files:

```text
$ cat meta-arm/recipes-security/optee-ftpm/optee-ftpm_.%.bbappend >> <project-root>/components/yocto/layers/meta-arm/recipes-security/optee-ftpm/optee-ftpm_.%.bbappend
```

## Add device tree node:

Add in the ```<project-root>/project-spec/meta-user/recipes-bsp/device-tree/files/system-user.dtsi```:

```text
firmware {
        optee {
                compatible = "linaro,optee-tz";
                method = "smc";
        };
};
```

## Build OP-TEE components:

```text
$ petalinux-build -c optee-os

$ petalinux-build -c optee-examples

$ petalinux-build -c optee-test

$ petalinux-build -c optee-client
```

## Add kernel configuration for TEE support:

Copy the ```kernel_optee.cfg``` file in ```<project-root>/project-spec/meta-user/recipes-kernel/linux/linux-xlnx```:

```text
$ cp project-spec/meta-user/recipes-kernel/linux/linux-xlnx/kernel_optee.cfg <project-root>/project-spec/meta-user/recipes-kernel/linux/linux-xlnx
```

Add these lines to the ```<project-root>/project-spec/meta-user/recipes-kernel/linux/linux-xlnx_%.bbappend```:

```text
SRC_URI:append = " file://kernel_optee.cfg"
KERNEL_FEATURES:append = " kernel_optee.cfg"
```

## Add OP-TEE components to the final image:

Add this line to the ```<project-root>/project-spec/meta-user/conf/petalinuxbsp.conf```:

```text
IMAGE_INSTALL:append = " optee-examples optee-client optee-test"
```

## Add user packages:

Add these lines in ```<project-root>/project-spec/meta-user/conf/user-rootfsconfig```:

```text
CONFIG_optee-client
CONFIG_optee-examples
```

Then enable the packages:

```text
$ petalinux-config -c rootfs
```
Navigate to ```user packages``` and enable ```optee-client``` and ```optee-examples```.

## Build the project:

```text
$ petalinux-build
```

## Package the BOOT.bin image:

Copy the file ```bootgen.bif``` in the project main directory and package the BOOT.bin:

```text
$ petalinux-package boot --bif bootgen.bif --force
```

## Prepare the SD card with two partitions:

1. One FAT32 for the boot image
2. One ext4 for the rootfs

## Flash the SD card:

1. Copy the boot image:

```text
$ cp images/linux/{BOOT.BIN,boot.scr,image.ub} /path/to/the/sd/boot/partition
```

2. Copy the rootfs:

```text
$ sudo rm -rf /path/to/sd/rootfs/partition/*
$ sudo tar -xvf images/linux/rootfs.tar.gz --directory /path/to/sd/rootfs/partition/
```
