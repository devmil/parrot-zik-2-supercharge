echo "================================"
echo "Preparing the original apk"
echo "================================"


#Check prerequisites
If (-Not (Test-Path "00_source\base.apk"))
{
	echo "please copy the Parrot Zik 2 app to 00_source"
	exit -1
}

./02_decompile.ps1

echo "================================"
echo "Done"
echo "================================"