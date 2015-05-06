#!/bin/bash

echo "================================"
echo "Patching and repackaging the apk"
echo "================================"

#Check prerequisites
if [ ! -d "00_source/base_src" ]; then
	echo "please prepare the source before executing this script. You can use 02_decompile or 10_prepare_original_apk for that"
	exit -1
fi

if [ ! -f "01_key/de.devmil.parrotzik2supercharge.keystore" ]; then
	echo "please create a Android signing keystore and copy it to 01_key/de.devmil.parrotzik2supercharge.keystore. Make sure that it contains a 'market' alias that gets used for signing the apk."
	exit -1
fi

./04_patch.sh
./05_compile.sh

echo "================================"
echo "Done"
echo "================================"
