package affiniter;

import java.io.*;
import java.util.*;
import soot.*;
import soot.jimple.*;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StringConstant;
import soot.util.*;
import soot.util.Chain;

public class AffinityInserter extends BodyTransformer {

    // Uma abordagem comum no soot é declarar o construtor como privado e ter
    // um metodo estático e publico .v responsável pela instanciação do objeto
    private  AffinityInserter() {}
    public static  AffinityInserter v() {
       return new AffinityInserter();
    }

    private SootClass javaLangProcess;
    private SootClass javaUtilScanner;
    private SootClass androidEnvironment;
    private SootClass androidProcess;
    private SootClass javaIoFile;
    private SootClass javaFOS;
    private SootClass javaLangString;
    private SootClass javaLangSBuild;

    protected  void internalTransform(Body body, String phaseName, Map options) {
        SootClass sClass = body.getMethod().getDeclaringClass();
        SootMethod m = body.getMethod();

        String className = sClass.toString();
        String methodName = m.toString();

        callGlobalConfig(body);
    }






    /*
     * Log Method
     */
    private void log(String str) {
        System.out.println(str);
    }

    /*
     * Method for inserting local declaration in sootMethod
     */
    private Local insertDeclaration(String name, String type, Body body) {
        Local tmp = Jimple.v().newLocal(name, RefType.v(type));
        body.getLocals().add(tmp);
        return tmp;
    }


    private void callGlobalConfig(Body body) {
        SootClass sClass = body.getMethod().getDeclaringClass();
        SootMethod m = body.getMethod();

        String className = sClass.toString();
        String methodName = m.toString();
        
        // if (!className.contains(apk.getPackageName()))
        //     return; //--> seg fault in class files
        
        // bug: must get the real main activity in the apk file
        log("### Checking if class: (" + className + ") is an entry point ###");
        if (!className.contains("MainActivity"))
            return;

        // we are only interested in instrumenting the entry point for the app        
        log("### Checking if method: (" + methodName + ") is valid ###");
        if (!methodName.contains("onCreate(android.os.Bundle)"))
            return; //--> seg fault in class files


        log("###### Instrumenting " + className + "########");

        /* Inserting variables declaration we will use */
        Local var_this   = insertDeclaration("$r_this", sClass.getType().toString(), body);
        Local var_bundle = insertDeclaration("$r_bundle", "android.os.Bundle", body);
        Local var_intent = insertDeclaration("$r_intent", "android.content.Intent", body);
                       
        
        
        /* Requiring necessary classes */
        SootClass androidContentIntent = Scene.v().getSootClass("android.content.Intent");
        SootClass androidOsBundle = Scene.v().getSootClass("android.os.Bundle");
        SootClass libClass = Scene.v().getSootClass("corebinder.Binder");



        /*Calling Library*/

        // $_r_this := @this: dvfs.lac.cll.MainActivity;        
        // we wont create a new instance of this. instead we will find the
        // current one, as one must've been declared
        // if not found, create and initialize
        Iterator locals = body.getLocals().snapshotIterator();

        boolean foundThis = false;

        while(locals.hasNext()) {
            Local l = (Local) locals.next();
            if (l.getType() == sClass.getType()) {
                var_this = l;
                log(">> Updated reference for this var");
                foundThis = true;
                break;
            }
        }

        /*Get units chain and First unit where to insert system call*/
        final PatchingChain units = body.getUnits();
        Unit first = (Unit) units.getFirst();
        Unit iPoint = units.getSuccOf(units.getSuccOf(first));
        


        // $_r_this := @this: package.app.MainActivity;        
        Unit u1 = Jimple.v().newIdentityStmt(var_this, 
            Jimple.v().newThisRef(sClass.getType()));

                
        // $_r_intent = virtualinvoke $_r_this.<android.app.MainActivity: android.content.Intent getIntent()>();
        Unit u2 = Jimple.v().newAssignStmt(var_intent,
                Jimple.v().newVirtualInvokeExpr(var_this, 
                Scene.v().getMethod("<android.app.Activity: android.content.Intent getIntent()>").makeRef()));

        
        // $_r_bundle = virtualinvoke $_r_intent.<android.content.Intent: android.os.Bundle getExtras()>();
        Unit u3 = Jimple.v().newAssignStmt(var_bundle,
                Jimple.v().newVirtualInvokeExpr(var_intent, 
                androidContentIntent.getMethod("android.os.Bundle getExtras()").makeRef()));



        /*$i0 = staticinvoke <corebinder.Binder: void androidGlobalBinder(android.os.Bundle)>();*/                
        Unit u4 = Jimple.v().newInvokeStmt( Jimple.v().newStaticInvokeExpr(
                libClass.getMethod("void androidGlobalBinder(android.os.Bundle)").makeRef(), var_bundle));

        //units.insertAfter(u4, first);
                
        units.insertAfter(u4, iPoint);
        units.insertAfter(u3, iPoint);
        units.insertAfter(u2, iPoint);
        
    }

}
