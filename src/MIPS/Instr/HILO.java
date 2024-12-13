package MIPS.Instr;

public class HILO extends MipsInstruction {
    private String op;
    private String rd;

    public HILO(String op, String rd) {
        this.op = op;
        this.rd = rd;
    }

    public String toString() {
        return op + " " + rd;
    }
}
