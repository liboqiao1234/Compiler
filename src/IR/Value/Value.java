package IR.Value;

import IR.Type.ArrayType;
import IR.Type.IRType;

import java.util.ArrayList;

public class Value {
    private IRType type;
    private String name;
    private int id;
    private ArrayList<Use> useList;

    private static int idCounter = 0;

    public Value (String nameIR, IRType type) {
        this.type = type;
        this.name = nameIR;
        this.id = idCounter++;
        this.useList = new ArrayList<>();
    }

    public void addUse(User user) {
        useList.add(new Use(user, this));
    }

    public void removeUse(User user) {

        useList.remove(new Use(user, this));
    }

    public ArrayList<Use> getUseList() {
        return useList;// 所有对当前Value的use情况
    }

    public void replaceLoadValue(Value oldValue, Value newValue) {
        // 其实oldValue就是this，因为load语句返回值就是本身Value
        for (Use use : useList) {
            // 根据use找到User，然后把user语句中的loadValue替换成新的Value
            Instruction instr =(Instruction) use.getUser();
            instr.replaceArgument(oldValue, newValue);
            newValue.addUse(use.getUser());
        }
        useList.clear();
    }

    public void updateType(IRType type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String newName) {
        this.name = newName;
    }

    public IRType getType() {
        return type;
    }

    @Override
    public String toString() {
        return type.toString() + " " + name;
    }
}
