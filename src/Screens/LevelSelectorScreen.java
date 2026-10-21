package Screens;

import Engine.*;
import Game.GameState;
import Game.ScreenCoordinator;
import Level.Map;
import MapEditor.EditorMaps;
import Maps.TitleScreenMap;
import Utils.PlayerControls;
import java.awt.*;
import java.util.ArrayList;

// This is the screen where the player picks which level to play
// Levels can be chosen by clicking a button, or with the arrow keys/WASD + ENTER. ESC (or the back button) returns to the menu
public class LevelSelectorScreen extends Screen {
    private static final int COLUMNS = 2;
    private static final int CARD_WIDTH = 300;
    private static final int CARD_HEIGHT = 90;
    private static final int CARD_GAP = 24;
    private static final int GRID_TOP = 150;
    private static final int BACK_WIDTH = 160;
    private static final int BACK_HEIGHT = 44;
    private static final int HOVER_LIFT = 4;
    private static final int PIXEL = 4; // size of one "pixel" in the button art

    private static final Font NUMBER_FONT = new Font(Font.MONOSPACED, Font.BOLD, 30);
    private static final Font NAME_FONT = new Font(Font.MONOSPACED, Font.BOLD, 24);
    private static final Font BACK_FONT = new Font(Font.MONOSPACED, Font.BOLD, 20);

    private static final Color PANEL_COLOR = new Color(20, 28, 48, 225);
    private static final Color PANEL_HOVER_COLOR = new Color(38, 52, 86, 240);
    private static final Color ACCENT = new Color(49, 207, 240);
    private static final Color GOLD = new Color(255, 215, 0);

    protected ScreenCoordinator screenCoordinator;
    protected KeyLocker keyLocker = new KeyLocker();
    protected ArrayList<String> maps;
    protected Map background;

    // display names are looked up once; getting them requires building the whole map, which is far too slow to do every frame
    private String[] displayNames;

    // index of the button currently highlighted: 0..maps.size()-1 are levels, maps.size() is the back button
    private int focusedIndex = 0;
    private int lastMouseX = -1;
    private int lastMouseY = -1;

    public LevelSelectorScreen(ScreenCoordinator screenCoordinator) {
        this.maps = EditorMaps.getMaps();
        this.screenCoordinator = screenCoordinator;
    }

    @Override
    public void initialize() {
        background = new TitleScreenMap();
        background.setAdjustCamera(false);
        focusedIndex = 0;

        displayNames = new String[maps.size()];
        for (int i = 0; i < maps.size(); i++) {
            displayNames[i] = EditorMaps.getMapByName(maps.get(i)).getDisplayName();
        }

        // the ENTER/click that opened this screen shouldn't also pick a level
        keyLocker.lockKey(Key.ENTER);
        keyLocker.lockKey(Key.ESC);
        Mouse.clearLeftClick();
    }

    @Override
    public void update() {
        background.update(null);

        updateMouseHover();
        updateKeyboardNavigation();

        if (Mouse.consumeLeftClick()) {
            int clicked = getButtonAt(Mouse.getX(), Mouse.getY());
            if (clicked != -1) {
                activate(clicked);
            }
        }
    }

    // moving the mouse over a button focuses it
    private void updateMouseHover() {
        int mouseX = Mouse.getX();
        int mouseY = Mouse.getY();
        if (mouseX != lastMouseX || mouseY != lastMouseY) {
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            int hovered = getButtonAt(mouseX, mouseY);
            if (hovered != -1) {
                focusedIndex = hovered;
            }
        }
    }

    private void updateKeyboardNavigation() {
        int backIndex = maps.size();

        if (pressed(PlayerControls.getRightControl())) focusedIndex = Math.min(focusedIndex + 1, backIndex);
        if (pressed(PlayerControls.getLeftControl())) focusedIndex = Math.max(focusedIndex - 1, 0);
        if (pressed(PlayerControls.getCrouchControl())) focusedIndex = focusedIndex + COLUMNS < maps.size() ? focusedIndex + COLUMNS : backIndex;
        if (pressed(PlayerControls.getJumpControl())) {
            if (focusedIndex == backIndex) {
                focusedIndex = maps.size() - 1;
            } else {
                focusedIndex = Math.max(focusedIndex - COLUMNS, 0);
            }
        }

        if (pressed(Key.ENTER)) activate(focusedIndex);
        else if (pressed(Key.ESC)) screenCoordinator.setGameState(GameState.MENU);
    }

    // returns true once per key press (the key must be released before it can trigger again)
    private boolean pressed(Key key) {
        if (Keyboard.isKeyUp(key)) {
            keyLocker.unlockKey(key);
        }
        if (Keyboard.isKeyDown(key) && !keyLocker.isKeyLocked(key)) {
            keyLocker.lockKey(key);
            return true;
        }
        return false;
    }

    private void activate(int index) {
        if (index == maps.size()) {
            screenCoordinator.setGameState(GameState.MENU);
        } else {
            screenCoordinator.setSelectedMapName(maps.get(index));
            screenCoordinator.setGameState(GameState.LEVEL);
        }
    }

    // layout ui
    private int screenWidth() {
        return ScreenManager.getScreenWidth();
    }

    private int gridLeft() {
        int columns = Math.min(COLUMNS, maps.size());
        return (screenWidth() - (columns * CARD_WIDTH + (columns - 1) * CARD_GAP)) / 2;
    }

    private Rectangle getCardBounds(int index) {
        int column = index % COLUMNS;
        int row = index / COLUMNS;
        return new Rectangle(
                gridLeft() + column * (CARD_WIDTH + CARD_GAP),
                GRID_TOP + row * (CARD_HEIGHT + CARD_GAP),
                CARD_WIDTH, CARD_HEIGHT);
    }

    private Rectangle getBackBounds() {
        int rows = (maps.size() + COLUMNS - 1) / COLUMNS;
        int y = GRID_TOP + rows * (CARD_HEIGHT + CARD_GAP) + 10;
        return new Rectangle((screenWidth() - BACK_WIDTH) / 2, y, BACK_WIDTH, BACK_HEIGHT);
    }

    private Rectangle getBounds(int index) {
        return index == maps.size() ? getBackBounds() : getCardBounds(index);
    }

    // returns the index of the button under the given point, or -1 if none
    private int getButtonAt(int x, int y) {
        for (int i = 0; i <= maps.size(); i++) {
            if (getBounds(i).contains(x, y)) {
                return i;
            }
        }
        return -1;
    }

    // most of the drawing code here is from AI. I think it does a good job at rendering expecially with pixel
    // art - Alex
    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        Graphics2D g = graphicsHandler.getGraphics();
        RenderingHints oldHints = g.getRenderingHints();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        background.draw(graphicsHandler);

        for (int i = 0; i < maps.size(); i++) {
            drawCard(g, i);
        }
        drawBackButton(g);

        g.setRenderingHints(oldHints);
    }

    // fills a rectangle with its corners notched off by `cut` pixels
    private void fillNotched(Graphics2D g, int x, int y, int w, int h, int cut, Color color) {
        g.setColor(color);
        g.fillRect(x + cut, y, w - 2 * cut, h);
        g.fillRect(x, y + cut, w, h - 2 * cut);
    }

    // a chunky pixel-art box: dark outline, light top edge and darker bottom edge
    private void drawPixelBox(Graphics2D g, int x, int y, int w, int h, Color fill, Color border) {
        fillNotched(g, x, y, w, h, PIXEL, border);
        fillNotched(g, x + PIXEL, y + PIXEL, w - 2 * PIXEL, h - 2 * PIXEL, PIXEL, fill);
        // bevel
        g.setColor(brighter(fill));
        g.fillRect(x + 2 * PIXEL, y + PIXEL, w - 4 * PIXEL, PIXEL / 2);
        g.setColor(darker(fill));
        g.fillRect(x + 2 * PIXEL, y + h - PIXEL - PIXEL / 2, w - 4 * PIXEL, PIXEL / 2);
    }

    private Color brighter(Color c) {
        return new Color(Math.min(255, c.getRed() + 30), Math.min(255, c.getGreen() + 30), Math.min(255, c.getBlue() + 30));
    }

    private Color darker(Color c) {
        return new Color(Math.max(0, c.getRed() - 15), Math.max(0, c.getGreen() - 15), Math.max(0, c.getBlue() - 15));
    }

    // text with a hard 2px black drop shadow, vertically centered on centerY
    private void drawPixelText(Graphics2D g, String text, int x, int centerY, Font font, Color color) {
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics();
        int baseline = centerY + (metrics.getAscent() - metrics.getDescent()) / 2;
        g.setColor(Color.BLACK);
        g.drawString(text, x + 2, baseline + 2);
        g.setColor(color);
        g.drawString(text, x, baseline);
    }

    private void drawCard(Graphics2D g, int index) {
        boolean focused = index == focusedIndex;
        Rectangle bounds = getCardBounds(index);
        int x = bounds.x;
        int y = bounds.y - (focused ? HOVER_LIFT : 0);
        int w = bounds.width;
        int h = bounds.height;

        drawPixelBox(g, x, y, w, h, focused ? PANEL_HOVER_COLOR : PANEL_COLOR, focused ? GOLD : Color.BLACK);

        // square number badge with notched corners
        int badge = 52;
        int badgeX = x + 20;
        int badgeY = y + (h - badge) / 2;
        fillNotched(g, badgeX, badgeY, badge, badge, PIXEL, Color.BLACK);
        fillNotched(g, badgeX + 2, badgeY + 2, badge - 4, badge - 4, PIXEL - 2, focused ? GOLD : ACCENT);

        Font numberFont = NUMBER_FONT;
        g.setFont(numberFont);
        String number = String.valueOf(index + 1);
        int numberX = badgeX + (badge - g.getFontMetrics().stringWidth(number)) / 2;
        drawPixelText(g, number, numberX, badgeY + badge / 2, numberFont, Color.WHITE);

        drawPixelText(g, displayNames[index], badgeX + badge + 18, y + h / 2,
                NAME_FONT, focused ? GOLD : Color.WHITE);
    }

    private void drawBackButton(Graphics2D g) {
        boolean focused = focusedIndex == maps.size();
        Rectangle bounds = getBackBounds();
        int y = bounds.y - (focused ? HOVER_LIFT : 0);

        drawPixelBox(g, bounds.x, y, bounds.width, bounds.height, focused ? PANEL_HOVER_COLOR : PANEL_COLOR, focused ? GOLD : Color.BLACK);

        Font font = BACK_FONT;
        g.setFont(font);
        String label = "BACK";
        int labelX = bounds.x + (bounds.width - g.getFontMetrics().stringWidth(label)) / 2;
        drawPixelText(g, label, labelX, y + bounds.height / 2, font, focused ? GOLD : Color.WHITE);
    }
}
