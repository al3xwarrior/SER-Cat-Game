package Players;

import Builders.FrameBuilder;
import Engine.Config;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import Engine.Key;
import Engine.Keyboard;
import Engine.KeyLocker;
import Engine.ScreenManager;
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

// health bar: lost health stays visible as a lighter "trail" that drains away shortly after a hit, and the bar pulses when health is low
        protected final int healthTrailDelayFrames = 30;
        protected final float healthTrailDrainPerFrame = 1f;
        protected final float lowHealthPercent = 0.3f; // below this the health bar flashes and the screen edges glow red
        protected final float criticalHealthPercent = 0.15f; // below this the warning pulses faster
        protected float healthTrail = 100f;
        protected int healthTrailDelayRemaining = 0;
        protected int healthPulseFrames = 0;
        protected int staminaFailFlashFramesRemaining = 0;



    public Cat(float x, float y) {
        super(new SpriteSheet(ImageLoader.load("Test_hires.png"), 64, 64), x, y, "STAND_RIGHT");
        gravity = .5f;
        baseFriction = 1.2f;
        terminalVelocityY = 15f;
        terminalVelocityX = 5f;
        jumpHeight = 11.5f; // one unit is euqal to 7.3f
        jumpDegrade = .5f;
        walkSpeed = 1f;
        momentumYIncrease = .5f;
        airJumps = 1;
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

        updateHealthTrail();
        healthPulseFrames++;
    }

    public void updateHealthTrail() {
        if (healthTrail < health) {
            // healed: the trail just follows the health up
            healthTrail = health;
        } else if (healthTrail > health) {
            if (hurtFramesRemaining == HURT_INVINCIBILITY_FRAMES - 1) {
                // just got hit, wait a bit before the trail starts draining
                healthTrailDelayRemaining = healthTrailDelayFrames;
            }

            if (healthTrailDelayRemaining > 0) {
                healthTrailDelayRemaining--;
            } else {
                healthTrail = Math.max(health, healthTrail - healthTrailDrainPerFrame);
            }
        }
    }

    public void drawHealthBar(GraphicsHandler graphicsHandler) {
        int barX = 35;
        int barY = 25;
        int barWidth = 150;
        int barHeight = 14;

        int filledWidth = (int) (barWidth * getHealthPercent());
        int trailWidth = (int) (barWidth * (healthTrail / maxHealth));

        Color fillColor = new Color(220, 40, 40);
        Color borderColor = Color.black;

        // low health: flash the bar and its border as a warning
        if (isLowHealth() && (healthPulseFrames / getLowHealthFlashFrames()) % 2 == 0) {
            fillColor = new Color(255, 110, 110);
            borderColor = Color.white;
        }

        graphicsHandler.drawFilledRectangle(barX, barY, barWidth, barHeight, new Color(60, 60, 60, 200));

        if (trailWidth > filledWidth) {
            graphicsHandler.drawFilledRectangle(barX, barY, trailWidth, barHeight, new Color(255, 200, 200));
        }

        if (filledWidth > 0) {
            graphicsHandler.drawFilledRectangle(barX, barY, filledWidth, barHeight, fillColor);
        }

        graphicsHandler.drawRectangle(barX, barY, barWidth, barHeight, borderColor, 2);
    }

    public boolean isLowHealth() {
        return health > 0 && getHealthPercent() <= lowHealthPercent;
    }

    // how many frames each low health flash lasts, faster when health is critical
    protected int getLowHealthFlashFrames() {
        return getHealthPercent() <= criticalHealthPercent ? 8 : 15;
    }

    // pulsing red glow around the edges of the screen while health is low
    public void drawLowHealthWarning(GraphicsHandler graphicsHandler) {
        if (!isLowHealth()) {
            return;
        }

        // smooth pulse between 0 and 1, one full pulse every 2 flashes of the health bar
        double pulse = (Math.sin(healthPulseFrames * Math.PI / getLowHealthFlashFrames()) + 1) / 2;
        int maxAlpha = (int) (40 + 50 * pulse);

        // layers of see-through red bands that get fainter towards the middle of the screen, which looks like a soft glow
        int layers = 6;
        int layerThickness = 6;
        int width = ScreenManager.getScreenWidth();
        int height = ScreenManager.getScreenHeight();

        for (int i = 0; i < layers; i++) {
            int alpha = maxAlpha * (layers - i) / layers / 2;
            Color glow = new Color(220, 30, 30, alpha);
            int inset = i * layerThickness;

            graphicsHandler.drawFilledRectangle(inset, inset, width - inset * 2, layerThickness, glow); // top
            graphicsHandler.drawFilledRectangle(inset, height - inset - layerThickness, width - inset * 2, layerThickness, glow); // bottom
            graphicsHandler.drawFilledRectangle(inset, inset + layerThickness, layerThickness, height - (inset + layerThickness) * 2, glow); // left
            graphicsHandler.drawFilledRectangle(width - inset - layerThickness, inset + layerThickness, layerThickness, height - (inset + layerThickness) * 2, glow); // right
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

    public void updatePlayerFriction(float amount) {
        super.appliedFriction = amount;
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
        int barY = 47; // sits right under the health bar
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
        graphicsHandler.drawFilledRectangle(0, 0, ScreenManager.getScreenWidth(), ScreenManager.getScreenHeight(), tint);
    }



    

    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        return new HashMap<String, Frame[]>() {{
            put("STAND_RIGHT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(0, 0))
                            .withScale(1)
                            .withBounds(23, 29, 24, 27)
                            .build()
            });

            put("STAND_LEFT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(0, 0))
                            .withScale(1)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .withBounds(23, 29, 24, 27)
                            .build()
            });

            put("WALK_RIGHT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(1, 0), 5)
                            .withScale(1)
                            .withBounds(23, 29, 24, 27)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(1, 1), 5)
                            .withScale(1)
                            .withBounds(23, 29, 24, 27)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(1, 2), 5)
                            .withScale(1)
                            .withBounds(23, 29, 24, 27)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(1, 3), 5)
                            .withScale(1)
                            .withBounds(23, 29, 24, 27)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(1, 4), 5)
                            .withScale(1)
                            .withBounds(23, 29, 24, 27)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(1, 5), 5)
                            .withScale(1)
                            .withBounds(23, 29, 24, 27)
                            .build(),
            });

            put("WALK_LEFT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(1, 0), 5)
                            .withScale(1)
                            .withBounds(23, 29, 24, 27)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(1, 1), 5)
                            .withScale(1)
                            .withBounds(23, 29, 24, 27)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(1, 2), 5)
                            .withScale(1)
                            .withBounds(23, 29, 24, 27)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(1, 3), 5)
                            .withScale(1)
                            .withBounds(23, 29, 24, 27)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(1, 4), 5)
                            .withScale(1)
                            .withBounds(23, 29, 24, 27)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(1, 5), 5)
                            .withScale(1)
                            .withBounds(23, 29, 24, 27)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .build(),
            });

            put("JUMP_RIGHT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(2, 0))
                            .withScale(1)
                            .withBounds(23, 29, 24, 27)
                            .build()
            });

            put("JUMP_LEFT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(2, 0))
                            .withScale(1)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .withBounds(23, 29, 24, 27)
                            .build()
            });

            put("FALL_RIGHT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(3, 0))
                            .withScale(1)
                            .withBounds(23, 29, 24, 27)
                            .build()
            });

            put("FALL_LEFT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(3, 0))
                            .withScale(1)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .withBounds(23, 29, 24, 27)
                            .build()
            });

            put("CROUCH_RIGHT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(4, 0))
                            .withScale(1)
                            .withBounds(23, 29, 24, 27)
                            .build()
            });

            put("CROUCH_LEFT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(4, 0))
                            .withScale(1)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .withBounds(23, 29, 24, 27)
                            .build()
            });

            put("DEATH_RIGHT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(5, 0), 8)
                            .withScale(1)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(5, 1), 8)
                            .withScale(1)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(5, 2), -1)
                            .withScale(1)
                            .build()
            });

            put("DEATH_LEFT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(5, 0), 8)
                            .withScale(1)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(5, 1), 8)
                            .withScale(1)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(5, 2), -1)
                            .withScale(1)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .build()
            });

            put("SWIM_STAND_RIGHT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(6, 0))
                            .withScale(1)
                            .withBounds(23, 29, 24, 27)
                            .build()
            });

            put("SWIM_STAND_LEFT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(6, 0))
                            .withScale(1)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .withBounds(23, 29, 24, 27)
                            .build()
            });
        }};
    }
}
