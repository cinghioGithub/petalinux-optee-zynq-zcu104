# Build OP-TEE with fTPM support, with Petalinux on Xilinx zcu104

This is currently a work in progress.

Testing with Petalinux v2024.2 and OP-TEE v4.5.0

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

## Change the machine name:

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

## Update OP-TEE from ```4.1.0``` to ```4.5.0```
### Components to Update

- **optee**
- **optee-ftpm**

---

To update OP-TEE and its fTPM component in your PetaLinux project, you need to remove the old recipe directories and replace them with the updated versions from the GitHub repository.

These components are located in:
```
<petalinux-proj>/components/yocto/layers/meta-arm/recipes-security/
```

### Remove old directories
Navigate to the recipes-security folder and delete the existing OP-TEE recipe directories:

```
rm -rf <petalinux-proj>/components/yocto/layers/meta-arm/recipes-security/optee

rm -rf <petalinux-proj>/components/yocto/layers/meta-arm/recipes-security/optee-ftpm
```

Clone or download optee and optee-ftpm updated versions from meta-arm/recipes-security/ 

### Note
In this version, everything required for measured boot is integrated.

## Update device tree

Add in the ```<project-root>/project-spec/meta-user/recipes-bsp/device-tree/files/system-user.dtsi```:

```text
/include/ "system-conf.dtsi"
/ {
    firmware {
        optee {
                compatible = "linaro,optee-tz";
                method = "smc";
        };
    };

    // Reserve the memory region
    reserved-memory {
        #address-cells = <2>;
        #size-cells = <2>;
        ranges;

        tpm_event_log_reserved: memory@70000000 {
            reg = <0x00000000 0x79640000 0x00000000 0x00001000>; /* Base 0x79640000, Size 4KB */
            no-map;
        };
    };
};
```

## Build OP-TEE components:

```text
$ petalinux-build -c optee-os

$ petalinux-build -c optee-examples

$ petalinux-build -c optee-test

$ petalinux-build -c optee-client

$ petalinux-build -c optee-ftpm
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
CONFIG_optee-test
```

Then enable the packages:

```text
$ petalinux-config -c rootfs
```
Navigate to ```user packages``` and enable ```optee-client```, ```optee-examples``` and ```optee-test```.

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

### 1. Add this files

- **Patch File:**  
  `0001-combined-patch.patch`

This patch contains the necessary changes to resolve issues with IMA and fTPM.

- **Ima config file:**
  `ima_hash.cfg`

CONFIG_IMA_DEFAULT_HASH set to sha256

### 2. Add files to the Kernel Recipe

- **Target File:**  
  `project-root/project-spec/meta-user/recipes-kernel/linux/linux-xlnx/linux-xlnx_%.bbappend`

- **Actions:**
  1. Place the two files in the following directory:
     ```
     project-root/project-spec/meta-user/recipes-kernel/linux/linux-xlnx/
     ```
  2. Modify the `linux-xlnx_%.bbappend` file to include files by adding the following line:
     ```bitbake
      SRC_URI += "file://0001-combined-patch.patch \
                  file://ima_hash.cfg \
                  "
     ```

---

## Customizing the IMA Policy

To integrate your custom IMA policy into your PetaLinux project, follow these steps:

### 1. Add the Custom IMA Policy Recipe

- **Directory Structure:**  

`project-root/project-spec/meta-user/recipes-security/my-ima-policy/` 
(first you need to create recipes-security directory)

- **Files:**  
- `my-ima-policy.bb`  
- `files/my_custom_policy`

Place your custom IMA policy in `my-ima-policy/files/my_custom_policy` and create the BitBake recipe `my-ima-policy/my-ima-policy.bb` in the same directory (my-ima-policy).

### 2. Update the PetaLinux BSP Configuration

- **Target File:**  
`project-root/project-spec/meta-user/conf/petalinuxbsp.conf`

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
`project-root/project-spec/meta-user/conf/user-rootfsconfig`

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

## Configure Keylime Agent for Integrity Verification

### 1. Add the Custom IMA Policy Recipe

- **Directory Structure:**  

`project-root/project-spec/meta-user/recipes-app/keylime/` 
(first you need to create recipes-app directory)

- **Files:**  
- `files/keylime_agent`  
- `files/keylime_agent.service`
- `files/keylime-agent.conf`  
- `files/LICENSE`
- `files/setup_keylime.sh`  
- `files/var-lib-keylime-secure.mount`
- `keylime.bb`

Place these files in the same directory (keylime).

### 2. Update the PetaLinux BSP Configuration

- **Target File:**  
`project-root/project-spec/meta-user/conf/petalinuxbsp.conf`

- **Actions:**
1. Append your custom IMA policy to the image installation:
   ```bitbake
   IMAGE_INSTALL:append = " keylime"
   ```

### 3. Update the User Root Filesystem Configuration

- **Target File:**  
`project-root/project-spec/meta-user/conf/user-rootfsconfig`

- **Action:**
- Add the following line to enable keylime agent:
  ```bitbake
  CONFIG_keylime
  ```

Then enable the packages:

```text
$ petalinux-config -c rootfs
```
Navigate to ```user packages``` and enable ```keylime``` .


## Change the boot arguments for loading the rootfs from sd card

```text
$ petalinux-config
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

## Package the BOOT.bin image (MEASURED BOOT ENABLED):
Ensure the `bootgen.bif` file includes the custom `measured_boot_fsbl.elf` and properly loads the OP-TEE binary:

```bif
the_ROM_image:
{
    [bootloader, destination_cpu=a53-0] <petalinux-proj>/measured_boot_fsbl.elf
    [pmufw_image] images/linux/pmufw.elf
    [destination_device=pl] images/linux/system.bit
    [destination_cpu=a53-0, exception_level=el-3, trustzone] images/linux/bl31.elf
    [destination_cpu=a53-0, load=0x100000] images/linux/system.dtb
    [destination_cpu=a53-0, exception_level=el-2] images/linux/u-boot.elf
    [load=0x60000000, startup=0x60000000, exception_level=el-1, trustzone, destination_cpu=a53-0] images/linux/tee_raw.bin
}
```
Use this command to obtain BOOT.bin image
```text
$ petalinux-package boot --bif bootgen.bif --force
```

## Prepare and Flash the SD Card

Before flashing, format the SD card to create two partitions:
- One FAT32 partition for the boot image.
- One ext4 partition for the root filesystem.

### Clear previous files in temporary directories
rm -rf sd1/*
rm -rf sd2/*

### Copy boot images from petalinux-project
cp <petalinux-proj>/images/linux/{BOOT.BIN,image.ub,boot.scr} ./sd1
cp <petalinux-proj>/images/linux/rootfs.tar.gz ./sd2

### Format the SD card partitions (adjust /dev/sdb1 and /dev/sdb2 as needed)
sudo mkfs.vfat /dev/sdb1
sudo mkfs.ext4 /dev/sdb2

### Mount the formatted partitions
sudo mount /dev/sdb1 /mnt/boot/
sudo mount /dev/sdb2 /mnt/rootfs/

### Flash the boot image files and extract the rootfs tarball
sudo cp sd1/* /mnt/boot/
sudo tar xvfp sd2/rootfs.tar.gz --directory /mnt/rootfs
sudo cp sd2/* /mnt/rootfs/

### Unmount the partitions
sudo umount /mnt/boot
sudo umount /mnt/rootfs
