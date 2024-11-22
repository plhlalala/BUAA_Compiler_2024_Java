package middleend.LLVM_components;

public class Use {
    private User user;
    private Value value;
    private int pos;

    public Use(User user, Value value, int pos) {
        this.user = user;
        this.value = value;
        this.pos = pos;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }
}
