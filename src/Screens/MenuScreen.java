package Screens;

import Engine.*;
import Game.GameState;
import Game.ScreenCoordinator;
import Level.Map;
import Maps.TitleScreenMap;
import SpriteFont.SpriteFont;
import Utils.PlayerControls;
import java.awt.*;

// This is the class for the main menu screen
public class MenuScreen extends Screen {
    protected ScreenCoordinator screenCoordinator;
    protected int currentMenuItemHovered = 0; // current menu item being "hovered" over
    protected int menuItemSelected = -1;
    protected SpriteFont playGame;
    protected SpriteFont credits;
    protected SpriteFont controls;
    protected Map background;
    protected int keyPressTimer;
    protected int pointerLocationX, pointerLocationY;
    protected KeyLocker keyLocker = new KeyLocker();

    // clickable area of each menu item (the blue square plus its text), in the same order as the items
    protected Rectangle[] menuItemBounds;
    protected int lastMouseX = -1;
    protected int lastMouseY = -1;

    public MenuScreen(ScreenCoordinator screenCoordinator) {
        this.screenCoordinator = screenCoordinator;
    }

    @Override
    public void initialize() {
        playGame = new SpriteFont("PLAY GAME", 200, 123, "Arial", 30, new Color(255, 255, 255));
        playGame.setOutlineColor(new Color(20, 27, 79));
        playGame.setOutlineThickness(5);
        credits = new SpriteFont("CREDITS", 200, 223, "Arial", 30, new Color(255, 255, 255));
        credits.setOutlineColor(new Color(20, 27, 79));
        credits.setOutlineThickness(5);
        controls = new SpriteFont("CONTROLS", 200, 323, "Arial", 30, new Color(255, 255, 255));
        controls.setOutlineColor(new Color(20, 27, 79));
        controls.setOutlineThickness(5);
        background = new TitleScreenMap();
        background.setAdjustCamera(false);
        keyPressTimer = 0;
        menuItemSelected = -1;
        keyLocker.lockKey(Key.ENTER);

        menuItemBounds = new Rectangle[]{getMenuItemBounds(playGame), getMenuItemBounds(credits), getMenuItemBounds(controls)};
        Mouse.clearLeftClick(); // the click that opened this screen shouldn't also pick an item
    }

    // the area from the blue square to the end of the item's text
    private Rectangle getMenuItemBounds(SpriteFont item) {
        int textWidth = new Canvas().getFontMetrics(item.getFont()).stringWidth(item.getText());
        int left = 170;
        int top = Math.round(item.getY()) - 6;
        return new Rectangle(left, top, Math.round(item.getX()) + textWidth - left + 10, 46);
    }

    // returns the index of the menu item under the given point, or -1 if there is none
    private int getMenuItemAt(int x, int y) {
        for (int i = 0; i < menuItemBounds.length; i++) {
            if (menuItemBounds[i].contains(x, y)) {
                return i;
            }
        }
        return -1;
    }

    public void update() {
        // update background map (to play tile animations)
        background.update(null);

        // if down or up is pressed, change menu item "hovered" over (blue square in front of text will move along with currentMenuItemHovered changing)
        if (Keyboard.isKeyDown(PlayerControls.getCrouchControl()) &&  keyPressTimer == 0) {
            keyPressTimer = 14;
            currentMenuItemHovered++;
        } else if (Keyboard.isKeyDown(PlayerControls.getJumpControl()) &&  keyPressTimer == 0) {
            keyPressTimer = 14;
            currentMenuItemHovered--;
        } else {
            if (keyPressTimer > 0) {
                keyPressTimer--;
            }
        }

        // moving the mouse over an item hovers it, clicking an item chooses it
        int mouseX = Mouse.getX();
        int mouseY = Mouse.getY();
        if (mouseX != lastMouseX || mouseY != lastMouseY) {
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            int hovered = getMenuItemAt(mouseX, mouseY);
            if (hovered != -1) {
                currentMenuItemHovered = hovered;
            }
        }
        if (Mouse.consumeLeftClick()) {
            int clicked = getMenuItemAt(mouseX, mouseY);
            if (clicked != -1) {
                currentMenuItemHovered = clicked;
                chooseMenuItem(clicked);
                return;
            }
        }

        // if down is pressed on last menu item or up is pressed on first menu item, "loop" the selection back around to the beginning/end
        if (currentMenuItemHovered > 2) {
            currentMenuItemHovered = 0;
        } else if (currentMenuItemHovered < 0) {
            currentMenuItemHovered = 2;
        }

        // sets location for blue square in front of text (pointerLocation) and also sets color of spritefont text based on which menu item is being hovered
        if (currentMenuItemHovered == 0) {
            playGame.setColor(new Color(255, 215, 0));
            credits.setColor(new Color(255, 255, 255));
            controls.setColor(new Color(255, 255, 255));
            pointerLocationX = 170;
            pointerLocationY = 130;
        } else if (currentMenuItemHovered == 1) {
            playGame.setColor(new Color(255, 255, 255));
            credits.setColor(new Color(255, 215, 0));
            controls.setColor(new Color(255, 255, 255));
            pointerLocationX = 170;
            pointerLocationY = 230;
        } else if (currentMenuItemHovered == 2) {
            playGame.setColor(new Color(255, 255, 255));
            credits.setColor(new Color(255, 255, 255));
            controls.setColor(new Color(255, 215, 0));
            pointerLocationX = 170;
            pointerLocationY = 330;
        }

        // if enter is pressed on menu item, change to appropriate screen based on which menu item was chosen
        if (Keyboard.isKeyUp(Key.ENTER)) {
            keyLocker.unlockKey(Key.ENTER);
        }
        if (!keyLocker.isKeyLocked(Key.ENTER) && Keyboard.isKeyDown(Key.ENTER)) {
            chooseMenuItem(currentMenuItemHovered);
        }
    }

    // changes to the appropriate screen based on which menu item was chosen
    private void chooseMenuItem(int item) {
        menuItemSelected = item;
        if (menuItemSelected == 0) {
//            screenCoordinator.setGameState(GameState.LEVEL);
            screenCoordinator.setGameState(GameState.LEVEL_SELECTOR);
        } else if (menuItemSelected == 1) {
            screenCoordinator.setGameState(GameState.CREDITS);
        } else if (menuItemSelected == 2) {
            screenCoordinator.setGameState(GameState.CONTROLS);
        }
    }

    public void draw(GraphicsHandler graphicsHandler) {
        background.draw(graphicsHandler);
        playGame.draw(graphicsHandler);
        credits.draw(graphicsHandler);
        controls.draw(graphicsHandler);
        graphicsHandler.drawFilledRectangleWithBorder(pointerLocationX, pointerLocationY, 20, 20, new Color(255, 215, 0), Color.black, 2);
    }
}
