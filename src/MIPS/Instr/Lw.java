package MIPS.Instr;

public class Lw extends MipsInstruction{
    private String rt, rs;// target and source
    private int offset;

    public Lw(String rt, String rs, int offset){
        this.rt = rt;
        this.rs = rs;
        this.offset = offset;

    }

    public String getRt() {
        return rt;
    }

    public String getRs() {
        return rs;
    }

    public int getOffset() {
        return offset;
    }

    public String toString(){
        return "lw " + rt + ", " + offset + "(" + rs + ")";
    }
}
