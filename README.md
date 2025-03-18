# Build OP-TEE with fTPM and IMA support, with Petalinux on Xilinx zcu104

This is currently a work in progress.

Tested with Petalinux v2024.2, OP-TEE v4.5.0, and Linux v6.6.40.

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

## Update OP-TEE from ```4.1.0``` to ```4.3.0```

```text
$ git clone https://git.yoctoproject.org/meta-arm

$ rm -rf <project-root>/components/yocto/layers/meta-arm/recipes-security/*

$ cp -r meta-arm/meta-arm/recipes-security/* <project-root>/components/yocto/layers/meta-arm/recipes-security/
```

## Append lines in ```meta-arm/recipes-security/optee``` files:

```text
$ cat meta-arm/recipes-security/optee/optee-os_4.%.bbappend >> <project-root>/components/yocto/layers/meta-arm/recipes-security/optee/optee-os_4.%.bbappend

$ cat meta-arm/recipes-security/optee/optee-examples_4.%.bbappend >> <project-root>/components/yocto/layers/meta-arm/recipes-security/optee/optee-examples_4.%.bbappend

$ cat meta-arm/recipes-security/optee/optee-os-tadevkit_4.%.bbappend >> <project-root>/components/yocto/layers/meta-arm/recipes-security/optee/optee-os-tadevkit_4.%.bbappend

$ cat meta-arm/recipes-security/optee/optee-test_4.%.bbappend >> <project-root>/components/yocto/layers/meta-arm/recipes-security/optee/optee-test_4.%.bbappend
```

## In ```meta-arm/recipes-security/optee/optee-client.inc```

In ```do_install:append()``` change ```${UNPACKDIR}``` in ```${WORKDIR}```

## Append lines in ```meta-arm/recipes-security/optee-ftpm``` files:

```text
$ cat meta-arm/recipes-security/optee-ftpm/optee-ftpm_%.bbappend >> <project-root>/components/yocto/layers/meta-arm/recipes-security/optee-ftpm/optee-ftpm_%.bbappend
```

## Update OP-TEE from ```4.3.0``` to ```4.5.0```

### Components to Update

- **optee-client**
- **optee-examples**
- **optee-os**
- **optee-os-tadevkit**
- **optee-test**

---

## Step-by-Step Instructions

### 1. Update **optee-client**

- **Original File:**  
  `meta-user/recipes-security/optee/optee-client_4.3.0.bb`

- **Actions:**
  1. Modify the `SRCREV` to the 4.5.0 commit:
     ```bitbake
     SRCREV = "6486773583b5983af8250a47cf07eca938e0e422"
     ```
  2. Rename the file to:
     ```
     optee-client_4.5.0.bb
     ```

---

### 2. Update **optee-examples**

- **Original File:**  
  `meta-user/recipes-security/optee/optee-examples_4.3.0.bb`

- **Actions:**
  1. Modify the `SRCREV` to:
     ```bitbake
     SRCREV = "5306d2c7c618bb4a91df17a2d5d79ae4701af4a3"
     ```
  2. Rename the file to:
     ```
     optee-examples_4.5.0.bb
     ```

---

### 3. Update **optee-os**

- **Original File:**  
  `meta-user/recipes-security/optee/optee-os_4.3.0.bb`

- **Actions:**
  1. Modify the `SRCREV` to:
     ```bitbake
     SRCREV = "0919de0f7c79ad35ad3c8ace5f823ad1344b4716"
     ```
  2. **Remove any patches** that were applied for version 4.3.0, as they are no longer needed.
  3. Rename the file to:
     ```
     optee-os_4.5.0.bb
     ```

---

### 4. Update **optee-os-tadevkit**

- **Original File:**  
  `meta-user/recipes-security/optee/optee-os-tadevkit_4.3.0.bb`

- **Actions:**
  1. Modify the first line to require the updated `optee-os` file:
     ```bitbake
     require recipes-security/optee/optee-os_4.5.0.bb
     ```
  2. Rename the file to:
     ```
     optee-os-tadevkit_4.5.0.bb
     ```

---

### 5. Update **optee-test**

- **Original File:**  
  `meta-user/recipes-security/optee/optee-test_4.3.0.bb`

- **Actions:**
  1. Modify the `SRCREV` to:
     ```bitbake
     SRCREV = "a1739a182ebbf0500e54cd313e5591079c36f968"
     ```
  2. Rename the file to:
     ```
     optee-test_4.5.0.bb
     ```

---


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

In addition, navigate to ```Filesystem Packages -> misc -> tpm2``` and enable the following packages:

```text
tpm2-abrmd
tpm2-abrmd-dev
tpm2-pkcs11
tpm2-tools
tpm2-tools-dev
tpm2-tools-dbg
tpm2-tss
tpm2-tss-dbg
tpm2-tss-engine
tpm2-tss-engine-dev
```

## Adding a Patch to Resolve Issues with IMA and fTPM

In addition to updating the OP-TEE components, a patch is required to fix issues with IMA (Integrity Measurement Architecture) and fTPM. Follow these steps to integrate the patch into the kernel.

### 1. Create a Patch

- **Patch File:**  
  `0001-combined-patch.patch`

This patch contains the necessary changes to resolve issues with IMA and fTPM.

### 2. Add the Patch to the Kernel Recipe

- **Target File:**  
  `<project-root>/project-spec/meta-user/recipes-kernel/linux/linux-xlnx/linux-xlnx_%.bbappend`

- **Actions:**
  1. Place the patch `0001-combined-patch.patch` in the following directory:
     ```
     <project-root>/project-spec/meta-user/recipes-kernel/linux/linux-xlnx/
     ```
  2. Modify the `linux-xlnx_%.bbappend` file to include the patch by adding the following line:
     ```bitbake
     SRC_URI += "file://0001-combined-patch.patch \
                 "
     ```

---

## Customizing the IMA Policy

To integrate your custom IMA policy into your PetaLinux project, follow these steps:

### 1. Add the Custom IMA Policy Recipe

- **Directory Structure:**  

`<project-root>/project-spec/meta-user/recipes-security/my-ima-policy/`

- **Files:**  
- `my-ima-policy.bb`  
- `files/my_custom_policy`

Place your custom IMA policy in `files/my_custom_policy` and create the BitBake recipe `my-ima-policy.bb` in the `my-ima-policy` directory.

### 2. Update the PetaLinux BSP Configuration

- **Target File:**  
`<project-root>/project-spec/meta-user/conf/petalinuxbsp.conf`

- **Actions:**
1. Append your custom IMA policy to the image installation:
   ```bitbake
   IMAGE_INSTALL:append = " my-ima-policy"
   ```
2. Enable the IMA feature by appending:
   ```bitbake
   DISTRO_FEATURES:append = " ima"
   ```

### 3. Update the User Root Filesystem Configuration

- **Target File:**  
`<project-root>/project-spec/meta-user/conf/user-rootfsconfig`

- **Action:**
- Add the following line to enable your custom IMA policy:
  ```bitbake
  CONFIG_my-ima-policy
  ```

Then enable the packages:

```text
$ petalinux-config -c rootfs
```
Navigate to ```user packages``` and enable ```my-ima-policy``` .

## Change the boot arguments for loading the rootfs from sd card

```text
$ petalinux-config -c rootfs
```

Navigate to ```DTG Settings -> Kernel Bootargs``` and disable ```generate boot args automatically```.

Then set ```earlycon console=ttyUSB1,115200 root=/dev/mmcblk0p2 rw rootwait ima_policy=tcb ignore_loglevel``` in ```user set kernel bootargs```.

Note: Ensure you verify the actual serial port to which the board is connected. 
In this case, it is USB1, but it may vary depending on your setup. 
Adjust the `console` boot argument accordingly (e.g., `console=ttyUSB0` or `console=ttyUSB2`).

It is IMPORTANT to verify the packaged image name:

```text
$ petalinux-config
```
 
Navigate to `Image Packaging Configuration` and check that `INITRAMFS/INITRD Image name` is set to `petalinux-image-minimal`.

## Build the project:

```text
$ petalinux-build
```

## Package the BOOT.bin image:

Copy the file ```bootgen.bif``` in the project main directory and package the BOOT.bin:

```text
$ petalinux-package boot --bif bootgen.bif --force
```

## Prepare and Flash the SD Card

Before flashing, format the SD card to create two partitions:
- One FAT32 partition for the boot image.
- One ext4 partition for the root filesystem.

### Clear previous files in temporary directories

```text
$ rm -rf sd1/*
$ rm -rf sd2/*
```

### Copy boot images from petalinux-project

```text
$ cp <petalinux-proj>/images/linux/{BOOT.BIN,image.ub,boot.scr} ./sd1
$ cp <petalinux-proj>/images/linux/rootfs.tar.gz ./sd2
```

### Format the SD card partitions (adjust /dev/sdb1 and /dev/sdb2 as needed)

```text
$ sudo mkfs.vfat /dev/sdb1
$ sudo mkfs.ext4 /dev/sdb2
```

### Mount the formatted partitions

```text
$ sudo mount /dev/sdb1 /mnt/boot/
$ sudo mount /dev/sdb2 /mnt/rootfs/
```

### Flash the boot image files and extract the rootfs tarball

```text
$ sudo cp sd1/* /mnt/boot/
$ sudo tar xvfp sd2/rootfs.tar.gz --directory /mnt/rootfs
$ sudo cp sd2/* /mnt/rootfs/
```

### Unmount the partitions

```text
$ sudo umount /mnt/boot
$ sudo umount /mnt/rootfs
```