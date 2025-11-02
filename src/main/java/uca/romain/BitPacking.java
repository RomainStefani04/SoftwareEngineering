package uca.romain;

public interface BitPacking {

    int[] compress(int[] array);
    int[] decompress(int[] compressedArray);
    int get( int[] compressedArray, int i);
}
