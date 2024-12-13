package MIPS.Instr;

public class MD extends MipsInstruction {
    private String op;
    private String rs;
    private String rt;

    public MD(String op, String rs, String rt) {
        this.op = op;
        this.rs = rs;
        this.rt = rt;
    }

    public String toString() {
        return op + " " + rs + ", " + rt;
    }

}
