package uca.romain;

public class BasicBitPacking implements BitPacking {

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

        // Trouver le nombre maximum de bits nécessaires
        int maxValue = 0;
        for (int value : array) {
            if (value < 0) {
                throw new IllegalArgumentException("Les valeurs négatives ne sont pas supportées: " + value);
            }
            maxValue = Math.max(maxValue, value);
        }
        int bitsNeeded = maxValue == 0 ? 1 : 32 - Integer.numberOfLeadingZeros(maxValue);

        // Calculer la taille du tableau compressé
        long totalBits = (long) arraySize * bitsNeeded;
        int numInts = (int) ((totalBits + 31) / 32);
        int[] result = new int[1 + numInts];

        // Métadonnées optimisées: arraySize sur 26 bits (bits 6-31), bitsNeeded sur 6 bits (bits 0-5)
        result[0] = (arraySize << 6) | (bitsNeeded & 0x3F);

        // Compression avec possibilité de chevauchement
        for (int i = 0; i < arraySize; i++) {
            int bitPos = i * bitsNeeded;
            int arrayIndex = (bitPos / 32) + 1;
            int bitOffset = bitPos % 32;

            if (bitOffset + bitsNeeded <= 32) {
                // Tout tient dans un seul int
                result[arrayIndex] |= (array[i] << bitOffset);
            } else {
                // Déborde sur deux ints
                result[arrayIndex] |= (array[i] << bitOffset);
                result[arrayIndex + 1] |= (array[i] >>> (32 - bitOffset));
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
        int bitsNeeded = extractBitsNeeded(compressedArray);

        if (i < 0) {
            throw new IndexOutOfBoundsException("L'index ne peut pas être négatif: " + i);
        }
        if (i >= arraySize) {
            throw new IndexOutOfBoundsException("L'index " + i + " est hors limites (taille: " + arraySize + ")");
        }

        int bitPos = i * bitsNeeded;
        int arrayIndex = (bitPos / 32) + 1;
        int bitOffset = bitPos % 32;

        int mask = (1 << bitsNeeded) - 1;

        if (bitOffset + bitsNeeded <= 32) {
            // Tout dans un seul int
            return (compressedArray[arrayIndex] >>> bitOffset) & mask;
        } else {
            // Réparti sur deux ints
            int bitsInFirst = 32 - bitOffset;
            int firstPart = (compressedArray[arrayIndex] >>> bitOffset) & ((1 << bitsInFirst) - 1);
            int secondPart = compressedArray[arrayIndex + 1] & ((1 << (bitsNeeded - bitsInFirst)) - 1);
            return firstPart | (secondPart << bitsInFirst);
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

        int mask = (1 << bitsNeeded) - 1;

        for (int i = 0; i < arraySize; i++) {
            int bitPos = i * bitsNeeded;
            int arrayIndex = (bitPos / 32) + 1;
            int bitOffset = bitPos % 32;

            if (bitOffset + bitsNeeded <= 32) {
                // Tout dans un seul int
                decompressed[i] = (compressedArray[arrayIndex] >>> bitOffset) & mask;
            } else {
                // Réparti sur deux ints
                int bitsInFirst = 32 - bitOffset;
                int firstPart = (compressedArray[arrayIndex] >>> bitOffset) & ((1 << bitsInFirst) - 1);
                int secondPart = compressedArray[arrayIndex + 1] & ((1 << (bitsNeeded - bitsInFirst)) - 1);
                decompressed[i] = firstPart | (secondPart << bitsInFirst);
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