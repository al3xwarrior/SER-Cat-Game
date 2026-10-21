package Screens;

import Engine.*;
import Game.GameState;
import Game.ScreenCoordinator;
import Level.Map;
import Maps.TitleScreenMap;
import SpriteFont.SpriteFont;
import java.awt.*;

// This class is for the credits screen
public class CreditsScreen extends Screen {
    protected ScreenCoordinator screenCoordinator;
    protected Map background;
    protected KeyLocker keyLocker = new KeyLocker();
    protected SpriteFont creditsLabel;
    protected SpriteFont createdByLabel;
    protected SpriteFont returnInstructionsLabel;

    public CreditsScreen(ScreenCoordinator screenCoordinator) {
        this.screenCoordinator = screenCoordinator;
    }

    @Override
    public void initialize() {
        // setup graphics on screen (background map, spritefont text)
        background = new TitleScreenMap();
        background.setAdjustCamera(false);

        creditsLabel = new SpriteFont("Credits", 15, 7, "Arial", 30, Color.white);
        creditsLabel.setOutlineColor(new Color(20, 27, 79));
        creditsLabel.setOutlineThickness(5);

        createdByLabel = new SpriteFont("Created by:\n     Daniel Rodriguez\n     Alex McCammon\n     Flavio Di Palma\n     Jack Wickenheisser", 130, 121, "Arial", 20, Color.white);
        createdByLabel.setOutlineColor(new Color(20, 27, 79));
        createdByLabel.setOutlineThickness(5);

        returnInstructionsLabel = new SpriteFont("Press Enter to return to the menu", 20, 532, "Arial", 30, Color.white);
        returnInstructionsLabel.setOutlineColor(new Color(20, 27, 79));
        returnInstructionsLabel.setOutlineThickness(5);
        
        keyLocker.lockKey(Key.ENTER);
    }

    public void update() {
        background.update(null);

        if (Keyboard.isKeyUp(Key.ENTER)) {
            keyLocker.unlockKey(Key.ENTER);
        }

        // if space is pressed, go back to main menu
        if (!keyLocker.isKeyLocked(Key.ENTER) && Keyboard.isKeyDown(Key.ENTER)) {
            screenCoordinator.setGameState(GameState.MENU);
        }
    }

    public void draw(GraphicsHandler graphicsHandler) {
        background.draw(graphicsHandler);
        creditsLabel.draw(graphicsHandler);
        createdByLabel.drawWithParsedNewLines(graphicsHandler, 2);
        returnInstructionsLabel.draw(graphicsHandler);
    }
}
