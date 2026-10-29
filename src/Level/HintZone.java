package Level;

// A horizontal stretch of a map that shows a short hint on screen while the player is inside it
// (e.g. telling the player they can use the time warp right before a section built around it)
public class HintZone {
    private final float startX;
    private final float endX;
    private final String text;

    // startX and endX are in pixels (map coordinates)
    public HintZone(float startX, float endX, String text) {
        this.startX = startX;
        this.endX = endX;
        this.text = text;
    }

    public boolean contains(Player player) {
        float playerCenterX = player.getX() + player.getWidth() / 2f;
        return playerCenterX >= startX && playerCenterX <= endX;
    }

    public String getText() {
        return text;
    }
}
