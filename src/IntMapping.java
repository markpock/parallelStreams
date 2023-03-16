import java.util.concurrent.*;
import java.util.function.IntUnaryOperator;

public class IntMapping {
    public static int[] map(IntUnaryOperator function, int[] input) {
        int[] output = new int[input.length];
        ParallelIntStream.POOL.invoke(new IntMapTask(function, input, output));
        return output;
    }
}

class IntMapTask extends RecursiveAction {
    int lo, hi;
    static int[] input, output;
    static IntUnaryOperator function;
    public IntMapTask(IntUnaryOperator function, int[] input, int[] output) {
        this(0, input.length);
        IntMapTask.input = input;
        IntMapTask.output = output;
        IntMapTask.function = function;
    }

    private IntMapTask(int lo, int hi) {
        this.lo = lo; this.hi = hi;
    }

    @Override
    protected void compute() {
        if (hi - lo < 2) {
            output[lo] = function.applyAsInt(input[lo]);
            return;
        }
        int mid = lo + (hi - lo) / 2;
        IntMapTask left  = new IntMapTask(lo, mid),
                   right = new IntMapTask(mid, hi);
        left.fork();
        right.compute();
        left.join();
    }
}
