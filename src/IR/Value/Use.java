package IR.Value;

public class Use {
    private User user;
    private Value usedV;

    public Use(User user, Value usedV) {
        this.user = user;
        this.usedV = usedV;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Use && ((Use) obj).getUser() == user && ((Use) obj).getUsedV() == usedV;
    }

    public User getUser() {
        return user;
    }

    public Value getUsedV() {
        return usedV;
    }
}
