# Minispire

Ein spielbares Konsolen-Deckbuilding-Spiel auf Basis der abstrahierten Kernmechaniken von *Slay the Spire*.

## Starten

Voraussetzungen: Java 21 und Maven.

```powershell
mvn compile
java -cp target/classes dev.minispire.App
```

Standardmäßig öffnet sich die grafische Swing-Oberfläche. In VS Code kann `App.java` auch mit `Strg+F5`
gestartet werden. Die klassische Terminalansicht bleibt über folgenden Befehl verfügbar:

```powershell
java -cp target/classes dev.minispire.App --console
```

## Steuerung

- Zahlen und Befehle werden in das Eingabefeld am unteren Fensterrand eingegeben und mit Enter bestätigt.
- Entscheidungen werden über die angezeigten Nummern getroffen.
- Im Kampf spielt eine Nummer die entsprechende Handkarte.
- `0` beendet den Spielerzug.
- `D` zeigt im Kampf das dauerhafte Deck und die Relikte an.

Das Spiel umfasst drei Akte, sechs Knotentypen, mehrere Gegner, Kartenbelohnungen, Relikte, Rastplätze,
Kartenverbesserungen sowie Stärke, Schwäche, Verwundbarkeit und Gift.
