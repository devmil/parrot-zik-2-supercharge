$config = "debug"
$classesDirectory = ".\03_project\dummy_app\build\intermediates\classes\$config"

cd $classesDirectory

# clean up
echo "============================"
echo "Cleaning up"
echo "============================"

$dexFiles = Get-ChildItem ".\de\devmil\parrotzik2supercharge\*.dex"
$smaliFiles = Get-ChildItem ".\out\de\devmil\parrotzik2supercharge\*.smali"

foreach($df in $dexFiles)
{
    Remove-Item $df
}
foreach($sf in $smaliFiles)
{
    Remove-Item $sf
}

# dex and smali
echo "============================"
echo "Dexing"
echo "============================"

$classFiles = Get-ChildItem ".\de\devmil\parrotzik2supercharge\*.class"

foreach($cf in $classFiles)
{
    $fName = $cf.Name
    $relPath = Resolve-Path $cf -Relative
    $relDexFilePath = "de\devmil\parrotzik2supercharge\$fName.dex"
    echo "Dexing $fName"
    dx --dex --output=$relDexFilePath $relPath
	echo "creating smali for $fName"
	java -jar ..\..\..\..\..\..\utils\baksmali-2.0.3.jar $relDexFilePath
}

cd ..\..\..\..\..\..

$smaliDirPath = ".\00_source\base_src\smali\de\devmil\parrotzik2supercharge"
if (-Not (Test-Path $smaliDirPath))
{
    $smaliDir = New-Item $smaliDirPath -ItemType directory
}
$existingSmalis = Get-ChildItem "$smaliDirPath"

foreach($sm in $existingSmalis)
{
    Remove-Item $sm
}

echo "============================"
echo "Copying results"
echo "============================"

$createdSmalisFolder = "$classesDirectory\out\de\devmil\parrotzik2supercharge"

$createdSmalis = Get-ChildItem "$createdSmalisFolder\*.smali"

foreach($csm in $createdSmalis)
{
    echo $csm.Name
    Copy-Item $csm -Destination $smaliDir
    Remove-Item $csm
}