/**
 * Repräsentiert den aktuellen Zustand des Spiels für Q-Learning.
 * Enthält 8 Boolean-Werte die Gefahren, Apfel-Position und Tail-Gefahr relativ zur Snake beschreiben.
 */
public class GameState {
    private final boolean dangerAhead;
    private final boolean dangerLeft;
    private final boolean dangerRight;
    
    private final boolean appleAhead;
    private final boolean appleLeft;
    private final boolean appleRight;
    
    private final boolean tailDangerLeft;
    private final boolean tailDangerRight;
    
    /**
     * Erstellt einen neuen GameState.
     *
     * @param dangerAhead Gefahr direkt vor der Snake (Wand oder Körper)
     * @param dangerLeft Gefahr links von der Snake (Wand oder Körper)
     * @param dangerRight Gefahr rechts von der Snake (Wand oder Körper)
     * @param appleAhead Apfel liegt vor der Snake
     * @param appleLeft Apfel liegt links von der Snake
     * @param appleRight Apfel liegt rechts von der Snake
     * @param tailDangerLeft Eigener Körper in 2-3 Tiles Entfernung links
     * @param tailDangerRight Eigener Körper in 2-3 Tiles Entfernung rechts
     */
    public GameState(boolean dangerAhead, boolean dangerLeft, boolean dangerRight,
                     boolean appleAhead, boolean appleLeft, boolean appleRight,
                     boolean tailDangerLeft, boolean tailDangerRight) {
        this.dangerAhead = dangerAhead;
        this.dangerLeft = dangerLeft;
        this.dangerRight = dangerRight;
        this.appleAhead = appleAhead;
        this.appleLeft = appleLeft;
        this.appleRight = appleRight;
        this.tailDangerLeft = tailDangerLeft;
        this.tailDangerRight = tailDangerRight;
    }
    
    public boolean isDangerAhead() { return dangerAhead; }
    public boolean isDangerLeft() { return dangerLeft; }
    public boolean isDangerRight() { return dangerRight; }
    public boolean isAppleAhead() { return appleAhead; }
    public boolean isAppleLeft() { return appleLeft; }
    public boolean isAppleRight() { return appleRight; }
    public boolean isTailDangerLeft() { return tailDangerLeft; }
    public boolean isTailDangerRight() { return tailDangerRight; }
    
    /**
     * Berechnet Hash-Code basierend auf allen 8 Boolean-Werten.
     * Jeder Boolean trägt eine Potenz von 2 bei (Bit-Maske).
     *
     * @return Hash-Code für HashMap-Verwendung
     */
    @Override
    public int hashCode() {
        int result = 0;
        if (dangerAhead) result += 1;
        if (dangerLeft) result += 2;
        if (dangerRight) result += 4;
        if (appleAhead) result += 8;
        if (appleLeft) result += 16;
        if (appleRight) result += 32;
        if (tailDangerLeft) result += 64;
        if (tailDangerRight) result += 128;
        return result;
    }
    
    /**
     * Vergleicht zwei GameStates auf Gleichheit.
     *
     * @param obj zu vergleichendes Objekt
     * @return true wenn alle Boolean-Werte identisch sind
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        GameState other = (GameState) obj;
        return dangerAhead == other.dangerAhead &&
               dangerLeft == other.dangerLeft &&
               dangerRight == other.dangerRight &&
               appleAhead == other.appleAhead &&
               appleLeft == other.appleLeft &&
               appleRight == other.appleRight &&
               tailDangerLeft == other.tailDangerLeft &&
               tailDangerRight == other.tailDangerRight;
    }
    
    @Override
    public String toString() {
        return String.format("State[D:%s%s%s A:%s%s%s T:%s%s]",
            dangerAhead ? "F" : "-",
            dangerLeft ? "L" : "-",
            dangerRight ? "R" : "-",
            appleAhead ? "F" : "-",
            appleLeft ? "L" : "-",
            appleRight ? "R" : "-",
            tailDangerLeft ? "L" : "-",
            tailDangerRight ? "R" : "-");
    }
}
