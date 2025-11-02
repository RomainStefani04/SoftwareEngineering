package uca.romain;

public class BitPackingFactory {

    public enum CompressionType {
        BASIC,
        NO_OVERLAP,
        OVERFLOW
    }

    public static BitPacking createBitPacking(CompressionType type) {
        if (type == null) {
            throw new IllegalArgumentException("Le type de compression ne peut pas être null");
        }

        return switch (type) {
            case BASIC -> new BasicBitPacking();
            case NO_OVERLAP -> new NoOverlapBitPacking();
            case OVERFLOW -> new OverflowBitPacking();
            default -> throw new IllegalArgumentException("Type de compression non supporté: " + type);
        };
    }

    public static BitPacking createBitPacking(String typeName) {
        if (typeName == null || typeName.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du type ne peut pas être null ou vide");
        }

        try {
            CompressionType type = CompressionType.valueOf(typeName.toUpperCase());
            return createBitPacking(type);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Type de compression invalide: " + typeName +
                    ". Types valides: BASIC, NO_OVERLAP, OVERFLOW");
        }
    }
}