//import java.util.concurrent.*;
//import java.util.function.ToIntBiFunction;
//
//public class IntSuffix {
//    /**
//     * Requires the input for all methods to be an array with
//     * a power-of-two-length. Undefined behaviour otherwise.
//     */
//    static ForkJoinPool POOL = new ForkJoinPool();
//    public static int[] max(int[] input) {
//        int[] output = new int[input.length];
//        IntNode[] leaves = new IntNode[input.length];
//        IntNode root = POOL.invoke(new BuildTreeTask(input, leaves, Math::max));
//        POOL.invoke(new PercolateTask(output, leaves, root, Integer.MIN_VALUE, Math::max));
//        return output;
//    }
//
//    public static int[] run(int[] input, ToIntBiFunction<Integer, Integer> function, int defaultElement) {
//        int[] output = new int[input.length];
//        IntNode[] leaves = new IntNode[input.length];
//        IntNode root = POOL.invoke(new BuildTreeTask(input, leaves, function));
//        POOL.invoke(new PercolateTask(output, leaves, root, defaultElement, function));
//        return output;
//    }
//
//    public static int[] run(int[] input, ToIntBiFunction<Integer, Integer> function) {
//        return IntSuffix.run(input, function, 0);
//    }
//}
//
//class IntNode {
//    int value, fromRight;
//    IntNode left, right;
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
//
//    public BuildTreeTask(int[] input, IntNode[] leaves, ToIntBiFunction<Integer, Integer> function) {
//        this(0, input.length);
//        this.input = input;
//        this.leaves = leaves;
//        this.function = function;
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
//        node.right = right.compute();
//        node.left = left.join();
//        node.value = function.applyAsInt(node.left.value, node.right.value);
//        return node;
//    }
//}
//
//class PercolateTask extends RecursiveAction {
//    IntNode node;
//    static int[] output;
//    static IntNode[] leaves;
//    static ToIntBiFunction<Integer, Integer> function;
//
//    public PercolateTask(int[] output, IntNode[] leaves, IntNode node, int fromRight, ToIntBiFunction<Integer, Integer> function) {
//        this(node, fromRight);
//        this.output = output;
//        this.leaves = leaves;
//        this.function = function;
//    }
//
//    private PercolateTask(IntNode node, int fromRight) {
//        this.node = node;
//        this.node.fromRight = fromRight;
//    }
//
//    protected void compute() {
//        int hi = node.hi, lo = node.lo;
//        if (hi - lo < 2) {
//            output[lo] = function.applyAsInt(leaves[lo].fromRight, leaves[lo].value);
//            return;
//        }
//        PercolateTask left  = new PercolateTask(node.left, function.applyAsInt(node.fromRight, node.right.value)),
//                right = new PercolateTask(node.right, node.fromRight);
//        left.fork();
//        right.compute();
//        left.join();
//    }
//}
