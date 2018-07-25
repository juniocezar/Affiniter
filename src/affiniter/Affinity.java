package affiniter;

import soot.*;

public class Affinity {

   public static void main (String[] args) {

      AffinityInserter prof = AffinityInserter.v();

      // general + jimple
      PackManager.v().getPack("jtp").add(new Transform("jtp.gotoCounter",
          prof));

      //resolve the PrintStream and System soot-classes
      Scene.v().addBasicClass("android.os.Environment",SootClass.SIGNATURES);
      Scene.v().addBasicClass("android.os.Process",SootClass.SIGNATURES);
      Scene.v().addBasicClass("java.io.FileOutputStream",SootClass.SIGNATURES);
      Scene.v().addBasicClass("corebinder.Binder",SootClass.SIGNATURES);
      Scene.v().addBasicClass("java.lang.String",SootClass.SIGNATURES);
      Scene.v().addBasicClass("java.lang.StringBuilder",SootClass.SIGNATURES);  
      
      
      
      soot.Main.main(args);
   }
}
