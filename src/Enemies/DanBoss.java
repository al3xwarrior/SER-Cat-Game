package Enemies;
 
import Builders.FrameBuilder;
import Engine.ImageLoader;
import GameObject.Frame;
import EnhancedMapTiles.EndLevelBox;
import GameObject.ImageEffect;
import GameObject.SpriteSheet;
import Level.Enemy;
import Level.MapEntity;
import Level.Player;
import Utils.AirGroundState;
import Utils.Direction;
import Utils.Point;
 
import java.util.HashMap;
import java.util.Random;
 
public class DanBoss extends Enemy {
    
    private static final int MAX_HEALTH = 12;
 
    //movement that makes him almost as fast as boomer
 
    private static final float WALK_SPEED = 4f;
    private static final float JUMP_VELOCITY = 12.5f;
    private static final float GRAVITY = 1.0f;
    private static final float TERMINAL_VELOCITY = 15f;
    private static final int AIR_JUMPS = 1;
 
    //how close he gets to boomer(in pixles)
    private static final float STOP_DISTANCE = 10;
 
    private static final int ATTACK_INTERVAL_FRAMES = 180;
    private static final int WINDUP_FRAMES = 30;
    private static final float FIREBALL_SPEED = 4f;
    private static final int FIREBALL_LIFETIME = 150;
    private static final int FIREBALL_SIZE = 21;           // 7px sprite * scale 3
 
    private enum State { CHASE, WINDUP }
 
    private final EndLevelBox endLevelBox;
    private final Random random = new Random();
 
    private State state;
    private Direction facing;
    private AirGroundState airGroundState;
    private float velocityY;
    private int airJumpsLeft;
    private int attackTimer;
    private int windupTimer;
    private int hopTimer;
 
    public DanBoss(Point location, EndLevelBox endLevelBox) {
        super(location.x, location.y, MAX_HEALTH, new SpriteSheet(ImageLoader.load("Test_dan.png"), 64, 64), "STAND_RIGHT");
        this.endLevelBox = endLevelBox;
 
        this.contactDamage = 25;
        this.killHealAmount = 30;
        this.hockeyStickDropChance = 0f;
 
        // the box stays locked until the boss is dead
        if (endLevelBox != null) {
            endLevelBox.setLocked(true);
        }
 
        initialize();
    }
 
    public DanBoss(Point location, Point location2, Direction left, float f) {
        this(location, null);
    }
 
    @Override
    public void initialize() {
        super.initialize();
        state = State.CHASE;
        facing = Direction.RIGHT;
        airGroundState = AirGroundState.AIR;
        velocityY = 0;
        airJumpsLeft = AIR_JUMPS;
        attackTimer = ATTACK_INTERVAL_FRAMES;
        windupTimer = 0;
        hopTimer = 60 + random.nextInt(60);
    }
 
    @Override
    public void update(Player player) {
        float bossCenterX = getX1() + getWidth() / 2f;
        float playerCenterX = player.getX1() + player.getWidth() / 2f;
        float dx = playerCenterX - bossCenterX;
 
        float bossFeetY = getY1() + getHeight();
        float playerFeetY = player.getY1() + player.getHeight();
        boolean playerAbove = playerFeetY < bossFeetY - 40;
 
        float moveX = 0;
        boolean onGround = airGroundState == AirGroundState.GROUND;
 
        if (state == State.CHASE) {
            // face and walk toward the player
            if (Math.abs(dx) > STOP_DISTANCE) {
                facing = dx > 0 ? Direction.RIGHT : Direction.LEFT;
                moveX = facing == Direction.RIGHT ? WALK_SPEED : -WALK_SPEED;
            }
 
            // time to attack?
            attackTimer--;
            if (attackTimer <= 0) {
                state = State.WINDUP;
                windupTimer = WINDUP_FRAMES;
                facing = dx > 0 ? Direction.RIGHT : Direction.LEFT;
            }
        } else {
            // WINDUP: stand still, then fire
            windupTimer--;
            if (windupTimer <= 0) {
                shootVolley(player);
                state = State.CHASE;
                attackTimer = ATTACK_INTERVAL_FRAMES;
            }
        }
 
        // horizontal movement (returns how far he actually moved, so less than asked means a wall)
        boolean blocked = false;
        if (moveX != 0) {
            float moved = moveXHandleCollision(moveX);
            blocked = Math.abs(moved) < Math.abs(moveX);
        }
 
        // jumping, only while chasing
        if (state == State.CHASE) {
            hopTimer--;
            if (onGround) {
                boolean randomHop = hopTimer <= 0 && Math.abs(dx) < 400;
                if (blocked || playerAbove || randomHop) {
                    jump();
                    hopTimer = 60 + random.nextInt(80);
                }
            } else if (velocityY > 0 && airJumpsLeft > 0 && playerAbove) {
                // double jump on the way down if the player is still above him
                airJumpsLeft--;
                velocityY = -JUMP_VELOCITY;
            }
        }
 
        // gravity
        velocityY = Math.min(velocityY + GRAVITY, TERMINAL_VELOCITY);
        moveYHandleCollision(velocityY);
 
        updateAnimation(moveX);
 
        // handles animation frames and checks if he touched the player
        super.update(player);
    }
 
    private void jump() {
        velocityY = -JUMP_VELOCITY;
        airGroundState = AirGroundState.AIR;
    }
 
    private void updateAnimation(float moveX) {
        String suffix = facing == Direction.RIGHT ? "_RIGHT" : "_LEFT";
        String animation;
        if (state == State.WINDUP) {
            animation = "CROUCH";
        } else if (airGroundState == AirGroundState.AIR) {
            animation = velocityY < 0 ? "JUMP" : "FALL";
        } else if (moveX != 0) {
            animation = "WALK";
        } else {
            animation = "STAND";
        }
        this.currentAnimationName = animation + suffix;
    }
 
    // shoots one fireball in a straight line aimed at the player
    private void shootVolley(Player player) {
        if (map == null) {
            return;
        }
 
        boolean right = facing == Direction.RIGHT;
        float originX = right ? getX1() + getWidth() : getX1() - FIREBALL_SIZE;
        float originY = getY1() + getHeight() / 4f;
 
        float targetX = player.getX1() + player.getWidth() / 2f;
        float targetY = player.getY1() + player.getHeight() / 2f;
        double angle = Math.atan2(targetY - originY, targetX - originX);
 
        float vx = (float) (FIREBALL_SPEED * Math.cos(angle));
        float vy = (float) (FIREBALL_SPEED * Math.sin(angle));
 
        // last argument 0 = this fireball never splits
        map.addEnemy(new BossFireball(new Point(originX, originY), vx, vy, FIREBALL_LIFETIME, 0));
    }
 
    // called by Enemy when the 15th hit lands
    @Override
    protected void die(Player player) {
        super.die(player);
        if (endLevelBox != null) {
            endLevelBox.setLocked(false);
        }
    }
 
    @Override
    public void onEndCollisionCheckY(boolean hasCollided, Direction direction, MapEntity entityCollidedWith) {
        if (direction == Direction.DOWN) {
            if (hasCollided) {
                velocityY = 0;
                airGroundState = AirGroundState.GROUND;
                airJumpsLeft = AIR_JUMPS;
            } else {
                airGroundState = AirGroundState.AIR;
            }
        } else if (direction == Direction.UP && hasCollided) {
            // bonked his head, stop going up
            velocityY = 0;
        }
    }
 
    // same sprite sheet layout as the cat: stand, walk, jump, fall, crouch
    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        HashMap<String, Frame[]> animations = new HashMap<>();
 
        for (boolean right : new boolean[]{true, false}) {
            String suffix = right ? "_RIGHT" : "_LEFT";
            ImageEffect effect = right ? ImageEffect.NONE : ImageEffect.FLIP_HORIZONTAL;
 
            animations.put("STAND" + suffix, new Frame[]{frame(spriteSheet, 0, 0, 0, effect)});
 
            // walk frames are shorter than the cat's (5) so he animates faster too
            Frame[] walk = new Frame[6];
            for (int col = 0; col < 6; col++) {
                walk[col] = frame(spriteSheet, 1, col, 4, effect);
            }
            animations.put("WALK" + suffix, walk);
 
            animations.put("JUMP" + suffix, new Frame[]{frame(spriteSheet, 2, 0, 0, effect)});
            animations.put("FALL" + suffix, new Frame[]{frame(spriteSheet, 3, 0, 0, effect)});
            animations.put("CROUCH" + suffix, new Frame[]{frame(spriteSheet, 4, 0, 0, effect)});
        }
 
        return animations;
    }
 
    // builds one frame, delay of 0 means a single static frame
    private Frame frame(SpriteSheet spriteSheet, int row, int col, int delay, ImageEffect effect) {
        FrameBuilder builder = delay > 0
                ? new FrameBuilder(spriteSheet.getSprite(row, col), delay)
                : new FrameBuilder(spriteSheet.getSprite(row, col));
        return builder
                .withScale(2)
                .withImageEffect(effect)
                .withBounds(12, 22, 42, 38)
                .build();
    }
}