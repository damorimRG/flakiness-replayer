package instr.transformers;


import soot.*;
import soot.jimple.*;
import soot.util.Chain;

import java.util.*;

public class SleepInvokerTransformer extends BodyTransformer {

  /* some internal fields */
  static SootClass sleepClass;
  static SootMethod sleepIF, reportSleepIF;

  static {
    sleepClass    = Scene.v().loadClassAndSupport("instr.util.Sleep");
    sleepIF = sleepClass.getMethod("void sleepIF(java.lang.String)");
    reportSleepIF   = sleepClass.getMethod("void report()");
  }

  /* internalTransform goes through a method body and inserts
   * counter instructions before an INVOKESTATIC instruction
   */
  protected void internalTransform(Body body, String phase, Map options) {
    // body's method
    SootMethod method = body.getMethod();

    // debugging
    System.out.println("instrumenting method : " + method.getSignature());

    // get body's unit as a chain
    Chain units = body.getUnits();

    // get a snapshot iterator of the unit since we are going to
    // mutate the chain when iterating over it.
    //
    Iterator stmtIt = units.snapshotIterator();

    // typical while loop for iterating over each statement
    while (stmtIt.hasNext()) {

      // cast back to a statement.
      Stmt stmt = (Stmt)stmtIt.next();

      // there are many kinds of statements, here we are only
      // interested in statements containing InvokeStatic
      // NOTE: there are two kinds of statements may contain
      //       invoke expression: InvokeStmt, and AssignStmt
      if (!stmt.containsInvokeExpr()) {
        continue;
      }


      // now we reach the real instruction
      // call Chain.insertBefore() to insert instructions
      //
      // 1. first, make a new invoke expression
      InvokeExpr incExpr= Jimple.v().newStaticInvokeExpr(sleepIF.makeRef(),
              StringConstant.v(method.getName()));
      // 2. then, make a invoke statement
      Stmt incStmt = Jimple.v().newInvokeStmt(incExpr);

      // 3. insert new statement into the chain
      //    (we are mutating the unit chain).
      units.insertBefore(incStmt, stmt);

    }


    // Do not forget to insert instructions to report the counter
    // this only happens before the exit points of main method.

    // 1. check if this is the main method by checking signature
    String signature = method.getSubSignature();
    boolean isMain = signature.equals("void main(java.lang.String[])");

    // re-iterate the body to look for return statement
    if (isMain) {
      stmtIt = units.snapshotIterator();

      while (stmtIt.hasNext()) {
        Stmt stmt = (Stmt)stmtIt.next();

        // check if the instruction is a return with/without value
        if ((stmt instanceof ReturnStmt)
                ||(stmt instanceof ReturnVoidStmt)) {
          // 1. make invoke expression of MyCounter.report()
          InvokeExpr reportExpr= Jimple.v().newStaticInvokeExpr(reportSleepIF.makeRef());

          // 2. then, make a invoke statement
          Stmt reportStmt = Jimple.v().newInvokeStmt(reportExpr);

          // 3. insert new statement into the chain
          //    (we are mutating the unit chain).
          units.insertBefore(reportStmt, stmt);
        }
      }
    }
  }
}