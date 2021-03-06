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
import java.util.Random;

public class AffinityInserter extends BodyTransformer {

    protected ApkFile apk;
    private  AffinityInserter(ApkFile apk) {this.apk = apk;}    
    private static AffinityInserter instance = null;
    private Random r = new Random();
    

    /*
     * Factory that returns same instance for this class
     */
    public static  AffinityInserter v(ApkFile apk) {    
        if (instance == null) {
            instance = new AffinityInserter(apk);
        }
        
        return instance;
    }


    protected  void internalTransform(Body body, String phaseName, Map options) {
        SootClass sClass = body.getMethod().getDeclaringClass();
        SootMethod m = body.getMethod();

        String className = sClass.toString();
        String methodName = m.toString();
        
        // Simple bypass to avoid analyzing the code of libs.
        // must be removed, since it might leave some important
        // user defined classes aside.        
        if (!className.contains(apk.getPackageName()))
            return;


        // Inserting getter for c.config and system call to taskset
        //insertCLReader(body);


        // Statically setting c.config for methods
        setCoreConfig(body);
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


    /*
     * Method for mapping int notation for cconfig to string hexa values
     */
    private String convertConfig(int b, int l) {
        String command = " ";

        switch (b) {
            case 0: command += "0"; break;
            case 1: command += "8"; break;
            case 2: command += "a"; break;
            case 3: command += "e"; break;
            case 4: command += "f"; break;
        }

        switch (l) {
            case 0: command += "0 "; break;
            case 1: command += "8 "; break;
            case 2: command += "a "; break;
            case 3: command += "e "; break;
            case 4: command += "f "; break;
        }

        if (b == l && b == 0)
            command = " ff ";

        return command;
    }



    /*
     * Method for inserting core binding at the beginning 
     * of the current method of the apk file
     */
    private void setCoreConfig(Body body) {
        SootClass sClass = body.getMethod().getDeclaringClass();
        SootMethod m = body.getMethod();

        String className = sClass.toString();
        String methodName = m.toString();


        // gen random config
        int b = r.nextInt(5);
        int l = r.nextInt(5);
        String coreConfig = convertConfig(b, l);

        log("### Binding the method (" + methodName + ") to configuration: " 
            + coreConfig + "###");
        


        /* Inserting variables declaration we will use */
        Local var_process= insertDeclaration("$r_process", "java.lang.Process", body);
        Local var_runtime= insertDeclaration("$r_runtime", "java.lang.Runtime", body);
        Local var_id     = insertDeclaration("$r_pid", IntType.v().toString(), body);
        Local var_id_str = insertDeclaration("$r_pid_str", "java.lang.String", body);        
        Local var_builder= insertDeclaration("$r_str_builder", "java.lang.StringBuilder", body);
        Local var_cmd    = insertDeclaration("$r_command", "java.lang.String", body);
        
               
        
        /*Get units chain and First unit where to insert system call*/
        final PatchingChain units = body.getUnits();
        Iterator u = units.iterator();
        Unit first = (Unit) u.next();
        first = (Unit) u.next();


        
        /* Requiring necessary classes */
        SootClass androidOsProcess = Scene.v().getSootClass("android.os.Process");


        /* Creating Instructions to read process id */

        /*$i0 = staticinvoke <android.os.Process: int myTid()>();*/                
        Unit u1 = Jimple.v().newAssignStmt(var_id,
                Jimple.v().newStaticInvokeExpr(
                androidOsProcess.getMethod("int myTid()").makeRef()));


        /*$r4 = staticinvoke <java.lang.String: java.lang.String valueOf(int)>($i0);*/
        Unit u2 = Jimple.v().newAssignStmt(var_id_str, 
                Jimple.v().newStaticInvokeExpr(
                Scene.v().getMethod("<java.lang.String: java.lang.String valueOf(int)>").makeRef(),
                var_id));


        /*$r5 = new java.lang.StringBuilder;*/
        Unit u3 = Jimple.v().newAssignStmt(var_builder, 
                Jimple.v().newNewExpr(RefType.v("java.lang.StringBuilder")));
        

        /*specialinvoke $r5.<java.lang.StringBuilder: void <init>()>();*/
        Unit u4 = Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(var_builder,
                Scene.v().getMethod("<java.lang.StringBuilder: void <init>()>").makeRef()));
        

        /*virtualinvoke $r5.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>("taskset -p ");*/
        Unit u5 = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(var_builder, 
                Scene.v().getMethod("<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>").makeRef(),
                StringConstant.v("taskset -p ")));

        Unit u6 = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(var_builder, 
                Scene.v().getMethod("<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>").makeRef(),
                StringConstant.v(coreConfig)));

        Unit u7 = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(var_builder, 
                Scene.v().getMethod("<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>").makeRef(),
                StringConstant.v(" ")));
        
        /*virtualinvoke $r5.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>($r4);*/
        Unit u8 = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(var_builder, 
                Scene.v().getMethod("<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>").makeRef(),
                var_id_str));
       
        /*$r4 = virtualinvoke $r5.<java.lang.StringBuilder: java.lang.String toString()>();*/
        Unit u9 = Jimple.v().newAssignStmt(var_cmd,
                Jimple.v().newVirtualInvokeExpr(var_builder, 
                Scene.v().getMethod("<java.lang.StringBuilder: java.lang.String toString()>").makeRef()));
        




        /* Creating Instructions to set core configuration using taskset */

        /*$_var_runtime = staticinvoke <java.lang.Runtime: java.lang.Runtime getRuntime()>();*/
        Unit u10 = Jimple.v().newAssignStmt(var_runtime, 
                Jimple.v().newStaticInvokeExpr(
                Scene.v().getMethod("<java.lang.Runtime: java.lang.Runtime getRuntime()>").makeRef()));

        /*$var_process = virtualinvoke $var_runtime.<java.lang.Runtime: java.lang.Process exec(java.lang.String)>("ls");*/
        Unit u11 = Jimple.v().newAssignStmt(var_process,
                Jimple.v().newVirtualInvokeExpr(var_runtime,
                Scene.v().getMethod("<java.lang.Runtime: java.lang.Process exec(java.lang.String)>").makeRef(),
                var_cmd));

        /*virtualinvoke $r4.<java.lang.Process: int waitFor()>();*/
        Unit u12 = Jimple.v().newInvokeStmt(
                Jimple.v().newVirtualInvokeExpr(var_process, 
                Scene.v().getMethod("<java.lang.Process: int waitFor()>").makeRef()));



        /* Inserting created units in unit block */
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




    /*
     * Method for inserting a command line reader that
     * searches for the argument configuration. Once it is read
     * This method also inserts a system call to taskset, leading
     * to a binding of app instance -> core configuration 
     */
    private void insertCLReader(Body body) {

        SootClass sClass = body.getMethod().getDeclaringClass();
        SootMethod m = body.getMethod();

        String className = sClass.toString();
        String methodName = m.toString();
        
        if (!className.contains(apk.getPackageName()))
            return; //--> seg fault in class files
        
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
        Local var_bool   = insertDeclaration("$r_bool", BooleanType.v().toString(), body);
        Local var_core   = insertDeclaration("$r_core", "java.lang.String", body);
        Local var_process= insertDeclaration("$r_process", "java.lang.Process", body);
        Local var_runtime= insertDeclaration("$r_runtime", "java.lang.Runtime", body);
        Local var_id     = insertDeclaration("$r_pid", IntType.v().toString(), body);
        Local var_id_str = insertDeclaration("$r_pid_str", "java.lang.String", body);        
        Local var_builder= insertDeclaration("$r_str_builder", "java.lang.StringBuilder", body);
        Local var_cmd    = insertDeclaration("$r_command", "java.lang.String", body);
        
               
        
        /*Get units chain and First unit where to insert system call*/
        final PatchingChain units = body.getUnits();
        Iterator u = units.iterator();
        Unit first = (Unit) u.next();
        first = (Unit) u.next();


        
        /* Requiring necessary classes */
        SootClass androidContentIntent = Scene.v().getSootClass("android.content.Intent");
        SootClass androidOsBundle = Scene.v().getSootClass("android.os.Bundle");
        SootClass androidOsBaseBundle = Scene.v().getSootClass("android.os.BaseBundle");
        SootClass androidOsProcess = Scene.v().getSootClass("android.os.Process");

        /* Creating Instructions to read command line argument*/

        // $_r_this := @this: dvfs.lac.cll.MainActivity;        
        // we wont create a new instance of this. instead we will find the
        // current one, as one must've been declared
        // if not found, create and initialize
        Iterator locals = body.getLocals().snapshotIterator();
        while(locals.hasNext()) {
            Local l = (Local) locals.next();
            if (l.getType() == sClass.getType()) {
                var_this = l;
                log(">> Updated reference for this var");
                break;
            }
        }


        // $_r_this := @this: dvfs.lac.cll.MainActivity;        
        Unit u1 = Jimple.v().newIdentityStmt(var_this, 
            Jimple.v().newThisRef(sClass.getType()));

                
        // $_r_intent = virtualinvoke $_r_this.<dvfs.lac.cll.MainActivity: android.content.Intent getIntent()>();
        Unit u2 = Jimple.v().newAssignStmt(var_intent,
                Jimple.v().newVirtualInvokeExpr(var_this, 
                Scene.v().getMethod("<android.app.Activity: android.content.Intent getIntent()>").makeRef()));


        
        // $_r_bundle = virtualinvoke $_r_intent.<android.content.Intent: android.os.Bundle getExtras()>();
        Unit u3 = Jimple.v().newAssignStmt(var_bundle,
                Jimple.v().newVirtualInvokeExpr(var_intent, 
                Scene.v().getMethod("<android.content.Intent: android.os.Bundle getExtras()>").makeRef()));

        // if $_r_bundle == null goto label1;

        // $_r_bool = virtualinvoke $_r_bundle.<android.os.Bundle: boolean containsKey(java.lang.String)>("configuration");        
        Unit u4 = Jimple.v().newAssignStmt(var_bool,
                Jimple.v().newVirtualInvokeExpr(var_bundle, 
                androidOsBaseBundle.getMethod("boolean containsKey(java.lang.String)").makeRef(), 
                StringConstant.v("configuration")));

        // if $_r_bool == 0 goto label1;

        // $_r_core = virtualinvoke $r_bundle.<android.os.Bundle: java.lang.String getString(java.lang.String)>("configuration");
        Unit u5 = Jimple.v().newAssignStmt(var_core,
                Jimple.v().newVirtualInvokeExpr(var_bundle, 
                androidOsBaseBundle.getMethod("java.lang.String getString(java.lang.String)").makeRef(),
                StringConstant.v("configuration")));



        /* Creating Instructions to read process id */

        /*$i0 = staticinvoke <android.os.Process: int myPid()>();*/                
        Unit u6 = Jimple.v().newAssignStmt(var_id,
                Jimple.v().newStaticInvokeExpr(
                androidOsProcess.getMethod("int myPid()").makeRef()));


        /*$r4 = staticinvoke <java.lang.String: java.lang.String valueOf(int)>($i0);*/
        Unit u7 = Jimple.v().newAssignStmt(var_id_str, 
                Jimple.v().newStaticInvokeExpr(
                Scene.v().getMethod("<java.lang.String: java.lang.String valueOf(int)>").makeRef(),
                var_id));


        /*$r5 = new java.lang.StringBuilder;*/
        Unit u8 = Jimple.v().newAssignStmt(var_builder, 
                Jimple.v().newNewExpr(RefType.v("java.lang.StringBuilder")));
        

        /*specialinvoke $r5.<java.lang.StringBuilder: void <init>()>();*/
        Unit u9 = Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(var_builder,
                Scene.v().getMethod("<java.lang.StringBuilder: void <init>()>").makeRef()));
        

        /*virtualinvoke $r5.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>("taskset -p ");*/
        Unit u10 = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(var_builder, 
                Scene.v().getMethod("<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>").makeRef(),
                StringConstant.v("taskset -pa "))); // -p with tid

        Unit u11 = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(var_builder, 
                Scene.v().getMethod("<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>").makeRef(),
                var_core));

        Unit u11_5 = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(var_builder, 
                Scene.v().getMethod("<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>").makeRef(),
                StringConstant.v(" ")));
        
        /*virtualinvoke $r5.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>($r4);*/
        Unit u12 = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(var_builder, 
                Scene.v().getMethod("<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>").makeRef(),
                var_id_str));
       
        /*$r4 = virtualinvoke $r5.<java.lang.StringBuilder: java.lang.String toString()>();*/
        Unit u13 = Jimple.v().newAssignStmt(var_cmd,
                Jimple.v().newVirtualInvokeExpr(var_builder, 
                Scene.v().getMethod("<java.lang.StringBuilder: java.lang.String toString()>").makeRef()));
        




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
                Scene.v().getMethod("<java.lang.Process: int waitFor()>").makeRef()));



        /* Inserting created units in unit block */
        units.insertAfter(u16, first);
        units.insertAfter(u15, first);        
        units.insertAfter(u14, first);
        units.insertAfter(u13, first);
        units.insertAfter(u12, first);
        units.insertAfter(u11_5, first);
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

    }

}