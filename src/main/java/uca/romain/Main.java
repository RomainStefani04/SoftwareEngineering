package uca.romain;

import java.util.Scanner;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n---------------------------------------------------");
            System.out.println("\n1. Lancer le benchmark");
            System.out.println("2. Utiliser BASIC");
            System.out.println("3. Utiliser NO_OVERLAP");
            System.out.println("4. Utiliser OVERFLOW");
            System.out.println("5. Quitter");
            System.out.print("\nVotre choix: ");

            int choice = scanner.nextInt();

            if (choice == 1) {
                Benchmark benchmark = new Benchmark();
                benchmark.run();
            } else if (choice >= 2 && choice <= 4) {
                String method = choice == 2 ? "BASIC" : (choice == 3 ? "NO_OVERLAP" : "OVERFLOW");
                useMethod(scanner, method);
            } else if (choice == 5) {
                break;
            } else {
                System.out.println("\nChoix invalide");
            }
        }

        scanner.close();
    }

    private static void useMethod(Scanner scanner, String methodName) {
        BitPacking packing = BitPackingFactory.createBitPacking(methodName);

        System.out.println("\n----- " + methodName + " -----");
        System.out.println("\nEntrez votre tableau:");
        System.out.println("Format: nombre1,nombre2,nombre3,...");
        System.out.println("Exemple: 1,2,3,1024,4,5,2048");
        System.out.print("\nTableau: ");
        scanner.nextLine();
        String input = scanner.nextLine();

        String[] parts = input.split(",");
        int[] array = new int[parts.length];
        try {
            for (int i = 0; i < parts.length; i++) {
                array[i] = Integer.parseInt(parts[i].trim());
            }
        } catch (NumberFormatException e) {
            System.out.println("\nErreur: format invalide");
            return;
        }

        System.out.println("\nTableau original: " + Arrays.toString(array));
        System.out.println("Taille: " + array.length + " éléments = " + (array.length * 32) + " bits");

        int[] compressed = null;
        try {
            compressed = packing.compress(array);
            System.out.println("\nCompression réussie !");
            System.out.println("Tableau compressé: " + compressed.length + " ints = " + (compressed.length * 32) + " bits");
            int bitsSaved = (array.length - compressed.length) * 32;
            double percent = (100.0 * bitsSaved) / (array.length * 32);
            System.out.println("Gain: " + bitsSaved + " bits (" + String.format("%.1f", percent) + "%)");
        } catch (Exception e) {
            System.out.println("\nErreur lors de la compression: " + e.getMessage());
            return;
        }

        while (true) {
            System.out.println("\n--- Que voulez-vous faire ? ---");
            System.out.println("1. Décompresser");
            System.out.println("2. Accéder à un élément (get)");
            System.out.println("3. Afficher le tableau compressé");
            System.out.println("4. Revenir au menu principal");
            System.out.print("\nChoix: ");

            int action = scanner.nextInt();

            if (action == 1) {
                try {
                    int[] decompressed = packing.decompress(compressed);
                    System.out.println("\nDécompression réussie !");
                    System.out.println("Tableau décompressé: " + Arrays.toString(decompressed));

                    if (Arrays.equals(array, decompressed)) {
                        System.out.println("Vérification: identique au tableau original !");
                    } else {
                        System.out.println("ERREUR: différent du tableau original !");
                    }
                } catch (Exception e) {
                    System.out.println("\nErreur lors de la décompression: " + e.getMessage());
                }

            } else if (action == 2) {
                System.out.print("\nIndex à récupérer (0 à " + (array.length - 1) + "): ");
                int index = scanner.nextInt();

                try {
                    int value = packing.get(compressed, index);
                    System.out.println("\nValeur à l'index " + index + ": " + value);
                    System.out.println("Valeur originale: " + array[index]);

                    if (value == array[index]) {
                        System.out.println("Correct !");
                    } else {
                        System.out.println("ERREUR: valeur incorrecte !");
                    }
                } catch (Exception e) {
                    System.out.println("\nErreur: " + e.getMessage());
                }

            } else if (action == 3) {
                System.out.println("\nTableau compressé (" + compressed.length + " ints):");
                System.out.print("[");
                for (int i = 0; i < Math.min(compressed.length, 20); i++) {
                    System.out.print(compressed[i]);
                    if (i < compressed.length - 1) System.out.print(", ");
                }
                if (compressed.length > 20) {
                    System.out.print(", ... (+" + (compressed.length - 20) + " éléments)");
                }
                System.out.println("]");

            } else if (action == 4) {
                break;
            } else {
                System.out.println("\nChoix invalide");
            }
        }
    }
}