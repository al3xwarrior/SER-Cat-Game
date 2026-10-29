package Level;

import Engine.GraphicsHandler;
import GameObject.Frame;
import GameObject.SpriteSheet;
import Powerups.HockeyStickPowerup;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.HashMap;
import java.util.Random;

// This class is a base class for all enemies in the game -- all enemies should extend from it
public class Enemy extends MapEntity {

    private int health;

    // damage this enemy does to the player and how much health you get back for killing it
    // each enemy can change these in its constructor
    protected int contactDamage = 20;
    protected int killHealAmount = 5;

    // chance of dropping a hockey stick when killed (0.1 = 10%)
    protected float hockeyStickDropChance = 0.1f;
    private static final Random random = new Random();

    // death animation: the enemy flashes white while the cat's claws slash across it, then it shrinks and fades away
    private static final int DEATH_DURATION = 40;   // total frames before the enemy is removed
    private static final int DEATH_HOLD_FRAMES = 8; // frames spent flashing before it starts shrinking
    private static final int SLASH_COUNT = 3;
    private static final int SLASH_STAGGER = 2;     // frames between the start of each claw streak
    private static final int SLASH_GROW_FRAMES = 6; // frames for a streak to sweep across
    private static final int SLASH_FADE_FRAMES = 8; // frames for a streak's tail to catch up and vanish
    private static final Color SLASH_CORE = Color.WHITE;
    private static final Color SLASH_EDGE = new Color(49, 207, 240);

    // hit reaction (non-lethal hits): a quick flash, shake and a claw streak for each hit taken so far
    private static final int HIT_DURATION = 18;
    private static final int HIT_FLASH_FRAMES = 6;

    private int hitsTaken;
    private int hitTimer;
    private boolean dying;
    private int deathTimer;
    private boolean slashFromLeft; // the cat swipes from the side it is on

    public Enemy(float x, float y, int health, SpriteSheet spriteSheet, String startingAnimation) {
        super(x, y, spriteSheet, startingAnimation);
        this.health = health;
    }

    public Enemy(float x, float y, int health, HashMap<String, Frame[]> animations, String startingAnimation) {
        super(x, y, animations, startingAnimation);
        this.health = health;
    }

    public Enemy(float x, float y, int health, Frame[] frames) {
        super(x, y, frames);
        this.health = health;
    }

    public Enemy(float x, float y, int health, Frame frame) {
        super(x, y, frame);
        this.health = health;
    }

    public Enemy(float x, float y, int health) {
        super(x, y);
        this.health = health;
    }

    @Override
    public void initialize() {
        super.initialize();
        dying = false;
        deathTimer = 0;
        hitsTaken = 0;
        hitTimer = 0;
    }

    public boolean isDying() {
        return dying;
    }

    // called by the camera every frame instead of update(player) directly
    // while dying, the enemy's own AI/movement (defined in subclasses' update) is skipped entirely, so no subclass needs to know about death
    public void updateEntity(Player player) {
        if (dying) {
            updateDying();
        } else {
            if (hitTimer > 0) {
                hitTimer--;
            }
            update(player);
        }
    }

    public void update(Player player) {
        super.update();
        if (intersects(player)) {
            touchedPlayer(player);
        }
    }

    // A subclass can override this method to specify what it does when it touches the player
    public void touchedPlayer(Player player) {
        if (player.playerState != PlayerState.POUNCING) {
            player.hurtPlayer(this);
        } else {
            this.health--;
            this.hitsTaken++;
            player.hitEnemy(this);

            if (this.health <= 0) {
                die(player);
            } else {
                hitTimer = HIT_DURATION;
                slashFromLeft = isPlayerOnLeft(player);
            }
        }
    }

    public int getContactDamage() {
        return contactDamage;
    }

    public int getKillHealAmount() {
        return killHealAmount;
    }

    // starts the death animation; the enemy is removed from the map once it finishes
    protected void die(Player player) {
        dying = true;
        deathTimer = 0;
        slashFromLeft = isPlayerOnLeft(player);

        player.healPlayer(killHealAmount);
        if (map != null && random.nextFloat() < hockeyStickDropChance) {
            map.addEnhancedMapTile(HockeyStickPowerup.droppedBy(this));
        }
        // keep updating even if the camera moves away so the enemy is always cleaned up
        setIsUpdateOffScreen(true);
    }

    private boolean isPlayerOnLeft(Player player) {
        return player.getX1() + player.getWidth() / 2f < getX1() + getWidth() / 2f;
    }

    private void updateDying() {
        deathTimer++;
        if (deathTimer >= DEATH_DURATION) {
            this.mapEntityStatus = MapEntityStatus.REMOVED;
        }
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        if (!dying && hitTimer <= 0) {
            super.draw(graphicsHandler);
            return;
        }

        Graphics2D g = graphicsHandler.getGraphics();
        int width = currentFrame.getWidth();
        int height = currentFrame.getHeight();
        int baseX = Math.round(getCalibratedXLocation());
        int baseY = Math.round(getCalibratedYLocation());

        if (!dying) {
            drawHit(graphicsHandler, g, baseX, baseY, width, height);
            return;
        }

        // 0 while flashing, then 0 -> 1 while shrinking away
        float t = Math.max(0f, Math.min(1f, (deathTimer - DEATH_HOLD_FRAMES) / (float) (DEATH_DURATION - DEATH_HOLD_FRAMES)));
        float scale = 1f - t;
        int drawWidth = Math.max(1, Math.round(width * scale));
        int drawHeight = Math.max(1, Math.round(height * scale));

        // shrinks toward its center while floating up a bit and getting knocked away from the cat
        int knockback = Math.round(t * 14) * (slashFromLeft ? 1 : -1);
        int drawX = baseX + (width - drawWidth) / 2 + knockback;
        int drawY = baseY + (height - drawHeight) / 2 - Math.round(t * 12);

        // blink white while the slash lands
        Color flash = deathTimer < DEATH_HOLD_FRAMES && (deathTimer / 2) % 2 == 0 ? Color.WHITE : tintColor;

        Composite oldComposite = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f - t));
        graphicsHandler.drawImage(currentFrame.getImage(), drawX, drawY, drawWidth, drawHeight, currentFrame.getImageEffect(), flash);
        g.setComposite(oldComposite);

        drawSlash(g, baseX + width / 2, baseY + height / 2, width, height, deathTimer, SLASH_COUNT);
    }

    // reaction to a hit that didn't kill: blink white, shake, and show one more claw streak for every hit taken so far
    private void drawHit(GraphicsHandler graphicsHandler, Graphics2D g, int baseX, int baseY, int width, int height) {
        int age = HIT_DURATION - hitTimer;
        Color flash = age < HIT_FLASH_FRAMES && (age / 2) % 2 == 0 ? Color.WHITE : tintColor;

        // shake side to side, settling down as the reaction ends
        int shakeAmount = Math.round(4f * hitTimer / HIT_DURATION);
        int shake = (age % 2 == 0 ? 1 : -1) * shakeAmount;

        graphicsHandler.drawImage(currentFrame.getImage(), baseX + shake, baseY, width, height, currentFrame.getImageEffect(), flash);
        drawSlash(g, baseX + width / 2, baseY + height / 2, width, height, age, Math.min(hitsTaken, SLASH_COUNT));
    }

    // draws a few diagonal claw streaks sweeping across the enemy, as if the cat swiped at it
    // age is how many frames the slash has been playing; count is how many streaks to draw
    private void drawSlash(Graphics2D g, int centerX, int centerY, int width, int height, int slashAge, int count) {
        Stroke oldStroke = g.getStroke();
        Color oldColor = g.getColor();
        Object oldAntialiasing = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        // streaks run top-to-bottom, angled away from the side the cat is on
        float dirX = slashFromLeft ? 1f : -1f;
        float halfWidth = width * 0.35f;
        float halfHeight = height * 0.6f;
        float spacing = Math.max(8f, width / 6f);

        for (int i = 0; i < count; i++) {
            int age = slashAge - i * SLASH_STAGGER;
            if (age <= 0) {
                continue;
            }
            float head = Math.min(1f, age / (float) SLASH_GROW_FRAMES);
            float tail = Math.max(0f, (age - SLASH_GROW_FRAMES) / (float) SLASH_FADE_FRAMES);
            if (tail >= 1f) {
                continue;
            }

            // each streak is shifted sideways along the swipe so they sit next to each other like claw marks
            float offset = (i - (count - 1) / 2f) * spacing;
            float startX = centerX - dirX * halfWidth + offset;
            float startY = centerY - halfHeight;
            float endX = centerX + dirX * halfWidth + offset;
            float endY = centerY + halfHeight;

            int x1 = Math.round(startX + (endX - startX) * tail);
            int y1 = Math.round(startY + (endY - startY) * tail);
            int x2 = Math.round(startX + (endX - startX) * head);
            int y2 = Math.round(startY + (endY - startY) * head);

            g.setStroke(new BasicStroke(8f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
            g.setColor(SLASH_EDGE);
            g.drawLine(x1, y1, x2, y2);
            g.setStroke(new BasicStroke(3f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
            g.setColor(SLASH_CORE);
            g.drawLine(x1, y1, x2, y2);
        }

        g.setStroke(oldStroke);
        g.setColor(oldColor);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAntialiasing);
    }
}
