#!/bin/bash
if [ ! -f "00_source/base.apk" ]; then
	echo "please copy the Parrot Zik 2 app to 00_source"
	exit -1
fi
if [ -d "00_source/base_src" ]; then
	cp 00_source/base_src/AndroidManifest.xml am.x
	rm -R 00_source/base_src
fi
java -jar utils/apktool.jar d -o 00_source/base_src  00_source/base.apk
if [ -f "am.x" ]; then
	rm 00_source/base_src/AndroidManifest.xml
	mv am.x 00_source/base_src/AndroidManifest.xml
fi