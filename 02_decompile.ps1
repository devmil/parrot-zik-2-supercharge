# check for base.apk existence
$strBaseApkFileName=".\00_source\base.apk"

If (-Not (Test-Path $strBaseApkFileName))
{
    echo "please copy the Parrot Zik 2 app to 00_source"
    exit 1
}

echo "============================"
echo "Removing existing files"
echo "============================"

$strDecompileTargetDirectory=".\00_source\base_src"
if(Test-Path $strDecompileTargetDirectory)
{
    Remove-Item $strDecompileTargetDirectory -Recurse
}

echo "============================"
echo "Decompiling apk"
echo "============================"

java -jar utils\apktool.jar d -o $strDecompileTargetDirectory  $strBaseApkFileName

echo "============================"
echo "Patching Android Manifest"
echo "============================"

$strManifestFile=".\00_source\base_src\AndroidManifest.xml"
$strPatchFile=".\00_source\manifest_win.patch"
.\utils\windows\patch.exe $strManifestFile $strPatchFile