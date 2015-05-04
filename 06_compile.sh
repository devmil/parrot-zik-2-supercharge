#!/bin/bash
java -jar utils/apktool.jar b -o 07_result/de.devmil.parrotzik2supercharge.apk 00_source/base_src
cp 07_result/de.devmil.parrotzik2supercharge.apk 07_result/de.devmil.parrotzik2supercharge_signed.apk
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore 01_key/de.devmil.parrotzik2supercharge.keystore 07_result/de.devmil.parrotzik2supercharge_signed.apk market
