package uca.romain;

import java.util.Arrays;

public class OverflowBitPacking implements BitPacking {

    @Override
    public int[] compress(int[] array) {
        if (array == null) {
            throw new IllegalArgumentException("Le tableau ne peut pas être null");
        }
        if (array.length == 0) {
            throw new IllegalArgumentException("Le tableau ne peut pas être vide");
        }
        if (array.length > (1 << 26) - 1) {
            throw new IllegalArgumentException("Le tableau est trop grand: " + array.length +
                    " éléments (maximum: " + ((1 << 26) - 1) + ")");
        }

        int arraySize = array.length;

        // Calculer les bitLengths
        int[] bitLengths = new int[arraySize];
        for (int i = 0; i < arraySize; i++) {
            if (array[i] < 0) {
                throw new IllegalArgumentException("Les valeurs négatives ne sont pas supportées: " + array[i]);
            }
            bitLengths[i] = array[i] == 0 ? 1 : 32 - Integer.numberOfLeadingZeros(array[i]);
        }

        // Trouver le bitsNeeded optimal
        int bitsNeeded = findBestBitSize(bitLengths);

        if (bitsNeeded > 32) {
            throw new IllegalStateException("bitsNeeded trop grand: " + bitsNeeded);
        }

        // Compter les overflow
        int overflowCount = 0;
        for (int i = 0; i < arraySize; i++) {
            if (bitLengths[i] > bitsNeeded) {
                overflowCount++;
            }
        }

        // Calculer les tailles
        int bitsPerElement = 1 + bitsNeeded;
        int intsForMain = calculateIntsForMain(arraySize, bitsPerElement);
        int overflowStartIndex = 1 + intsForMain;

        // Créer le tableau result
        int[] result = new int[overflowStartIndex + overflowCount];

        // Métadonnées optimisées: arraySize sur 26 bits (bits 6-31), bitsNeeded sur 6 bits (bits 0-5)
        result[0] = (arraySize << 6) | (bitsNeeded & 0x3F);

        // Compression avec 1 boucle et 2 index
        int currentOverflowIndex = 0;

        for (int i = 0; i < arraySize; i++) {
            int flag;
            int valueToStore;

            if (bitLengths[i] > bitsNeeded) {
                // Overflow
                result[overflowStartIndex + currentOverflowIndex] = array[i];
                flag = 1;
                valueToStore = currentOverflowIndex;
                currentOverflowIndex++;
            } else {
                // Normal
                flag = 0;
                valueToStore = array[i];
            }

            // Insérer dans la zone principale
            int combined = (flag << bitsNeeded) | valueToStore;
            int bitPos = i * bitsPerElement;
            int arrayIndex = (bitPos >> 5) + 1;
            int bitOffset = bitPos & 31;

            if (bitOffset + bitsPerElement <= 32) {
                result[arrayIndex] |= (combined << bitOffset);
            } else {
                result[arrayIndex] |= (combined << bitOffset);
                result[arrayIndex + 1] |= (combined >> (32 - bitOffset));
            }
        }

        return result;
    }

    @Override
    public int get(int[] compressedArray, int i) {
        if (compressedArray == null) {
            throw new IllegalArgumentException("Le tableau compressé ne peut pas être null");
        }
        if (compressedArray.length == 0) {
            throw new IllegalArgumentException("Le tableau compressé ne peut pas être vide");
        }

        int arraySize = extractArraySize(compressedArray);
        if (i < 0) {
            throw new IndexOutOfBoundsException("L'index ne peut pas être négatif: " + i);
        }
        if (i >= arraySize) {
            throw new IndexOutOfBoundsException("L'index " + i + " est hors limites (taille: " + arraySize + ")");
        }

        int bitsNeeded = extractBitsNeeded(compressedArray);
        int bitsPerElement = 1 + bitsNeeded;

        // Extraire flag + valeur
        int bitPos = i * bitsPerElement;
        int combined = extractBits(compressedArray, bitPos, bitsPerElement, 1);

        int flag = combined >>> bitsNeeded;
        int value = combined & ((1 << bitsNeeded) - 1);

        if (flag == 0) {
            return value;
        } else {
            // Overflow
            int overflowStartIndex = 1 + calculateIntsForMain(arraySize, bitsPerElement);
            return compressedArray[overflowStartIndex + value];
        }
    }

    @Override
    public int[] decompress(int[] compressedArray) {
        if (compressedArray == null) {
            throw new IllegalArgumentException("Le tableau compressé ne peut pas être null");
        }
        if (compressedArray.length == 0) {
            throw new IllegalArgumentException("Le tableau compressé ne peut pas être vide");
        }

        int arraySize = extractArraySize(compressedArray);
        int bitsNeeded = extractBitsNeeded(compressedArray);
        int[] decompressed = new int[arraySize];

        int bitsPerElement = 1 + bitsNeeded;
        int valueMask = (1 << bitsNeeded) - 1;
        int overflowStartIndex = 1 + calculateIntsForMain(arraySize, bitsPerElement);

        for (int i = 0; i < arraySize; i++) {
            int bitPos = i * bitsPerElement;
            int combined = extractBits(compressedArray, bitPos, bitsPerElement, 1);

            int flag = combined >>> bitsNeeded;
            int value = combined & valueMask;

            decompressed[i] = (flag == 0) ? value : compressedArray[overflowStartIndex + value];
        }

        return decompressed;
    }

    private int extractBits(int[] array, int bitPos, int numBits, int startIndex) {
        int arrayIndex = (bitPos >> 5) + startIndex;
        int bitOffset = bitPos & 31;

        if (bitOffset + numBits <= 32) {
            // Cas simple : bits dans un seul int
            return (array[arrayIndex] >>> bitOffset) & ((1 << numBits) - 1);
        } else {
            // Cas complexe : bits répartis sur deux ints
            int bitsInFirst = 32 - bitOffset;
            return ((array[arrayIndex] >>> bitOffset) & ((1 << bitsInFirst) - 1))
                    | ((array[arrayIndex + 1] & ((1 << (numBits - bitsInFirst)) - 1)) << bitsInFirst);
        }
    }

    private int extractArraySize(int[] compressedArray) {
        return (compressedArray[0] >> 6) & 0x3FFFFFF;
    }

    private int extractBitsNeeded(int[] compressedArray) {
        return compressedArray[0] & 0x3F;
    }

    private int calculateIntsForMain(int arraySize, int bitsPerElement) {
        return ((arraySize * bitsPerElement + 31) >> 5); // >> 5 à la place de /32
    }

    private int findBestBitSize(int[] bitLengths) {
        // Cloner pour ne pas modifier l'original
        int[] sorted = bitLengths.clone();
        Arrays.sort(sorted);

        int totalElements = sorted.length;
        int maxBitLength = sorted[totalElements - 1];

        long smallestTotalSize = Long.MAX_VALUE;
        int bestBitSize = maxBitLength;

        int i = 0;
        while (i < totalElements) {
            int currentBitSize = sorted[i];

            int j = i;
            while (j < totalElements && sorted[j] == currentBitSize) {
                j++;
            }

            int overflowCount = totalElements - j;

            boolean indexFits = (overflowCount == 0) ||
                    ((long) overflowCount <= (1L << currentBitSize));

            if (indexFits) {
                // IMPORTANT: +1 pour le bit de flag
                long baseSize = (long) totalElements * (1 + currentBitSize);
                long overflowSize = (long) overflowCount * 32;
                long totalSize = baseSize + overflowSize;

                if (totalSize < smallestTotalSize) {
                    smallestTotalSize = totalSize;
                    bestBitSize = currentBitSize;
                }
            }

            i = j;
        }

        return bestBitSize;
    }
}