package Players;

import Builders.FrameBuilder;
import Engine.Config;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import Engine.Key;
import Engine.Keyboard;
import Engine.KeyLocker;
import GameObject.Frame;
import GameObject.ImageEffect;
import GameObject.SpriteSheet;
import Level.Player;

import java.awt.*;
import java.util.HashMap;

// This is the class for the Cat player character
// basically just sets some values for physics and then defines animations
public class Cat extends Player {

        // stamina mechenic that is used to do enhanced movements as well as stop time
        protected float stamina = 100f;
        protected final float maxStamina = 100f;
        protected final float staminaCostPercent = 25f;

// stamina regeneration, refills from empty to full in staminaRegenSeconds, after a short delay since stamina was last used
        protected final float staminaRegenSeconds = 8f;
        protected final float staminaRegenPerFrame = maxStamina / (staminaRegenSeconds * Config.TARGET_FPS);
        protected final int staminaRegenDelayFrames = Config.TARGET_FPS; // 1 second after a movement ability
        protected final int timeWarpRegenDelayFrames = Config.TARGET_FPS * 3; // 3 seconds after the time warp ends
        protected int regenDelayFramesRemaining = 0;

// key to activate the stamina ability
        protected final Key abilityKey = Key.SHIFT;

        protected KeyLocker keyLocker = new KeyLocker();

// Time warping ability, will allow the cat to slow down the entire stage
        protected final int timeWarpDuration = 420; // last around 7 seconds if ran at 60 fps

        protected int timeWarpFramesRemaining = 0;

// visual feedback: the world gets tinted while time warping, and the stamina bar flashes red and shakes when a move fails from lack of stamina
        protected final int timeWarpFadeFrames = 15; // how long the tint takes to fade in/out
        protected final int timeWarpWarningFrames = 90; // tint flickers during the last 1.5 seconds of the warp
        protected int timeWarpOverlayFrames = 0;
        protected final Color timeWarpTintColor = new Color(90, 60, 200);
        protected final int timeWarpTintMaxAlpha = 70;

        protected final int staminaFailFlashDuration = 20;
        protected int staminaFailFlashFramesRemaining = 0;



    public Cat(float x, float y) {
        super(new SpriteSheet(ImageLoader.load("Cat.png"), 24, 24), x, y, "STAND_RIGHT");
        gravity = .5f;
        baseFriction = 1.2f;
        terminalVelocityY = 15f;
        terminalVelocityX = 5f;
        jumpHeight = 11.5f; // one block is euqal to 7.3f
        jumpDegrade = .5f;
        walkSpeed = 1f;
        momentumYIncrease = .5f;
    }

    public void update() {
        super.update();
        updateStaminaAbilityInput();
        updateTimeWarpTimer();
        updateStaminaRegen();
        updateVisualFeedbackTimers();
    }

    public void updateVisualFeedbackTimers() {
        if (isTimeWarping()) {
            timeWarpOverlayFrames = Math.min(timeWarpFadeFrames, timeWarpOverlayFrames + 1);
        } else {
            timeWarpOverlayFrames = Math.max(0, timeWarpOverlayFrames - 1);
        }

        if (staminaFailFlashFramesRemaining > 0) {
            staminaFailFlashFramesRemaining--;
        }
    }

    @Override
    protected boolean consumeStamina(float amount) {
        return useStamina(amount);
    }

    public void updateTimeWarpTimer() {
        if (isTimeWarping()) {
            timeWarpFramesRemaining--;
            if (timeWarpFramesRemaining <= 0) {
                setTimeWarping(false);
                regenDelayFramesRemaining = timeWarpRegenDelayFrames;
            }
        }
    }

    // stamina does not regenerate while time warping, otherwise the time warp would pay for itself
    public void updateStaminaRegen() {
        if (isTimeWarping()) {
            return;
        }

        if (regenDelayFramesRemaining > 0) {
            regenDelayFramesRemaining--;
        } else {
            gainStamina(staminaRegenPerFrame);
        }
    }

    public void updateStaminaAbilityInput() {
        if (Keyboard.isKeyDown(abilityKey) && !keyLocker.isKeyLocked(abilityKey)) {
            if (timeWarpFramesRemaining <= 0 && useStamina(staminaCostPercent)) {
                setTimeWarping(true);
                timeWarpFramesRemaining = timeWarpDuration;
                keyLocker.lockKey(abilityKey);
            }
        }

        if (Keyboard.isKeyUp(abilityKey)) {
            keyLocker.unlockKey(abilityKey);
        }
    }

        public boolean useStamina(float amount) {
                if (stamina - amount < 0f) {
                        staminaFailFlashFramesRemaining = staminaFailFlashDuration;
                        return false;
                }

                stamina = Math.max(0f, stamina - amount);
                regenDelayFramesRemaining = Math.max(regenDelayFramesRemaining, staminaRegenDelayFrames);
                return true;
        }

        public void gainStamina(float amount){
                stamina = Math.min(maxStamina, stamina + amount);
        }

        public float getStamina(){
                return stamina;
        }

        public float getMaxStamina(){
                return maxStamina;
        }
        public float getStaminaPercent(){
                return stamina / maxStamina;
        }

    public void draw(GraphicsHandler graphicsHandler) {
        super.draw(graphicsHandler);
        // drawBounds(graphicsHandler, new Color(255, 0, 0, 170));
    }

    public void drawStaminaBar(GraphicsHandler graphicsHandler) {
        int barX = 35;
        int barY = 35;
        int barWidth = 100;
        int barHeight = 10;

        int filledWidth = (int) (barWidth * getStaminaPercent());

        Color fillColor = new Color(80, 200, 225);
        Color borderColor = Color.black;

        // not enough stamina for a move: shake the bar side to side and flash it red
        if (staminaFailFlashFramesRemaining > 0) {
                barX += (staminaFailFlashFramesRemaining % 4 < 2) ? 3 : -3;
                if (staminaFailFlashFramesRemaining % 8 < 4) {
                        fillColor = new Color(230, 60, 60);
                        borderColor = new Color(230, 60, 60);
                }
        }

        graphicsHandler.drawFilledRectangle(barX, barY, barWidth, barHeight, new Color(60,60,60,200));

        if (filledWidth > 0) {
                graphicsHandler.drawFilledRectangle(barX, barY, filledWidth, barHeight, fillColor);
        }

        graphicsHandler.drawRectangle(barX, barY, barWidth, barHeight, borderColor, 2);

        // thin bar under the stamina bar showing how much time warp is left
        if (isTimeWarping()) {
                int warpWidth = (int) (barWidth * ((float) timeWarpFramesRemaining / timeWarpDuration));
                graphicsHandler.drawFilledRectangle(barX, barY + barHeight + 4, warpWidth, 4, timeWarpTintColor.brighter());
        }
}

    // tints the whole screen while time warping, should be drawn after the map but before the cat so the cat stays untinted
    public void drawTimeWarpOverlay(GraphicsHandler graphicsHandler) {
        if (timeWarpOverlayFrames <= 0) {
            return;
        }

        int alpha = timeWarpTintMaxAlpha * timeWarpOverlayFrames / timeWarpFadeFrames;

        // flicker as a warning that the time warp is about to run out
        if (isTimeWarping() && timeWarpFramesRemaining < timeWarpWarningFrames && (timeWarpFramesRemaining / 6) % 2 == 0) {
            alpha /= 2;
        }

        Color tint = new Color(timeWarpTintColor.getRed(), timeWarpTintColor.getGreen(), timeWarpTintColor.getBlue(), alpha);
        graphicsHandler.drawFilledRectangle(0, 0, Config.GAME_WINDOW_WIDTH, Config.GAME_WINDOW_HEIGHT, tint);
    }



    

    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        return new HashMap<String, Frame[]>() {{
            put("STAND_RIGHT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(0, 0))
                            .withScale(3)
                            .withBounds(8, 9, 8, 9)
                            .build()
            });

            put("STAND_LEFT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(0, 0))
                            .withScale(3)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .withBounds(8, 9, 8, 9)
                            .build()
            });

            put("WALK_RIGHT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(1, 0), 14)
                            .withScale(3)
                            .withBounds(8, 9, 8, 9)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(1, 1), 14)
                            .withScale(3)
                            .withBounds(8, 9, 8, 9)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(1, 2), 14)
                            .withScale(3)
                            .withBounds(8, 9, 8, 9)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(1, 3), 14)
                            .withScale(3)
                            .withBounds(8, 9, 8, 9)
                            .build()
            });

            put("WALK_LEFT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(1, 0), 14)
                            .withScale(3)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .withBounds(8, 9, 8, 9)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(1, 1), 14)
                            .withScale(3)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .withBounds(8, 9, 8, 9)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(1, 2), 14)
                            .withScale(3)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .withBounds(8, 9, 8, 9)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(1, 3), 14)
                            .withScale(3)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .withBounds(8, 9, 8, 9)
                            .build()
            });

            put("JUMP_RIGHT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(2, 0))
                            .withScale(3)
                            .withBounds(8, 9, 8, 9)
                            .build()
            });

            put("JUMP_LEFT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(2, 0))
                            .withScale(3)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .withBounds(8, 9, 8, 9)
                            .build()
            });

            put("FALL_RIGHT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(3, 0))
                            .withScale(3)
                            .withBounds(8, 9, 8, 9)
                            .build()
            });

            put("FALL_LEFT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(3, 0))
                            .withScale(3)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .withBounds(8, 9, 8, 9)
                            .build()
            });

            put("CROUCH_RIGHT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(4, 0))
                            .withScale(3)
                            .withBounds(8, 12, 8, 6)
                            .build()
            });

            put("CROUCH_LEFT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(4, 0))
                            .withScale(3)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .withBounds(8, 12, 8, 6)
                            .build()
            });

            put("DEATH_RIGHT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(5, 0), 8)
                            .withScale(3)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(5, 1), 8)
                            .withScale(3)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(5, 2), -1)
                            .withScale(3)
                            .build()
            });

            put("DEATH_LEFT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(5, 0), 8)
                            .withScale(3)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(5, 1), 8)
                            .withScale(3)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(5, 2), -1)
                            .withScale(3)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .build()
            });

            put("SWIM_STAND_RIGHT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(6, 0))
                            .withScale(3)
                            .withBounds(8, 9, 8, 9)
                            .build()
            });

            put("SWIM_STAND_LEFT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(6, 0))
                            .withScale(3)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .withBounds(8, 9, 8, 9)
                            .build()
            });
        }};
    }
}
