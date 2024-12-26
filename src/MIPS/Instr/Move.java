package MIPS.Instr;

public class Move extends MipsInstruction {
    private String dst;
    private String src;

    public Move(String dst, String src) {
        this.dst = dst;
        this.src = src;
    }

    @Override
    public String toString() {
        return "move " + dst + ", " + src;
    }
    // move $t0, $t1    => set t0 to t1
}
