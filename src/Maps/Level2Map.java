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
import Tilesets.WhitneyAveTileset;
import Utils.Direction;

import java.util.ArrayList;

// Level 2: Whitney Ave in Hamden, from the bottom of Sleeping Giant down to Lake Whitney
// the map layout is in level2_map.txt, all the numbers here are tile positions
public class Level2Map extends Map {
    public Level2Map() {
        super("level2_map.txt", "Level 2", new WhitneyAveTileset());
        playerStartPosition = getMapTile(3, 8).getLocation();
    }

    @Override
    public ArrayList<Enemy> loadEnemies() {
        ArrayList<Enemy> enemies = new ArrayList<>();
        // {left edge, right edge, row}
        int[][] squirrelPatrols = {
                {8, 20, 9},     // trailhead, before the slide
                {48, 55, 8},    // Haven Beer roof, between the AC units
                {62, 68, 15},   // street between Haven Beer and Eli's, up to the crate
                {85, 91, 9},    // Eli's upper roof
                {96, 104, 15},  // street under the fire escape
                {106, 113, 5},  // Dickerman's roof, up to the AC unit
                {126, 133, 5},  // other side of the rooftop gap
                {216, 228, 12}  // last stretch before the exit
        };
        for (int[] patrol : squirrelPatrols) {
            enemies.add(new SquirrelEnemy(
                    getMapTile(patrol[0], patrol[2]).getLocation(),
                    getMapTile(patrol[1], patrol[2]).getLocation(),
                    Direction.LEFT,
                    2.5f));
        }

        // {left edge, right edge, row, speed}
        float[][] birdLanes = {
                {29, 38, 14, 2.0f},    // above the bear at the bottom of the slide
                {96, 104, 11, 1.5f},   // across the Dickerman's fire escape
                {105, 118, 4, 2.0f},   // over Dickerman's roof
                {161, 174, 10, 2.0f},  // Lake Whitney, first half
                {172, 186, 9, 2.5f}    // Lake Whitney, second half
        };
        for (int i = 0; i < birdLanes.length; i++) {
            float[] lane = birdLanes[i];
            enemies.add(new BirdEnemy(
                    getMapTile((int) lane[0], (int) lane[2]).getLocation(),
                    getMapTile((int) lane[1], (int) lane[2]).getLocation(),
                    i % 2 == 0 ? Direction.LEFT : Direction.RIGHT,
                    lane[3]));
        }

        // {left edge, right edge, row}
        int[][] bearPatrols = {{28, 39, 16}, {160, 185, 17}, {206, 214, 13}};
        for (int[] patrol : bearPatrols) {
            enemies.add(new BearEnemy(
                    getMapTile(patrol[0], patrol[2]).getLocation(),
                    getMapTile(patrol[1], patrol[2]).getLocation(),
                    Direction.RIGHT));
        }

        return enemies;
    }

    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> tiles = new ArrayList<>();
        // platforms across Lake Whitney {left edge, right edge, row}
        int[][] crossings = {{161, 166, 13}, {169, 174, 13}, {178, 185, 13}};
        Direction direction = Direction.RIGHT;
        for (int[] crossing : crossings) {
            tiles.add(new HorizontalMovingPlatform(
                    ImageLoader.load("GreenPlatform.png"),
                    getMapTile(crossing[0], crossing[2]).getLocation(),
                    getMapTile(crossing[1], crossing[2]).getLocation(),
                    TileType.JUMP_THROUGH_PLATFORM,
                    3,
                    new Rectangle(0, 6, 16, 4),
                    direction));
            // switch directions so they don't all move together
            direction = direction == Direction.RIGHT ? Direction.LEFT : Direction.RIGHT;
        }

        // end of the level
        tiles.add(new EndLevelBox(getMapTile(230, 13).getLocation()));
        return tiles;
    }
}
