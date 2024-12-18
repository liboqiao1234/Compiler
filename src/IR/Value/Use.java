package IR.Value;

public class Use {
    private User user;
    private Value usedV;

    public Use(User user, Value usedV) {
        this.user = user;
        this.usedV = usedV;
    }

    public User getUser() {
        return user;
    }

    public Value getUsedV() {
        return usedV;
    }
}
