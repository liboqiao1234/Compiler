package MIPS;

import IR.Instr.StoreInstr;
import MIPS.Instr.Comment;
import MIPS.Instr.La;
import MIPS.Instr.Label;
import MIPS.Instr.Lw;
import MIPS.Instr.MipsInstruction;
import MIPS.Instr.Move;
import MIPS.Instr.Sw;

import java.util.ArrayList;

public class MipsModule {
    private ArrayList<MipsInstruction> data = new ArrayList<>();
    private ArrayList<MipsInstruction> text = new ArrayList<>();
    private static final MipsModule instance = new MipsModule();

    private MipsModule() {

    }

    public void deleteLast() {
        text.remove(text.size() - 1);
    }

    public MipsInstruction getLast() {
        return text.get(text.size() - 1);
    }

    public static MipsModule getInstance() {
        return instance;
    }

    public void addData(MipsInstruction instr) {
        data.add(instr);
    }

    private MipsInstruction getLastInstr(){
        if (text.isEmpty())return null;
        int index = text.size() - 1;
        while (text.get(index) instanceof Label || text.get(index) instanceof Comment) {
            index--;
        }
        if (index > 0) {
            return text.get(index);
        }
        return null;
    }

    public void addText(MipsInstruction instr) {
        if (instr instanceof Lw && !text.isEmpty() && getLastInstr() instanceof Sw) { //  || instr instanceof La
            Lw lwInstr = (Lw) instr;
            Sw swInstr = (Sw) getLastInstr();
//            System.err.println("detected");
            if (lwInstr.getRs().equals(swInstr.getRs()) && lwInstr.getOffset() == swInstr.getOffset()) {
                if (lwInstr.getRt().equals(swInstr.getRt())) {
                    return;
                } else {
                    text.add(new Move(lwInstr.getRt(), swInstr.getRt()));
                    return;
                }
            }
        }
        text.add(instr);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(".data\n");
        for (MipsInstruction instr : data) {
            sb.append(instr.toString()).append("\n");
        }
        sb.append("\n");
        sb.append(".text\n");
        for (MipsInstruction instr : text) {
            if (!(instr instanceof Label)) {
                // label
                sb.append("    ");
            }
            sb.append(instr.toString()).append("\n");
        }
        return sb.toString();
    }
}
