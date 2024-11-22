package middleend.LLVM_components;

public class BlockNameProvider {
    private static final BlockNameProvider provider = new BlockNameProvider();
    private static int count = 0;

    private BlockNameProvider() {
    }

    public static BlockNameProvider getProvider() {
        return provider;
    }

    public String alloc() {
        String name = Integer.toString(count);
        count++;
        return name;
    }

}
