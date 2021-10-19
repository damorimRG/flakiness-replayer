package instr.transformers;

import java.util.Map;

import soot.Unit;

import soot.Body;
import soot.BodyTransformer;
import soot.Pack;
import soot.dexpler.instructions.InvokeStaticInstruction;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.toolkits.ide.libsumm.FixedMethods;

public class PrintFixedMethodTransformer extends BodyTransformer {

    static int yes, no;

    @Override
    protected void internalTransform(Body b, String phaseName, Map<String, String> options) {
        for (Unit u : b.getUnits()) {
            Stmt s = (Stmt) u;
            if (s.containsInvokeExpr()) {
                InvokeExpr ie = s.getInvokeExpr();
                if (FixedMethods.isFixed(ie)) {
                    System.err.println("+++ " + ie);
                    yes++;
                } else {
                    System.err.println(" -  " + ie);
                    no++;
                }
            }
        }
    }
};