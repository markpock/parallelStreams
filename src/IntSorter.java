import java.util.Comparator;
import java.util.concurrent.RecursiveAction;

public class IntSorter {
    /**
     * Does some mergesorting...
     */
    public static int[] sort(int[] input, Comparator<Integer> c) {
        int[] aux = new int[input.length];
        IntArrayCopyTask.parallel(input, aux, 0, input.length);
        int[] output = new int[input.length];
        ParallelIntStream.POOL.invoke(new SortTask(0, input.length, aux, output));
        return output;
    }

    public static int[] sort(int[] input) {
        return IntSorter.sort(input, Integer::compare);
    }
}

class SortTask extends RecursiveAction {
    int[] src, dst;
    int lo, hi;

    public SortTask(int lo, int hi, int[] src, int[] dst) {
        this.lo = lo;
        this.hi = hi;
        this.src = src;
        this.dst = dst;
    }

    @Override
    protected void compute() {
        MergeTask task;
        int mid = lo + (hi - lo) / 2;
        if (hi - lo < 3) {
            task = new MergeTask(new Region(lo, mid), new Region(mid, hi), new Region(lo, hi), src, dst);
            task.compute();
            return;
        }
        SortTask left  = new SortTask(lo, mid, src, dst),
                 right = new SortTask(mid, hi, src, dst);
        left.fork();
        right.compute();
        left.join();
        task = new MergeTask(new Region(lo, mid), new Region(mid, hi), new Region(lo, hi), dst, src);
        task.compute();
        IntArrayCopyTask.parallel(src, dst, lo, hi);
    }
}

class Region {
    int start, end;
    public Region(int start, int end) { this.start = start; this.end = end; }
    public int length() { return end - start; }
    public static Region max(Region first, Region second) {
        if (first.length() >= second.length()) return first;
        return second;
    }
    public static Region min(Region first, Region second) {
        if (first.length() >= second.length()) return second;
        return first;
    }
    public int median() { return start + (end - start) / 2; }
}

class MergeTask extends RecursiveAction {
    Region first, second, writer;
    int[] src, dst;
    public MergeTask(Region first, Region second, Region writer, int[] src, int[] dst) {
        this.first = first;
        this.second = second;
        this.writer = writer;
        this.src = src;
        this.dst = dst;
    }

    @Override
    protected void compute() {
        if (first.length() == 0 || second.length() == 0) {
            Region write;
            if (first.length() == 0) write = second;
            else write = first;
            assert write.length() == writer.length();
            for (int srcN = write.start, dstN = writer.start; srcN < write.end; srcN++, dstN++) {
                dst[dstN] = src[srcN];
            }
            return;
        } else if (first.length() == 1 && second.length() == 1) {
            assert writer.length() == 2;
            int firstNum = Math.min(src[first.start], src[second.start]);
            int secNum = Math.max(src[first.start], src[second.start]);
            dst[writer.start] = firstNum;
            dst[writer.end - 1] = secNum;
            return;
        }
        Region large = Region.max(first, second);
        Region small = Region.min(first, second);

        int medLoc = large.median();
        int splitLoc = scan(src, small, src[medLoc]);

        Region r1m = new Region(large.start, medLoc);
        Region r2m = new Region(small.start, splitLoc);
        Region writerM = new Region(writer.start, writer.start + r1m.length() + r2m.length());

        Region r1n = new Region(medLoc, large.end);
        Region r2n = new Region(splitLoc, small.end);
        Region writerN = new Region(writer.end - r1n.length() - r2n.length(), writer.end);

        MergeTask left = new MergeTask(r1m, r2m, writerM, src, dst);
        MergeTask right = new MergeTask(r1n, r2n, writerN, src, dst);

        left.fork();
        right.compute();
        left.join();
    }

    public static int scan(int[] arr, Region r, int check) {
        int i = r.start;
        for (; i < r.end; i++) {
            if (arr[i] >= check) break;
        }
        return i;
    }
}


class IntArrayCopyTask extends RecursiveAction {
    public static final int CUTOFF = 1;
    private final int[] src, dst;
    private final int lo, hi;
    private final int srcStart, dstStart;

    public IntArrayCopyTask(int[] src, int[] dst, int lo, int hi, int srcStart, int dstStart) {
        this.src = src;
        this.dst = dst;
        this.lo = lo;
        this.hi = hi;
        this.srcStart = srcStart;
        this.dstStart = dstStart;
    }

    public IntArrayCopyTask(int[] src, int[] dst, int lo, int hi) {
        this(src, dst, lo, hi, 0, 0);
    }

    @SuppressWarnings("ManualArrayCopy")
    protected void compute() {
        if (hi - lo <= CUTOFF) {
            for (int i = lo; i < hi; i++) dst[i + dstStart] = src[i + srcStart];
            return;
        }
        int mid = lo + (hi - lo) / 2;
        IntArrayCopyTask left  = new IntArrayCopyTask(src, dst, lo, mid),
                right = new IntArrayCopyTask(src, dst, mid, hi);
        left.fork();
        right.compute();
        left.join();
    }

    public static void parallel(int[] src, int[] dst, int lo, int hi) {
        ParallelIntStream.POOL.invoke(new IntArrayCopyTask(src, dst, lo, hi));
    }
}