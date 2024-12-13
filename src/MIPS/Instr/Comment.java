package MIPS.Instr;

public class Comment extends MipsInstruction {
    private String comment;

    public Comment(String comment) {
        this.comment = comment;
    }

    public String toString() {
        return "# " + comment;
    }
}
