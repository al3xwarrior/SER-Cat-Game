package Powerups;

import Engine.ImageLoader;
import GameObject.Rectangle;
import Level.Player;
import Utils.Point;

public class PlaceholderPowerup extends Powerup {
    public PlaceholderPowerup(Point location) {
        super(ImageLoader.load("Powerup.png"), location, new Rectangle(0, 0, 16, 16), 3);
    }

    @Override
    protected void applyEffect(Player player) {
        System.out.println("Picked up a placeholder power-up!");
    }
}
