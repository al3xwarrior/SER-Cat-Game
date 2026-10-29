package Maps;

import Engine.GraphicsHandler;
import Engine.ImageLoader;
import Engine.ScreenManager;
import GameObject.ImageEffect;
import GameObject.SpriteSheet;
import Level.Map;
import Level.Player;
import Tilesets.WhitneyAveTileset;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

// Represents the map that is used as a background for the main menu, credits, controls and level selector screens
// It is a small looping scene on the Whitney Ave tileset: the cat runs across the rooftops, hopping gaps and dodging squirrels
// The rooftops themselves come from title_screen_map.txt, everything that moves (cat, squirrels, birds) is scripted here
public class TitleScreenMap extends Map {
    private static final int TILE = 48;

    private static final int[][] ROOFS = {{0, 4, 10}, {6, 9, 9}, {11, 16, 10}};
    private static final int AC_COLUMN = 8;

    // cat movement
    private static final float CAT_SPEED = 3f;
    private static final float CAT_GRAVITY = 0.5f;
    private static final float CAT_JUMP_VELOCITY = -10f;
    private static final float CAT_START_X = -60f;
    private static final float CAT_END_X = 730f;
    private static final int CAT_HALF_WIDTH = 14;   // half the width of the cat's hitbox
    private static final int CAT_SPRITE_CENTER = 28; // x offset of the cat's body in its 64px sprite
    private static final int JUMP_LOOKAHEAD_FRAMES = 9; // start a hop this many frames before touching an obstacle
    private static final int LEDGE_LOOKAHEAD_FRAMES = 3;
    private static final int OFF_SCREEN_MARGIN = 80;

    // the cat waits at the end of the rooftops for at least this long (3 seconds at 60fps)
    private static final int END_PAUSE_FRAMES = 180;

    // squirrels run left along the rooftops: {start x, speed, frame they appear on}
    private static final float[][] SQUIRRELS = {{840f, 1.8f, 120}};
    private static final int SQUIRREL_SCALE = 2;
    private static final float SQUIRREL_HALF_WIDTH = 16f;

    // birds fly left across the sky: {start x, y, speed}
    private static final float[][] BIRDS = {{830f, 150f, 2.0f}, {1000f, 230f, 2.0f}};
    private static final int BIRD_SCALE = 2;

    private enum Phase { RUN, PAUSE, EXIT }

    private final BufferedImage[] catRun = new BufferedImage[6];
    private final BufferedImage catStand;
    private final BufferedImage catJump;
    private final BufferedImage catFall;
    private final BufferedImage[] squirrelFrames = new BufferedImage[2];
    private final BufferedImage[] birdFrames = new BufferedImage[2];

    // how many empty pixel rows sit under the lowest drawn pixel of each image, so sprites can be lined up on the rooftops by their feet
    private final HashMap<BufferedImage, Integer> feetOffsets = new HashMap<>();

    private Phase phase;
    private int phaseTimer;
    private int sceneFrame;

    private float catX;
    private float catFeetY;
    private float catVelocityY;
    private boolean catOnGround;
    private int catAnimationFrame;

    private final ArrayList<float[]> squirrels = new ArrayList<>(); // {x, feet y, velocity y, speed, spawn frame}

    public TitleScreenMap() {
        super("title_screen_map.txt", "Title Screen Map", new WhitneyAveTileset());

        SpriteSheet catSheet = new SpriteSheet(ImageLoader.load("Test_hires.png"), 64, 64);
        for (int i = 0; i < catRun.length; i++) {
            catRun[i] = catSheet.getSprite(1, i);
        }
        catStand = catSheet.getSprite(0, 0);
        catJump = catSheet.getSprite(2, 0);
        catFall = catSheet.getSprite(3, 0);

        SpriteSheet squirrelSheet = new SpriteSheet(ImageLoader.load("SquirrelEnemy.png"), 30, 19);
        SpriteSheet birdSheet = new SpriteSheet(ImageLoader.load("BirdEnemy.png"), 31, 21);
        for (int i = 0; i < 2; i++) {
            squirrelFrames[i] = squirrelSheet.getSprite(0, i);
            birdFrames[i] = birdSheet.getSprite(0, i);
        }

        for (BufferedImage image : catRun) {
            measureFeet(image);
        }
        measureFeet(catStand);
        measureFeet(catJump);
        measureFeet(catFall);
        for (BufferedImage image : squirrelFrames) {
            measureFeet(image);
        }

        resetScene();
    }

    private void resetScene() {
        phase = Phase.RUN;
        phaseTimer = 0;
        sceneFrame = 0;
        catX = CAT_START_X;
        catFeetY = ROOFS[0][2] * TILE;
        catVelocityY = 0;
        catOnGround = true;
        catAnimationFrame = 0;

        squirrels.clear();
        for (float[] squirrel : SQUIRRELS) {
            // x, feet y, velocity y, speed (negative since they run left), spawn frame
            squirrels.add(new float[]{squirrel[0], ROOFS[squirrel[0] > 600 ? 2 : 0][2] * TILE, 0f, -squirrel[1], squirrel[2]});
        }
    }

    @Override
    public void update(Player player) {
        super.update(player);

        sceneFrame++;
        updateSquirrels();
        updateCat();

        switch (phase) {
            case RUN:
                // the cat has made it to the end of the rooftops
                if (catOnGround && catX >= CAT_END_X) {
                    phase = Phase.PAUSE;
                    phaseTimer = END_PAUSE_FRAMES;
                }
                break;
            case PAUSE:
                // wait out the pause and for every enemy to leave the screen, then the cat runs off and the scene starts over
                phaseTimer--;
                if (phaseTimer <= 0 && allEnemiesOffScreen()) {
                    phase = Phase.EXIT;
                }
                break;
            case EXIT:
                if (catX > ScreenManager.getScreenWidth() + OFF_SCREEN_MARGIN) {
                    resetScene();
                }
                break;
        }
    }

    private void updateSquirrels() {
        for (float[] squirrel : squirrels) {
            if (sceneFrame < squirrel[4]) {
                continue;
            }
            squirrel[0] += squirrel[3];
            float surface = surfaceAt(squirrel[0]);
            if (squirrel[2] == 0 && surface != Float.MAX_VALUE) {
                squirrel[1] = surface;
            } else {
                // ran off a roof, fall down into the street
                squirrel[2] += CAT_GRAVITY;
                squirrel[1] += squirrel[2];
            }
        }
    }

    private void updateCat() {
        // the cat runs until it lands at the end of the rooftops, then waits there until it is time to leave
        boolean running = phase == Phase.EXIT || !(catOnGround && catX >= CAT_END_X);
        float speed = running ? CAT_SPEED : 0f;

        if (catOnGround && running && shouldJump()) {
            catVelocityY = CAT_JUMP_VELOCITY;
            catOnGround = false;
        }

        catX += speed;
        catAnimationFrame++;

        if (!catOnGround) {
            float previousFeetY = catFeetY;
            catVelocityY += CAT_GRAVITY;
            catFeetY += catVelocityY;

            // landing: the cat's feet moved down through a rooftop
            float surface = surfaceAt(catX);
            if (catVelocityY > 0 && surface != Float.MAX_VALUE && previousFeetY <= surface && catFeetY >= surface) {
                catFeetY = surface;
                catVelocityY = 0;
                catOnGround = true;
            }
        }
    }

    // the cat hops when it is about to run off a rooftop, into the AC unit, or into a squirrel
    private boolean shouldJump() {
        // ledge ahead
        if (surfaceAt(catX + CAT_SPEED * LEDGE_LOOKAHEAD_FRAMES) == Float.MAX_VALUE) {
            return true;
        }

        float catFront = catX + CAT_HALF_WIDTH;

        // AC unit
        float acLeft = AC_COLUMN * TILE;
        float timeToAc = (acLeft - catFront) / CAT_SPEED;
        if (timeToAc > 0 && timeToAc <= JUMP_LOOKAHEAD_FRAMES) {
            return true;
        }

        // squirrels run toward the cat, so they close the distance faster
        for (float[] squirrel : squirrels) {
            if (sceneFrame < squirrel[4] || squirrel[2] != 0) {
                continue;
            }
            float squirrelFront = squirrel[0] - SQUIRREL_HALF_WIDTH;
            float timeToSquirrel = (squirrelFront - catFront) / (CAT_SPEED - squirrel[3]);
            if (timeToSquirrel > 0 && timeToSquirrel <= JUMP_LOOKAHEAD_FRAMES && Math.abs(squirrel[1] - catFeetY) < 4) {
                return true;
            }
        }
        return false;
    }

    // true once every squirrel and bird has appeared and then left the screen
    private boolean allEnemiesOffScreen() {
        for (float[] squirrel : squirrels) {
            boolean appeared = sceneFrame >= squirrel[4];
            boolean gone = squirrel[0] < -OFF_SCREEN_MARGIN || squirrel[1] > ScreenManager.getScreenHeight() + OFF_SCREEN_MARGIN;
            if (!appeared || !gone) {
                return false;
            }
        }
        for (float[] bird : BIRDS) {
            if (birdX(bird) > -OFF_SCREEN_MARGIN) {
                return false;
            }
        }
        return true;
    }

    private float birdX(float[] bird) {
        return bird[0] - sceneFrame * bird[2];
    }

    // y of the rooftop under the given x, or Float.MAX_VALUE if there is a gap there
    // the first and last rooftops continue off the sides of the screen so things can run in and out of view
    private float surfaceAt(float x) {
        int column = (int) Math.floor(x / TILE);
        for (int i = 0; i < ROOFS.length; i++) {
            int[] roof = ROOFS[i];
            boolean afterStart = column >= roof[0] || i == 0;
            boolean beforeEnd = column <= roof[1] || i == ROOFS.length - 1;
            if (afterStart && beforeEnd) {
                return roof[2] * TILE;
            }
        }
        return Float.MAX_VALUE;
    }

    // ---- drawing (ai helped a good amount)

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        super.draw(graphicsHandler); // sky, buildings, street, lamp post and tree
        drawBirds(graphicsHandler);
        drawSquirrels(graphicsHandler);
        drawCat(graphicsHandler);
    }

    private void drawBirds(GraphicsHandler graphicsHandler) {
        for (float[] bird : BIRDS) {
            int x = Math.round(birdX(bird));
            if (x < -OFF_SCREEN_MARGIN) {
                continue;
            }
            BufferedImage image = birdFrames[(sceneFrame / 8) % 2];
            graphicsHandler.drawImage(image, x, Math.round(bird[1]), image.getWidth() * BIRD_SCALE, image.getHeight() * BIRD_SCALE, ImageEffect.FLIP_HORIZONTAL);
        }
    }

    private void drawSquirrels(GraphicsHandler graphicsHandler) {
        for (float[] squirrel : squirrels) {
            if (sceneFrame < squirrel[4]) {
                continue;
            }
            BufferedImage image = squirrelFrames[(sceneFrame / 8) % 2];
            int width = image.getWidth() * SQUIRREL_SCALE;
            int height = image.getHeight() * SQUIRREL_SCALE;
            int x = Math.round(squirrel[0]) - width / 2;
            int y = Math.round(squirrel[1]) - height + feetOffset(image) * SQUIRREL_SCALE;
            graphicsHandler.drawImage(image, x, y, width, height, ImageEffect.FLIP_HORIZONTAL);
        }
    }

    private void drawCat(GraphicsHandler graphicsHandler) {
        BufferedImage image;
        if (!catOnGround) {
            image = catVelocityY < 0 ? catJump : catFall;
        } else if (phase != Phase.EXIT && catX >= CAT_END_X) {
            image = catStand;
        } else {
            image = catRun[(catAnimationFrame / 4) % catRun.length];
        }
        int x = Math.round(catX) - CAT_SPRITE_CENTER;
        int y = Math.round(catFeetY) - image.getHeight() + feetOffset(image);
        graphicsHandler.drawImage(image, x, y, image.getWidth(), image.getHeight(), ImageEffect.NONE);
    }

    // helpers

    // records how many empty rows are under the lowest drawn pixel of an image
    private void measureFeet(BufferedImage image) {
        int lowest = 0;
        for (int y = image.getHeight() - 1; y >= 0 && lowest == 0; y--) {
            for (int x = 0; x < image.getWidth(); x++) {
                if ((image.getRGB(x, y) >>> 24) != 0) {
                    lowest = y + 1;
                    break;
                }
            }
        }
        feetOffsets.put(image, image.getHeight() - lowest);
    }

    private int feetOffset(BufferedImage image) {
        return feetOffsets.getOrDefault(image, 0);
    }
}
