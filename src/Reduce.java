import java.util.concurrent.*;
import java.util.function.BiFunction;

public class Reduce {
    static ForkJoinPool POOL = new ForkJoinPool();
    public static <I, O> O reduce(I[] input, BiFunction<I, I, O> initial, BiFunction<O, O, O> combiner, I identity) {
        return POOL.invoke(new ReduceTask<>(input, initial, combiner, identity));
    }

    public static <T> T reduce(T[] input, BiFunction<T, T, T> function, T identity) {
        return POOL.invoke(new ReduceTask<>(input, function, function, identity));
    }
}

class ReduceTask<I, O> extends RecursiveTask<O> {
    int lo, hi;
    I identity;
    I[] input;
    BiFunction<I, I, O> initial;
    BiFunction<O, O, O> combiner;

    public ReduceTask(I[] input, BiFunction<I, I, O> initial, BiFunction<O, O, O> combiner, I identity) {
        this(0, input.length, input, initial, combiner, identity);

    }

    private ReduceTask(int lo, int hi, I[] input, BiFunction<I, I, O> initial, BiFunction<O, O, O> combiner, I identity) {
        this.lo = lo; this.hi = hi;
        this.input = input;
        this.identity = identity;
        this.initial = initial;
        this.combiner = combiner;
    }

    @Override
    protected O compute() {
        if (hi - lo < 2) return initial.apply(identity, input[lo]);
        int mid = lo + (hi - lo) / 2;
        ReduceTask<I, O> left  = new ReduceTask<>(lo, mid, input, initial, combiner, identity),
                         right = new ReduceTask<>(mid, hi, input, initial, combiner, identity);
        left.fork();
        O rightVal = right.compute();
        return combiner.apply(left.join(), rightVal);
    }
}
