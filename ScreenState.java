public class ScreenState {
    private String stateName;

    ScreenState(String stateName) {
        this.stateName = stateName;
    }

    public void display() {
        // Default display implementation (can be overridden or expanded upon)
        System.out.println("Displaying " + stateName + " screen...");
    }
}
