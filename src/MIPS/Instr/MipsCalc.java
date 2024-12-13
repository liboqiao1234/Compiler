package MIPS.Instr;

import IR.Instr.Operator;

public class MipsCalc extends MipsInstruction {
    private String op;
    private String rs;
    private String rt;
    private String rd = "";
    private int imm;

    public MipsCalc(String op, String rd, String rs, String rt) {
        this.op = op;
        this.rs = rs;
        this.rt = rt;
        this.rd = rd;
    }

    public MipsCalc(String op, String rs, String rt, int imm) {
        this.op = op;
        this.rs = rs;
        this.rt = rt;
        this.imm = imm;
    }

    public String toString() {
        if (rd.equals("")) {
            return op + " " + rs + ", " + rt + ", " + imm;
        }
        return op + " " + rd + ", " + rs + ", " + rt;
    }
}
