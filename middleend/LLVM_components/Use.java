package middleend.LLVM_components;

public class Use {
    private User user;
    private IrValue irValue;
    private int pos;

    public Use(User user, IrValue irValue, int pos) {
        this.user = user;
        this.irValue = irValue;
        this.pos = pos;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public IrValue getValue() {
        return irValue;
    }

    public void setValue(IrValue irValue) {
        this.irValue = irValue;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }
}
