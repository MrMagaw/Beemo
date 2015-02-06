package pokersquares.trainers;

public interface Trainer {
    public abstract void runSession(long millis);
    public abstract void update();
}
