package Powerups;

import Engine.ImageLoader;
import GameObject.Rectangle;
import Level.Enemy;
import Level.Player;
import Utils.Point;

import java.awt.image.BufferedImage;

// hockey stick power-up, makes the player invincible for a few seconds
public class HockeyStickPowerup extends Powerup {
    private static final int INVINCIBILITY_DURATION_FRAMES = 240; // 4 seconds

    private final BufferedImage hockeyStickImage;

    public HockeyStickPowerup(Point location) {
        this(location, ImageLoader.load("HockeyStick.png"));
    }

    private HockeyStickPowerup(Point location, BufferedImage hockeyStickImage) {
        super(hockeyStickImage, location, new Rectangle(0, 0, 16, 16), 3);
        this.hockeyStickImage = hockeyStickImage;
    }

    // makes a hockey stick that drops from a dead enemy
    public static HockeyStickPowerup droppedBy(Enemy enemy) {
        // stick is 16x16 scaled by 3, put it in the middle of the enemy
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
