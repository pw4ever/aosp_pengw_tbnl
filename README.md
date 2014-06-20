<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**  *generated with [DocToc](http://doctoc.herokuapp.com/)*

- [TBNL: an Android Open Source Project (AOSP) product](#tbnl-an-android-open-source-project-aosp-product)
  - [Dependencies](#dependencies)
  - [Cheatsheet](#cheatsheet)
    - [If you want to use it without the burden of AOSP...](#if-you-want-to-use-it-without-the-burden-of-aosp)
    - [Pick the right version](#pick-the-right-version)
    - [One-time setup](#one-time-setup)
    - [Compile the guest side](#compile-the-guest-side)
    - [Compile the host side](#compile-the-host-side)
    - [Installation](#installation)
    - [Monitoring Android and visualizing traces](#monitoring-android-and-visualizing-traces)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

# TBNL: an Android Open Source Project (AOSP) product

[![see a demo](http://img.youtube.com/vi/tAXsuHNtMdg/0.jpg)](http://youtu.be/tAXsuHNtMdg)

This is an AOSP product. 

For lack of imagination (and also to keep its nature open), it is currently named TBNL (To Be Named Later), inspired by (and in homage to) [Edi Weitz](http://weitz.de/)'s [TBNL](http://weitz.de/tbnl/).

TBNL is composed of 2 components: `tbnl` and `mastermind`, which are the guest side (on Android) and the host side (on your dev machine---Linux recommended), respectively. 

* `tbnl` taps into Android's runtime and sends traces to `mastermind` through [Android Debug Bridge (ADB)](https://developer.android.com/tools/help/adb.html).

* `mastermind` collects traces from an arbitrary number of local/remote online/offline sessions and visualize them and (optionally) forming a feedback-loop through injecting into the online sessions (the injection currently can be done manually---injection heuristics is currently under research and is WIP---12 June 2014).

## Dependencies

Currently, these are tested on Linux, specifically, [Arch Linux](https://www.archlinux.org/) and [Ubuntu](http://www.ubuntu.com/).

* `tbnl`

  * [AOSP](http://source.android.com/source/index.html)

* `mastermind`

  * [Java SE 8](http://openjdk.java.net/projects/jdk8/)

  * [Leiningen](http://leiningen.org/)

  * [Graphviz](http://www.graphviz.org/) for visualization (optional)

## Cheatsheet

### If you want to use it without the burden of AOSP...

* Android 4.3 (Jellybean)---recommended with the [Genymotion emulator](http://www.genymotion.com/)'s [ARM translator](http://forum.xda-developers.com/showthread.php?t=2528952)

```bash
wget http://voidstar.info/tmp/tbnl/JLS36G.tar
tar xf JLS36G.tar
./install-to-guest.sh -s emulator-5554   # replace the arguments with the ones you use with ADB to connect with your Android device/emulator
```

* Android 4.4 (Kitkat)

```bash
wget http://voidstar.info/tmp/tbnl/KVT49L.tar
tar xf KVT49L.tar
./install-to-guest.sh -s emulator-5554   # replace the arguments with the ones you use with ADB to connect with your Android device/emulator
```

### Pick the right version

Instead of using the "master" branch, pick the branch that you are going to use. Since at this time (12 June 2014), the [Genymotion emulator](http://www.genymotion.com/)'s [ARM translator](http://forum.xda-developers.com/showthread.php?t=2528952) works well with [Android 4.3 Jellybean](http://www.androidfilehost.com/?fid=23311191640114013), but not [Android 4.4 Kitkat](http://www.androidfilehost.com/?fid=23311191640114013), I recommend the `android-jb-4.3_r3.1` branch to use with Genymotion.

### One-time setup

```bash
# cd into AOSP source topdir, say, $HOME/project/aosp---the directory outputed by $(gettop) after "source build/envsetup.sh"
git clone https://github.com/pw4ever/aosp_pengw_tbnl device/aosp_pengw_tbnl
```

After this, there will be an entry `aosp_pengw_tbnl-eng` when you do `lunch` in AOSP. Choose and build it:

```bash
lunch aosp_pengw_tbnl-eng
```

Maybe you'd like to check out [aosp-helper][] to help you do this and more (e.g. build emulator (goldfish) kernel/LKMs).

### Compile the guest side

Since `tbnl` leverages AOSP's building infrastructure, make sure you have [`source build/envsetup.sh` and `lunch aosp_pengw_tbnl-eng`](http://source.android.com/source/building-running.html) before proceeding.

```bash
cd $(gettop)/device/aosp_pengw_tbnl/pengw_tbnl/tbnl/
mm
```

### Compile the host side

Again, make sure `Java SE 8` and `Leiningen` are ready. NOTE: Since `AOSP` may requires, say, Oracle JDK 6, so you should arrange that AOSP can see its JDK 6 while you can use JDK 8.

A clean way to do this is to set [`export JAVA_HOME=/opt/java6`](device/aosp_pengw_tbnl/pengw_tbnl/frameworks/base/cmds/tbnl/) (suppose you are using Arch Linux and has installed `jdk6-compat` from AUR---adapt the path to your case, e.g., `/usr/lib/jvm/java-6-oracle/` for Ubuntu) before `source build/envsetup.sh` and `lunch aosp_pengw_tbnl-eng`, and set up JDK 8 to be used otherwise (i.e., by default).

Again, assume you are at AOSP root.

```bash
cd device/aosp_pengw_tbnl/pengw_tbnl/host-side-tools/info.voidstar.android.tbnl.mastermind
lein uberjar
```

### Installation

There are a few helper script to help you do that.

Suppose you have an ADB device "192.168.56.101:5555" (e.g., using Genymotion---use `adb devices` to list this) available.

Make sure you have done `source build/envsetup.sh` and `lunch aosp_pengw_tbnl-eng`

```bash
cd $(gettop)/device/aosp_pengw_tbnl/pengw_tbnl/
./00stage-prepare.sh   # this creates a "staging directory" with name "99stage" and collects `tbnl` and `mastermind` into appropriate places under it
./01stage-install.sh -s 192.168.56.101:5555   # this pushes `tbnl` to the device and set up permissions appripriately
```

### Monitoring Android and visualizing traces

Since we are running `mastermind` for this, this again require JDK 8. Setup PATH appropriately.

```bash
cd device/aosp_pengw_tbnl/pengw_tbnl/99stage/host/
mkfifo adb1
adb shell tbnl monitor all | tee trace1.txt > adb1
./mastermind mastermind.jar --cmd=visualize --monitor-trace=adb1 --output-name-root=trace1 --verbose=true
```

Now, play with the device (open Apps, etc.), and the tbnl trace will be recorded in `trace1.txt` and visualization will be recorded as `trace1_*.[png|pdf]` (with * be the consecutive increasing number representing time-evolving visualization).

Note, `--monitor-trace` can be specified multiple times with different files (including regular files or FIFO) to monitor multiple online/offline local traces. With [Ncat](http://nmap.org/ncat/), you can monitor remote online sessions---this is left as a small exercise for you.

[aosp-helper]: https://github.com/pw4ever/aosp-hacking-helper "AOSP hacking helper"
