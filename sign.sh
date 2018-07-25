#!/bin/bash

echo "Signing $1 file"

mkdir -p install _tmp
rm -r install _tmp
mkdir -p install _tmp

app=$( basename $1 )

cp $1 _tmp/
cd _tmp

apktool d $app -o out

#cd ${app::-4}
#awk 'NR==1{print; print "    <uses-permission android:name=\"android.permission.WRITE_EXTERNAL_STORAGE\" />"} NR!=1' AndroidManifest.xml > tmp.xml
#mv tmp.xml AndroidManifest.xml
#cd ..

echo "Copiando instrumentation files"
cp -v -r ../libs/binder/corebinder out/smali
cp -v -r ../libs/binder/corebinder out/lib
sleep 1

apktool b out -o toSign-${app}


#keytool -genkey -v -keystore my-release-key.keystore -alias alias_name -keyalg RSA -keysize 2048 -validity 10000
zipalign -v 4 toSign-${app} ../install/$app
echo asdasd | jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore ../key/debug.keystore ../install/$app alias_name

cd ..
rm -r _tmp/

