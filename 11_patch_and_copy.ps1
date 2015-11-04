echo "================================"
echo "Patching and repackaging the apk"
echo "================================"

#Check prerequisites
If (-Not (Test-Path "00_source\base_src"))
{
	echo "please prepare the source before executing this script. You can use 02_decompile or 10_prepare_original_apk for that"
	exit -1
}

If (-Not (Test-Path "01_key/de.devmil.parrotzik2supercharge.keystore"))
{
	echo "please create a Android signing keystore and copy it to 01_key/de.devmil.parrotzik2supercharge.keystore. Make sure that it contains a 'market' alias that gets used for signing the apk."
	exit -1
}

./04_patch.ps1
./05_compile.ps1

echo "================================"
echo "Done"
echo "================================"
