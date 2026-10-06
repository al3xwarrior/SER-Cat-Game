package Level;

import GameObject.Frame;
import GameObject.SpriteSheet;

import java.util.HashMap;

// This class is a base class for all enemies in the game -- all enemies should extend from it
public class Enemy extends MapEntity {

    private int health;

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

            // If the player is attacking - Alex
            if (/*to be replaced when the player has attacking support*/1 == 2) {
                this.health--;
                if (health <= 0) {
                    // TODO: impliment a way to remove the enemy from the level - Alex
                }
                return;
            }

            touchedPlayer(player);
        }
    }

    // A subclass can override this method to specify what it does when it touches the player
    public void touchedPlayer(Player player) {
        player.hurtPlayer(this);
    }
}
