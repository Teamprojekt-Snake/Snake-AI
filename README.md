# Snake AI - Q-Learning Implementation

Snake-Spiel mit Reinforcement Learning (Q-Learning) Implementierung.

## Features

- **Manual Play**: Klassisches Snake-Spiel mit Tastatursteuerung
- **AI Training**: Trainiere die KI mit Q-Learning Algorithmus
- **AI Play**: Schaue der trainierten KI beim Spielen zu

## Q-Learning Details

### Algorithmus
- **Epsilon-Greedy Strategy** mit Decay
- **Learning Rate (α)**: 0.1
- **Discount Factor (γ)**: 0.95
- **Epsilon Decay**: 0.9995
- **Epsilon Minimum**: 0.01

### State Representation
8 Boolean-Werte:
- Gefahr vorne/links/rechts (Wand oder Körper)
- Apfel vorne/links/rechts
- Tail-Gefahr links/rechts (Körper in 2-3 Tiles Entfernung)

### Rewards
- Apfel gegessen: +10.0
- Tod: -10.0
- Näher zum Apfel: +0.1 × Distanzverbesserung
- Weiter vom Apfel: -0.1 × Distanzverschlechterung

## Training

### Empfohlene Trainingsdauer
- **50.000 - 60.000 Games** für optimale Performance
- Training dauert ca. 3 Minuten (Headless Mode)
- **Peak Performance**: ~50k-60k Games
  - Avg Score: ~28
  - Max Score: ~100
  - States Learned: ~192 (von 256 möglich)

### Training Modes
1. **Headless (empfohlen)**: ~350 games/sec (~20.000 games/min), keine GUI
2. **Mit GUI**: ~60 games/min, zum Zuschauen

## Usage

### Kompilieren
```bash
javac -d out/production/SWM src/main/java/*.java
```

### Starten
```bash
java -cp out/production/SWM Snake_GUI
```

### Steuerung (Manual Mode)
- **Pfeiltasten**: Snake bewegen
- **ESC**: Pause-Menü

## Datenspeicherung

Trainierte Q-Tables und Statistiken werden in `data/` gespeichert:
- `qtable.txt`: Q-Table (State-Action Werte)
- `session_stats.txt`: Letzte Session Statistiken
- `training_history.txt`: Alle Trainings-Sessions

## Performance

### Headless Training
- **0-10k Games**: Exploration Phase (ε: 1.0 → 0.37)
- **10k-40k Games**: Learning Phase (ε: 0.37 → 0.02)
- **40k-60k Games**: Peak Performance (ε: 0.02 → 0.01)

Nach ~60k Games ist die AI vollständig trainiert.

## Technische Details

- **Java Swing** für GUI
- **HashMap** für Q-Table Speicherung
- **Synchronized Methods** für Thread-Safety (Headless Mode)
- **File-based Persistence** (Text Format)

## Projekt Struktur

```
src/main/java/
├── Snake_GUI.java          # Hauptfenster & Mode Selection
├── BoardPanel.java         # Spielfeld & Logik
├── GameState.java          # State Representation
├── QLearningAgent.java     # Q-Learning Algorithmus
├── QTableStorage.java      # Q-Table Persistenz
├── TrainingStats.java      # Statistik Tracking
└── GameLogger.java         # Logging Utility
```

## Entwicklungshinweise

Diese README-Dokumentation wurde mithilfe von Claude Sonnet 4.5 verbessert und verfeinert.
