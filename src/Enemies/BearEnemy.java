package Enemies;

import Builders.FrameBuilder;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.ImageEffect;
import GameObject.SpriteSheet;
import Level.Enemy;
import Level.MapEntity;
import Level.Player;
import Utils.AirGroundState;
import Utils.Direction;
import Utils.Point;

import java.awt.image.BufferedImage;
import java.util.HashMap;

public class BearEnemy extends Enemy {
    protected Point startLocation;
    protected Point endLocation;

    private static final int GROUND_OFFSET = 3;

    protected float movementSpeed = 1.1f;
    private Direction startFacingDirection;
    protected Direction facingDirection;
    protected AirGroundState airGroundState;

    public BearEnemy(Point startLocation, Point endLocation, Direction facingDirection) {
        super(startLocation.x, startLocation.y + GROUND_OFFSET, 3, new SpriteSheet(ImageLoader.load("BearEnemy.png"), 29, 15), "WALK_RIGHT");
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.startFacingDirection = facingDirection;
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
        airGroundState = AirGroundState.GROUND;
    }

    @Override
    public void update(Player player) {
        float startBound = startLocation.x;
        float endBound = endLocation.x;

        if (facingDirection == Direction.RIGHT) {
            currentAnimationName = "WALK_RIGHT";
            moveXHandleCollision(movementSpeed);
        } else {
            currentAnimationName = "WALK_LEFT";
            moveXHandleCollision(-movementSpeed);
        }

        if (getX1() + getWidth() >= endBound) {
            float difference = endBound - getX2();
            moveXHandleCollision(-difference);
            facingDirection = Direction.LEFT;
        } else if (getX1() <= startBound) {
            float difference = startBound - getX1();
            moveXHandleCollision(difference);
            facingDirection = Direction.RIGHT;
        }

        super.update(player);
    }

    @Override
    public void onEndCollisionCheckX(boolean hasCollided, Direction direction, MapEntity entityCollidedWith) {
        if (hasCollided) {
            if (direction == Direction.RIGHT) {
                facingDirection = Direction.LEFT;
                currentAnimationName = "WALK_LEFT";
            } else {
                facingDirection = Direction.RIGHT;
                currentAnimationName = "WALK_RIGHT";
            }
        }
    }

    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        return new HashMap<String, Frame[]>() {{
            // was having issues with the bear sprite. I guess I got lucky with the dimensions of
            // the other enemies. This method is mostly AI otherwise i would have been stuck :( - Alex
            Frame[] left = new Frame[4];
            Frame[] right = new Frame[4];
            for (int i = 0; i < 4; i++) {
                BufferedImage bear = spriteSheet.getImage().getSubimage(i * 30 + i * 2, 0, 23, 15);
                left[i] = new FrameBuilder(bear, 10)
                        .withScale(3)
                        .withBounds(0, 0, 23, 15)
                        .build();
                right[i] = new FrameBuilder(bear, 10)
                        .withScale(3)
                        .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                        .withBounds(0, 0, 23, 15)
                        .build();
            }
            put("WALK_LEFT", left);
            put("WALK_RIGHT", right);
        }};
    }

    public enum DinosaurState {
        WALK, SHOOT_WAIT, SHOOT
    }
}
