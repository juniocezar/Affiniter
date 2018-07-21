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
import affiniter.util.ApkFile;

public class AffinityInserter extends BodyTransformer {

   protected ApkFile apk;
    // Uma abordagem comum no soot é declarar o construtor como privado e ter
    // um metodo estático e publico .v responsável pela instanciação do objeto
    private  AffinityInserter(ApkFile apk) {this.apk = apk;}
    public static  AffinityInserter v(ApkFile apk) {
       return new AffinityInserter(apk);
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
        
        if (!className.contains(apk.getPackageName()))
            return; //--> seg fault in class files
        
        System.out.println("###### Too See " + className + "########");
        
        if (!methodName.contains("onCreate"))
            return; //--> seg fault in class files
        

        System.out.println("###### Instrumenting " + className + "########");

        /* Inserting variables declaration we will use */
        /*Files*/
        Local dvfsFile_0 = Jimple.v().newLocal("$__dvfsFILE_0", RefType.v("java.io.File"));
        Local dvfsFile_1 = Jimple.v().newLocal("$__dvfsFILE_1", RefType.v("java.io.File"));
        body.getLocals().add(dvfsFile_0);
        body.getLocals().add(dvfsFile_1);

        /*Runtime*/
        Local dvfsRuntime_0 = Jimple.v().newLocal("$__dvfsRuntime_0", RefType.v("java.lang.Runtime"));
        body.getLocals().add(dvfsRuntime_0);

        /*Process*/
        Local dvfsProcess_0 = Jimple.v().newLocal("$__dvfsProcess_0", RefType.v("java.lang.Runtime"));
        body.getLocals().add(dvfsProcess_0);

        /*Scanner*/
        Local dvfsScan_0 = Jimple.v().newLocal("$__dvfsScan_1", RefType.v("java.util.Scanner"));
        body.getLocals().add(dvfsScan_0);

        /*InputStream*/
        Local dvfsIS_0 = Jimple.v().newLocal("$__dvfsIS_1", RefType.v("java.io.InputStream"));
        body.getLocals().add(dvfsIS_0);

        /*String*/
        Local dvfsString_0 = Jimple.v().newLocal("$__dvfsString1", RefType.v("java.lang.String"));
        body.getLocals().add(dvfsString_0);
        Local dvfsString_1 = Jimple.v().newLocal("$__dvfsString1", RefType.v("java.lang.String"));
        body.getLocals().add(dvfsString_1);

        /*FileOutputStream*/
        Local dvfsFOS_0 = Jimple.v().newLocal("$__dvfsFOS_1", RefType.v("java.io.FileOutputStream"));
        body.getLocals().add(dvfsFOS_0);

        Local byte_0 = Jimple.v().newLocal("$__dvfsByte", ArrayType.v(RefType.v("byte"), 1));
        body.getLocals().add(byte_0);
        
        Local id_0 = Jimple.v().newLocal("$__dvfsMyId", RefType.v("int"));
        body.getLocals().add(id_0);
        
               
 
        
        /*Get units chain and First unit where to insert system call*/
        final PatchingChain units = body.getUnits();
        Iterator u = units.iterator();
        Unit first = (Unit) u.next();
        first = (Unit) u.next();


        

        /* Creating Instructions to make system call*/
        
        
        /*$i0 = staticinvoke <android.os.Process: int myPid()>();*/
        Local dvfs_id = Jimple.v().newLocal("$__dvfsID", RefType.v("int"));
        body.getLocals().add(dvfs_id);
        
        androidProcess = Scene.v().getSootClass("android.os.Process");
        AssignStmt _myId = Jimple.v().newAssignStmt(dvfs_id, Jimple.v().newStaticInvokeExpr(
        		androidProcess.getMethod("int myPid()").makeRef()));


        /*$r4 = staticinvoke <java.lang.String: java.lang.String valueOf(int)>($i0);*/
        Local dvfs_id_val = Jimple.v().newLocal("$__dvfsIDVal", RefType.v("java.lang.String"));
        body.getLocals().add(dvfs_id_val);
        
        javaLangString = Scene.v().getSootClass("java.lang.String");
        AssignStmt _myIdVal = Jimple.v().newAssignStmt(dvfs_id_val, Jimple.v().newStaticInvokeExpr(
        		javaLangString.getMethod("java.lang.String valueOf(int)").makeRef(), dvfs_id));


        /*$r5 = new java.lang.StringBuilder;*/
        Local dvfs_idSB = Jimple.v().newLocal("$__dvfsSB", RefType.v("java.lang.StringBuilder"));
        body.getLocals().add(dvfs_idSB);
        
        AssignStmt _sb = Jimple.v().newAssignStmt(dvfs_idSB, 
        		Jimple.v().newNewExpr(RefType.v("java.lang.StringBuilder")));
        

        /*specialinvoke $r5.<java.lang.StringBuilder: void <init>()>();*/
        javaLangSBuild = Scene.v().getSootClass("java.lang.StringBuilder");
        InvokeStmt _sbInvoke = Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(
        		dvfs_idSB, javaLangSBuild.getMethod("void <init>()").makeRef()));
        

        /*virtualinvoke $r5.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>("taskset -p ");*/
        InvokeStmt _buildCommand = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(
        		dvfs_idSB, javaLangSBuild.getMethod("java.lang.StringBuilder append(java.lang.String)").makeRef(),
        		StringConstant.v("taskset -p ")));

        InvokeStmt _buildCommand2 = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(
        		dvfs_idSB, javaLangSBuild.getMethod("java.lang.StringBuilder append(java.lang.String)").makeRef(),
        		dvfsString_1));
        
        /*virtualinvoke $r5.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>($r4);*/
        InvokeStmt _commandConcat = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(
        		dvfs_idSB, javaLangSBuild.getMethod("java.lang.StringBuilder append(java.lang.String)").makeRef(),
        		dvfs_id_val));
       
        /*$r4 = virtualinvoke $r5.<java.lang.StringBuilder: java.lang.String toString()>();*/
        AssignStmt _commandTS = Jimple.v().newAssignStmt(dvfs_id_val,
        		Jimple.v().newVirtualInvokeExpr(dvfs_idSB, 
        				javaLangSBuild.getMethod("java.lang.String toString()").makeRef()));
        

        /*$__dvfsRuntime_0 = staticinvoke <java.lang.Runtime: java.lang.Runtime getRuntime()>();*/
        AssignStmt _runtime = Jimple.v().newAssignStmt(
        		dvfsRuntime_0, Jimple.v().newStaticInvokeExpr(
                Scene.v().getMethod("<java.lang.Runtime: java.lang.Runtime getRuntime()>").makeRef()));

        /*$r4 = virtualinvoke $r3.<java.lang.Runtime: java.lang.Process exec(java.lang.String)>("ls");*/
        AssignStmt _process = Jimple.v().newAssignStmt(
        		dvfsProcess_0, Jimple.v().newVirtualInvokeExpr(dvfsRuntime_0,
                Scene.v().getMethod("<java.lang.Runtime: java.lang.Process exec(java.lang.String)>").makeRef(),
                dvfs_id_val)); // Note ls is a sample command --> turn into generic));

        /*virtualinvoke $r4.<java.lang.Process: int waitFor()>();*/
        javaLangProcess = Scene.v().getSootClass("java.lang.Process");
        SootMethod pCall = javaLangProcess.getMethod("int waitFor()");
        InvokeStmt __waitfor = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(dvfsProcess_0, pCall.makeRef()));

        /*$r5 = new java.util.Scanner;*/
        AssignStmt _scanner = Jimple.v().newAssignStmt(dvfsScan_0,
        		Jimple.v().newNewExpr(RefType.v("java.util.Scanner")));

        /*$dvfsIS_0 = virtualinvoke $dvfsProcess_0.<java.lang.Process: java.io.InputStream getInputStream()>();*/
        AssignStmt _inputStream = Jimple.v().newAssignStmt(
        		dvfsIS_0, Jimple.v().newVirtualInvokeExpr(dvfsProcess_0,
        		javaLangProcess.getMethod("java.io.InputStream getInputStream()").makeRef()));

        /*specialinvoke $dvfsScan_0.<java.util.Scanner: void <init>(java.io.InputStream)>($dvfsIS_0);*/
        javaUtilScanner = Scene.v().getSootClass("java.util.Scanner");
        SootMethod sCall = javaUtilScanner.getMethod("void <init>(java.io.InputStream)");
        InvokeStmt __scanner = Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(dvfsScan_0,
        		sCall.makeRef(), dvfsIS_0));

        /*$dvfsScan_0 = virtualinvoke $dvfsScan_0.<java.util.Scanner: java.util.Scanner useDelimiter(java.lang.String)>("\\A");*/        
        AssignStmt _useDel = Jimple.v().newAssignStmt(
        		dvfsScan_0, Jimple.v().newVirtualInvokeExpr(dvfsScan_0,
        		javaUtilScanner.getMethod("java.util.Scanner useDelimiter(java.lang.String)").makeRef(),
        		StringConstant.v("\\A")));

        /*$dvfsString_0 = virtualinvoke $dvfsScan_0.<java.util.Scanner: java.lang.String next()>();*/
        AssignStmt _sNext = Jimple.v().newAssignStmt(
        		dvfsString_0, Jimple.v().newVirtualInvokeExpr(dvfsScan_0,
        		javaUtilScanner.getMethod("java.lang.String next()").makeRef()));

        /*$dvfsFile_0 = new java.io.File;*/
        AssignStmt _file = Jimple.v().newAssignStmt(dvfsFile_0,
        		Jimple.v().newNewExpr(RefType.v("java.io.File")));

        /*$r8 = staticinvoke <android.os.Environment: java.io.File getExternalStorageDirectory()>();*/
        androidEnvironment = Scene.v().getSootClass("android.os.Environment");
        AssignStmt _exSD = Jimple.v().newAssignStmt(
        		dvfsFile_1, Jimple.v().newStaticInvokeExpr(
        		androidEnvironment.getMethod("java.io.File getExternalStorageDirectory()").makeRef()));

        /*specialinvoke $r2.<java.io.File: void <init>(java.io.File,java.lang.String)>($r8, "ARQUIVO.SAIDA");*/
        javaIoFile = Scene.v().getSootClass("java.io.File");
        SootMethod iCall = javaIoFile.getMethod("void <init>(java.io.File,java.lang.String)");
        InvokeStmt __outFile = Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(dvfsFile_0,
        		iCall.makeRef(), dvfsFile_1, StringConstant.v("__DVFSlog__.txt")));

        /*$r9 = new java.io.FileOutputStream;*/
        AssignStmt _fileOS = Jimple.v().newAssignStmt(dvfsFOS_0,
        		Jimple.v().newNewExpr(RefType.v("java.io.FileOutputStream")));

        /*specialinvoke $r9.<java.io.FileOutputStream: void <init>(java.io.File)>($r2);*/
        javaFOS = Scene.v().getSootClass("java.io.FileOutputStream");
        SootMethod fosCall = javaFOS.getMethod("void <init>(java.io.File)");
        InvokeStmt __outFileOS = Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(dvfsFOS_0,
            fosCall.makeRef(), dvfsFile_0));
                       
        
		/*$r12 = virtualinvoke $r7.<java.lang.String: byte[] getBytes()>();*/
        javaLangString = Scene.v().getSootClass("java.lang.String");
        AssignStmt _byte = Jimple.v().newAssignStmt(
        		byte_0, Jimple.v().newVirtualInvokeExpr(
        		dvfsString_0, javaLangString.getMethod("byte[] getBytes()").makeRef()));
        
		/*virtualinvoke $r9.<java.io.FileOutputStream: void write(byte[])>($r12);*/        
        SootMethod writeCall = javaFOS.getMethod("void write(byte[])");
        InvokeStmt __write = Jimple.v().newInvokeStmt(
        		Jimple.v().newVirtualInvokeExpr(dvfsFOS_0, writeCall.makeRef(), byte_0));
        
        
        
        
        // read input file
        /*$r2 = new java.io.File;*/
        Local dvfsFile_in = Jimple.v().newLocal("$__dvfsFILE_IN", RefType.v("java.io.File"));
        body.getLocals().add(dvfsFile_in);
        AssignStmt newInFile = Jimple.v().newAssignStmt(dvfsFile_in,
        		Jimple.v().newNewExpr(RefType.v("java.io.File")));

        /*$r3 = staticinvoke <android.os.Environment: java.io.File getExternalStorageDirectory()>();*/
        //_exSD //dvfsFile_1

        /*specialinvoke $r2.<java.io.File: void <init>(java.io.File,java.lang.String)>($r3, "__dvfs__.conf");*/
        InvokeStmt initFile = Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(dvfsFile_in,
        		javaIoFile.getMethod("void <init>(java.io.File,java.lang.String)").makeRef(),
        		dvfsFile_1, StringConstant.v("__dvfs__.conf")));

        /*$r4 = new java.util.Scanner;*/
        Local scanIN = Jimple.v().newLocal("$__dvfsSCAN_1", RefType.v("java.io.File"));
        body.getLocals().add(scanIN);
        
        AssignStmt _scannerIN = Jimple.v().newAssignStmt(scanIN,
        		Jimple.v().newNewExpr(RefType.v("java.util.Scanner")));
        
        /*specialinvoke $r4.<java.util.Scanner: void <init>(java.io.File)>($r2);*/
        SootMethod inCall = javaUtilScanner.getMethod("void <init>(java.io.File)");
        InvokeStmt __scannerIN = Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(scanIN,
        		inCall.makeRef(), dvfsFile_in));
        
        AssignStmt _useDelIN = Jimple.v().newAssignStmt(
        		scanIN, Jimple.v().newVirtualInvokeExpr(scanIN,
        		javaUtilScanner.getMethod("java.util.Scanner useDelimiter(java.lang.String)").makeRef(),
        		StringConstant.v("\\A")));

        /*$dvfsString_0 = virtualinvoke $dvfsScan_0.<java.util.Scanner: java.lang.String next()>();*/
        AssignStmt _sNextIN = Jimple.v().newAssignStmt(
        		dvfsString_1, Jimple.v().newVirtualInvokeExpr(scanIN,
        		javaUtilScanner.getMethod("java.lang.String next()").makeRef()));        

        /*$r4 = virtualinvoke $r4.<java.util.Scanner: java.util.Scanner useDelimiter(java.lang.String)>("\\A");*/

        /*$r5 = virtualinvoke $r4.<java.util.Scanner: java.lang.String next()>();*/

        
        
        
        units.insertAfter(__write, first);
     	units.insertAfter(_byte, first);
        units.insertAfter(__outFileOS, first);
        units.insertAfter(_fileOS, first);
        units.insertAfter(__outFile, first);

        units.insertAfter(_file, first);
        units.insertAfter(_sNext, first);
        units.insertAfter(_useDel, first);
        units.insertAfter(__scanner, first);
        units.insertAfter(_inputStream, first);
        units.insertAfter(_scanner, first);
        units.insertAfter(__waitfor, first);
        units.insertAfter(_process, first);
        units.insertAfter(_runtime, first);
        units.insertAfter(_commandTS, first); // MOVE
        units.insertAfter(_commandConcat, first); // MOVE
        units.insertAfter(_buildCommand2, first); // in file
        units.insertAfter(_buildCommand, first); // MOVE
        
        
             
        units.insertAfter(_sNextIN, first);
        units.insertAfter(_useDelIN, first);
        units.insertAfter(__scannerIN, first);
        units.insertAfter(_scannerIN, first);
        units.insertAfter(initFile, first);
        units.insertAfter(newInFile, first);
        
        
        units.insertAfter(_exSD, first);
        units.insertAfter(_sbInvoke, first); // MOVE
        units.insertAfter(_sb, first); // MOVE
        units.insertAfter(_myIdVal, first); // MOVE
        units.insertAfter(_myId, first); // MOVE
    }
}






/*


// adicionando contador como atributo da classe. Esta sincronizado
        // pois o código pode (deve?) ser executado paralelamente. Queremos
        // evitar a criacao de dois atributos com mesmo nome na classe
        synchronized (this) {
          // so inserimos o contador se encontrarmos a main, pois saberemos
          // que a classe tem um ponto de entrada para retornar os dados
          // gerados
            if (!Scene.v().getMainClass().
                    declaresMethod("void main(java.lang.String[])"))
                throw new RuntimeException("couldn't find main() in java file");

            if (gotoCounterAdded)
                gotoCounter = Scene.v().getMainClass().getFieldByName("gotoCount");
            else {
                // Add gotoCounter field
                gotoCounter = new SootField("gotoCount", LongType.v(),
                                                Modifier.STATIC);
                Scene.v().getMainClass().addField(gotoCounter);

                javaIoPrintStream = Scene.v().getSootClass("java.io.PrintStream");
                javaStringBuilder = Scene.v().getSootClass("java.lang.StringBuilder");

                gotoCounterAdded = true;
            }
          }


        // Inserindo codigo para contar novo goto encontrado

            boolean isMainMethod = body.getMethod().getSubSignature().equals("void main(java.lang.String[])");

            // variavel tmp local ao methodo que recebera uma referencia para o
            // o contador global posteriormente
            Local tmpLocal = Jimple.v().newLocal("tmp", LongType.v());
            // adicionando a definicao dessa variavel com tipo Long, na lista de
            // definicoes do metodo
            body.getLocals().add(tmpLocal);

            // pegamos o snapshotIterator em vez do iterator normal, porque iremos
            // alterar a nossa Chain units enquanto iteramos sobre ela, com isso
            // queremos evitar problemas de concorrencia com leituras e
            // modificacoes da nossa chain de units
            // http://www.sable.mcgill.ca/~plam/doc/soot/util/Chain.html#snapshotIterator()
            Iterator stmtIt = units.snapshotIterator();
            Stmt special = null;

            while(stmtIt.hasNext()) {
                // pegando o primeiro statement do corpo do methodo
                Stmt s = (Stmt) stmtIt.next();

                // caso seja um goto, iremos inserir um incrementador no codigo
                if(s instanceof GotoStmt) {
                    // #1 inserir tmp = gotoCount; (assign)
                    // #2 inserir tmp = tmp + 1;   (sum)
                    // #3 inserir gotoCount = tmp; (update)

                    AssignStmt assign = Jimple.v().newAssignStmt(tmpLocal,
                                 Jimple.v().newStaticFieldRef(gotoCounter.makeRef()));
                    AssignStmt sum = Jimple.v().newAssignStmt(tmpLocal,
                                 Jimple.v().newAddExpr(tmpLocal, LongConstant.v(1L)));
                    AssignStmt update = Jimple.v().newAssignStmt(
                                 Jimple.v().newStaticFieldRef(gotoCounter.makeRef()),
                                 tmpLocal);

                    // inserimos o incrementador antes do goto encontrado
                    units.insertBefore(assign, s);
                    units.insertBefore(sum, s);
                    units.insertBefore(update, s);

                } else if (s instanceof InvokeStmt) {
                    // verificar se podemos ter a chamada da funcao exit
                    // pois devemos imprimir o valor de gotoCounter antes
                    // que o programa encerre sua execucao
                    InvokeExpr iexpr = (InvokeExpr) ((InvokeStmt)s).getInvokeExpr();

                    // especial add news antes na init
                    if (iexpr instanceof SpecialInvokeExpr) {
                        SootMethod target = ((SpecialInvokeExpr)iexpr).getMethod();
                        if (target.getSignature().equals("<java.lang.StringBuilder: void <init>()>")) {
                            special = s;
                        }
                    }
                    else if (iexpr instanceof StaticInvokeExpr) {
                        SootMethod target = ((StaticInvokeExpr)iexpr).getMethod();

                        if (target.getSignature().equals("<java.lang.System: void exit(int)>")) {
                            if (!addedLocals) {
                                tmpRef = Jimple.v().newLocal("$tmpRef", RefType.v("java.io.PrintStream"));
                                body.getLocals().add(tmpRef);

                                tmpLong = Jimple.v().newLocal("$tmpLong", LongType.v());
                                body.getLocals().add(tmpLong);

                                tmpString = Jimple.v().newLocal("$tmpString", RefType.v("java.lang.String"));
                                body.getLocals().add(tmpString);

                                // tmpBuild1 = new java.lang.StringBuilder;
                                tmpBuilder1 = Jimple.v().newLocal("$tmpBuild1", RefType.v("java.lang.StringBuilder"));
                                body.getLocals().add(tmpBuilder1);

                                tmpBuilder2 = Jimple.v().newLocal("$tmpBuild2", RefType.v("java.lang.StringBuilder"));
                                body.getLocals().add(tmpBuilder2);

                                tmpBuilder3 = Jimple.v().newLocal("$tmpBuild3", RefType.v("java.lang.StringBuilder"));
                                body.getLocals().add(tmpBuilder3);

                                tmpBuilder4 = Jimple.v().newLocal("$tmpBuild4", RefType.v("java.lang.StringBuilder"));
                                body.getLocals().add(tmpBuilder4);

                                AssignStmt t1 = Jimple.v().newAssignStmt(tmpBuilder1,
                                    Jimple.v().newNewExpr(RefType.v("java.lang.StringBuilder")));
                                units.insertBefore(t1, s);

                                SpecialInvokeExpr construtor = Jimple.v().newSpecialInvokeExpr(tmpBuilder1,
                                        javaStringBuilder.getMethod("void <init>()").makeRef());
                                units.insertBefore(Jimple.v().newInvokeStmt(construtor), s);


                                addedLocals = true;
                            }

                            // inserir "tmpRef = java.lang.System.out;"
                            AssignStmt refSystemOut = Jimple.v().newAssignStmt(
                                          tmpRef, Jimple.v().newStaticFieldRef(
                                          Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef()));

                            // inserir "tmpLong = gotoCounter;"
                            AssignStmt localCopy = Jimple.v().newAssignStmt(tmpLong,
                                          Jimple.v().newStaticFieldRef(gotoCounter.makeRef()));

                            // inserir "tmpRef.println(tmpLong);"
                            // SootMethod toCall = javaIoPrintStream.getMethod("void println(long)");
                            // InvokeStmt inv = Jimple.v().newInvokeStmt(
                            //               Jimple.v().newVirtualInvokeExpr(tmpRef, toCall.makeRef(), tmpLong));

                            // inserir str = string("gotos found")                    ;
                            SootMethod appendtoCall = javaStringBuilder.getMethod("java.lang.StringBuilder append(java.lang.String)");
                            SootMethod appendtoCallLong = javaStringBuilder.getMethod("java.lang.StringBuilder append(long)");
                            SootMethod toString = javaStringBuilder.getMethod("java.lang.String toString()");
                            // virtualinvoke $r0.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>("Gotos Found: ")
                            InvokeExpr builderInv1 =
                              Jimple.v().newVirtualInvokeExpr(tmpBuilder1, appendtoCall.makeRef(), StringConstant.v("Gotos found: "));
                            // $r2 = virtualinvoke $r0.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>("Valor: ");
                            AssignStmt s1 = Jimple.v().newAssignStmt(tmpBuilder2, builderInv1);
                            // virtualinvoke $r2.<java.lang.StringBuilder: java.lang.StringBuilder append(long)>(gotoCounter);
                            InvokeExpr builderInv2 =
                              Jimple.v().newVirtualInvokeExpr(tmpBuilder2, appendtoCallLong.makeRef(), tmpLong);
                            // $r3 = virtualinvoke $r2.<java.lang.StringBuilder: java.lang.StringBuilder append(long)>(gotoCounter);
                            AssignStmt s2 = Jimple.v().newAssignStmt(tmpBuilder3, builderInv2);

                            // add string srt = stringbuild.toString();
                            //tmpString
                            InvokeExpr genString = Jimple.v().newVirtualInvokeExpr(tmpBuilder3, toString.makeRef());
                            AssignStmt s3 = Jimple.v().newAssignStmt(tmpString, genString);

                            SootMethod toCall = javaIoPrintStream.getMethod("void println(java.lang.String)");
                            InvokeStmt inv = Jimple.v().newInvokeStmt(
                                          Jimple.v().newVirtualInvokeExpr(tmpRef, toCall.makeRef(), tmpString));



                            units.insertBefore(refSystemOut, s);
                            units.insertBefore(localCopy, s);
                            units.insertBefore(s1, s);
                            units.insertBefore(s2, s);
                            units.insertBefore(s3, s);
                            units.insertBefore(inv, s);
                        }
                    }
                }
                else if (isMainMethod && (s instanceof ReturnStmt || s instanceof ReturnVoidStmt)) {
                    // verificar se termos um return na main, pois devemos imprimir
                    // o valor de gotoCounter antes de encerrar a execucao da aplicacao
                    if (!addedLocals) {
                        tmpRef = Jimple.v().newLocal("$tmpRef", RefType.v("java.io.PrintStream"));
                        body.getLocals().add(tmpRef);

                        tmpLong = Jimple.v().newLocal("$tmpLong", LongType.v());
                        body.getLocals().add(tmpLong);

                        tmpString = Jimple.v().newLocal("$tmpString", RefType.v("java.lang.String"));
                        body.getLocals().add(tmpString);

                        // tmpBuild1 = new java.lang.StringBuilder;
                        tmpBuilder1 = Jimple.v().newLocal("$tmpBuild1", RefType.v("java.lang.StringBuilder"));
                        body.getLocals().add(tmpBuilder1);

                        tmpBuilder2 = Jimple.v().newLocal("$tmpBuild2", RefType.v("java.lang.StringBuilder"));
                        body.getLocals().add(tmpBuilder2);

                        tmpBuilder3 = Jimple.v().newLocal("$tmpBuild3", RefType.v("java.lang.StringBuilder"));
                        body.getLocals().add(tmpBuilder3);

                        tmpBuilder4 = Jimple.v().newLocal("$tmpBuild4", RefType.v("java.lang.StringBuilder"));
                        body.getLocals().add(tmpBuilder4);

                        AssignStmt t1 = Jimple.v().newAssignStmt(tmpBuilder1,
                            Jimple.v().newNewExpr(RefType.v("java.lang.StringBuilder")));
                        units.insertBefore(t1, s);

                        SpecialInvokeExpr construtor = Jimple.v().newSpecialInvokeExpr(tmpBuilder1,
                                javaStringBuilder.getMethod("void <init>()").makeRef());
                        units.insertBefore(Jimple.v().newInvokeStmt(construtor), s);


                        addedLocals = true;
                    }
                    // inserir "tmpRef = java.lang.System.out;"
                    AssignStmt refSystemOut = Jimple.v().newAssignStmt(
                                  tmpRef, Jimple.v().newStaticFieldRef(
                                  Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef()));

                    // inserir "tmpLong = gotoCounter;"
                    AssignStmt localCopy = Jimple.v().newAssignStmt(tmpLong,
                                  Jimple.v().newStaticFieldRef(gotoCounter.makeRef()));

                    // inserir "tmpRef.println(tmpLong);"
                    // SootMethod toCall = javaIoPrintStream.getMethod("void println(long)");
                    // InvokeStmt inv = Jimple.v().newInvokeStmt(
                    //               Jimple.v().newVirtualInvokeExpr(tmpRef, toCall.makeRef(), tmpLong));

                    // inserir str = string("gotos found")                    ;
                    SootMethod appendtoCall = javaStringBuilder.getMethod("java.lang.StringBuilder append(java.lang.String)");
                    SootMethod appendtoCallLong = javaStringBuilder.getMethod("java.lang.StringBuilder append(long)");
                    SootMethod toString = javaStringBuilder.getMethod("java.lang.String toString()");
                    // virtualinvoke $r0.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>("Gotos Found: ")
                    InvokeExpr builderInv1 =
                      Jimple.v().newVirtualInvokeExpr(tmpBuilder1, appendtoCall.makeRef(), StringConstant.v("Gotos found: "));
                    // $r2 = virtualinvoke $r0.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>("Valor: ");
                    AssignStmt s1 = Jimple.v().newAssignStmt(tmpBuilder2, builderInv1);
                    // virtualinvoke $r2.<java.lang.StringBuilder: java.lang.StringBuilder append(long)>(gotoCounter);
                    InvokeExpr builderInv2 =
                      Jimple.v().newVirtualInvokeExpr(tmpBuilder2, appendtoCallLong.makeRef(), tmpLong);
                    // $r3 = virtualinvoke $r2.<java.lang.StringBuilder: java.lang.StringBuilder append(long)>(gotoCounter);
                    AssignStmt s2 = Jimple.v().newAssignStmt(tmpBuilder3, builderInv2);

                    // add string srt = stringbuild.toString();
                    //tmpString
                    InvokeExpr genString = Jimple.v().newVirtualInvokeExpr(tmpBuilder3, toString.makeRef());
                    AssignStmt s3 = Jimple.v().newAssignStmt(tmpString, genString);

                    SootMethod toCall = javaIoPrintStream.getMethod("void println(java.lang.String)");
                    InvokeStmt inv = Jimple.v().newInvokeStmt(
                                  Jimple.v().newVirtualInvokeExpr(tmpRef, toCall.makeRef(), tmpString));



                    units.insertBefore(refSystemOut, s);
                    units.insertBefore(localCopy, s);
                    units.insertBefore(s1, s);
                    units.insertBefore(s2, s);
                    units.insertBefore(s3, s);
                    units.insertBefore(inv, s);

                }
            }






*/
