JARPATH := src:.:input/classes:bin:libs/jasmin-2.5.0.jar:libs/polyglot.jar:libs/soot-2.5.1.jar:libs/android.jar:bin:libs/flowdroid/AXMLPrinter2.jar:libs/flowdroid/axml-2.0.jar:/libs/flowdroid/guala.jar:bin:bin/infoflow/axml:libs/flowdroid/axml-2.1.jar
SOOTPATH := .:input/classes:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/rt.jar:libs/android.jar
ANDROIDJARS := libs/
APK ?= app-debug.apk
OUT ?= dex


all: build android

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

build:
	javac -cp $(JARPATH)  ./src/times/TimesInst.java -d bin
	javac -cp $(JARPATH)  ./src/times/Times.java -d bin
	javac -cp $(JARPATH)  ./src/times/util/ApkFile.java -d bin

class:
	clear
	java -cp $(JARPATH) affiniter.Times -cp $(SOOTPATH) -src-prec class -p jb use-original-names  HelloWorld

S:
	clear
	java -cp $(JARPATH) affiniter.Times -cp $(SOOTPATH) -src-prec class -p jb use-original-names $(OUT) $(FILE)

android:
	java -cp $(JARPATH) affiniter.Times -cp $(SOOTPATH) -android-jars jar-libs -allow-phantom-refs -src-prec apk -process-dir $(APK)

a2j:
	java -cp $(JARPATH) soot.Main -cp $(SOOTPATH) -android-jars jar-libs -allow-phantom-refs -src-prec apk -f J -process-dir $(APK)

a2ji:
	java -cp $(JARPATH) times.Times -pp -cp $(SOOTPATH) -pp -android-jars $(ANDROIDJARS) -allow-phantom-refs -src-prec apk -f J -process-dir $(APK)

and:
	java -cp $(JARPATH) affiniter.Times -cp $(SOOTPATH) -android-jars jar-libs -allow-phantom-refs -src-prec apk -process-dir $(A)

apk:
	java -cp $(JARPATH) times.Times -pp -cp $(SOOTPATH) -pp -android-jars $(ANDROIDJARS) -allow-phantom-refs -src-prec apk -f $(OUT) -process-dir $(APK)


limpa:
	rm sootOutput/* install/*

clean:
	@rm -r bin sootOutput 2>/dev/null || true
	@mkdir -p bin sootOutput
