package MIPS;

import MIPS.Instr.Label;
import MIPS.Instr.MipsInstruction;

import java.util.ArrayList;

public class MipsModule {
    private ArrayList<MipsInstruction> data = new ArrayList<>();
    private ArrayList<MipsInstruction> text = new ArrayList<>();
    private static final MipsModule instance = new MipsModule();

    private MipsModule() {

    }

    public static MipsModule getInstance() {
        return instance;
    }

    public void addData(MipsInstruction instr) {
        data.add(instr);
    }

    public void addText(MipsInstruction instr) {
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
            if (!(instr instanceof Label)){
                // label
                sb.append("    ");
            }
            sb.append(instr.toString()).append("\n");
        }
        return sb.toString();
    }
}
