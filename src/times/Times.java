package times;

import times.util.ApkFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import soot.*;
import soot.jimple.*;
import soot.jimple.Jimple;
import soot.options.Options;
import soot.util.*;
import soot.util.Chain;

public class Times {
   static boolean found = true;



   public static void main (String[] args) {

      ApkFile apk = new ApkFile(args);


      // check number of arguments ?
      TimesInst prof = TimesInst.v(apk);

      // general + jimple
      PackManager.v().getPack("jtp").add(new Transform("jtp.gotoCounter",
          prof));

      //resolve the PrintStream and System soot-classes
      Scene.v().addBasicClass("java.io.PrintStream",SootClass.SIGNATURES);
      Scene.v().addBasicClass("java.lang.System",SootClass.SIGNATURES);
      Scene.v().addBasicClass("java.lang.Process",SootClass.SIGNATURES);
      Scene.v().addBasicClass("java.lang.Runtime",SootClass.SIGNATURES);
      Scene.v().addBasicClass("java.util.Scanner",SootClass.SIGNATURES);
      Scene.v().addBasicClass("java.io.File",SootClass.SIGNATURES);
      Scene.v().addBasicClass("android.os.Environment",SootClass.SIGNATURES);
      Scene.v().addBasicClass("android.os.Process",SootClass.SIGNATURES);
      Scene.v().addBasicClass("java.io.FileOutputStream",SootClass.SIGNATURES);
      Scene.v().addBasicClass("java.nio.charset",SootClass.SIGNATURES);
      Scene.v().addBasicClass("java.lang.String",SootClass.SIGNATURES);
      Scene.v().addBasicClass("java.lang.StringBuilder",SootClass.SIGNATURES);  
      Scene.v().addBasicClass("android.content.Intent",SootClass.SIGNATURES);        
      Scene.v().addBasicClass("android.app.Activity",SootClass.SIGNATURES);        
      Scene.v().addBasicClass("android.content.Context",SootClass.SIGNATURES);              

      Scene.v().addBasicClass("android.os.Bundle",SootClass.SIGNATURES);              
      Scene.v().addBasicClass("android.os.BaseBundle",SootClass.SIGNATURES);              
      Scene.v().addBasicClass("android.content.Intent",SootClass.SIGNATURES);              
      

      soot.Main.main(args);
   }
}
