package Screens;

import Engine.*;
import Game.GameState;
import Game.ScreenCoordinator;
import Level.Map;
import Maps.TitleScreenMap;
import SpriteFont.SpriteFont;
import Utils.PlayerControls;
import java.awt.*;

// This class is for the credits screen
public class ControlScreen extends Screen {
    protected ScreenCoordinator screenCoordinator;
    protected Map background;
    protected KeyLocker keyLocker = new KeyLocker();

    protected Boolean assigningKey = false;
    
    protected int selectedItem = 0;

    protected SpriteFont jumpButton;
    protected SpriteFont leftButton;
    protected SpriteFont rightButton;
    protected SpriteFont crouchButton;
    
    protected SpriteFont dashButton;
    protected SpriteFont pounceButton;
    protected SpriteFont diveButton;

    protected SpriteFont returnButton;
    protected SpriteFont instructions;

    public ControlScreen(ScreenCoordinator screenCoordinator) {
        this.screenCoordinator = screenCoordinator;
    }

    @Override
    public void initialize() {
        // setup graphics on screen (background map, spritefont text)
        background = new TitleScreenMap();
        background.setAdjustCamera(false);

        jumpButton = new SpriteFont("Jump: [W]", 150, 100, "Arial", 30, new Color(255, 255, 255));
        jumpButton.setOutlineColor(new Color(20, 27, 79));
        jumpButton.setOutlineThickness(5);

        leftButton = new SpriteFont("Left: [A]", 150, 175, "Arial", 30, new Color(255, 255, 255));
        leftButton.setOutlineColor(new Color(20, 27, 79));
        leftButton.setOutlineThickness(5);

        rightButton = new SpriteFont("Right: [D]", 150, 250, "Arial", 30, new Color(255, 255, 255));
        rightButton.setOutlineColor(new Color(20, 27, 79));
        rightButton.setOutlineThickness(5);

        crouchButton = new SpriteFont("Crouch: [S]", 150, 325, "Arial", 30, new Color(255, 255, 255));
        crouchButton.setOutlineColor(new Color(20, 27, 79));
        crouchButton.setOutlineThickness(5);

        dashButton = new SpriteFont("Dash: [J]", 400, 100, "Arial", 30, new Color(255, 255, 255));
        dashButton.setOutlineColor(new Color(20, 27, 79));
        dashButton.setOutlineThickness(5);

        pounceButton = new SpriteFont("Pounce: [K]", 400, 175, "Arial", 30, new Color(255, 255, 255));
        pounceButton.setOutlineColor(new Color(20, 27, 79));
        pounceButton.setOutlineThickness(5);

        diveButton = new SpriteFont("Dive: [L]", 400, 250, "Arial", 30, new Color(255, 255, 255));
        diveButton.setOutlineColor(new Color(20, 27, 79));
        diveButton.setOutlineThickness(5);

        returnButton = new SpriteFont("Return to Menu", 300, 425, "Arial", 30, new Color(255, 255, 255));
        returnButton.setOutlineColor(new Color(20, 27, 79));
        returnButton.setOutlineThickness(5);

        instructions = new SpriteFont("Use the jump, left, right, and crouch keys to navigate the menu.\nPress Enter then a key to assign a control.\nTwo controls cannot be assigned to the same key.", 15, 7, "Arial", 20, new Color(255, 255, 255));
        instructions.setOutlineColor(new Color(20, 27, 79));
        instructions.setOutlineThickness(5);
        
        keyLocker.lockKey(Key.ENTER);
    }

    public void update() {
        background.update(null);

        if (Keyboard.isKeyUp(Key.ENTER)) {
            keyLocker.unlockKey(Key.ENTER);
        }

        if (Keyboard.isKeyUp(PlayerControls.getJumpControl())) {
            keyLocker.unlockKey(PlayerControls.getJumpControl());
        }

        if (Keyboard.isKeyUp(PlayerControls.getCrouchControl())) {
            keyLocker.unlockKey(PlayerControls.getCrouchControl());
        }

        if (Keyboard.isKeyUp(PlayerControls.getLeftControl())) {
            keyLocker.unlockKey(PlayerControls.getLeftControl());
        }

        if (Keyboard.isKeyUp(PlayerControls.getRightControl())) {
            keyLocker.unlockKey(PlayerControls.getRightControl());
        }

        if (assigningKey) {
            for (Key key : Key.values()) {
                if (Keyboard.isKeyDown(key) && !keyLocker.isKeyLocked(key) && key != Key.ENTER) {
                    switch (selectedItem) {
                        case 0 -> { // jump
                            PlayerControls.setJumpControl(key);
                        }
                        case 1 ->{ // left
                            PlayerControls.setLeftControl(key);
                        }
                        case 2 ->{ // right
                            PlayerControls.setRightControl(key);
                        }
                        case 3 ->{ // down
                            PlayerControls.setCrouchControl(key);
                        }
                        case 4 ->{ // dash
                            PlayerControls.setDashControl(key);
                        }
                        case 5 ->{ // pounce
                            PlayerControls.setPounceControl(key);
                        }
                        case 6 ->{ // dive
                            PlayerControls.setDiveControl(key);
                        }
                    }

                    assigningKey = false;
                    keyLocker.lockKey(key);
                }
            }

            return;
        }

        // if enter is pressed, then edit key
        if (!keyLocker.isKeyLocked(Key.ENTER) && Keyboard.isKeyDown(Key.ENTER)) {
            if (selectedItem != 7) {
                assigningKey = true;
            } else {
                screenCoordinator.setGameState(GameState.MENU);
            }
        }

        if (!keyLocker.isKeyLocked(PlayerControls.getJumpControl()) && Keyboard.isKeyDown(PlayerControls.getJumpControl())) {
            selectedItem--;
            if (selectedItem < 0) selectedItem = 7;
            keyLocker.lockKey(PlayerControls.getJumpControl());
        }

        if (!keyLocker.isKeyLocked(PlayerControls.getCrouchControl()) && Keyboard.isKeyDown(PlayerControls.getCrouchControl())) {
            selectedItem++;
            if (selectedItem > 7) selectedItem = 0;
            keyLocker.lockKey(PlayerControls.getCrouchControl());
        }

        if (!keyLocker.isKeyLocked(PlayerControls.getLeftControl()) && Keyboard.isKeyDown(PlayerControls.getLeftControl())) {
            selectedItem -= 4;
            if (selectedItem < 0) selectedItem = 7 - Math.abs(1 - selectedItem);
            keyLocker.lockKey(PlayerControls.getLeftControl());
        }

        if (!keyLocker.isKeyLocked(PlayerControls.getRightControl()) && Keyboard.isKeyDown(PlayerControls.getRightControl())) {
            selectedItem += 4;
            if (selectedItem > 7) selectedItem = selectedItem - 7;
            keyLocker.lockKey(PlayerControls.getRightControl());
        }

        jumpButton.setColor(new Color(255, 255, 255));
        jumpButton.setText("Jump: [" + PlayerControls.getJumpControl() + "]");
        
        leftButton.setColor(new Color(255, 255, 255));
        leftButton.setText("Left: [" + PlayerControls.getLeftControl() + "]");

        rightButton.setColor(new Color(255, 255, 255));
        rightButton.setText("Right: [" + PlayerControls.getRightControl() + "]");

        crouchButton.setColor(new Color(255, 255, 255));
        crouchButton.setText("Crouch: [" + PlayerControls.getCrouchControl() + "]");

        diveButton.setColor(new Color(255, 255, 255));
        diveButton.setText("Dive: [" + PlayerControls.getDiveControl() + "]");

        dashButton.setColor(new Color(255, 255, 255));
        dashButton.setText("Dash: [" + PlayerControls.getDashControl() + "]");

        pounceButton.setColor(new Color(255, 255, 255));
        pounceButton.setText("Pounce: [" + PlayerControls.getPounceControl() + "]");

        returnButton.setColor(new Color(255, 255, 255));

        switch (selectedItem) {
            case 0 -> {
                jumpButton.setColor(new Color(255, 215, 0));
            }
            case 1 ->{
                leftButton.setColor(new Color(255, 215, 0));
            }
            case 2 ->{
                rightButton.setColor(new Color(255, 215, 0));
            }
            case 3 ->{
                crouchButton.setColor(new Color(255, 215, 0));
            }
            case 4 ->{
                dashButton.setColor(new Color(255, 215, 0));
            }
            case 5 ->{
                pounceButton.setColor(new Color(255, 215, 0));
            }
            case 6 ->{
                diveButton.setColor(new Color(255, 215, 0));
            }
            case 7 ->{
                returnButton.setColor(new Color(255, 215, 0));
            }
        }
    }

    public void draw(GraphicsHandler graphicsHandler) {
        background.draw(graphicsHandler);
        jumpButton.draw(graphicsHandler);
        leftButton.draw(graphicsHandler);
        rightButton.draw(graphicsHandler);
        crouchButton.draw(graphicsHandler);
        dashButton.draw(graphicsHandler);
        pounceButton.draw(graphicsHandler);
        diveButton.draw(graphicsHandler);
        returnButton.draw(graphicsHandler);
        instructions.drawWithParsedNewLines(graphicsHandler, 2);
    }
}
