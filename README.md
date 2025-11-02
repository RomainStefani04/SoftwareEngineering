# Bit Packing Compression - Java Project

## Description du projet
Ce projet a été réalisé dans le cadre du cours Software Engineering du Master 1 Informatique à l’Université Côte d’Azur.  
Il implémente plusieurs méthodes de compression d'entiers par Bit Packing, afin de réduire la taille des tableaux d’entiers tout en permettant un accès direct à leurs éléments sans décompression complète.

Trois méthodes sont disponibles :
- BASIC : compression standard avec chevauchement.
- NO_OVERLAP : version sans chevauchement.
- OVERFLOW : version optimisée pour les tableaux contenant des valeurs aberrantes (outliers).

Un module de benchmark est inclus pour comparer leurs performances.

---

## Architecture du projet

### Structure principale
- `BitPacking` : interface commune (compress, decompress, get)
- `BasicBitPacking`, `NoOverlapBitPacking`, `OverflowBitPacking` : implémentations concrètes
- `BitPackingFactory` : pattern Factory pour instancier les méthodes
- `Benchmark` : exécute des tests de performance
- `Main` : interface en ligne de commande

---

## Installation et exécution

### 1. Cloner le dépôt
```bash
git clone https://github.com/RomainStefani04/SoftwareEngineering.git
cd SoftwareEngineering
```

### 2. Compiler le projet
```bash
javac -d out $(find ./src -name "*.java")
```

### 3. Lancer le programme
```bash
java -cp out uca.romain.Main
```

---

## Utilisation

Au lancement, le programme affiche le menu suivant :

```
1. Lancer le benchmark
2. Utiliser BASIC
3. Utiliser NO_OVERLAP
4. Utiliser OVERFLOW
5. Quitter
```

### 1. Lancer le benchmark
Exécute automatiquement un test de performance sur les trois méthodes :
- Temps de compression
- Temps de décompression
- Temps d’accès direct (get)
- Taille compressée

### 2 à 4. Utiliser une méthode manuellement
Permet de saisir un tableau d'entiers sous la forme :
```
1,2,3,1024,4,5,2048
```

Le programme :
- affiche le tableau et sa taille en bits,
- compresse le tableau et affiche le gain d’espace,
- propose un sous-menu :
  - 1 : Décompresser
  - 2 : Accéder à un élément
  - 3 : Afficher le tableau compressé
  - 4 : Revenir au menu principal

---

## Résumé des méthodes

| Méthode | Principe | Avantage principal |
|----------|-----------|--------------------|
| BASIC | Compression standard avec chevauchement | Bon équilibre général |
| NO_OVERLAP | Aucun chevauchement | Accès rapide, plus simple |
| OVERFLOW | Gestion d'une zone de débordement | Efficace avec des outliers |

---

## Gestion d’erreurs
- Valeurs négatives interdites
- Taille maximale : environ 67 millions d’éléments
- Vérification des index lors des accès directs

---

## Auteur
Romain STEFANI  
Master 1 Informatique – Université Côte d’Azur  
Année universitaire 2025–2026

---

## Référence
Projet GitHub : [https://github.com/RomainStefani04/SoftwareEngineering](https://github.com/RomainStefani04/SoftwareEngineering)
