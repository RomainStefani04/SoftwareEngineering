package uca.romain;

import java.util.Random;

public class Benchmark {

    public void run() {
        System.out.println("-------------BitPacking - Benchmark-------------");

        int[] sizes = {1000, 10000, 100000};

        for (int size : sizes) {
            testSize(size);
        }
    }

    public void testSize(int size) {
        System.out.println("\n-----------------------------------------------");
        System.out.println("Taille du tableau: " + size + " entiers");

        System.out.println("\nUniforme (0-100)");
        test(generateUniform(size, 100));

        System.out.println("\nAvec outliers (2% grandes valeurs)");
        test(generateWithOutliers(size, 100, 1000000, 0.02));

        System.out.println("\nGrandes valeurs (0-100000)");
        test(generateUniform(size, 100000));
    }

    public void test(int[] data) {
        System.out.println("------------------------------------------------");
        System.out.println("Méthode \t| Compress \t| Decomp \t| Get \t| Taille \t| Gain");

        testMethod("BASIC", data);
        testMethod("NO_OVERLAP", data);
        testMethod("OVERFLOW", data);
    }

    private void testMethod(String method, int[] data) {
        BitPacking packing = BitPackingFactory.createBitPacking(method);

        for (int i = 0; i < 200; i++) {
            packing.compress(data);
        }

        long totalCompress = 0;
        int[] compressed = null;
        for (int i = 0; i < 500; i++) {
            long start = System.nanoTime();
            compressed = packing.compress(data);
            totalCompress += System.nanoTime() - start;
        }
        long avgCompress = totalCompress / 500;

        long totalDecompress = 0;
        for (int i = 0; i < 500; i++) {
            long start = System.nanoTime();
            packing.decompress(compressed);
            totalDecompress += System.nanoTime() - start;
        }
        long avgDecompress = totalDecompress / 500;

        Random rand = new Random(42);
        long totalGet = 0;
        for (int i = 0; i < 500; i++) {
            for (int j = 0; j < 10; j++) {
                int idx = rand.nextInt(data.length);
                long start = System.nanoTime();
                packing.get(compressed, idx);
                totalGet += System.nanoTime() - start;
            }
        }
        long avgGet = totalGet / 5000;

        int bitsOriginal = data.length * 32;
        int bitsCompressed = compressed.length * 32;
        int bitsSaved = bitsOriginal - bitsCompressed;
        double percentSaved = (100.0 * bitsSaved) / bitsOriginal;

        System.out.println(method + " \t| " +
                avgCompress / 1000.0 + " \t| " +
                avgDecompress / 1000.0 + " \t| " +
                (double) avgGet + " \t| " +
                compressed.length + " ints \t| " +
                bitsSaved + " bits (" +
                percentSaved);
    }

    public static int[] generateUniform(int size, int maxValue) {
        int[] array = new int[size];
        Random rand = new Random(42);
        for (int i = 0; i < size; i++) {
            array[i] = rand.nextInt(maxValue + 1);
        }
        return array;
    }

    public static int[] generateWithOutliers(int size, int normalMax, int outlierMax, double outlierRatio) {
        int[] array = new int[size];
        Random rand = new Random(42);
        for (int i = 0; i < size; i++) {
            if (rand.nextDouble() < outlierRatio) {
                array[i] = normalMax + rand.nextInt(outlierMax - normalMax + 1);
            } else {
                array[i] = rand.nextInt(normalMax + 1);
            }
        }
        return array;
    }
}