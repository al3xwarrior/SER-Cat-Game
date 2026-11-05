package Enemies;
 
import Builders.FrameBuilder;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.SpriteSheet;
import Level.Enemy;
import Level.MapEntity;
import Level.MapEntityStatus;
import Level.Player;
import Utils.Direction;
import Utils.Point;
 
import java.util.HashMap;
 
// The fireball DanBoss shoots. Unlike the normal Fireball it can travel at any angle,
// and it can split into two smaller-angle fireballs after a short delay.
// Fireballs created by a split never split again.
public class BossFireball extends Enemy {
    // how far (in degrees) each child fireball veers away from the parent's direction
    private static final float SPLIT_ANGLE_DEGREES = 12f;
 
    private final float velocityX;
    private final float velocityY;
    private int existenceFrames;
    // frames until this fireball splits, 0 or less means it never splits
    private int splitCountdown;
 
    public BossFireball(Point location, float velocityX, float velocityY, int existenceFrames, int splitCountdown) {
        super(location.x, location.y, 67, new SpriteSheet(ImageLoader.load("Fireball.png"), 7, 7), "DEFAULT");
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.existenceFrames = existenceFrames;
        this.splitCountdown = splitCountdown;
        this.contactDamage = 10;
        initialize();
    }
 
    @Override
    public void update(Player player) {
        existenceFrames--;
        if (existenceFrames <= 0) {
            this.mapEntityStatus = MapEntityStatus.REMOVED;
            return;
        }
 
        if (splitCountdown > 0) {
            splitCountdown--;
            if (splitCountdown == 0) {
                split();
                return;
            }
        }
 
        if (velocityX != 0) {
            moveXHandleCollision(velocityX);
        }
        if (velocityY != 0) {
            moveYHandleCollision(velocityY);
        }
 
        // it may have hit a wall/floor while moving
        if (this.mapEntityStatus == MapEntityStatus.REMOVED) {
            return;
        }
        super.update(player);
    }
 
    // replaces this fireball with two that fan out on either side of its current direction
    private void split() {
        this.mapEntityStatus = MapEntityStatus.REMOVED;
        if (map == null) {
            return;
        }
 
        float speed = (float) Math.sqrt(velocityX * velocityX + velocityY * velocityY);
        double angle = Math.atan2(velocityY, velocityX);
        int remainingLife = Math.max(1, existenceFrames);
 
        for (int side = -1; side <= 1; side += 2) {
            double childAngle = angle + side * Math.toRadians(SPLIT_ANGLE_DEGREES);
            float childVx = (float) (speed * Math.cos(childAngle));
            float childVy = (float) (speed * Math.sin(childAngle));
            map.addEnemy(new BossFireball(new Point(getX(), getY()), childVx, childVy, remainingLife, 0));
        }
    }
 
    @Override
    public void onEndCollisionCheckX(boolean hasCollided, Direction direction, MapEntity entityCollidedWith) {
        if (hasCollided) {
            this.mapEntityStatus = MapEntityStatus.REMOVED;
        }
    }
 
    @Override
    public void onEndCollisionCheckY(boolean hasCollided, Direction direction, MapEntity entityCollidedWith) {
        if (hasCollided) {
            this.mapEntityStatus = MapEntityStatus.REMOVED;
        }
    }
 
    @Override
    public void touchedPlayer(Player player) {
        // same as the normal fireball, it disappears when it touches the player
        super.touchedPlayer(player);
        this.mapEntityStatus = MapEntityStatus.REMOVED;
    }
 
    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        return new HashMap<String, Frame[]>() {{
            put("DEFAULT", new Frame[]{
                    new FrameBuilder(spriteSheet.getSprite(0, 0))
                            .withScale(3)
                            .withBounds(1, 1, 5, 5)
                            .build()
            });
        }};
    }
}
