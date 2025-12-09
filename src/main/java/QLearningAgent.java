import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class QLearningAgent {
    
    private final Map<GameState, double[]> qTable = new HashMap<>();
    
    private final double learningRate = 0.1;
    private final double discountFactor = 0.95;
    private double epsilon = 1.0;
    private final double epsilonDecay = 0.9995;
    private final double epsilonMin = 0.01;
    
    private final Random random = new Random();
    
    private static final int ACTION_STRAIGHT = 0;
    private static final int ACTION_LEFT = 1;
    private static final int ACTION_RIGHT = 2;
    private static final int NUM_ACTIONS = 3;
    
    private GameState lastState = null;
    private int lastAction = -1;
    
    /**
     * Wählt eine Action basierend auf dem aktuellen State.
     * Epsilon-Greedy: manchmal random (exploration), manchmal beste Action (exploitation).
     *
     * @param state aktueller GameState
     * @return gewählte Action (0=geradeaus, 1=links, 2=rechts)
     */
    public int chooseAction(GameState state) {
        if (random.nextDouble() < epsilon) {
            return random.nextInt(NUM_ACTIONS);
        } else {
            return getBestAction(state);
        }
    }
    
    private int getBestAction(GameState state) {
        double[] qValues = getQValues(state);
        
        int bestAction = 0;
        double bestValue = qValues[0];
        
        for (int i = 1; i < NUM_ACTIONS; i++) {
            if (qValues[i] > bestValue) {
                bestValue = qValues[i];
                bestAction = i;
            }
        }
        
        return bestAction;
    }
    
    private double[] getQValues(GameState state) {
        return qTable.computeIfAbsent(state, k -> new double[NUM_ACTIONS]);
    }
    
    /**
     * Lernt aus einer Erfahrung.
     * Q-Learning Update-Formel: Q(s,a) = Q(s,a) + α * (reward + γ * maxQ(s') - Q(s,a))
     *
     * @param currentState aktueller State
     * @param action gewählte Action
     * @param reward erhaltener Reward
     * @param nextState nächster State
     */
    public void learn(GameState currentState, int action, double reward, GameState nextState) {
        double[] currentQValues = getQValues(currentState);
        double[] nextQValues = getQValues(nextState);
        
        double maxNextQ = nextQValues[0];
        for (int i = 1; i < NUM_ACTIONS; i++) {
            if (nextQValues[i] > maxNextQ) {
                maxNextQ = nextQValues[i];
            }
        }
        
        double oldQ = currentQValues[action];
        double newQ = oldQ + learningRate * (reward + discountFactor * maxNextQ - oldQ);
        currentQValues[action] = newQ;
        
        GameLogger.fine(String.format("Learning: %s -> Action %d -> Reward %.1f -> New Q: %.2f",
                currentState, action, reward, newQ));
    }
    
    public void updateLastExperience(GameState state, int action) {
        this.lastState = state;
        this.lastAction = action;
    }
    
    public void processReward(double reward, GameState newState) {
        if (lastState != null && lastAction != -1) {
            learn(lastState, lastAction, reward, newState);
        }
    }
    
    public void decayEpsilon() {
        if (epsilon > epsilonMin) {
            epsilon *= epsilonDecay;
        }
    }
    
    public int getBestActionForPlay(GameState state) {
        return getBestAction(state);
    }
    
    public int getQTableSize() {
        return qTable.size();
    }
    
    public double getEpsilon() {
        return epsilon;
    }
    
    public Map<GameState, double[]> getQTable() {
        return qTable;
    }
}
