package MIPS.Instr;

public class Label extends MipsInstruction {
    private final String label;

    public Label(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "BB_" + label + ":";
    }
}
