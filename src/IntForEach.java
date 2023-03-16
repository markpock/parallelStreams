import java.util.concurrent.*;
import java.util.function.IntConsumer;

public class IntForEach {
    public static void forEach(IntConsumer function, int[] input) {
        ParallelIntStream.POOL.invoke(new IntForEachTask(function, input));
    }
}

class IntForEachTask extends RecursiveAction {
    int lo, hi;
    static int[] input, output;
    static IntConsumer function;
    public IntForEachTask(IntConsumer function, int[] input) {
        this(0, input.length);
        IntForEachTask.input = input;
        IntForEachTask.output = output;
        IntForEachTask.function = function;
    }

    private IntForEachTask(int lo, int hi) {
        this.lo = lo; this.hi = hi;
    }

    @Override
    protected void compute() {
        if (hi - lo < 2) {
            function.accept(input[lo]);
            return;
        }
        int mid = lo + (hi - lo) / 2;
        IntForEachTask left  = new IntForEachTask(lo, mid),
                right = new IntForEachTask(mid, hi);
        left.fork();
        right.compute();
        left.join();
    }
}
