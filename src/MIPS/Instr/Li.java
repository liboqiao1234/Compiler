package MIPS.Instr;

public class Li extends MipsInstruction {
    private String rd;
    private int imm;


    public Li( String rd, int imm) {
        this.imm = imm;
        this.rd = rd;
    }

    @Override
    public String toString() {
        return "li " + rd + ", " + imm;
    }
}
