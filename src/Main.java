import java.util.Arrays;
import java.util.OptionalInt;
import java.util.Random;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;

public class Main {
    public static void main(String[] args) {
        int[] input = new int[]{14, 50, 38, 29, 6, 12, 3, 5, 1};
        //int[] input = new int[]{5, 3, 12, 6, 29, 38, 50, 14};
        //int[] input = new int[]{0, 4, 6, 8, 9, 1, 2, 3, 5, 7};
        //System.out.println(Arrays.toString(IntTreeFix.max(input)));
        checker(input, x -> x * x + 2, Integer::sum);
        for (int i = 1; i <= 100; i++) {
            input = random(i);
            int finalI = i;
            checker(input, x -> x * x - finalI, Math::max);
        }
        System.out.println(Arrays.toString(IntSorter.sort(input)));
        IntStream x = IntStream.of(input);
        x.reduce(Integer::sum);
        x.toArray();
    }

    static void checker(int[] input, IntUnaryOperator mapping, IntBinaryOperator reducer) {
        System.out.println("Beginning a run:");
        System.out.print("\t");
        System.out.println(Arrays.toString(input));

        IntStream stream = IntStream.of(input);
        ParallelIntStream streamOrdered = ParallelIntStream.of(input);

        int[] s11 = stream.map(mapping).toArray();
        int[] s21 = streamOrdered.map(mapping).toArray();

        assertArrsEqual(s11, s21);

        System.out.print("\t");
        System.out.println(Arrays.toString(s11));

        int[] wau = Arrays.copyOf(s21, s21.length);
        Arrays.sort(wau);
        int[] woah = IntSorter.sort(s11);
        assertArrsEqual(woah, wau);
        System.out.println(Arrays.toString(woah));

        stream = IntStream.of(s11);
        streamOrdered = ParallelIntStream.of(s21);

        OptionalInt val1opt = stream.reduce(reducer);
        assert val1opt.isPresent();
        int val1 = val1opt.getAsInt();
        int val2 = streamOrdered.reduce(reducer).getAsInt();

        assert val1 == val2;
        System.out.println("Reduced value: " + val1);
    }

    static void assertArrsEqual(int[] arr1, int[] arr2) {
        assert arr1.length == arr2.length;
        for (int i = 0; i < arr1.length; i++) {
            assert arr1[i] == arr2[i];
        }
    }

    static int[] random(int size) {
        int[] array = new int[size];
        Random rand = new Random();
        for (int i = 0; i < array.length; i++) {
            array[i] = rand.nextInt(100);
        }
        return array;
    }
}











