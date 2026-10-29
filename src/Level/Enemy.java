package Level;

import GameObject.Frame;
import GameObject.SpriteSheet;
import Powerups.HockeyStickPowerup;
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
            player.hitEnemy(this);

            if (this.health <= 0) {
                this.mapEntityStatus = MapEntityStatus.REMOVED;
                player.healPlayer(killHealAmount);

                if (map != null && random.nextFloat() < hockeyStickDropChance) {
                    map.addEnhancedMapTile(HockeyStickPowerup.droppedBy(this));
                }
            }
        }
    }

    public int getContactDamage() {
        return contactDamage;
    }

    public int getKillHealAmount() {
        return killHealAmount;
    }
}
