import java.util.concurrent.*;
import java.util.function.IntBinaryOperator;

public class IntTreeFix {

    private static int[] run(int[] input, IntBinaryOperator function, int defaultElement, boolean usePrefix) {
        if (input.length == 0) return new int[]{};
        int preferredChild = usePrefix ? 0 : 1;
        int[] output = new int[input.length];

        int height = (int) Math.ceil(Math.log(input.length) / Math.log(2));
        int numNodes = (int) Math.pow(2, height + 1);

        IntNode[] tree   = new IntNode[numNodes],
                  leaves = new IntNode[input.length];
        tree[0] = ParallelIntStream.POOL.invoke(new IntBuildTreeTask(input, tree, function, preferredChild, leaves));
        ParallelIntStream.POOL.invoke(new IntPercolateTask(output, tree, leaves, function, preferredChild, defaultElement));
        return output;
    }

    public static int[] prefix(int[] input, IntBinaryOperator function, int defaultElement) {
        return IntTreeFix.run(input, function, defaultElement, true);
    }

    public static int[] prefix(int[] input, IntBinaryOperator function) {
        return IntTreeFix.prefix(input, function, 0);
    }

    public static int[] suffix(int[] input, IntBinaryOperator function, int defaultElement) {
        return IntTreeFix.run(input, function, defaultElement, false);
    }

    public static int[] suffix(int[] input, IntBinaryOperator function) {
        return IntTreeFix.suffix(input, function, 0);
    }

    public static int[] prefixMax(int[] input) {
        return IntTreeFix.run(input, Math::max, Integer.MIN_VALUE, true);
    }

    public static int[] suffixMax(int[] input) {
        return IntTreeFix.run(input, Math::max, Integer.MIN_VALUE, false);
    }

    public static int[] max(int[] input) {
        return IntTreeFix.run(input, Math::max, Integer.MIN_VALUE, true);
    }
}

class IntNode {
    int value, fromPref;

    public String toString() {
        return "<val: " + value + ", fromPref: " + fromPref + ">";
    }

    static int childIdx(int node, int childNum) {
        return 2 * node + 1 + childNum;
    }
}

class IntBuildTreeTask extends RecursiveTask<IntNode> {
    int lo, hi, idx;
    static int[] input;
    static int child;
    static IntNode[] tree, leaves;
    static IntBinaryOperator function;

    public IntBuildTreeTask(int[] input, IntNode[] tree, IntBinaryOperator function, int preferredChild, IntNode[] leaves) {
        this(0, input.length, 0);
        IntBuildTreeTask.tree = tree;
        IntBuildTreeTask.input = input;
        IntBuildTreeTask.leaves = leaves;
        IntBuildTreeTask.function = function;
        IntBuildTreeTask.child = preferredChild;
    }

    private IntBuildTreeTask(int lo, int hi, int idx) {
        this.lo = lo;
        this.hi = hi;
        this.idx = idx;
    }

    protected IntNode compute() {
        IntNode node = new IntNode();
        if (hi - lo < 2) {
            leaves[lo] = node;
            node.value = input[lo];
            return node;
        }
        int mid      = lo + (hi - lo) / 2,
            leftIdx  = IntNode.childIdx(idx, child),
            rightIdx = IntNode.childIdx(idx, 1 - child);
        IntBuildTreeTask left  = new IntBuildTreeTask(lo, mid, leftIdx),
                      right = new IntBuildTreeTask(mid, hi, rightIdx);
        left.fork();
        tree[rightIdx] = right.compute();
        tree[leftIdx]  = left.join();
        node.value = function.applyAsInt(tree[leftIdx].value, tree[rightIdx].value);
        return node;
    }
}

class IntPercolateTask extends RecursiveAction {
    int lo, hi, idx;
    static int child;
    static int[] output;
    static IntNode[] tree, leaves;
    static IntBinaryOperator function;

    public IntPercolateTask(int[] output, IntNode[] tree, IntNode[] leaves, IntBinaryOperator function, int preferredChild, int fromPref) {
        this(0, output.length, Integer.MIN_VALUE, 0); // fromPref won't get set here
        IntPercolateTask.tree = tree;
        IntPercolateTask.leaves = leaves;
        IntPercolateTask.output = output;
        IntPercolateTask.function = function;
        IntPercolateTask.child = preferredChild;
        tree[idx].fromPref = fromPref;
    }

    private IntPercolateTask(int lo, int hi, int fromPref, int idx) {
        if (tree != null) tree[idx].fromPref = fromPref;
        this.idx = idx;
        this.hi = hi;
        this.lo = lo;
    }

    protected void compute() {
        if (hi - lo < 2) {
            IntNode leaf = leaves[lo];
            output[lo] = function.applyAsInt(leaf.fromPref, leaf.value);
            return;
        }
        int mid      = lo + (hi - lo) / 2,
            leftIdx  = IntNode.childIdx(idx, child),
            rightIdx = IntNode.childIdx(idx, 1 - child);
        int rightFromPref = function.applyAsInt(tree[idx].fromPref, tree[leftIdx].value);
        IntPercolateTask left  = new IntPercolateTask(tree[idx].fromPref, leftIdx, lo, mid),
                      right = new IntPercolateTask(rightFromPref, rightIdx, mid, hi);
        left.fork();
        right.compute();
        left.join();
    }
}
