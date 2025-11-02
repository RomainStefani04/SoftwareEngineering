package uca.romain;

public class NoOverlapBitPacking implements BitPacking {

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

        int maxValue = 0;
        for (int value : array) {
            if (value < 0) {
                throw new IllegalArgumentException("Les valeurs négatives ne sont pas supportées: " + value);
            }
            maxValue = Math.max(maxValue, value);
        }
        int maxBitsNeeded = maxValue == 0 ? 1 : 32 - Integer.numberOfLeadingZeros(maxValue);

        // Calcul de la taille : 1 seule variable temporaire
        int elementsPerInt = 32 / maxBitsNeeded;
        int[] result = new int[1 + (arraySize + elementsPerInt - 1) / elementsPerInt];

        // Métadonnées optimisées: arraySize sur 26 bits (bits 6-31), maxBitsNeeded sur 6 bits (bits 0-5)
        result[0] = (arraySize << 6) | (maxBitsNeeded & 0x3F);

        int currentArrayIndex = 1;
        int currentBitOffset = 0;

        for (int i = 0; i < arraySize; i++) {
            if (currentBitOffset + maxBitsNeeded <= 32) {
                result[currentArrayIndex] |= (array[i] << currentBitOffset);
                currentBitOffset += maxBitsNeeded;
            } else {
                currentArrayIndex++;
                result[currentArrayIndex] = array[i];
                currentBitOffset = maxBitsNeeded;
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
        int maxBitsNeeded = extractBitsNeeded(compressedArray);

        if (i < 0) {
            throw new IndexOutOfBoundsException("L'index ne peut pas être négatif: " + i);
        }
        if (i >= arraySize) {
            throw new IndexOutOfBoundsException("L'index " + i + " est hors limites (taille: " + arraySize + ")");
        }

        int elementsPerInt = 32 / maxBitsNeeded;
        int arrayIndex = 1 + (i / elementsPerInt);
        int positionInInt = i % elementsPerInt;
        int bitOffset = positionInInt * maxBitsNeeded;

        int mask = (1 << maxBitsNeeded) - 1;
        return (compressedArray[arrayIndex] >>> bitOffset) & mask;
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
        int maxBitsNeeded = extractBitsNeeded(compressedArray);
        int[] decompressed = new int[arraySize];

        int elementsPerInt = 32 / maxBitsNeeded;
        int mask = (1 << maxBitsNeeded) - 1;
        int fullInts = arraySize / elementsPerInt;
        int remaining = arraySize % elementsPerInt;

        int outputIndex = 0;

        // Boucle principale optimale (aucun branchement)
        for (int arrayIndex = 1; arrayIndex <= fullInts; arrayIndex++) {
            int bitOffset = 0;
            for (int j = 0; j < elementsPerInt; j++) {
                decompressed[outputIndex++] = (compressedArray[arrayIndex] >>> bitOffset) & mask;
                bitOffset += maxBitsNeeded;
            }
        }

        // Dernier int partiel
        if (remaining > 0) {
            int bitOffset = 0;
            for (int j = 0; j < remaining; j++) {
                decompressed[outputIndex++] = (compressedArray[fullInts + 1] >>> bitOffset) & mask;
                bitOffset += maxBitsNeeded;
            }
        }

        return decompressed;
    }

    private int extractArraySize(int[] compressedArray) {
        return (compressedArray[0] >> 6) & 0x3FFFFFF;
    }

    private int extractBitsNeeded(int[] compressedArray) {
        return compressedArray[0] & 0x3F;
    }
}