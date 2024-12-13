package MIPS.Instr;

public class B extends MipsInstruction {
    private String op; // BNE BEQ (Blez?)
    private String label;
    private String rs, rt;

    public B(String op, String label, String rs, String rt) {
        this.op = op;
        this.label = label;
        this.rs = rs;
        this.rt = rt;
    }

    @Override
    public String toString() {
        return op + " " + rs + ", " + rt +", " + label;
    }
}
