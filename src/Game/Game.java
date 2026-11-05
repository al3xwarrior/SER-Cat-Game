package Game;

import Engine.GameWindow;
import Engine.ScreenManager;
import Music.MusicPlayer;

/*
 * The game starts here
 * This class just starts up a GameWindow and attaches the ScreenCoordinator to the ScreenManager instance in the GameWindow
 * From this point on the ScreenCoordinator class will dictate what the game does
 */
public class Game {

    private static MusicPlayer musicPlayer;

    public static void main(String[] args) {
        //inits music
        musicPlayer = new MusicPlayer();
        //starts playing the song
        musicPlayer.play();
        new Game();
    }

    public Game() {
        GameWindow gameWindow = new GameWindow();
        ScreenManager screenManager = gameWindow.getScreenManager();
        screenManager.setCurrentScreen(new ScreenCoordinator());
        gameWindow.startGame();
    }
}
