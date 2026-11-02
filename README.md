# Mon Budget

Petite application Android pour suivre son budget mensuel.

## Principe

1. Saisir le **salaire** du mois.
2. Lister ses **charges fixes** récurrentes (loyer, box, assurances…), cochables quand elles sont payées.
3. L'app calcule le **disponible** : salaire − charges fixes.
4. Ajouter les **dépenses ponctuelles** au fil du mois (essence, courses…), chacune datée ;
   le **solde restant** se met à jour à chaque dépense.

## Fonctions

- Un enregistrement **par mois**, avec navigation ◀ ▶ entre les mois.
- Les **charges fixes se reportent** automatiquement d'un mois sur l'autre (statut « payé » remis à zéro).
- **Ajouter, modifier, supprimer** une charge fixe ou une dépense ponctuelle.
- Le solde s'affiche en **rouge** quand il passe sous zéro.
- Tout est enregistré sur le téléphone (aucune connexion, aucun compte).

## Installation

L'APK est construit automatiquement par GitHub Actions à chaque envoi de code.

1. Ouvrir l'onglet **Actions** du dépôt → dernier build réussi.
2. Télécharger l'artefact **mon-budget-apk**.
3. Décompresser le `.zip`, puis installer le fichier `app-debug.apk` sur le téléphone
   (autoriser l'installation d'apps depuis cette source si demandé).

## Technique

| Élément | Valeur |
|---|---|
| Package | `fr.olbino.budget` |
| minSdk / targetSdk | 26 / 35 |
| Langage | Kotlin |
| Stockage | SharedPreferences (JSON), local |
| Dépendances | AndroidX AppCompat, Material 3 |

## Fichiers principaux

| Fichier | Rôle |
|---|---|
| `MainActivity.kt` | Écran unique : mois, salaire, listes, calculs, dialogues |
| `Model.kt` | Données d'un mois (salaire, charges fixes, dépenses) |
| `Store.kt` | Sauvegarde/lecture par mois |
