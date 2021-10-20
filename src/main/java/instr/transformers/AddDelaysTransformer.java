package instr.transformers;

/**
 * TODO:
 *  (1) check why sometimes add sleep twice on the same (e.g., first) instruction
 *  (2) avoid instrumentation within loops <- Done
 */


import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.jimple.toolkits.annotation.logic.LoopFinder;
import soot.tagkit.AnnotationTag;
import soot.tagkit.VisibilityAnnotationTag;
import soot.util.Chain;

import java.io.*;
import java.util.*;

public class AddDelaysTransformer extends BodyTransformer {

  // script parameters
  double overhead;
  long minimalDelay;
//  String csvTimes;
  long randomSeed;
  boolean debug;

  // info about running times (avg) of each test case to be instrumented
//  Set<String> methodsToInstrument;
  SootClass threadClass, printClass;
  SootMethod sleepMethod, printMethod;

  Random ran;
  ArrayList<String> p;


  public AddDelaysTransformer(double overhead, long minimalDelay, long randomSeed, boolean debug) {
    this.overhead = overhead;
    this.minimalDelay = minimalDelay;
//    this.csvTimes = csvTimes;
    this.randomSeed = randomSeed;
    this.debug = debug;

//    this.methodsToInstrument = new HashSet<String>();
//    this.methodsToInstrument = readFile(methodsToInstrumentFileName);
//    this.methodsToInstrument = null;

    // soot supporting classes
    this.threadClass = Scene.v().loadClassAndSupport("java.lang.Thread");
    this.printClass = Scene.v().loadClassAndSupport("instr.util.Sleep");
    //System.out.println("AA");
    this.sleepMethod = threadClass.getMethod("void sleep(long)");
    this.printMethod = printClass.getMethod("void report(java.lang.String)");

    //System.out.println("a");
    //java.lang.Thread.sleep();
    // random seed
    this.ran = new Random(randomSeed);
  }

//  private final Set<String> readFile(String csvFile) {
//    Map<String, Long> map = new HashMap<String, Long>();
//    try {
//      BufferedReader br = new BufferedReader(new FileReader(csvFile));
//      String line = null;
//      while ((line = br.readLine()) != null) {
//        String str[] = line.split(",");
//        double timeMillis = Double.parseDouble(str[1]) * 1000;
//        map.put(str[0], (long) timeMillis);
//      }
//    } catch (IOException ioException) {
//      System.err.print("fatal error! csv file can't be found " + csvFile);
//      System.exit(1);
//    }
//    return map;
//  }

  /* internalTransform goes through a method body and inserts
    * counter instructions before an INVOKESTATIC instruction
    */

  protected void salveTestTimeInCsv(String testName, long normalTimeTest, long timeTestWithSlepps){

    FileWriter writer = null;
    try {
      writer = new FileWriter("check_instrumentation_times.csv", true);
      //testname, normalTimeTest, timeTestWithSlepps
      double normalTimeTestSeconds = normalTimeTest/1000.0;
      double timeTestWithSleppsSeconds = timeTestWithSlepps / 1000.0;
      writer.write( testName +","+ normalTimeTestSeconds + "," +timeTestWithSleppsSeconds +"\n");
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }


  }
  protected void internalTransform(Body body, String phase, Map options) {
    // body's method
    SootMethod method = body.getMethod();
    //System.out.println(body);
    VisibilityAnnotationTag tag = (VisibilityAnnotationTag) method.getTag("VisibilityAnnotationTag");
    boolean isTearDownSetup = false;
    if (tag != null) {
      for (AnnotationTag annotation : tag.getAnnotations()) {
        if (annotation.getType().equals("Lorg/junit/Before;") || annotation.getType().equals("Lorg/junit/After;")) {
          isTearDownSetup = true;
          break;
        }
      }
    }

    String methodID = method.getDeclaringClass().getName() + "#" + method.getName();

    // DEBUG!
    if (!methodID.equals("com.alibaba.json.bvt.parser.autoType.AutoTypeTest2_deny#test_0")) {
      return;
    }

    System.out.println("  instrumenting test method : " + method.getSignature());

    // get body's unit as a chain
    Chain units = body.getUnits();

    // get a snapshot iterator of the unit since we are going to
    // mutate the chain when iterating over it.
    Iterator stmtIt = units.snapshotIterator();

    // used to find loops
    LoopFinder lf = new LoopFinder();
    Set<Loop> loops = lf.getLoops(body);
    List<Stmt> statementsInLoops = new ArrayList<Stmt>();
    for (Loop l : loops){
      List<Stmt> statements = l.getLoopStatements();
      if (statements.size() > 0){
        statementsInLoops.addAll(statements);
      }
    }

    // decide a priori which instructions should be instrumented.
    //TODO: check what probability should be used
    Set<Stmt> tobe_instrumented = new HashSet<Stmt>();
    Stmt lastStmt = null;
    if (stmtIt.hasNext()){ // to remove bug of duplicates sleeps in first line
      stmtIt.next();
    }
    while (stmtIt.hasNext()) {
      // cast back to a statement.
      Stmt stmt = (Stmt) stmtIt.next();
      lastStmt = stmt;
      if (statementsInLoops.contains(stmt)){
        continue;
      }
      boolean choice = ran.nextBoolean();
      if (choice) tobe_instrumented.add(stmt);

//      System.out.println(choice? "instrumented" : "not instrumented");
    }

    // precompute delays
    int numberOfDelays = tobe_instrumented.size();
    // it could happen that we do not include any delays in the code
    if (numberOfDelays == 0) return;

    long totalTime = 0;
    long delayMillis = minimalDelay;
//    if (!isTearDownSetup) {
//      totalTime = testTime.get(methodID);
//      // totalTime == 0 implies delayMillis == minimalDelay
//      if (totalTime != 0) {
//        delayMillis = (long) ((totalTime * overhead) / numberOfDelays);
//        // in a few cases the delayMillis is 0, for example when the test time is 0.001
//        if (delayMillis == 0){
//          delayMillis = minimalDelay;
//        }
//      }
//    }

    // do the actual instrumentation now that we know length of each delayMillis
    for (Stmt stmt : tobe_instrumented) {
      InvokeExpr incExpr= Jimple.v().newStaticInvokeExpr(sleepMethod.makeRef(), LongConstant.v(delayMillis));
      // 2. then, make a invoke statement
      Stmt incStmt = Jimple.v().newInvokeStmt(incExpr);
      // 3. insert new statement into the chain
      //    (we are mutating the unit chain).
      units.insertBefore(incStmt, stmt);
    }
    long totalTimeOfSleeps = tobe_instrumented.size() * delayMillis;
    System.out.println("  added " + tobe_instrumented.size() + " sleeps");
    if (lastStmt != null && this.debug) {
      String report = "   " +  method.getSignature()+ "INS --> TotalSleeps: " + tobe_instrumented.size() +
              ". delayMillis: " + delayMillis + ". totalTimeOfAllSleeps: " + (totalTimeOfSleeps/1000.0) +
              ". totalTimeTest: " + (totalTime/1000.0) + ". totalTimeWithSleeps: " + (totalTimeOfSleeps + totalTime)/1000.0 + ";";
      InvokeExpr printExp= Jimple.v().newStaticInvokeExpr(printMethod.makeRef(),
              StringConstant.v(report));
      Stmt reportStmt = Jimple.v().newInvokeStmt(printExp);
      units.insertBefore(reportStmt,lastStmt);
    }
    //testname, normalTimeTest, timeTestWithSlepps
    // this.salveTestTimeInCsv(methodID,totalTime, (totalTimeOfSleeps + totalTime));
  }
}
