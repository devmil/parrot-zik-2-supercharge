#!/bin/bash
echo "============================"
echo "Packaging apk"
echo "============================"
java -jar utils/apktool.jar b -o 06_result/de.devmil.parrotzik2supercharge.apk 00_source/base_src
echo "============================"
echo "Copying apk"
echo "============================"
cp 06_result/de.devmil.parrotzik2supercharge.apk 06_result/de.devmil.parrotzik2supercharge_signed.apk
echo "============================"
echo "Signing apk"
echo "============================"
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore 01_key/de.devmil.parrotzik2supercharge.keystore 06_result/de.devmil.parrotzik2supercharge_signed.apk market
echo "The repackaged apk is ready:"
echo "06_result/de.devmil.parrotzik2supercharge_signed.apk"
