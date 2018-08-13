package times;

import java.io.*;
import java.util.*;
import java.util.Random;
import soot.*;
import soot.jimple.*;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.ReturnStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StringConstant;
import soot.util.*;
import soot.util.Chain;
import times.util.ApkFile;
import java.util.List;
import java.util.ArrayList;

public class TimesInst extends BodyTransformer {

    protected ApkFile apk;
    private  TimesInst(ApkFile apk) {this.apk = apk;}
    private static TimesInst instance = null;
    private Random r = new Random();



    private Local var_init  ;
    private Local var_end   ;
    private Local var_diff  ;
    private Local var_ddiff ;

    private Local var_text   ;
    private Local var_builder;
    private Local var_ps     ;


    /*
     * Factory that returns same instance for this class
     */
    public static  TimesInst v(ApkFile apk) {
        if (instance == null) {
            instance = new TimesInst(apk);
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

        /*log("### Checking if class: (" + className + ") is an entry point ###");
        if (!className.contains("MainActivity"))
            return;
*/

/*
        // we are only interested in instrumenting the entry point for the app
        log("### Checking if method: (" + methodName + ") is valid ###");
        if (!methodName.contains("onCreate(android.os.Bundle)"))
            return; //--> seg fault in class files*/


        /* Inserting variables declaration we will use */
        var_init  = insertDeclaration("$r_initTime",   "long", body);
        var_end   = insertDeclaration("$r_endTime",    "long", body);
        var_diff  = insertDeclaration("$r_diffTime",   "long", body);
        var_ddiff = insertDeclaration("$r_doubleDiff", "double", body);



        var_text    = insertDeclaration("$r_pid_str", "java.lang.String", body);
        var_builder = insertDeclaration("$r_str_builder", "java.lang.StringBuilder", body);
        var_ps      = insertDeclaration("$r_ps", "java.io.PrintStream", body);



        final PatchingChain units = body.getUnits();
        int methodSize = units.size();

        if (methodSize > 10) {
            // get entry point (single)
            Unit entry = units.getFirst();
            List<Unit> outs = new ArrayList<Unit>();

            // get out points (before any return statement)
            Iterator u = units.snapshotIterator();
            while (stmtIt.hasNext()) {
              Stmt s = (Stmt) stmtIt.next();
              if (s instanceof ReturnStmt) {
                outs.add(s);
              }
              // also check if is a call to System.exit(), for instance
            }

            // insert timer
            insertTimerWithin(body, entry, outs, methonName);
          }




        /*while(u.hasNext()) {
            Unit unit = (Unit) u.next();
            if(unit.toString().contains("invoke") && unit.toString().contains(apk.getPackageName())) {
                insertTimer(body, unit);
            }
        }*/

        // Inserting getter for c.config and system call to taskset
        //insertCLReader(body);


        // Statically setting c.config for methods
    }


    private void insertTimerWithin(Body body, Unit entry, List<Unit> outs, String name) {
        final PatchingChain units = body.getUnits();


        /* Creating Instructions to time method */

        /*$l0 = staticinvoke <android.os.SystemClock: long elapsedRealtimeNanos()>();*/
        Unit u1 = Jimple.v().newAssignStmt(var_init,
                Jimple.v().newStaticInvokeExpr(
                Scene.v().getMethod("<android.os.SystemClock: long elapsedRealtimeNanos()>").makeRef()));


        Unit u2 = Jimple.v().newAssignStmt(var_end,
                Jimple.v().newStaticInvokeExpr(
                Scene.v().getMethod("<android.os.SystemClock: long elapsedRealtimeNanos()>").makeRef()));


        //        $l0 = $l1 - $l0;
        Unit u3 = Jimple.v().newAssignStmt(var_diff, Jimple.v().newSubExpr(var_end, var_init));


        //        $d0 = (double) $l0;
        Unit u4 = Jimple.v().newAssignStmt(var_ddiff, Jimple.v().newCastExpr(var_diff, DoubleType.v()));


        //        $d0 = $d0 / 1.0E9;
        Unit u5 = Jimple.v().newAssignStmt(var_ddiff, Jimple.v().newDivExpr(var_ddiff, DoubleConstant.v(1.0E9)));

        //        $r2 = new java.lang.StringBuilder;
        Unit u6 = Jimple.v().newAssignStmt(var_builder, Jimple.v().newNewExpr(RefType.v("java.lang.StringBuilder")));

        //        specialinvoke $r2.<java.lang.StringBuilder: void <init>()>();
        Unit u7 = Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(var_builder,
                Scene.v().getMethod("<java.lang.StringBuilder: void <init>()>").makeRef()));

        //        virtualinvoke $r2.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>("TIME: MainActivity.onCreate: ");
        Unit u8 = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(var_builder,
                Scene.v().getMethod("<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>").makeRef(),
                StringConstant.v("jTimer - " + name + " : " )));

        //        virtualinvoke $r2.<java.lang.StringBuilder: java.lang.StringBuilder append(double)>($d0);
        Unit u81 = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(var_builder,
                Scene.v().getMethod("<java.lang.StringBuilder: java.lang.StringBuilder append(double)>").makeRef(),
                var_ddiff));

        //        $r3 = virtualinvoke $r2.<java.lang.StringBuilder: java.lang.String toString()>();
        Unit u9 = Jimple.v().newAssignStmt(var_text,
                Jimple.v().newVirtualInvokeExpr(var_builder,
                Scene.v().getMethod("<java.lang.StringBuilder: java.lang.String toString()>").makeRef()));


        //        $r4 = <java.lang.System: java.io.PrintStream out>;
        Unit u10 = Jimple.v().newAssignStmt(var_ps,
                Jimple.v().newStaticFieldRef(
                Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef()));


        //        virtualinvoke $r4.<java.io.PrintStream: void println(java.lang.String)>($r3);
        Unit u11 = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(var_ps,
                Scene.v().getMethod("<java.io.PrintStream: void println(java.lang.String)>").makeRef(),
                var_text));


        /* Inserting created units in unit block */
        units.insertAfter(u11,entry);
        units.insertAfter(u10,entry);
        units.insertAfter(u9, entry);
        units.insertAfter(u81,entry);
        units.insertAfter(u8, entry);
        units.insertAfter(u7, entry);
        units.insertAfter(u6, entry);
        units.insertAfter(u5, entry);
        units.insertAfter(u4, entry);
        units.insertAfter(u3, entry);
        units.insertAfter(u2, entry);

        for (Unit out : outs)
          units.insertBefore(u1, out);

    }




    private void insertTimer(Body body, Unit target) {
        final PatchingChain units = body.getUnits();


        /* Creating Instructions to time method */

        /*$l0 = staticinvoke <android.os.SystemClock: long elapsedRealtimeNanos()>();*/
        Unit u1 = Jimple.v().newAssignStmt(var_init,
                Jimple.v().newStaticInvokeExpr(
                Scene.v().getMethod("<android.os.SystemClock: long elapsedRealtimeNanos()>").makeRef()));


        Unit u2 = Jimple.v().newAssignStmt(var_end,
                Jimple.v().newStaticInvokeExpr(
                Scene.v().getMethod("<android.os.SystemClock: long elapsedRealtimeNanos()>").makeRef()));


        //        $l0 = $l1 - $l0;
        Unit u3 = Jimple.v().newAssignStmt(var_diff, Jimple.v().newSubExpr(var_end, var_init));


        //        $d0 = (double) $l0;
        Unit u4 = Jimple.v().newAssignStmt(var_ddiff, Jimple.v().newCastExpr(var_diff, DoubleType.v()));


        //        $d0 = $d0 / 1.0E9;
        Unit u5 = Jimple.v().newAssignStmt(var_ddiff, Jimple.v().newDivExpr(var_ddiff, DoubleConstant.v(1.0E9)));

        //        $r2 = new java.lang.StringBuilder;
        Unit u6 = Jimple.v().newAssignStmt(var_builder, Jimple.v().newNewExpr(RefType.v("java.lang.StringBuilder")));

        //        specialinvoke $r2.<java.lang.StringBuilder: void <init>()>();
        Unit u7 = Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(var_builder,
                Scene.v().getMethod("<java.lang.StringBuilder: void <init>()>").makeRef()));

        //        virtualinvoke $r2.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>("TIME: MainActivity.onCreate: ");
        Unit u8 = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(var_builder,
                Scene.v().getMethod("<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>").makeRef(),
                StringConstant.v("TIME: " + target.toString() + " : " )));

        //        virtualinvoke $r2.<java.lang.StringBuilder: java.lang.StringBuilder append(double)>($d0);
        Unit u81 = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(var_builder,
                Scene.v().getMethod("<java.lang.StringBuilder: java.lang.StringBuilder append(double)>").makeRef(),
                var_ddiff));

        //        $r3 = virtualinvoke $r2.<java.lang.StringBuilder: java.lang.String toString()>();
        Unit u9 = Jimple.v().newAssignStmt(var_text,
                Jimple.v().newVirtualInvokeExpr(var_builder,
                Scene.v().getMethod("<java.lang.StringBuilder: java.lang.String toString()>").makeRef()));


        //        $r4 = <java.lang.System: java.io.PrintStream out>;
        Unit u10 = Jimple.v().newAssignStmt(var_ps,
                Jimple.v().newStaticFieldRef(
                Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef()));


        //        virtualinvoke $r4.<java.io.PrintStream: void println(java.lang.String)>($r3);
        Unit u11 = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(var_ps,
                Scene.v().getMethod("<java.io.PrintStream: void println(java.lang.String)>").makeRef(),
                var_text));


        /* Inserting created units in unit block */
        units.insertAfter(u11, target);
        units.insertAfter(u10, target);
        units.insertAfter(u9, target);
        units.insertAfter(u81, target);
        units.insertAfter(u8, target);
        units.insertAfter(u7, target);
        units.insertAfter(u6, target);
        units.insertAfter(u5, target);
        units.insertAfter(u4, target);
        units.insertAfter(u3, target);
        units.insertAfter(u2, target);

        units.insertBefore(u1, target);

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
        Local tmp;
        switch(type) {
            case "long":
                tmp = Jimple.v().newLocal(name, LongType.v());
                break;
            case "int":
                tmp = Jimple.v().newLocal(name, IntType.v());
                break;
            case "double":
                tmp = Jimple.v().newLocal(name, DoubleType.v());
                break;
            default:
                tmp = Jimple.v().newLocal(name, RefType.v(type));
                break;
        }

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
