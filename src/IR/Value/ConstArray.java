package IR.Value;

import IR.Type.ArrayType;
import IR.Type.IRType;
import IR.Type.IntType;
import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;

import java.util.ArrayList;

public class ConstArray extends ConstValue {
    private ArrayList<ConstValue> elements;
    private IRType elementType;

    public ConstArray(ArrayList<ConstValue> elements, int length) {
        super("", new ArrayType(elements.get(0).getType(), length));
        this.elements = elements;
        this.elementType = elements.get(0).getType();
        while (elements.size() < length) {
            elements.add(new ConstInt(0, elementType));
        }
    }


    public ArrayList<ConstValue> getElements() {
        return elements;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
//        sb.append("[");
//        sb.append(elements.size());
//        sb.append(" x ");
//        sb.append(elementType.toString());
//        sb.append("] ");
        if (elementType == IntType.I8) {
            // char[]
            sb.append("c\"");
            for (int i = 0; i < elements.size(); i++) {
                ConstInt element = (ConstInt) elements.get(i);
                if (element.getValue() == 0) {
                    sb.append("\\00");
                } else {
                    sb.append(element.getValue());
                }
            }
            sb.append("\"");
        } else {
            sb.append("[");
            for (ConstValue constValue : elements) {
                ConstInt element = (ConstInt) constValue;
                sb.append(element.getType().toString());
                sb.append(" ");
                sb.append(element.getValue());
                sb.append(", ");
            }
            sb.delete(sb.length() - 2, sb.length());
            sb.append("]");
        }
        return sb.toString();
    }
}
