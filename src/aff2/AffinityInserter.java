package aff2;

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
import aff2.util.ApkFile;

public class AffinityInserter extends BodyTransformer {

    protected ApkFile apk;
    private  AffinityInserter(ApkFile apk) {this.apk = apk;}
    private boolean instantiated = false;
    
    // Uma abordagem comum no soot é declarar o construtor como privado e ter
    // um metodo estático e publico .v responsável pela instanciação do objeto    
    public static  AffinityInserter v(ApkFile apk) {        
        return new AffinityInserter(apk);
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


    protected  void internalTransform(Body body, String phaseName, Map options) {
        SootClass sClass = body.getMethod().getDeclaringClass();
        SootMethod m = body.getMethod();

        String className = sClass.toString();
        String methodName = m.toString();
        
        if (!className.contains(apk.getPackageName()))
            return; //--> seg fault in class files
        
        // we are only interested in instrumenting the entry point for the app        
        if (!methodName.contains("onCreate"))
            return; //--> seg fault in class files
        

        log("###### Instrumenting " + className + "########");


        /* Inserting variables declaration we will use */
        Local var_this   = insertDeclaration("_r_this", sClass.getType().toString(), body);
        Local var_bundle = insertDeclaration("_r_bundle", "android.os.Bundle", body);
        Local var_intent = insertDeclaration("_r_intent", "android.content.Intent", body);
        Local var_bool   = insertDeclaration("_r_bool", "boolean", body);
        Local var_core   = insertDeclaration("_r_core", "java.lang.String", body);
        Local var_process= insertDeclaration("_r_process", "java.lang.Process", body);
        Local var_runtime= insertDeclaration("_r_runtime", "java.lang.Runtime", body);
        Local var_id     = insertDeclaration("_r_pid", "int", body);
        Local var_id_str = insertDeclaration("_r_pid_str", "java.lang.String", body);        
        Local var_builder= insertDeclaration("_r_str_builder", "java.lang.StringBuilder", body);
        Local var_cmd    = insertDeclaration("_r_command", "java.lang.String", body);
        
               
        
        /*Get units chain and First unit where to insert system call*/
        final PatchingChain units = body.getUnits();
        Iterator u = units.iterator();
        Unit first = (Unit) u.next();
        first = (Unit) u.next();


        
        /* Requiring necessary classes */
        SootClass androidContentIntent = Scene.v().getSootClass("android.content.Intent");
        SootClass androidOsBundle = Scene.v().getSootClass("android.os.bundle");
        SootClass androidOsProcess = Scene.v().getSootClass("android.os.Process");


        /* Creating Instructions to read command line argument*/

        // $_r_this := @this: dvfs.lac.cll.MainActivity;        
        Unit u1 = Jimple.v().newIdentityStmt(var_this, 
            Jimple.v().newThisRef(sClass.getType()));

        // $_r_intent = virtualinvoke $_r_this.<dvfs.lac.cll.MainActivity: android.content.Intent getIntent()>();
        Unit u2 = Jimple.v().newAssignStmt(var_intent,
                Jimple.v().newVirtualInvokeExpr(var_this, 
                sClass.getMethod("android.content.Intent getIntent()").makeRef()));
        
        // $_r_bundle = virtualinvoke $_r_intent.<android.content.Intent: android.os.Bundle getExtras()>();
        Unit u3 = Jimple.v().newAssignStmt(var_bundle,
                Jimple.v().newVirtualInvokeExpr(var_intent, 
                androidContentIntent.getMethod("android.os.Bundle getExtras()").makeRef()));

        // if $_r_bundle == null goto label1;

        // $_r_bool = virtualinvoke $_r_bundle.<android.os.Bundle: boolean containsKey(java.lang.String)>("configuration");        
        Unit u4 = Jimple.v().newAssignStmt(var_bool,
                Jimple.v().newVirtualInvokeExpr(var_bundle, 
                androidOsBundle.getMethod("boolean containsKey(java.lang.String)>(\"configuration\")").makeRef()));

        // if $_r_bool == 0 goto label1;

        // $_r_core = virtualinvoke $r_bundle.<android.os.Bundle: java.lang.String getString(java.lang.String)>("configuration");
        Unit u5 = Jimple.v().newAssignStmt(var_core,
                Jimple.v().newVirtualInvokeExpr(var_bundle, 
                androidOsBundle.getMethod("java.lang.String getString(java.lang.String)>(\"configuration\")").makeRef()));



        /* Creating Instructions to read process id */

        /*$i0 = staticinvoke <android.os.Process: int myPid()>();*/                
        Unit u6 = Jimple.v().newAssignStmt(var_id,
                Jimple.v().newStaticInvokeExpr(
                androidOsProcess.getMethod("int myPid()").makeRef()));


        /*$r4 = staticinvoke <java.lang.String: java.lang.String valueOf(int)>($i0);*/
        Unit u7 = Jimple.v().newAssignStmt(var_id_str, 
                Jimple.v().newStaticInvokeExpr(
                Scene.v().getMethod("java.lang.String valueOf(int)").makeRef(), var_id));


        /*$r5 = new java.lang.StringBuilder;*/
        Unit u8 = Jimple.v().newAssignStmt(var_builder, 
                Jimple.v().newNewExpr(RefType.v("java.lang.StringBuilder")));
        

        /*specialinvoke $r5.<java.lang.StringBuilder: void <init>()>();*/
        Unit u9 = Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(var_builder,
                Scene.v().getMethod("java.lang.StringBuilder void <init>()").makeRef()));
        

        /*virtualinvoke $r5.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>("taskset -p ");*/
        Unit u10 = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(
                var_builder, Scene.v().getMethod("java.lang.StringBuilder append(java.lang.String)").makeRef(),
                StringConstant.v("taskset -pa "))); // -p with tid

        Unit u11 = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(
                var_builder, Scene.v().getMethod("java.lang.StringBuilder append(java.lang.String)").makeRef(),
                var_core));
        
        /*virtualinvoke $r5.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>($r4);*/
        Unit u12 = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(
                var_builder, Scene.v().getMethod("java.lang.StringBuilder append(java.lang.String)").makeRef(),
                var_id_str));
       
        /*$r4 = virtualinvoke $r5.<java.lang.StringBuilder: java.lang.String toString()>();*/
        Unit u13 = Jimple.v().newAssignStmt(var_cmd,
                Jimple.v().newVirtualInvokeExpr(var_builder, 
                Scene.v().getMethod("java.lang.String toString()").makeRef()));
        




        /* Creating Instructions to set core configuration using taskset */

        /*$_var_runtime = staticinvoke <java.lang.Runtime: java.lang.Runtime getRuntime()>();*/
        Unit u14 = Jimple.v().newAssignStmt(var_runtime, 
                Jimple.v().newStaticInvokeExpr(
                Scene.v().getMethod("<java.lang.Runtime: java.lang.Runtime getRuntime()>").makeRef()));

        /*$var_process = virtualinvoke $var_runtime.<java.lang.Runtime: java.lang.Process exec(java.lang.String)>("ls");*/
        Unit u15 = Jimple.v().newAssignStmt(var_process,
                Jimple.v().newVirtualInvokeExpr(var_runtime,
                Scene.v().getMethod("<java.lang.Runtime: java.lang.Process exec(java.lang.String)>").makeRef(),
                var_cmd));

        /*virtualinvoke $r4.<java.lang.Process: int waitFor()>();*/
        Unit u16 = Jimple.v().newInvokeStmt(
                Jimple.v().newVirtualInvokeExpr(var_process, 
                Scene.v().getMethod("<java.lang.Process: int waitFor()").makeRef()));



        /* Inserting created units in unit block */
        units.insertAfter(u16, first);
        units.insertAfter(u15, first);        
        units.insertAfter(u14, first);
        units.insertAfter(u13, first);
        units.insertAfter(u12, first);
        units.insertAfter(u11, first);
        units.insertAfter(u10, first);
        units.insertAfter(u9, first);
        units.insertAfter(u8, first);
        units.insertAfter(u7, first);
        units.insertAfter(u6, first);
        units.insertAfter(u5, first);
        units.insertAfter(u4, first);
        units.insertAfter(u3, first);
        units.insertAfter(u2, first);
        units.insertAfter(u1, first);        
    }
}