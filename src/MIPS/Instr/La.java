package MIPS.Instr;

public class La extends MipsInstruction{
    private  String rd;
    private String label;

    public La(String rd, String label) {
        this.rd = rd;
        this.label = label;
    }

    @Override
    public String toString() {
        return "la " + rd + ", " + label;
    }
}
