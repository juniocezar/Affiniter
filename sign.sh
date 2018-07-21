#!/bin/bash

echo "Signing $1 file"

mkdir -p install _tmp
app=$( basename $1 )

cp $1 _tmp/
cd _tmp

apktool d $app

cd ${app::-4}
awk 'NR==1{print; print "    <uses-permission android:name=\"android.permission.WRITE_EXTERNAL_STORAGE\" />"} NR!=1' AndroidManifest.xml > tmp.xml
mv tmp.xml AndroidManifest.xml
cd ..

apktool b ${app::-4} -o toSign-${app}



keytool -genkey -v -keystore my-release-key.keystore -alias alias_name -keyalg RSA -keysize 2048 -validity 10000
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore my-release-key.keystore toSign-${app} alias_name
zipalign -v 4 toSign-${app} ../install/$app

cd ..
rm -r _tmp/

