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
    // Uma abordagem comum no soot é declarar o construtor como privado e ter
    // um metodo estático e publico .v responsável pela instanciação do objeto
    private  AffinityInserter(ApkFile apk) {this.apk = apk;}
    
    public static  AffinityInserter v(ApkFile apk) {
       return new AffinityInserter(apk);
    }

    /*
     * Method for inserting local declaration in sootMethod
     *
     */
    private Local insertDeclaration(String name, String type, SootMethod body) {
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
        

        System.out.println("###### Instrumenting " + className + "########");

        

        /* Inserting variables declaration we will use */
        Local var_this   = insertDeclaration("_r_this", sClass.getType(), body)
        Local var_bundle = insertDeclaration("_r_bundle", "android.os.Bundle", body);
        Local var_intent = insertDeclaration("_r_intent", "android.content.Intent", body);
        Local var_bool   = insertDeclaration("_r_bool", "boolean", body);
        Local var_core   = insertDeclaration("_r_core", "java.lang.String", body);
        //Local var_core   = insertDeclaration("", "", body);
        //Local var_core   = insertDeclaration("", "", body);
        //Local var_core   = insertDeclaration("", "", body);

               
        
        /*Get units chain and First unit where to insert system call*/
        final PatchingChain units = body.getUnits();
        Iterator u = units.iterator();
        Unit first = (Unit) u.next();
        first = (Unit) u.next();


        

        /* Requiring necessary classes */
        SootClass androidContentIntent = Scene.v().getSootClass("android.content.Intent");


        /* Creating Instructions read command line argument*/

        // $_r_this := @this: dvfs.lac.cll.MainActivity;        
        Unit u1 = Jimple.v().newIdentityStmt(var_this, 
            Jimple.v().newThisRef(sClass.getType()));

        //$_r_intent = virtualinvoke $_r_this.<dvfs.lac.cll.MainActivity: android.content.Intent getIntent()>();
        Unit u2 = Jimple.v().newAssignStmt(var_intent,
                Jimple.v().newVirtualInvokeExpr(var_this, 
                sClass.getMethod("android.content.Intent getIntent()").makeRef()));
        
        //$_r_bundle = virtualinvoke $_r_intent.<android.content.Intent: android.os.Bundle getExtras()>();
        Unit u3 = Jimple.v().newAssignStmt(var_bundle,
                Jimple.v().newVirtualInvokeExpr(var_intent, 
                androidContentIntent.getMethod("android.os.Bundle getExtras()").makeRef()));

        //
        
        
        
    }
}




to reproduce

public class dvfs.lac.cll.MainActivity extends android.support.v7.app.AppCompatActivity
{

    public void <init>()
    {
        dvfs.lac.cll.MainActivity $r0;

        $r0 := @this: dvfs.lac.cll.MainActivity;

        specialinvoke $r0.<android.support.v7.app.AppCompatActivity: void <init>()>();

        return;
    }

    protected void onCreate(android.os.Bundle)
    {
        dvfs.lac.cll.MainActivity $r0;
        android.os.Bundle $r1;
        java.io.PrintStream $r2;
        android.content.Intent $r3;
        boolean $z0;
        java.lang.String $r4;
        java.lang.StringBuilder $r5;

        $r0 := @this: dvfs.lac.cll.MainActivity;

        $r1 := @parameter0: android.os.Bundle;

        specialinvoke $r0.<android.support.v7.app.AppCompatActivity: void onCreate(android.os.Bundle)>($r1);

        virtualinvoke $r0.<dvfs.lac.cll.MainActivity: void setContentView(int)>(2131296283);

        $r3 = virtualinvoke $r0.<dvfs.lac.cll.MainActivity: android.content.Intent getIntent()>();

        $r1 = virtualinvoke $r3.<android.content.Intent: android.os.Bundle getExtras()>();

        if $r1 == null goto label1;

        $z0 = virtualinvoke $r1.<android.os.Bundle: boolean containsKey(java.lang.String)>("configuration");

        if $z0 == 0 goto label1;

        $r4 = virtualinvoke $r1.<android.os.Bundle: java.lang.String getString(java.lang.String)>("configuration");

        




        $r2 = <java.lang.System: java.io.PrintStream out>;

        $r5 = new java.lang.StringBuilder;

        specialinvoke $r5.<java.lang.StringBuilder: void <init>()>();

        virtualinvoke $r5.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>(">>>>>>> ");

        virtualinvoke $r5.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>($r4);

        $r4 = virtualinvoke $r5.<java.lang.StringBuilder: java.lang.String toString()>();

        virtualinvoke $r2.<java.io.PrintStream: void println(java.lang.String)>($r4);

     label1:
        return;
    }
}
