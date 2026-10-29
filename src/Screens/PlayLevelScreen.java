package Screens;

import Engine.GraphicsHandler;
import Engine.Screen;
import Engine.ScreenManager;
import Game.GameState;
import Game.ScreenCoordinator;
import Level.Map;
import Level.Player;
import Level.PlayerListener;
import MapEditor.EditorMaps;
import Players.Cat;
import SpriteFont.SpriteFont;

import java.awt.*;
import java.awt.font.FontRenderContext;

// This class is for when the platformer game is actually being played
public class PlayLevelScreen extends Screen implements PlayerListener {
    protected ScreenCoordinator screenCoordinator;
    protected Map map;
    protected Player player;
    protected PlayLevelScreenState playLevelScreenState;
    protected int screenTimer;
    protected LevelClearedScreen levelClearedScreen;
    protected LevelLoseScreen levelLoseScreen;
    protected boolean levelCompletedStateChangeStart;

    protected SpriteFont coordsDebugLabel;

    // hint text at the top of the screen
    protected static final Font HINT_FONT = new Font("Arial", Font.BOLD, 20);
    protected static final int HINT_Y = 110;
    protected static final float HINT_FADE_PER_FRAME = 0.08f;
    protected String hintText = null;
    protected float hintAlpha = 0f;

    public PlayLevelScreen(ScreenCoordinator screenCoordinator) {
        this.screenCoordinator = screenCoordinator;
    }

    public void initialize() {
        // define/setup map
        // the map is chosen on the level selector screen
        this.map = EditorMaps.getMapByName(screenCoordinator.getSelectedMapName());

        // setup player
        this.player = new Cat(map.getPlayerStartPosition().x, map.getPlayerStartPosition().y);
        this.player.setMap(map);
        this.player.addListener(this);

        levelClearedScreen = new LevelClearedScreen();
        levelLoseScreen = new LevelLoseScreen(this);

        // debug stuff - alex
        this.coordsDebugLabel = new SpriteFont("X: 1 | Y: 1", 0, 0, "Times New Roman", 24, Color.WHITE);

        this.playLevelScreenState = PlayLevelScreenState.RUNNING;
    }

    public void update() {
        // based on screen state, perform specific actions
        switch (playLevelScreenState) {
            // if level is "running" update player and map to keep game logic for the platformer level going
            case RUNNING:
                player.update();
                map.update(player);
                updateHint();
                break;
            // if level has been completed, bring up the level cleared screen
            case LEVEL_COMPLETED:
                if (levelCompletedStateChangeStart) {
                    screenTimer = 130;
                    levelCompletedStateChangeStart = false;
                } else {
                    levelClearedScreen.update();
                    screenTimer--;
                    if (screenTimer == 0) {
                        goToNextLevel();
                    }
                }
                break;
            // wait on level lose screen to make a decision (either resets level or sends player back to main menu)
            case LEVEL_LOSE:
                levelLoseScreen.update();
                break;
        }
    }

    public void draw(GraphicsHandler graphicsHandler) {
        // based on screen state, draw appropriate graphics
        switch (playLevelScreenState) {
            case RUNNING:
                map.draw(graphicsHandler);
                ((Cat) player).drawTimeWarpOverlay(graphicsHandler);
                player.draw(graphicsHandler);
                ((Cat) player).drawLowHealthWarning(graphicsHandler);
                ((Cat) player).drawHealthBar(graphicsHandler);
                ((Cat) player).drawStaminaBar(graphicsHandler);
                drawHint(graphicsHandler);
                coordsDebugLabel.setText("X: " + player.getLocation().x + " | Y: " + player.getLocation().y);
                coordsDebugLabel.draw(graphicsHandler);
                break;
            case LEVEL_COMPLETED:
                levelClearedScreen.draw(graphicsHandler);
                break;
            case LEVEL_LOSE:
                levelLoseScreen.draw(graphicsHandler);
                break;
        }
    }

    // hide the hint while time warping
    protected void updateHint() {
        String currentHint = player.isTimeWarping() ? null : map.getHintAt(player);

        if (currentHint != null) {
            hintText = currentHint;
            hintAlpha = Math.min(1f, hintAlpha + HINT_FADE_PER_FRAME);
        } else {
            hintAlpha = Math.max(0f, hintAlpha - HINT_FADE_PER_FRAME);
        }
    }

    protected void drawHint(GraphicsHandler graphicsHandler) {
        if (hintText == null || hintAlpha <= 0f) {
            return;
        }

        int textWidth = (int) HINT_FONT.getStringBounds(hintText, new FontRenderContext(null, true, true)).getWidth();
        int x = (ScreenManager.getScreenWidth() - textWidth) / 2;
        int alpha = Math.round(255 * hintAlpha);

        graphicsHandler.drawStringWithOutline(hintText, x, HINT_Y, HINT_FONT, new Color(255, 255, 255, alpha), new Color(0, 0, 0, alpha), 4f);
    }

    public PlayLevelScreenState getPlayLevelScreenState() {
        return playLevelScreenState;
    }

    @Override
    public void onLevelCompleted() {
        if (playLevelScreenState != PlayLevelScreenState.LEVEL_COMPLETED) {
            playLevelScreenState = PlayLevelScreenState.LEVEL_COMPLETED;
            levelCompletedStateChangeStart = true;
        }
    }

    @Override
    public void onDeath() {
        if (playLevelScreenState != PlayLevelScreenState.LEVEL_LOSE) {
            playLevelScreenState = PlayLevelScreenState.LEVEL_LOSE;
        }
    }

    public void resetLevel() {
        initialize();
    }

    // after level 1 go to level 2, otherwise go back to the menu
    public void goToNextLevel() {
        if (screenCoordinator.getSelectedMapName().equals("Level1Map")) {
            screenCoordinator.setSelectedMapName("Level2Map");
            initialize();
        } else {
            goBackToMenu();
        }
    }

    public void goBackToMenu() {
        screenCoordinator.setGameState(GameState.MENU);
    }

    // This enum represents the different states this screen can be in
    private enum PlayLevelScreenState {
        RUNNING, LEVEL_COMPLETED, LEVEL_LOSE
    }
}
