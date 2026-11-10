package Enemies;

import Builders.FrameBuilder;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.ImageEffect;
import GameObject.SpriteSheet;
import Level.Enemy;
import Level.MapTile;
import Level.Player;
import Level.TileType;
import Utils.Direction;
import Utils.Point;

import java.util.HashMap;

// fish only lives in water, swims back and forth and chases the player when they get in the water
public class FishEnemy extends Enemy {
    private static final float WANDER_SPEED = 0.8f;
    private static final float CHASE_RANGE = 10 * 48;

    private final float chaseSpeed;
    private final float leftBound;
    private final float rightBound;
    private Direction facingDirection = Direction.LEFT;

    public FishEnemy(Point start, Point end, float chaseSpeed) {
        super(start.x, start.y, 1, new SpriteSheet(ImageLoader.load("FishEnemy.png"), 20, 12), "SWIM_LEFT");
        this.leftBound = start.x;
        this.rightBound = end.x;
        this.chaseSpeed = chaseSpeed;
        this.contactDamage = 15;
        this.killHealAmount = 5;
        this.initialize();
    }

    @Override
    public void update(Player player) {
        float centerX = getX() + getWidth() / 2f;
        float centerY = getY() + getHeight() / 2f;
        float playerCenterX = player.getX() + player.getWidth() / 2f;
        float playerCenterY = player.getY() + player.getHeight() / 2f;
        float distance = (float) Math.hypot(playerCenterX - centerX, playerCenterY - centerY);

        float moveX;
        float moveY = 0f;

        // chase the player if they're in the water and close enough
        if (player.isInWater() && distance < CHASE_RANGE && distance > 0) {
            moveX = (playerCenterX - centerX) / distance * chaseSpeed;
            moveY = (playerCenterY - centerY) / distance * chaseSpeed;
        }
        // otherwise just swim back and forth
        else {
            if (getX() <= leftBound) {
                facingDirection = Direction.RIGHT;
            } else if (getX() + getWidth() >= rightBound) {
                facingDirection = Direction.LEFT;
            }
            moveX = facingDirection == Direction.RIGHT ? WANDER_SPEED : -WANDER_SPEED;
        }

        // only move if the fish would still be in the water
        float frontX = moveX > 0 ? getX() + getWidth() + moveX : getX() + moveX;
        if (moveX != 0 && isWater(frontX, centerY)) {
            moveXHandleCollision(moveX);
        }
        float edgeY = moveY > 0 ? getY() + getHeight() + moveY : getY() + moveY;
        if (moveY != 0 && isWater(centerX, edgeY)) {
            moveYHandleCollision(moveY);
        }

        if (moveX > 0) {
            facingDirection = Direction.RIGHT;
        } else if (moveX < 0) {
            facingDirection = Direction.LEFT;
        }
        currentAnimationName = facingDirection == Direction.RIGHT ? "SWIM_RIGHT" : "SWIM_LEFT";

        super.update(player);
    }

    private boolean isWater(float x, float y) {
        MapTile tile = map.getTileByPosition(Math.round(x), Math.round(y));
        return tile != null && tile.getTileType() == TileType.WATER;
    }

    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        return new HashMap<String, Frame[]>() {{
            put("SWIM_RIGHT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(0, 0), 12)
                            .withScale(3)
                            .withBounds(1, 2, 17, 8)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(0, 1), 12)
                            .withScale(3)
                            .withBounds(1, 2, 17, 8)
                            .build()
            });

            put("SWIM_LEFT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(0, 0), 12)
                            .withScale(3)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .withBounds(2, 2, 17, 8)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(0, 1), 12)
                            .withScale(3)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .withBounds(2, 2, 17, 8)
                            .build()
            });
        }};
    }
}
