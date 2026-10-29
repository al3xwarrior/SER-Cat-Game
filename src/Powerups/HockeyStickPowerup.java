package Powerups;

import Engine.ImageLoader;
import GameObject.Rectangle;
import Level.Enemy;
import Level.Player;
import Utils.Point;

import java.awt.image.BufferedImage;

// Grants the player temporary invincibility and visually equips them with a hockey stick for the duration
public class HockeyStickPowerup extends Powerup {
    private static final int INVINCIBILITY_DURATION_FRAMES = 240; // 4 seconds at the game's target 60 FPS

    private final BufferedImage hockeyStickImage;

    public HockeyStickPowerup(Point location) {
        this(location, ImageLoader.load("HockeyStick.png"));
    }

    private HockeyStickPowerup(Point location, BufferedImage hockeyStickImage) {
        super(hockeyStickImage, location, new Rectangle(0, 0, 16, 16), 3);
        this.hockeyStickImage = hockeyStickImage;
    }

    // creates a hockey stick that drops out of the given enemy when it is defeated
    public static HockeyStickPowerup droppedBy(Enemy enemy) {
        // the stick is 16x16 scaled by 3, center it on the enemy and line its bottom up with the enemy's bottom
        int size = 16 * 3;
        float x = enemy.getX() + enemy.getWidth() / 2f - size / 2f;
        float y = enemy.getY() + enemy.getHeight() - size;

        HockeyStickPowerup hockeyStick = new HockeyStickPowerup(new Point(x, y));
        hockeyStick.makeDropped();
        return hockeyStick;
    }

    @Override
    protected void applyEffect(Player player) {
        player.makeInvincible(INVINCIBILITY_DURATION_FRAMES);
        player.equipItem(hockeyStickImage);
    }
}
