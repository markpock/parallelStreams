import java.util.concurrent.*;
import java.util.function.Function;

public class Mapping {
    static ForkJoinPool POOL = new ForkJoinPool();
    public static <I, O> O[] map(Function<I, O> function, I[] input) {
        O[] output = (O[]) new Object[input.length];
        POOL.invoke(new MapTask<>(function, input, output));
        return output;
    }
}

class MapTask<I, O> extends RecursiveAction {
    int lo, hi;
    I[] input; O[] output;
    Function<I, O> function;
    public MapTask(Function<I, O> function, I[] input, O[] output) {
        this(0, input.length, function, input, output);
    }

    private MapTask(int lo, int hi, Function<I, O> function, I[] input, O[] output) {
        this.lo = lo; this.hi = hi;
        this.input = input;
        this.output = output;
        this.function = function;
    }

    @Override
    protected void compute() {
        if (hi - lo < 2) output[lo] = function.apply(input[lo]);
        int mid = lo + (hi - lo) / 2;
        MapTask<I, O> left  = new MapTask<>(lo, mid, function, input, output),
                      right = new MapTask<>(mid, hi, function, input, output);
        left.fork();
        right.compute();
        left.join();
    }
}
