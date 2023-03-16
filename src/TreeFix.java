import java.util.concurrent.*;
import java.util.function.BiFunction;

public class TreeFix {
    /**
     * Requires the input for all methods to be an array with
     * a power-of-two-length. Undefined behaviour otherwise.
     * Defaults to prefix, but can do suffix as well.
     */
    static ForkJoinPool POOL = new ForkJoinPool();


    private static <T> T[] run(T[] input, BiFunction<T, T, T> function, T defaultElement, boolean usePrefix) {
        if (input.length == 0) return (T[]) new Object[]{};
        int preferredChild = usePrefix ? 0 : 1;
        T[] output = (T[]) new Object[input.length];

        int height = (int) Math.ceil(Math.log(input.length) / Math.log(2));
        int numNodes = (int) Math.pow(2, height + 1);

        Node<T>[] tree   = (Node<T>[]) new Node[numNodes],
                  leaves = (Node<T>[]) new Node[input.length];
        tree[0] = POOL.invoke(new BuildTreeTask<>(input, tree, leaves, function, preferredChild));
        POOL.invoke(new PercolateTask<>(output, tree, leaves, function, defaultElement, preferredChild));
        return output;
    }

    public static <T> T[] prefix(T[] input, BiFunction<T, T, T> function, T defaultElement) {
        return TreeFix.run(input, function, defaultElement, true);
    }

    public static <T> T[] suffix(T[] input, BiFunction<T, T, T> function, T defaultElement) {
        return TreeFix.run(input, function, defaultElement, false);
    }
}

class Node<T> {
    T value, fromPref;

    public String toString() {
        return "<val: " + value + ", fromPref: " + fromPref + ">";
    }

    static int childIdx(int node, int childNum) {
        return 2 * node + 1 + childNum;
    }
}

class BuildTreeTask<T> extends RecursiveTask<Node<T>> {
    T[] input;
    int lo, hi, idx;
    static int child;
    Node<T>[] tree, leaves;
    BiFunction<T, T, T> function;

    public BuildTreeTask(T[] input, Node<T>[] tree, Node<T>[] leaves, BiFunction<T, T, T> function, int preferredChild) {
        this(input, tree, leaves, function, 0, input.length, 0);
        child = preferredChild;
    }

    private BuildTreeTask(T[] input, Node<T>[] tree, Node<T>[] leaves, BiFunction<T, T, T> function, int lo, int hi, int idx) {
        this.lo = lo;
        this.hi = hi;
        this.idx = idx;
        this.tree = tree;
        this.input = input;
        this.leaves = leaves;
        this.function = function;
    }

    protected Node<T> compute() {
        Node<T> node = new Node<>();
        if (hi - lo < 2) {
            leaves[lo] = node;
            node.value = input[lo];
            return node;
        }
        int mid      = lo + (hi - lo) / 2,
            leftIdx  = Node.childIdx(idx, child),
            rightIdx = Node.childIdx(idx, 1 - child);
        BuildTreeTask<T> left  = new BuildTreeTask<>(input, tree, leaves, function, lo, mid, leftIdx),
                         right = new BuildTreeTask<>(input, tree, leaves, function, mid, hi, rightIdx);
        left.fork();
        tree[rightIdx] = right.compute();
        tree[leftIdx]  = left.join();
        node.value = function.apply(tree[leftIdx].value, tree[rightIdx].value);
        return node;
    }
}

class PercolateTask<T> extends RecursiveAction {
    T[] output;
    int lo, hi, idx;
    static int child;
    Node<T>[] tree, leaves;
    BiFunction<T, T, T> function;

    public PercolateTask(T[] output, Node<T>[] tree, Node<T>[] leaves, BiFunction<T, T, T> function, T fromPref, int preferredChild) {
        this(output, tree, leaves, function, fromPref, 0, output.length, 0);
        this.child = preferredChild;
    }

    private PercolateTask(T[] output, Node<T>[] tree, Node<T>[] leaves, BiFunction<T, T, T> function, T fromPref, int lo, int hi, int idx) {
        this.lo = lo;
        this.hi = hi;
        this.idx = idx;
        this.tree = tree;
        this.leaves = leaves;
        this.output = output;
        this.function = function;
        tree[idx].fromPref = fromPref;
    }

    protected void compute() {
        if (hi - lo < 2) {
            Node<T> leaf = leaves[lo];
            output[lo] = function.apply(leaf.fromPref, leaf.value);
            return;
        }
        int mid      = lo + (hi - lo) / 2,
            leftIdx  = IntNode.childIdx(idx, child),
            rightIdx = IntNode.childIdx(idx, 1 - child);
        T rightFromPref = function.apply(tree[idx].fromPref, tree[leftIdx].value);
        PercolateTask<T> left  = new PercolateTask<>(output, tree, leaves, function, tree[idx].fromPref, lo, mid, leftIdx),
                         right = new PercolateTask<>(output, tree, leaves, function, rightFromPref, mid, hi, rightIdx);
        left.fork();
        right.compute();
        left.join();
    }
}
