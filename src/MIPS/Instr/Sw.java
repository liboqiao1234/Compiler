package MIPS.Instr;

public class Sw extends MipsInstruction {
    // 约定先-4，再存！
    private String rt, rs;// target and source
    private int offset;

    public Sw(String rt, String rs, int offset) {
        this.rt = rt;
        this.rs = rs;
        this.offset = offset;
    }

    public String toString() {
        return "sw " + rt + ", " + offset + "(" + rs + ")";
    }
}
