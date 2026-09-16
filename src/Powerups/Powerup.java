package Powerups;

import Builders.FrameBuilder;
import GameObject.Rectangle;
import Level.MapEntityStatus;
import Level.Player;
import Level.TileType;
import Utils.Point;
import java.awt.image.BufferedImage;
import Level.EnhancedMapTile;

public abstract class Powerup extends EnhancedMapTile {
    public Powerup(BufferedImage image, Point location, Rectangle bounds, float scale) {
        super(location.x, location.y, new FrameBuilder(image).withBounds(bounds).withScale(scale).build(), TileType.PASSABLE);
    }

    private boolean collected = false;

    @Override
    public void update(Player player) {
        super.update(player);
        if (!collected && intersects(player)) {
            collected = true;
            applyEffect(player);
            this.mapEntityStatus = MapEntityStatus.REMOVED;
        }
    }

    protected abstract void applyEffect(Player player);
}
