package MIPS.Instr;

public class J extends MipsInstruction {
    private String type; // jal, j, jr
    private String label; // register: $ra
    //private String register; // actually only $ra

    public J(String type, String label) {
        this.type = type;
        this.label = label;
    }

    public String toString() {
        if (type.equals("jr")) {
            return type + " " + label;
        }
        return type + " " + label;
    }

    public String getType() {
        return type;
    }

    public String getLabel() {
        return label;
    }
}
