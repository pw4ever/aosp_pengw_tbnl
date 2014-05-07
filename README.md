<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**  *generated with [DocToc](http://doctoc.herokuapp.com/)*

- [TBNL: an Android Open Source Project (AOSP) product](#tbnl-an-android-open-source-project-aosp-product)
	- [Cheatsheet](#cheatsheet)
		- [One-time setup](#one-time-setup)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

# TBNL: an Android Open Source Project (AOSP) product

This is an AOSP product. 

For lack of imagination (and also to keep its nature open), it is currently named TBNL (To Be Named Later), inspired by (and in homage to) [Edi Weitz](http://weitz.de/)'s [TBNL](http://weitz.de/tbnl/).

## Cheatsheet

### One-time setup

```bash
# cd into AOSP source topdir, say, $HOME/project/aosp---the directory outputed by $(gettop) after "source build/envsetup.sh"
git clone https://github.com/pw4ever/aosp_pengw_tbnl device/pengw
```

After this, there will be an entry `aosp_pengw_tbnl-eng` when you do `lunch` in AOSP. Choose and build it.

Maybe you'd like to check out [aosp-helper][] to help you build emulator kernel/LKMs, etc..


[aosp-helper]: https://github.com/pw4ever/aosp-hacking-helper "AOSP hacking helper"
