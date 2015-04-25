package pokersquares.trainers;

import java.util.Map;

public interface Trainer {
    public abstract void runSession(long millis);
    public abstract String getName();
    public abstract Map getBestPatterns();
}
