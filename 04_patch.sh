#!/bin/bash
CONFIG=debug

cd 03_project/dummy_app/build/intermediates/classes/$CONFIG

#clean up
echo "============================"
echo "Cleaning up"
echo "============================"
for f in de/devmil/parrotzik2supercharge/*.dex ; do
	rm $f
done
for f in out/de/devmil/parrotzik2supercharge/*.smali ; do
	rm $f
done

#dex and smali
echo "============================"
echo "Dexing"
echo "============================"
for f in de/devmil/parrotzik2supercharge/*.class ; do
	echo "Dexing $f"
	dx --dex --output=$f.dex $f
	echo "creating smali for $f"
	java -jar ../../../../../../utils/baksmali-2.0.3.jar $f.dex
done

cd ../../../../../..

if [ ! -d 00_source/base_src/smali/de ]; then
	mkdir 00_source/base_src/smali/de
fi

if [ ! -d 00_source/base_src/smali/de/devmil ]; then
	mkdir 00_source/base_src/smali/de/devmil
fi

if [ ! -d 00_source/base_src/smali/de/devmil/parrotzik2supercharge ]; then
	mkdir 00_source/base_src/smali/de/devmil/parrotzik2supercharge
fi

for f in 00_source/base_src/smali/de/devmil/parrotzik2supercharge/* ; do
	rm $f
done

echo "============================"
echo "Copying results"
echo "============================"
for f in 03_project/dummy_app/build/intermediates/classes/$CONFIG/out/de/devmil/parrotzik2supercharge/*.smali ; do
	cp $f 00_source/base_src/smali/de/devmil/parrotzik2supercharge/
	rm $f
done
