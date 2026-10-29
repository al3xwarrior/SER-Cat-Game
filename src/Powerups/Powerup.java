package Powerups;

import Builders.FrameBuilder;
import GameObject.Rectangle;
import Level.MapEntityStatus;
import Level.Player;
import Level.MapEntity;
import Level.TileType;
import Utils.Direction;
import Utils.Point;
import java.awt.image.BufferedImage;
import Level.EnhancedMapTile;

public abstract class Powerup extends EnhancedMapTile {
    public Powerup(BufferedImage image, Point location, Rectangle bounds, float scale) {
        super(location.x, location.y, new FrameBuilder(image).withBounds(bounds).withScale(scale).build(), TileType.PASSABLE);
    }

    private boolean collected = false;

    // power-ups dropped by enemies fall down and can't be picked up right away
    private static final float DROP_GRAVITY = 0.5f;
    private static final float DROP_MAX_FALL_SPEED = 10f;
    private static final int DROP_PICKUP_DELAY_FRAMES = 20;
    private boolean isFalling = false;
    private float fallSpeed = 0f;
    private int pickupDelayFramesRemaining = 0;

    public void makeDropped() {
        isFalling = true;
        fallSpeed = 0f;
        pickupDelayFramesRemaining = DROP_PICKUP_DELAY_FRAMES;
    }

    @Override
    public void update(Player player) {
        super.update(player);

        if (isFalling) {
            fallSpeed = Math.min(DROP_MAX_FALL_SPEED, fallSpeed + DROP_GRAVITY);
            moveYHandleCollision(fallSpeed);

            // fell off the map
            if (map != null && getY() > map.getHeightPixels()) {
                this.mapEntityStatus = MapEntityStatus.REMOVED;
                return;
            }
        }

        if (pickupDelayFramesRemaining > 0) {
            pickupDelayFramesRemaining--;
            return;
        }

        if (!collected && intersects(player)) {
            collected = true;
            applyEffect(player);
            this.mapEntityStatus = MapEntityStatus.REMOVED;
        }
    }

    // stop falling when it hits the ground
    @Override
    public void onEndCollisionCheckY(boolean hasCollided, Direction direction, MapEntity entityCollidedWith) {
        if (hasCollided && direction == Direction.DOWN) {
            isFalling = false;
            fallSpeed = 0f;
        }
    }

    protected abstract void applyEffect(Player player);
}
