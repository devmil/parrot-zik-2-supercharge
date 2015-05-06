#!/bin/bash
if [ ! -f "00_source/base.apk" ]; then
	echo "please copy the Parrot Zik 2 app to 00_source"
	exit -1
fi
echo "============================"
echo "Removing existing files"
echo "============================"
if [ -d "00_source/base_src" ]; then
	rm -R 00_source/base_src
fi
echo "============================"
echo "Decompiling apk"
echo "============================"
java -jar utils/apktool.jar d -o 00_source/base_src  00_source/base.apk

echo "============================"
echo "Patching Android Manifest"
echo "============================"
patch 00_source/base_src/AndroidManifest.xml < 00_source/manifest.patch
