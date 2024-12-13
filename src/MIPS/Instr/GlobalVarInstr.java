package MIPS.Instr;

import java.util.ArrayList;

public class GlobalVarInstr extends MipsInstruction {
    private String type; // word, byte, asciiz
    private String name;
    private String strVal;
    private int intVal;
    private ArrayList<Integer> arrVal;
    private int count; // intVal : count

    public GlobalVarInstr(String name, String strVal) {
        // stringLiteral
        this.name = name;
        this.strVal = strVal;
        this.type = "asciiz";
    }

    public GlobalVarInstr(String name, int intVal, String type) {
        // int or char
        // word or byte
        this.name = name;
        this.intVal = intVal;
        this.type = type;
    }

    public GlobalVarInstr(String name, ArrayList<Integer> arrVal, String type, int length) {
        // array
        if (arrVal == null || arrVal.isEmpty()) {
            // zeroInitializer.
            this.intVal = 0;
            this.count = length;
            this.type = type;
            this.name = name;
        } else {
            this.name = name;
            this.arrVal = arrVal;
            this.type = type;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(": .").append(type).append(" ");
        switch (type) {
            case "asciiz":
                sb.append("\"").append(strVal).append("\"");
                break;
            case "word":
            case "byte":
                if (arrVal != null) {
                    for (int i = 0; i < arrVal.size(); i++) {
                        sb.append(arrVal.get(i));
                        if (i != arrVal.size() - 1) {
                            sb.append(", ");
                        }
                    }
                } else if (count != 0) {
                    sb.append(intVal).append(":").append(count);
                } else {
                    sb.append(intVal);
                }
                break;
            default:
                System.err.println("error: unknown type: " + type);
                break;
        }
        return sb.toString();
    }
}
