package Level;

// part of a map that shows a hint on the screen while the player is in it
// like telling the player to use the time warp
public class HintZone {
    private final float startX;
    private final float endX;
    private final String text;

    // startX and endX are in pixels
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
