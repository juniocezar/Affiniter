JARPATH := src:.:input/classes:bin:libs/jasmin-2.5.0.jar:libs/polyglot.jar:libs/soot-2.5.1.jar:libs/android.jar:bin:libs/flowdroid/AXMLPrinter2.jar:libs/flowdroid/axml-2.0.jar:/libs/flowdroid/guala.jar:bin:bin/infoflow/axml:libs/flowdroid/axml-2.1.jar
SOOTPATH := .:input/classes:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/rt.jar:libs/android.jar:libs/Binder.jar
ANDROIDJARS := libs/
OUT ?= dex
BUILD_TOOLS := ~/Android/Sdk/build-tools/27.0.1

all: build android

build: build-base lib smali

prebuild:
	javac -cp $(JARPATH)  ./src/infoflow/axml/AXmlTypes.java -d bin
	javac -cp $(JARPATH)  ./src/infoflow/axml/AXmlElement.java -d bin
	javac -cp $(JARPATH)  ./src/infoflow/axml/AXmlNode.java -d bin
	javac -cp $(JARPATH)  ./src/infoflow/axml/AXmlNamespace.java -d bin
	javac -cp $(JARPATH)  ./src/infoflow/axml/AXmlDocument.java -d bin
	javac -cp $(JARPATH)  ./src/infoflow/axml/AXmlAttribute.java -d bin
	javac -cp $(JARPATH)  ./src/infoflow/axml/ApkHandler.java -d bin
	javac -cp $(JARPATH)  ./src/infoflow/axml/AXmlHandler.java -d bin

	javac -cp $(JARPATH)  ./src/infoflow/axml/parsers/AbstractBinaryXMLFileParser.java -d bin
	javac -cp $(JARPATH)  ./src/infoflow/axml/parsers/AXMLPrinter2Parser.java -d bin
	javac -cp $(JARPATH)  ./src/infoflow/axml/parsers/IBinaryXMLFileParser.java -d bin
	javac -cp $(JARPATH)  ./src/infoflow/axml/parsers/AXML20Parser.java -d bin


build-base:
	javac -cp $(JARPATH)  ./src/affiniter/AffinityInserter.java -d bin
	javac -cp $(JARPATH)  ./src/affiniter/Affinity.java -d bin
	javac -cp $(JARPATH)  ./src/affiniter/util/ApkFile.java -d bin

lib:
	javac -cp libs/android.jar src/lib/corebinder/Binder.java -d bin
	cd bin; jar -cf ../libs/Binder.jar corebinder

smali:
	mkdir tmp_smali
	cd bin; $(BUILD_TOOLS)/dx --dex --output=../tmp_smali/classes.dex corebinder/Binder.class
	cd tmp_smali; zip binder.zip classes.dex
	apktool d tmp_smali/binder.zip -o tmp_smali/binder -f
	cp -r tmp_smali/binder/smali/corebinder libs/binder
	rm -r tmp_smali


class:
	clear
	java -cp $(JARPATH) affiniter.Affinity -cp $(SOOTPATH) -src-prec class -p jb use-original-names  Hello

a2j:
	java -cp $(JARPATH) soot.Main -cp $(SOOTPATH) -android-jars jar-libs -allow-phantom-refs -src-prec apk -f J -process-dir $(APK)

a2jinst:
	java -cp $(JARPATH) affiniter.Affinity -pp -cp $(SOOTPATH) -pp -android-jars $(ANDROIDJARS) -allow-phantom-refs -src-prec apk -f J -process-dir $(APK)

apk:
	java -cp $(JARPATH) affiniter.Affinity -cp $(SOOTPATH) -pp -android-jars $(ANDROIDJARS) -allow-phantom-refs -src-prec apk -f $(OUT) -process-dir $(APK)


clear:
	rm sootOutput/* install/*

clean:
	@rm -r bin sootOutput 2>/dev/null || true
	@mkdir -p bin sootOutput
