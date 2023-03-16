//import java.util.concurrent.*;
//
//public class OldIntSuffix {
//    /**
//     * Requires the input for all methods to be an array with
//     * a power-of-two-length. Undefined behaviour otherwise.
//     */
//    static ForkJoinPool POOL = new ForkJoinPool();
//    public static int[] max(int[] input) {
//        int[] output = new int[input.length];
//        IntNode[] leaves = new IntNode[input.length];
//        IntNode root = POOL.invoke(new BuildTreeTask(input, leaves));
//        System.out.println(root);
//        POOL.invoke(new PercolateTask(output, leaves, root, Integer.MIN_VALUE));
//        return output;
//    }
//}
//
//class IntNode {
//    int value, fromRight;
//    IntNode left, right;
//    int lo, hi;
//
//    IntNode(int lo, int hi) {this.lo = lo; this.hi = hi;}
//
//    public String toString() {
//        //String result = "<val: " + value + ", fromPref: " + fromRight + ">";
//        String result = "" + value;
//        result += " [";
//        if (left != null) result += left.toString();
//        if (right != null) result += right.toString();
//        result += "] ";
//        return result;
//    }
//}
//
//class BuildTreeTask extends RecursiveTask<IntNode> {
//    int lo, hi;
//    static IntNode[] leaves;
//    static int[] input;
//
//    public BuildTreeTask(int[] input, IntNode[] leaves) {
//        this(0, input.length);
//        this.input = input;
//        this.leaves = leaves;
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
//        node.value = Math.max(node.left.value, node.right.value);
//        return node;
//    }
//}
//
//class PercolateTask extends RecursiveAction {
//    IntNode node;
//    int fromRight;
//    static int[] output;
//    static IntNode[] leaves;
//
//    public PercolateTask(int[] output, IntNode[] leaves, IntNode node, int fromRight) {
//        this(node, fromRight);
//        this.output = output;
//        this.leaves = leaves;
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
//            output[lo] = Math.max(leaves[lo].fromRight, leaves[lo].value);
//            return;
//        }
//        PercolateTask left  = new PercolateTask(node.left, Math.max(node.fromRight, node.right.value)),
//                right = new PercolateTask(node.right, node.fromRight);
//        left.fork();
//        right.compute();
//        left.join();
//    }
//}