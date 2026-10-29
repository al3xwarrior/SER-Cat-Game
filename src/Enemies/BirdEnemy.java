package Enemies;

import Builders.FrameBuilder;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.ImageEffect;
import GameObject.SpriteSheet;
import Level.Enemy;
import Level.MapEntity;
import Level.Player;
import Utils.Direction;
import Utils.Point;

import java.util.HashMap;

public class BirdEnemy extends Enemy {
    private final float movementSpeed;
    private final Direction startFacingDirection;
    private final float leftBound;
    private final float rightBound;
    private Direction facingDirection;

    public BirdEnemy(Point location, Direction facingDirection) {
        this(location, new Point(location.x + 6 * 48, location.y), facingDirection, 2.5f, true);
    }

    // Birds patrol a fixed flight lane, so they remain a recurring obstacle
    // instead of leaving the camera after one pass.
    public BirdEnemy(Point start, Point end, Direction facingDirection, float movementSpeed) {
        this(start, end, facingDirection, movementSpeed, false);
    }

    private BirdEnemy(Point start, Point end, Direction facingDirection, float movementSpeed, boolean startAtLeft) {
        super(startAtLeft || facingDirection == Direction.RIGHT ? start.x : end.x - 62,
                start.y, 1, new SpriteSheet(ImageLoader.load("BirdEnemy.png"), 31, 21), "WALK_LEFT");
        this.startFacingDirection = facingDirection;
        this.leftBound = start.x;
        this.rightBound = end.x;
        this.movementSpeed = movementSpeed;
        this.contactDamage = 20;
        this.killHealAmount = 5;
        this.initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        facingDirection = startFacingDirection;
        if (facingDirection == Direction.RIGHT) {
            currentAnimationName = "WALK_RIGHT";
        } else if (facingDirection == Direction.LEFT) {
            currentAnimationName = "WALK_LEFT";
        }
    }

    @Override
    public void update(Player player) {
        moveXHandleCollision(facingDirection == Direction.RIGHT ? movementSpeed : -movementSpeed);
        if (getX2() >= rightBound) {
            facingDirection = Direction.LEFT;
            currentAnimationName = "WALK_LEFT";
        } else if (getX1() <= leftBound) {
            facingDirection = Direction.RIGHT;
            currentAnimationName = "WALK_RIGHT";
        }

        super.update(player);
    }

    @Override
    public void onEndCollisionCheckX(boolean hasCollided, Direction direction, MapEntity entityCollidedWith) {
        if (hasCollided) {
            facingDirection = direction == Direction.RIGHT ? Direction.LEFT : Direction.RIGHT;
            currentAnimationName = facingDirection == Direction.RIGHT ? "WALK_RIGHT" : "WALK_LEFT";
        }
    }

    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        return new HashMap<String, Frame[]>() {{
            put("WALK_LEFT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(0, 0), 8)
                            .withScale(2)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .withBounds(6, 6, 6, 7)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(0, 1), 8)
                            .withScale(2)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .withBounds(0, 0, 3, 3)
                            .build()
            });

            put("WALK_RIGHT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(0, 0), 8)
                            .withScale(2)
                            .withBounds(6, 6, 12, 7)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(0, 1), 8)
                            .withScale(2)
                            .withBounds(6, 6, 12, 7)
                            .build()
            });
        }};
    }
}
