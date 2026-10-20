package Maps;

import Enemies.BearEnemy;
import Enemies.BirdEnemy;
import Enemies.SquirrelEnemy;
import Engine.ImageLoader;
import EnhancedMapTiles.EndLevelBox;
import EnhancedMapTiles.HorizontalMovingPlatform;
import GameObject.Rectangle;
import Level.Enemy;
import Level.EnhancedMapTile;
import Level.Map;
import Level.TileType;
import Powerups.HockeyStickPowerup;
import Tilesets.CommonTileset;
import Utils.Direction;

import java.util.ArrayList;

/**
 * Level 1: a fast downhill run with wildlife patrols and optional high routes.
 * Terrain and pass-through scenery live in level1_map.txt.
 * All coordinates below are zero-based tile coordinates (48 pixels per tile).
 */
public class Level1Map extends Map {
    public Level1Map() {
        super("level1_map.txt", "Level 1", new CommonTileset());
        playerStartPosition = getMapTile(3, 8).getLocation();
    }

    @Override
    public ArrayList<Enemy> loadEnemies() {
        ArrayList<Enemy> enemies = new ArrayList<>();
        // {left edge, right edge, height above the trail}. Alternating high
        // and low lanes make jumping a choice rather than a guaranteed dodge.
        int[][] birdPatrols = {
                {26, 34, 1}, {52, 62, 1}, {75, 81, 1}, {82, 90, 2},
                {109, 118, 1}, {123, 131, 2}, {140, 149, 1}, {169, 178, 2},
                {193, 200, 1}, {201, 211, 2}, {232, 242, 1}, {261, 270, 2},
                {291, 301, 1}, {306, 315, 2}, {320, 327, 1}, {326, 337, 1},
                {357, 367, 2}, {388, 398, 1}, {411, 418, 1}, {417, 427, 2},
                {448, 458, 1}, {470, 480, 2}
        };
        for (int i = 0; i < birdPatrols.length; i++) {
            int[] patrol = birdPatrols[i];
            int flightRow = findGroundLevel(patrol[0]) - patrol[2];
            enemies.add(new BirdEnemy(
                    getMapTile(patrol[0], flightRow).getLocation(),
                    getMapTile(patrol[1], flightRow).getLocation(),
                    i % 2 == 0 ? Direction.LEFT : Direction.RIGHT,
                    3.7f));
        }

        int[][] squirrelPatrols = {
                {37, 44}, {65, 72}, {121, 132}, {151, 160},
                {181, 190}, {243, 253}, {271, 280}, {338, 348},
                {369, 378}, {429, 438}, {453, 461}, {480, 487}
        };
        for (int i = 0; i < squirrelPatrols.length; i++) {
            int[] patrol = squirrelPatrols[i];
            int row = findGroundLevel(patrol[0]) - 1;
            enemies.add(new SquirrelEnemy(
                    getMapTile(patrol[0], row).getLocation(),
                    getMapTile(patrol[1], row).getLocation(),
                    i % 2 == 0 ? Direction.RIGHT : Direction.LEFT,
                    3.3f));
        }

        // Bears are wider, so each one gets a long, level clearing.
        int[][] bearPatrols = {{82, 99}, {204, 221}, {304, 318}, {391, 408}, {470, 488}};
        for (int i = 0; i < bearPatrols.length; i++) {
            int[] patrol = bearPatrols[i];
            int row = findGroundLevel(patrol[0]) - 1;
            enemies.add(new BearEnemy(
                    getMapTile(patrol[0], row).getLocation(),
                    getMapTile(patrol[1], row).getLocation(),
                    i % 2 == 0 ? Direction.RIGHT : Direction.LEFT,
                    2.1f));
        }
        return enemies;
    }

    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> tiles = new ArrayList<>();
        // Optional platforms let a skilled player skim over two busy slopes.
        // The ground below stays continuous, so neither ride is required.
        int[][] crossings = {{193, 204, 30}, {320, 331, 47}};
        for (int[] crossing : crossings) {
            tiles.add(new HorizontalMovingPlatform(
                    ImageLoader.load("GreenPlatform.png"),
                    getMapTile(crossing[0], crossing[2]).getLocation(),
                    getMapTile(crossing[1], crossing[2]).getLocation(),
                    TileType.JUMP_THROUGH_PLATFORM,
                    3,
                    new Rectangle(0, 6, 16, 4),
                    Direction.RIGHT));
        }

        int[][] pickupTiles = {{31, 14}, {156, 28}, {278, 44}, {402, 61}};
        for (int[] tile : pickupTiles) {
            tiles.add(new HockeyStickPowerup(getMapTile(tile[0], tile[1]).getLocation()));
        }

        // Flat trailhead, well before the right boundary, with room to land.
        tiles.add(new EndLevelBox(getMapTile(494, 74).getLocation()));
        return tiles;
    }
}
