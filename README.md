# VéloCoach 🚴

App Android native (Kotlin + Jetpack Compose) pour créer et planifier des entraînements vélo structurés, dans l'esprit de Runna — orientée séances à l'intensité (% FTP), façon Zwift/TrainerRoad.

## Fonctionnalités (MVP)

- **Planning hebdomadaire** : vue semaine (lundi → dimanche), navigation entre les semaines, assignation d'une séance à un jour, retrait, jour de repos.
- **Bibliothèque de séances** : liste des séances créées avec durée totale, intensité moyenne (% FTP) et nombre de blocs. Suppression.
- **Créateur de séances structurées** : assemblage de blocs
  - Échauffement, Continu, Intervalles (répétitions + récupération), Retour au calme
  - durée, plage de puissance min/max en % FTP pour chaque bloc
  - profil de puissance dessiné en direct (Canvas), durée et intensité moyennes recalculées en temps réel.
- **Détail de séance** : profil, statistiques, structure bloc par bloc, modification, suppression.
- **100 % local** : persistance Room, aucune connexion requise. Trois séances d'exemple sont créées au premier lancement.

## Stack technique

| Élément | Choix |
|---|---|
| Langage | Kotlin 1.9 |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM (ViewModel + StateFlow + Repository) |
| Persistance | Room (KSP) |
| Navigation | Navigation Compose |
| Min SDK | 26 (Android 8.0) |

## Structure

```
app/src/main/java/com/velocoach/app/
├── MainActivity.kt              # Navigation + barre d'onglets
├── VeloCoachApp.kt              # Application, DB, repository, données d'exemple
├── data/
│   ├── Model.kt                 # Entités Room (séance, bloc, planning) + relations
│   ├── AppDatabase.kt           # Base + DAOs
│   └── WorkoutRepository.kt     # Accès données
├── domain/
│   └── WorkoutMath.kt           # Durées, intensité moyenne, formatage, semaines
└── ui/
    ├── components/              # Profil de puissance (Canvas), labels de blocs
    ├── plan/                    # Écran Semaine (planning)
    ├── library/                 # Écran Séances (bibliothèque)
    ├── detail/                  # Écran Détail
    ├── editor/                  # Éditeur de séance (blocs + intervalles)
    └── theme/                   # Thème Material 3
```

## Compiler et lancer

1. Ouvrir le dossier dans **Android Studio** (Koala ou plus récent).
2. Laisser Gradle synchroniser (le wrapper télécharge Gradle 8.7).
3. Lancer la configuration `app` sur un émulateur ou un appareil (API 26+).

En ligne de commande : `./gradlew assembleDebug` (le binaire APK est généré dans `app/build/outputs/apk/debug/`).

## Feuille de route

1. **FTP personnel** : écran de réglage du FTP pour afficher les watts réels en plus des %.
2. **Suivi GPS** (comme Strava) : enregistrement de sortie en fond, carte, distance/vitesse.
3. **Capteurs Bluetooth** : home trainers FTMS, capteurs puissance/cadence/cardio.
4. **Guide d'exécution** : lecteur de séance pas-à-pas avec alertes de transitions.
5. **Export** : fichiers .fit / .zwo pour importer les séances dans un GPS Garmin ou Zwift.
6. **Backend** (si besoin plus tard) : comptes et synchronisation via Firebase ou Supabase.
