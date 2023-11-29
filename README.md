# Running the app on Pepper Emulator

This guide provides step-by-step instructions for installing Android Studio and PepperSDK on Ubuntu.

## Prerequisites

In order to run the robot emulator successfully you need **Ubuntu 22.04**

Make sure you have Java Development Kit (JDK) installed on your system. You can install it using the following command:

```bash
sudo apt update
sudo apt install default-jdk
```

### Step 1: Download Android Studio
Download **Android Studio Bumblebee | 2021.1.1 Patch 3** from [the official archive repository](https://developer.android.com/studio/archive?authuser=1).

### Step 2: Extract the Archive
Extract the downloaded archive to a location of your choice. 

### Required libraries for 64-bit machines 
If you are running a 64-bit version of Ubuntu, you need to install some 32-bit libraries with the following command:

```
sudo apt-get install libc6:i386 libncurses5:i386 libstdc++6:i386 lib32z1 libbz2-1.0:i386
```

### Step 3: Start Android Studio
Navigate to the android-studio/bin/ directory, and execute studio.sh.

### Step 4: Install PepperSDK
Inside Android studio go to File > Settings. 
Choose Plugins section from the sidebar.
Find and install PepperSDK.

### Step 5: Install Additional libraries
Inside Android studio go to Tools > PepperSDK > Emulator.
It will prompt you to install packages. Install everything from API 7.
Close the emulator.

### Step 6: Install Quick Emulator & Kernel-based Virtual Machine
- Install wemu-kvl:

```
sudo apt install qemu-kvm
```

- Add yourself to the kvm group.

```
sudo adduser <yourusername> kvm
```

### Step 7: Relink the correct libraries

- Navigate to the API lib folder and run:

```
cd /home/$USER/.local/share/Softbank\ Robotics/RobotSDK/API\ 7/tools/lib
```

- Back up the old library

```
mv libz.so.1 libz.so.1.bak
```

- Relink the System library 

```
ln -s /usr/lib/x86_64-linux-gnu/libz.so libz.so.1
```

### Step 8: Run the app
- Restart Android Studio.
- Close the current project.
- Select **Clone from VCS**.
- Paste the HTTPS link from GaioPepper repository.
- Wait for project sync.
- Run the robot emulator. (If only the tablet or only the robot viewer is opening, close the emulator and run it again).
- Run the app.
