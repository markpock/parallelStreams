import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.function.*;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class ParallelIntStream implements IntStream {
    static ForkJoinPool POOL = new ForkJoinPool();
    private int[] backing;
    private boolean closed = false;
    private List<Runnable> handlers;

    private ParallelIntStream(int[] backing, List<Runnable> handlers) {
        this.backing = backing;
        this.handlers = handlers;
    }

    public static ParallelIntStream of(int[] backing) {
        return new ParallelIntStream(backing, null);
    }

    @Override
    public ParallelIntStream map(IntUnaryOperator op) {
        if (closed);
        return new ParallelIntStream(IntMapping.map(op, backing), handlers);
    }

    @Override
    public <U> Stream<U> mapToObj(IntFunction<? extends U> mapper) {
        return null;
    }

    @Override
    public LongStream mapToLong(IntToLongFunction mapper) {
        return null;
    }

    @Override
    public DoubleStream mapToDouble(IntToDoubleFunction mapper) {
        return null;
    }

    @Override
    public IntStream flatMap(IntFunction<? extends IntStream> mapper) {
        return null;
    }

    @Override
    public IntStream distinct() {
        return null;
    }

    @Override
    public IntStream sorted() {
        return new ParallelIntStream(IntSorter.sort(backing), handlers);
    }

    @Override
    public IntStream peek(IntConsumer action) {
        return null;
    }

    @Override
    public IntStream limit(long maxSize) {
        if (maxSize >= backing.length) return this;
        int[] result = new int[(int) maxSize];
        POOL.invoke(new IntArrayCopyTask(backing, result, 0, 0));
        return new ParallelIntStream(result, handlers);
    }

    @Override
    public IntStream skip(long n) {
        if (n >= backing.length) return new ParallelIntStream(new int[0], handlers);
        int[] result = new int[(int) (backing.length - n)];
        POOL.invoke(new IntArrayCopyTask(backing, result, 0, 0, (int) n, 0));
        return new ParallelIntStream(result, handlers);
    }

    @Override
    public void forEach(IntConsumer action) {
        forEachOrdered(action);
    }

    @Override
    public void forEachOrdered(IntConsumer action) {
        IntForEach.forEach(action, backing);
        close();
    }

    public ParallelIntStream prefix(IntBinaryOperator function, int defaultElement) {
        return new ParallelIntStream(IntTreeFix.prefix(backing, function, defaultElement), handlers);
    }

    public ParallelIntStream prefix(IntBinaryOperator function) {
        return new ParallelIntStream(IntTreeFix.prefix(backing, function), handlers);
    }

    public ParallelIntStream suffix(IntBinaryOperator function, int defaultElement) {
        return new ParallelIntStream(IntTreeFix.suffix(backing, function, defaultElement), handlers);
    }

    public ParallelIntStream suffix(IntBinaryOperator function) {
        return new ParallelIntStream(IntTreeFix.suffix(backing, function), handlers);
    }

    public ParallelIntStream prefixMax() {
        return prefix(Math::max, Integer.MIN_VALUE);
    }

    public ParallelIntStream suffixMax() {
        return suffix(Math::max, Integer.MIN_VALUE);
    }

    public OptionalInt reduce(IntBinaryOperator op) {
        OptionalInt result = OptionalInt.of(IntReduce.reduce(backing, op));
        close();
        return result;
    }

    @Override
    public <R> R collect(Supplier<R> supplier, ObjIntConsumer<R> accumulator, BiConsumer<R, R> combiner) {
        R result = supplier.get();
        forEachOrdered(x -> accumulator.accept(result, x));
        return result;
    }

    @Override
    public int sum() {
        return reduce(0, Integer::sum);
    }

    @Override
    public OptionalInt min() {
        return reduce(Math::min);
    }

    @Override
    public OptionalInt max() {
        return reduce(Math::max);
    }

    @Override
    public long count() {
        long r = backing.length;
        close();
        return r;
    }

    @Override
    public OptionalDouble average() {
        double size = backing.length;
        double sum = sum();
        return OptionalDouble.of(sum / size);
    }

    @Override
    public IntSummaryStatistics summaryStatistics() {
        IntSummaryStatistics result = new IntSummaryStatistics(backing.length,
                IntReduce.reduce(backing, Math::min),
                IntReduce.reduce(backing, Math::max),
                IntReduce.reduce(backing, Integer::sum));
        close();
        return result;
    }

    public <T> T reduce(BiFunction<Integer, Integer, T> initial, BiFunction<T, T, T> combiner) {
        return IntReduce.reduce(backing, initial, combiner);
    }

    public <T> T reduce(BiFunction<Integer, Integer, T> initial, BiFunction<T, T, T> combiner, int identity) {
        return IntReduce.reduce(backing, initial, combiner, identity);
    }

    @Override
    public boolean anyMatch(IntPredicate predicate) {
        boolean result = map(x -> predicate.test(x) ? 1 : 0).reduce(Integer::sum).getAsInt() > 0;
        close();
        return result;
    }

    @Override
    public boolean allMatch(IntPredicate predicate) {
        boolean result = map(x -> predicate.test(x) ? 1 : 0).reduce(Integer::sum).getAsInt() == backing.length;
        close();
        return result;
    }

    @Override
    public boolean noneMatch(IntPredicate predicate) {
        boolean result = map(x -> predicate.test(x) ? 1 : 0).reduce(Integer::sum).getAsInt() == 0;
        close();
        return result;
    }

    @Override
    public OptionalInt findFirst() {
        OptionalInt result;
        if (backing.length == 0) result = OptionalInt.empty();
        else result = OptionalInt.of(backing[0]);
        close();
        return result;
    }

    @Override
    public OptionalInt findAny() {
        return findFirst();
    }

    @Override
    public LongStream asLongStream() {
        return null;
    }

    @Override
    public DoubleStream asDoubleStream() {
        return null;
    }

    @Override
    public Stream<Integer> boxed() {
        return null;
    }

    @Override
    public IntStream sequential() {
        // We are a parallel stream, so just give it to the util version
        return IntStream.of(backing).sequential();
    }

    @Override
    public IntStream parallel() {
        return this;
    }

    @Override
    public IntStream unordered() {
        return this;
    }

    @Override
    public IntStream onClose(Runnable closeHandler) {
        if (handlers == null) handlers = new ArrayList<>();
        handlers.add(closeHandler);
        return this;
    }

    @Override
    public void close() {
        for (Runnable closeHandler : handlers) closeHandler.run();
        closed = true;
    }

    @Override
    public PrimitiveIterator.OfInt iterator() {
        PrimitiveIterator.OfInt result = new PrimitiveIterator.OfInt() {
            private int i = 0;
            @Override
            public int nextInt() {
                int result = backing[i];
                i++;
                return result;
            }

            @Override
            public boolean hasNext() {
                return i < backing.length;
            }
        };
        close();
        return result;
    }

    @Override
    public Spliterator.OfInt spliterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isParallel() {
        return true;
    }

    @Override
    public int[] toArray() {
        int[] result = backing;
        close();
        return result;
    }

    @Override
    public int reduce(int identity, IntBinaryOperator op) {
        BiFunction<Integer, Integer, Integer> func = op::applyAsInt;
        int num = IntReduce.reduce(backing, func, func, identity);
        close();
        return num;
    }

    @Override
    public ParallelIntStream filter(IntPredicate predicate) {
        return new ParallelIntStream(IntFilter.filter(backing, predicate), handlers);
    }
}
