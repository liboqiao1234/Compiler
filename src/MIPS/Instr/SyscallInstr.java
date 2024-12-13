package MIPS.Instr;

public class SyscallInstr extends MipsInstruction{
    // 包含 li $v0, no 这一句，因为这两句完全在一起
    private int no;
//    private LI TODO: add LI

    public SyscallInstr(int no){
        this.no = no;
    }

    public String toString(){
        return "syscall";
    }
}
