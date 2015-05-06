#!/bin/bash

echo "================================"
echo "Preparing the original apk"
echo "================================"


#Check prerequisites
if [ ! -f "00_source/base.apk" ]; then
	echo "please copy the Parrot Zik 2 app to 00_source"
	exit -1
fi

./02_decompile.sh

echo "================================"
echo "Done"
echo "================================"