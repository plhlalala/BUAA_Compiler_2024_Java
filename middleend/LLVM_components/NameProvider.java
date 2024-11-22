package middleend.LLVM_components;

public class NameProvider {
    private static final NameProvider provider = new NameProvider();
    private static int count = 0;

    private NameProvider() {
    }

    public static NameProvider getProvider() {
        return provider;
    }

    public String alloc() {
        String name = Integer.toString(count);
        count++;
        return name;
    }

}
