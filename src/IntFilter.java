import java.util.concurrent.RecursiveAction;
import java.util.function.IntPredicate;

public class IntFilter {
    public static int[] filter(int[] input, IntPredicate pred) {
        int[] bitvector = IntMapping.map(x -> pred.test(x) ? 1 : 0, input);
        int[] bitsum = IntTreeFix.prefix(bitvector, Integer::sum);
        int[] output = new int[bitsum[bitsum.length - 1]];
        ParallelIntStream.POOL.invoke(new FilterMapTask(input, output, bitsum, bitvector));
        return output;
    }
}

class FilterMapTask extends RecursiveAction {
    int lo, hi;
    static int[] input, output, bitsum, bitvector;

    public FilterMapTask(int[] input, int[] output, int[] bitsum, int[] bitvector) {
        this(0, input.length);
        FilterMapTask.input = input;
        FilterMapTask.output = output;
        FilterMapTask.bitsum = bitsum;
        FilterMapTask.bitvector = bitvector;
    }

    private FilterMapTask(int lo, int hi) {
        this.lo = lo; this.hi = hi;
    }

    @Override
    protected void compute() {
        if (hi - lo < 2) {
            if (bitvector[lo] == 1) {
                output[bitsum[lo]] = input[lo];
            }
            return;
        }
        int mid = lo + (hi - lo) / 2;
        FilterMapTask left  = new FilterMapTask(lo, mid),
                      right = new FilterMapTask(mid, hi);
        left.fork();
        right.compute();
        left.join();
    }
}