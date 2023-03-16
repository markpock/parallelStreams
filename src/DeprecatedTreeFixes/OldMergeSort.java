//import java.util.Comparator;
//import java.util.concurrent.RecursiveAction;
//
//public class IntSorter {
//    /**
//     * Does some mergesorting...
//     */
//    public static int[] sort(int[] input, Comparator<Integer> c) {
//        int[] aux = new int[input.length];
//        int[] output = new int[input.length];
//        IntStreamOrdered.POOL.invoke(new SortTask(input, c, aux, output));
//    }
//
//    public static int[] sort(int[] input) {
//        return IntSorter.sort(input, Integer::compare);
//    }
//}
//
//class SortTask extends RecursiveAction {
//    int lo, hi;
//    static int[] input, aux, output;
//    static Comparator<Integer> c;
//    public SortTask(int[] input, Comparator<Integer> c, int[] aux, int[] output) {
//        this( 0, input.length);
//        this.output = output;
//        this.input = input;
//        this.aux = aux;
//        this.c = c;
//    }
//
//    private SortTask(int lo, int hi) {
//        this.lo = lo; this.hi = hi;
//    }
//
//    @Override
//    protected void compute() {
//        int mid = lo + (hi - lo) / 2;
//        if (hi - lo >= 2) {
//            SortTask left  = new SortTask(lo, mid),
//                    right = new SortTask(mid, hi);
//            left.fork();
//            right.compute();
//            left.join();
//        }
//        IntStreamOrdered.POOL.invoke(new MergeTask(lo, mid, mid, hi, input, c, aux, lo, hi));
//    }
//}
//
//class MergeTask extends RecursiveAction {
//    int seg1Start, seg1End, seg2Start, seg2End;
//    int writeToLo, writeToHi;
//    static int[] readFrom, writeTo;
//    static Comparator<Integer> c;
//    public MergeTask(int seg1Start, int seg1End, int seg2Start, int seg2End, int[] readFrom, Comparator<Integer> c, int[] writeTo, int writeToLo, int writeToHi) {
//        this(seg1Start, seg1End, seg2Start, seg2End, writeToLo, writeToHi);
//        this.readFrom = readFrom;
//        this.writeTo = writeTo;
//        this.c = c;
//    }
//
//    public MergeTask(int seg1Start, int seg1End, int seg2Start, int seg2End, int writeToLo, int writeToHi) {
//        this.seg1Start = seg1Start;
//        this.seg1End = seg1End;
//        this.seg2Start = seg2Start;
//        this.seg2End = seg2End;
//        this.writeToLo = writeToLo;
//        this.writeToHi = writeToHi;
//    }
//
//    @Override
//    protected void compute() {
//        int seg1Len = seg1End - seg1Start;
//        int seg2Len = seg2End - seg2Start;
//        int startSmall, endSmall, startLarge, endLarge;
//        if (seg1Len >= seg2Len) {
//            startLarge = seg1Start;
//            endLarge = seg1End;
//            startSmall = seg2Start;
//            endSmall = seg2End;
//        } else {
//            startSmall = seg1Start;
//            endSmall = seg1End;
//            startLarge = seg2Start;
//            endLarge = seg2End;
//        }
//        int largeLen = Math.max(seg1Len, seg2Len), smallLen = Math.min(seg1Len, seg2Len);
//        if (largeLen < 3 && smallLen < 3) {
//
//        }
//        int medianLargeLoc = startLarge + (endLarge - startLarge) / 2;
//        int medianLarge = readFrom[medianLargeLoc];
//
////        This is all binary search stuff I am not smart enough to implement, first linear scan.
////        int searchLowBound = startSmall, searchHighBound = endSmall,
////                pos = searchLowBound + (searchHighBound - searchLowBound) / 2;
////        while (searchHighBound - searchLowBound > 1) {
////            if (c.compare(readFrom[pos], medianLarge) > 0) {
////                searchHighBound = pos;
////            } else {
////                searchLowBound = pos;
////            }
////            pos = searchLowBound + (searchHighBound - searchLowBound) / 2;
////        }
//
//        int i = startSmall;
//        for (; i < endSmall; i++) {
//            if (c.compare(readFrom[i], medianLarge) > 0) break;
//        }
//        // Now we have a breakdown:
//        // startSmall | i | endSmall  --- startLarge | medianLargeLoc | endLarge
//        // startLarge -> i m startSmall -> medianLargeLoc | medianLargeLoc -> endLarge m i -> endSmall
//
//        MergeTask left  = new MergeTask(startLarge, i, startSmall, medianLargeLoc),
//                right = new MergeTask(medianLargeLoc, endLarge, i, endSmall);
//        left.fork();
//        right.compute();
//        left.join();
//    }
//}