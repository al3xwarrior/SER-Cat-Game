package Powerups;

import Engine.ImageLoader;
import GameObject.Rectangle;
import Level.Player;
import Utils.Point;

import java.awt.image.BufferedImage;

// Grants the player temporary invincibility and visually equips them with a hockey stick for the duration
public class HockeyStickPowerup extends Powerup {
    private static final int INVINCIBILITY_DURATION_FRAMES = 600; // 10 seconds at the game's target 60 FPS

    private final BufferedImage hockeyStickImage;

    public HockeyStickPowerup(Point location) {
        this(location, ImageLoader.load("HockeyStick.png"));
    }

    private HockeyStickPowerup(Point location, BufferedImage hockeyStickImage) {
        super(hockeyStickImage, location, new Rectangle(0, 0, 16, 16), 3);
        this.hockeyStickImage = hockeyStickImage;
    }

    @Override
    protected void applyEffect(Player player) {
        player.makeInvincible(INVINCIBILITY_DURATION_FRAMES);
        player.equipItem(hockeyStickImage);
    }
}
