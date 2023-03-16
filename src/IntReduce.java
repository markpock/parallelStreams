import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.function.IntBinaryOperator;

public class IntReduce {
    public static <T> T reduce(int[] input, BiFunction<Integer, Integer, T> initial, BiFunction<T, T, T> combiner, int identity) {
        return ParallelIntStream.POOL.invoke(new IntReduceTask<>(input, initial, combiner, identity));
    }

    public static <T> T reduce(int[] input, BiFunction<Integer, Integer, T> initial, BiFunction<T, T, T> combiner) {
        return ParallelIntStream.POOL.invoke(new IntReduceTask<>(input, initial, combiner,0));
    }

    public static int reduce(int[] input, IntBinaryOperator function) {
        BiFunction<Integer, Integer, Integer> func = function::applyAsInt;
        return ParallelIntStream.POOL.invoke(new IntReduceTask<>(input, func, func, 0));
    }
}

class IntReduceTask<T> extends RecursiveTask<T> {
    int lo, hi;
    static int identity;
    static int[] input;
    BiFunction<Integer, Integer, T> initial;
    BiFunction<T, T, T> combiner;

    public IntReduceTask(int[] input, BiFunction<Integer, Integer, T> initial, BiFunction<T, T, T> combiner, int identity) {
        this(0, input.length, initial, combiner);
        IntReduceTask.input = input;
        IntReduceTask.identity = identity;
    }

    private IntReduceTask(int lo, int hi, BiFunction<Integer, Integer, T> initial, BiFunction<T, T, T> combiner) {
        this.lo = lo; this.hi = hi;
        this.initial = initial;
        this.combiner = combiner;
    }

    @Override
    protected T compute() {
        if (hi - lo < 2) return initial.apply(identity, input[lo]);
        int mid = lo + (hi - lo) / 2;
        IntReduceTask<T> left  = new IntReduceTask<>(lo, mid, initial, combiner),
                         right = new IntReduceTask<>(mid, hi, initial, combiner);
        left.fork();
        T rightVal = right.compute();
        return combiner.apply(left.join(), rightVal);
    }
}
