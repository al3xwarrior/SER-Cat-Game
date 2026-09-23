package Level;

import Engine.GraphicsHandler;
import Engine.Key;
import Engine.KeyLocker;
import Engine.Keyboard;
import GameObject.GameObject;
import GameObject.ImageEffect;
import GameObject.SpriteSheet;
import Utils.AirGroundState;
import Utils.Direction;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public abstract class Player extends GameObject {
    // values that affect player movement
    // these should be set in a subclass
    protected float walkSpeed = 0;
    protected float gravity = 0;
    protected float baseFriction = 0;
    protected float jumpHeight = 0;
    protected float jumpDegrade = 0;
    protected float terminalVelocityY = 0;
    protected float terminalVelocityX = 0;
    protected float momentumYIncrease = 0;

    // values used to handle player movement
    protected float jumpForce = 0;
    protected float momentumY = 0;
    protected float momentumX = 0;
    protected float appliedFriction = 1.2f;
    protected float dashFrames = 0;
    protected float moveAmountX, moveAmountY;
    protected float lastAmountMovedX, lastAmountMovedY;

    // values used to keep track of player's current state
    protected PlayerState playerState;
    protected PlayerState previousPlayerState;
    protected Direction facingDirection;
    protected AirGroundState airGroundState;
    protected AirGroundState previousAirGroundState;
    protected LevelState levelState;

    // classes that listen to player events can be added to this list
    protected ArrayList<PlayerListener> listeners = new ArrayList<>();

    // define keys
    protected KeyLocker keyLocker = new KeyLocker(); // TODO: add WASD as movement keys
    protected Key JUMP_KEY = Key.UP;
    protected Key MOVE_LEFT_KEY = Key.LEFT;
    protected Key MOVE_RIGHT_KEY = Key.RIGHT;
    protected Key CROUCH_KEY = Key.DOWN;

    protected Key DASH_KEY = Key.Z;
    protected Key POUNCE_KEY = Key.X;

    // flags
    protected boolean isInvincible = false; // if true, player cannot be hurt by enemies (good for testing)

    // values used to handle a timed invincibility power-up (see makeInvincible)
    protected static final Color INVINCIBILITY_TINT_COLOR = new Color(130, 220, 255, 150);
    protected static final int INVINCIBILITY_FLICKER_WARNING_FRAMES = 90; // start flickering this many frames before invincibility ends, to warn the player
    protected int invincibilityFramesRemaining = 0;

    // image of whatever item the player currently has equipped (e.g. a hockey stick from a power-up), drawn alongside the player -- null if nothing is equipped
    protected BufferedImage equippedItemImage = null;

    public Player(SpriteSheet spriteSheet, float x, float y, String startingAnimationName) {
        super(spriteSheet, x, y, startingAnimationName);
        facingDirection = Direction.RIGHT;
        airGroundState = AirGroundState.AIR;
        previousAirGroundState = airGroundState;
        playerState = PlayerState.STANDING;
        previousPlayerState = playerState;
        levelState = LevelState.RUNNING;
    }

    public void update() {
        // if the player is grounded and they are not dashing, then we apply friction
        // we check for the dash because we want the beginning of the dash to be more smooth and snappy, applying friction to
        // the start of the dash will make it feel more sluggish
        if (getAirGroundState() == AirGroundState.GROUND && playerState != PlayerState.DASHING) {
            dashFrames = 0f;
            momentumX /= appliedFriction;
        }

        //if (Math.abs(momentumX) > terminalVelocityX) {
        //    momentumX = terminalVelocityX * Math.signum(momentumX);
        //}

        moveAmountX = momentumX;
        moveAmountY = 0;

        // if player is currently playing through level (has not won or lost)
        if (levelState == LevelState.RUNNING) {
            updateInvincibility();

            applyGravity();

            // update player's state and current actions, which includes things like determining how much it should move each frame and if its walking or jumping
            do {
                previousPlayerState = playerState;
                handlePlayerState();
            } while (previousPlayerState != playerState);

            previousAirGroundState = airGroundState;

            // move player with respect to map collisions based on how much player needs to move this frame
            lastAmountMovedX = super.moveXHandleCollision(moveAmountX);
            lastAmountMovedY = super.moveYHandleCollision(moveAmountY);

            handlePlayerAnimation();

            updateLockedKeys();

            // update player's animation
            super.update();
        }

        // if player has beaten level
        else if (levelState == LevelState.LEVEL_COMPLETED) {
            updateLevelCompleted();
        }

        // if player has lost level
        else if (levelState == LevelState.PLAYER_DEAD) {
            updatePlayerDead();
        }
    }

    // add gravity to player, which is a downward force
    protected void applyGravity() {
        moveAmountY += gravity + momentumY;
    }

    // based on player's current state, call appropriate player state handling method
    protected void handlePlayerState() {
        switch (playerState) {
            case STANDING:
                playerStanding();
                break;
            case WALKING:
                playerWalking();
                break;
            case CROUCHING:
                playerCrouching();
                break;
            case JUMPING:
                playerJumping();
                break;
            case SLIDING:
                playerSliding();
                break;
            case DASHING:
                playerDashing();
                break;
        }
    }

    // player STANDING state logic
    protected void playerStanding() {
        // double check that the friction is set back to normal
        appliedFriction = baseFriction;

        // if walk left or walk right key is pressed, player enters WALKING state
        if (Keyboard.isKeyDown(MOVE_LEFT_KEY) || Keyboard.isKeyDown(MOVE_RIGHT_KEY)) {
            playerState = PlayerState.WALKING;
        }

        // if jump key is pressed, player enters JUMPING state
        else if (Keyboard.isKeyDown(JUMP_KEY) && !keyLocker.isKeyLocked(JUMP_KEY)) {
            keyLocker.lockKey(JUMP_KEY);
            playerState = PlayerState.JUMPING;
        }

        // if crouch key is pressed, player enters CROUCHING state
        else if (Keyboard.isKeyDown(CROUCH_KEY)) {
            playerState = PlayerState.CROUCHING;
        }
    }

    // player WALKING state logic
    protected void playerWalking() {
        // if walk left key is pressed, move player to the left
        if (Keyboard.isKeyDown(MOVE_LEFT_KEY)) {
            momentumX -= walkSpeed;
            facingDirection = Direction.LEFT;
        }

        // if walk right key is pressed, move player to the right
        else if (Keyboard.isKeyDown(MOVE_RIGHT_KEY)) {
            momentumX += walkSpeed; 
            facingDirection = Direction.RIGHT;
        } else if (Keyboard.isKeyUp(MOVE_LEFT_KEY) && Keyboard.isKeyUp(MOVE_RIGHT_KEY)) {
            playerState = PlayerState.STANDING;
        }

        // if jump key is pressed, player enters JUMPING state
        if (Keyboard.isKeyDown(JUMP_KEY) && !keyLocker.isKeyLocked(JUMP_KEY)) {
            keyLocker.lockKey(JUMP_KEY);
            playerState = PlayerState.JUMPING;
        }

        // if crouch key is pressed,
        else if (Keyboard.isKeyDown(CROUCH_KEY)) {
            playerState = PlayerState.CROUCHING;
        }
    }

    // player CROUCHING state logic
    protected void playerCrouching() {
        // if the player's x velocity is above a certain threshold, player enters SLIDING state
        if (Math.abs(momentumX) > 2) {
            playerState = PlayerState.SLIDING;
        }

        // if crouch key is released, player enters STANDING state
        if (Keyboard.isKeyUp(CROUCH_KEY)) {
            playerState = PlayerState.STANDING;
        }

        // if jump key is pressed, player enters JUMPING state
        if (Keyboard.isKeyDown(JUMP_KEY) && !keyLocker.isKeyLocked(JUMP_KEY)) {
            keyLocker.lockKey(JUMP_KEY);
            playerState = PlayerState.JUMPING;
        }
    }

    // player SLIDING state logic
    protected void playerSliding() {
        appliedFriction = 1.025f; // TODO this probably shouldnt be hardcoded?

        // if crouch key is released, player enters STANDING state
        if (Keyboard.isKeyUp(CROUCH_KEY)) {
            playerState = PlayerState.STANDING;
            appliedFriction = baseFriction;
        }

        // if jump key is pressed, player enters JUMPING state
        if (Keyboard.isKeyDown(JUMP_KEY) && !keyLocker.isKeyLocked(JUMP_KEY)) {
            keyLocker.lockKey(JUMP_KEY);
            playerState = PlayerState.JUMPING;
            appliedFriction = baseFriction;
        }
        
        // additionally, if the player's x momentum is too slow and they are still crouching, player enters CROUCHING state
        if (Math.abs(momentumX) < 1 && Keyboard.isKeyDown(CROUCH_KEY)) {
            playerState = PlayerState.CROUCHING;
            appliedFriction = baseFriction;
        }

        // if we leave the ground during a slide, player enters JUMPING state
        else if (previousAirGroundState == AirGroundState.GROUND && airGroundState == AirGroundState.AIR) {
            playerState = PlayerState.JUMPING;
            appliedFriction = baseFriction;
        }
    }

    // player JUMPING state logic
    protected void playerJumping() {
        // if last frame player was on ground and this frame player is still on ground, the jump needs to be setup
        if (previousAirGroundState == AirGroundState.GROUND && airGroundState == AirGroundState.GROUND) {

            // sets animation to a JUMP animation based on which way player is facing
            currentAnimationName = facingDirection == Direction.RIGHT ? "JUMP_RIGHT" : "JUMP_LEFT";

            // player is set to be in air and then player is sent into the air
            airGroundState = AirGroundState.AIR;
            jumpForce = jumpHeight;
            if (jumpForce > 0) {
                moveAmountY -= jumpForce;
                jumpForce -= jumpDegrade;
                if (jumpForce < 0) {
                    jumpForce = 0;
                }
            }
        }

        // if player is in air (currently in a jump) and has more jumpForce, continue sending player upwards
        else if (airGroundState == AirGroundState.AIR) {
            if (jumpForce > 0) {
                moveAmountY -= jumpForce;
                jumpForce -= jumpDegrade;
                if (jumpForce < 0) {
                    jumpForce = 0;
                }
            }

            // allows you to move left and right while in the air
            if (Keyboard.isKeyDown(MOVE_LEFT_KEY)) {
                // if you're trying moving in the same direction as your momentum
                if (momentumX < 0) {
                    // if your momentum is lower than what it would be if you tried to move in that direction
                    if (Math.abs(momentumX) < 5f) {
                        momentumX = (momentumX - walkSpeed) / baseFriction;
                    }
                } else {
                    momentumX = (momentumX - walkSpeed) / baseFriction;
                }
            } else if (Keyboard.isKeyDown(MOVE_RIGHT_KEY)) {
                if (momentumX > 0) {
                    if (Math.abs(momentumX) < 5f) {
                        momentumX = (momentumX + walkSpeed) / baseFriction;
                    }
                } else {
                    momentumX = (momentumX + walkSpeed) / baseFriction;
                }
            }

            // if player is falling, increases momentum as player falls so it falls faster over time
            if (moveAmountY > 0) {
                increaseMomentum();
            }
        }

        // if player last frame was in air and this frame is now on ground, player enters STANDING state
        else if (previousAirGroundState == AirGroundState.AIR && airGroundState == AirGroundState.GROUND) {
            playerState = PlayerState.STANDING;
        }

        // if, at any moment that the player is airborne, player presses the dash key, player enters DASHING state
        if (Keyboard.isKeyDown(DASH_KEY) && !keyLocker.isKeyLocked(DASH_KEY) && dashFrames == 0f) {
            System.out.println("Player dashed!");
            keyLocker.lockKey(DASH_KEY);
            playerState = PlayerState.DASHING;
        }
    }

    // player DASHNG logic
    protected void playerDashing() {
        // if this is true, then we still need to apply the dash
        if (dashFrames == 0f) {
            airGroundState = AirGroundState.AIR;
            jumpForce = 0f;

            int xDir;
            int yDir;

            xDir = (Keyboard.isKeyDown(MOVE_LEFT_KEY) ? -1 : 0) + (Keyboard.isKeyDown(MOVE_RIGHT_KEY) ? 1 : 0);
            yDir = (Keyboard.isKeyDown(CROUCH_KEY) ? -1 : 0) + (Keyboard.isKeyDown(JUMP_KEY) ? 1 : 0);
            
            momentumX = 15 * xDir;
            momentumY = -15 * yDir;

            dashFrames = 7f;
        }

        else if (airGroundState == AirGroundState.GROUND) {
            dashFrames = 0f;
            momentumY = 0f;
            playerState = PlayerState.STANDING;
        }

        else if (dashFrames > 1f) {
            dashFrames--;
        }

        else if (dashFrames == 1f) {
            jumpForce = (momentumY < 0) ? momentumY * -0.5f : 0f;
            momentumY = 0f;
            playerState = PlayerState.JUMPING;
        }
    }

    // while player is in air, this is called, and will increase momentumY by a set amount until player reaches terminal velocity
    protected void increaseMomentum() {
        momentumY += momentumYIncrease;
        if (momentumY > terminalVelocityY) {
            momentumY = terminalVelocityY;
        }
    }

    protected void updateLockedKeys() {
        if (Keyboard.isKeyUp(DASH_KEY)) {
            keyLocker.unlockKey(DASH_KEY);
        }
        
        if (Keyboard.isKeyUp(JUMP_KEY)) {
            keyLocker.unlockKey(JUMP_KEY);
        }
    }

    // anything extra the player should do based on interactions can be handled here
    protected void handlePlayerAnimation() {
        if (playerState == PlayerState.STANDING) {
            // sets animation to a STAND animation based on which way player is facing
            this.currentAnimationName = facingDirection == Direction.RIGHT ? "STAND_RIGHT" : "STAND_LEFT";

            // handles putting goggles on when standing in water
            // checks if the center of the player is currently touching a water tile
            int centerX = Math.round(getBounds().getX1()) + Math.round(getBounds().getWidth() / 2f);
            int centerY = Math.round(getBounds().getY1()) + Math.round(getBounds().getHeight() / 2f);
            MapTile currentMapTile = map.getTileByPosition(centerX, centerY);
            if (currentMapTile != null && currentMapTile.getTileType() == TileType.WATER) {
                this.currentAnimationName = facingDirection == Direction.RIGHT ? "SWIM_STAND_RIGHT" : "SWIM_STAND_LEFT";
            }
        }
        else if (playerState == PlayerState.WALKING) {
            // sets animation to a WALK animation based on which way player is facing
            this.currentAnimationName = facingDirection == Direction.RIGHT ? "WALK_RIGHT" : "WALK_LEFT";
        }
        else if (playerState == PlayerState.CROUCHING) {
            // sets animation to a CROUCH animation based on which way player is facing
            this.currentAnimationName = facingDirection == Direction.RIGHT ? "CROUCH_RIGHT" : "CROUCH_LEFT";
        }
        else if (playerState == PlayerState.JUMPING) {
            // if player is moving upwards, set player's animation to jump. if player moving downwards, set player's animation to fall
            if (lastAmountMovedY <= 0) {
                this.currentAnimationName = facingDirection == Direction.RIGHT ? "JUMP_RIGHT" : "JUMP_LEFT";
            } else {
                this.currentAnimationName = facingDirection == Direction.RIGHT ? "FALL_RIGHT" : "FALL_LEFT";
            }
        }
    }

    @Override
    public void onEndCollisionCheckX(boolean hasCollided, Direction direction, MapEntity entityCollidedWith) { }

    @Override
    public void onEndCollisionCheckY(boolean hasCollided, Direction direction, MapEntity entityCollidedWith) {
        // if player collides with a map tile below it, it is now on the ground
        // if player does not collide with a map tile below, it is in air
        if (direction == Direction.DOWN) {
            if (hasCollided) {
                momentumY = 0;
                airGroundState = AirGroundState.GROUND;
            } else if (dashFrames == 0) {
                playerState = PlayerState.JUMPING;
                airGroundState = AirGroundState.AIR;
            }
        }

        // if player collides with map tile upwards, it means it was jumping and then hit into a ceiling -- immediately stop upwards jump velocity
        else if (direction == Direction.UP) {
            if (hasCollided) {
                jumpForce = 0;
            }
        }
    }

    // other entities can call this method to hurt the player
    public void hurtPlayer(MapEntity mapEntity) {
        if (!isInvincible) {
            // if map entity is an enemy, kill player on touch
            if (mapEntity instanceof Enemy) {
                levelState = LevelState.PLAYER_DEAD;
            }
        }
    }

    // makes the player invincible (immune to hurtPlayer) for the given number of frames, and gives it the invincibility glow
    public void makeInvincible(int frames) {
        isInvincible = true;
        invincibilityFramesRemaining = frames;
        setTintColor(INVINCIBILITY_TINT_COLOR);
    }

    // counts down the player's remaining invincibility time, flickering the glow briefly before it ends as a warning
    protected void updateInvincibility() {
        if (!isInvincible) {
            return;
        }

        invincibilityFramesRemaining--;
        if (invincibilityFramesRemaining <= 0) {
            isInvincible = false;
            setTintColor(null);
            unequipItem();
        } else if (invincibilityFramesRemaining < INVINCIBILITY_FLICKER_WARNING_FRAMES) {
            boolean flickerOn = (invincibilityFramesRemaining / 6) % 2 == 0;
            setTintColor(flickerOn ? INVINCIBILITY_TINT_COLOR : null);
        }
    }

    // visually equips the player with the given item image, drawn alongside the player until unequipItem is called
    public void equipItem(BufferedImage itemImage) {
        this.equippedItemImage = itemImage;
    }

    public void unequipItem() {
        this.equippedItemImage = null;
    }

    // other entities can call this to tell the player they beat a level
    public void completeLevel() {
        levelState = LevelState.LEVEL_COMPLETED;
    }

    // if player has beaten level, this will be the update cycle
    public void updateLevelCompleted() {
        // if player is not on ground, player should fall until it touches the ground
        if (airGroundState != AirGroundState.GROUND && map.getCamera().containsDraw(this)) {
            currentAnimationName = "FALL_RIGHT";
            applyGravity();
            increaseMomentum();
            super.update();
            moveYHandleCollision(moveAmountY);
        }
        // move player to the right until it walks off screen
        else if (map.getCamera().containsDraw(this)) {
            currentAnimationName = "WALK_RIGHT";
            super.update();
            moveXHandleCollision(walkSpeed * 3);
        } else {
            // tell all player listeners that the player has finished the level
            for (PlayerListener listener : listeners) {
                listener.onLevelCompleted();
            }
        }
    }

    // if player has died, this will be the update cycle
    public void updatePlayerDead() {
        // change player animation to DEATH
        if (!currentAnimationName.startsWith("DEATH")) {
            if (facingDirection == Direction.RIGHT) {
                currentAnimationName = "DEATH_RIGHT";
            } else {
                currentAnimationName = "DEATH_LEFT";
            }
            super.update();
        }
        // if death animation not on last frame yet, continue to play out death animation
        else if (currentFrameIndex != getCurrentAnimation().length - 1) {
          super.update();
        }
        // if death animation on last frame (it is set up not to loop back to start), player should continually fall until it goes off screen
        else if (currentFrameIndex == getCurrentAnimation().length - 1) {
            if (map.getCamera().containsDraw(this)) {
                moveY(3);
            } else {
                // tell all player listeners that the player has died in the level
                for (PlayerListener listener : listeners) {
                    listener.onDeath();
                }
            }
        }
    }

    public PlayerState getPlayerState() {
        return playerState;
    }

    public void setPlayerState(PlayerState playerState) {
        this.playerState = playerState;
    }

    public AirGroundState getAirGroundState() {
        return airGroundState;
    }

    public Direction getFacingDirection() {
        return facingDirection;
    }

    public void setFacingDirection(Direction facingDirection) {
        this.facingDirection = facingDirection;
    }

    public void setLevelState(LevelState levelState) {
        this.levelState = levelState;
    }

    public void addListener(PlayerListener listener) {
        listeners.add(listener);
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        super.draw(graphicsHandler);

        // draws whatever item the player currently has equipped (e.g. a power-up's hockey stick) just in front of the player, facing the same direction
        if (equippedItemImage != null) {
            int itemSize = Math.round(getHeight() * .6f);
            int overlap = Math.round(itemSize * .3f); // how far the item tucks into the player, so it reads as "held" instead of floating off to the side
            int drawX = Math.round(getCalibratedXLocation()) + (facingDirection == Direction.RIGHT ? getWidth() - overlap : overlap - itemSize);
            int drawY = Math.round(getCalibratedYLocation()) + Math.round(getHeight() * .2f);
            // ImageEffect imageEffect = facingDirection == Direction.RIGHT ? ImageEffect.NONE : ImageEffect.FLIP_HORIZONTAL; 
            // ^^ this line breaks the game

            // vv this is a longer version of the line above (which breaks the game), just to ensure this works on all devices
            if (facingDirection == Direction.RIGHT) {
                graphicsHandler.drawImage(equippedItemImage, drawX, drawY, itemSize, itemSize,ImageEffect.NONE);
            } else {
                graphicsHandler.drawImage(equippedItemImage, drawX, drawY, itemSize, itemSize,ImageEffect.FLIP_HORIZONTAL);
            }
            
        }
    }

    // Uncomment this to have game draw player's bounds to make it easier to visualize
    /*
    public void draw(GraphicsHandler graphicsHandler) {
        super.draw(graphicsHandler);
        drawBounds(graphicsHandler, new Color(255, 0, 0, 100));
    }
    */
}
