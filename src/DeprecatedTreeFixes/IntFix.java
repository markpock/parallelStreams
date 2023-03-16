//import java.util.concurrent.*;
//import java.util.function.ToIntBiFunction;
//
//public class IntFix {
//    /**
//     * Requires the input for all methods to be an array with
//     * a power-of-two-length. Undefined behaviour otherwise.
//     *
//     * Defaults to prefix, but can do suffix as well.
//     */
//    static ForkJoinPool POOL = new ForkJoinPool();
//
//    private static int[] run(int[] input, ToIntBiFunction<Integer, Integer> function, int defaultElement, boolean usePrefix) {
//        if (input.length == 0) return new int[]{};
//        int preferredChild = usePrefix ? 0 : 1;
//        int[] output = new int[input.length];
//        IntNode[] leaves = new IntNode[input.length];
//        IntNode root = POOL.invoke(new BuildTreeTask(input, leaves, function, preferredChild));
//        POOL.invoke(new PercolateTask(output, leaves, root, defaultElement, function, preferredChild));
//        return output;
//    }
//
//    public static int[] prefix(int[] input, ToIntBiFunction<Integer, Integer> function, int defaultElement) {
//        return IntFix.run(input, function, defaultElement, true);
//    }
//
//    public static int[] prefix(int[] input, ToIntBiFunction<Integer, Integer> function) {
//        return IntFix.prefix(input, function, 0);
//    }
//
//    public static int[] suffix(int[] input, ToIntBiFunction<Integer, Integer> function, int defaultElement) {
//        return IntFix.run(input, function, defaultElement, false);
//    }
//
//    public static int[] suffix(int[] input, ToIntBiFunction<Integer, Integer> function) {
//        return IntFix.suffix(input, function, 0);
//    }
//
//    public static int[] prefixMax(int[] input) {
//        return IntFix.run(input, Math::max, Integer.MIN_VALUE, true);
//    }
//
//    public static int[] suffixMax(int[] input) {
//        return IntFix.run(input, Math::max, Integer.MIN_VALUE, false);
//    }
//}
//
//class IntNode {
//    int value, fromPreferred;
//    IntNode[] children = new IntNode[2];
//    int lo, hi;
//
//    IntNode(int lo, int hi) {this.lo = lo; this.hi = hi;}
//}
//
//class BuildTreeTask extends RecursiveTask<IntNode> {
//    int lo, hi;
//    static IntNode[] leaves;
//    static int[] input;
//    static ToIntBiFunction<Integer, Integer> function;
//    static int child;
//
//    public BuildTreeTask(int[] input, IntNode[] leaves, ToIntBiFunction<Integer, Integer> function, int preferredChild) {
//        this(0, input.length);
//        this.input = input;
//        this.leaves = leaves;
//        this.function = function;
//        this.child = preferredChild;
//    }
//
//    private BuildTreeTask(int lo, int hi) {
//        this.lo = lo;
//        this.hi = hi;
//    }
//
//    protected IntNode compute() {
//        IntNode node = new IntNode(lo, hi);
//        if (hi - lo < 2) {
//            leaves[lo] = node;
//            node.value = input[lo];
//            return node;
//        }
//        int mid = lo + (hi - lo) / 2;
//        BuildTreeTask left  = new BuildTreeTask(lo, mid),
//                right = new BuildTreeTask(mid, hi);
//        left.fork();
//        node.children[child] = right.compute();
//        node.children[1 - child] = left.join();
//        node.value = function.applyAsInt(node.children[child].value, node.children[1 - child].value);
//        return node;
//    }
//}
//
//class PercolateTask extends RecursiveAction {
//    IntNode node;
//    static int[] output;
//    static IntNode[] leaves;
//    static ToIntBiFunction<Integer, Integer> function;
//    static int child;
//
//    public PercolateTask(int[] output, IntNode[] leaves, IntNode node, int fromRight, ToIntBiFunction<Integer, Integer> function, int preferredChild) {
//        this(node, fromRight);
//        this.output = output;
//        this.leaves = leaves;
//        this.function = function;
//        this.child = preferredChild;
//    }
//
//    private PercolateTask(IntNode node, int fromPreferred) {
//        this.node = node;
//        this.node.fromPreferred = fromPreferred;
//    }
//
//    protected void compute() {
//        int hi = node.hi, lo = node.lo;
//        if (hi - lo < 2) {
//            output[lo] = function.applyAsInt(leaves[lo].fromPreferred, leaves[lo].value);
//            return;
//        }
//        PercolateTask left  = new PercolateTask(node.children[1 - child], function.applyAsInt(node.fromPreferred, node.children[child].value)),
//                right = new PercolateTask(node.children[child], node.fromPreferred);
//        left.fork();
//        right.compute();
//        left.join();
//    }
//}
