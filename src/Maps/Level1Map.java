package Maps;

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
 * Level 1: Sleeping Giant descent. The terrain and climbable trees live in
 * level1_map.txt; moving platforms, wildlife, pickups and the exit live here.
 * All coordinates below are zero-based tile coordinates (48 pixels per tile).
 */
public class Level1Map extends Map {
    public Level1Map() {
        super("level1_map.txt", new CommonTileset());
        playerStartPosition = getMapTile(3, 8).getLocation();
    }

    @Override
    public ArrayList<Enemy> loadEnemies() {
        ArrayList<Enemy> enemies = new ArrayList<>();
        // Keep the first climb and platform crossing free of enemies.
        int[][] squirrelTiles = {{68, 18}, {167, 33}, {229, 43}, {370, 61}, {455, 71}};
        for (int[] tile : squirrelTiles) {
            enemies.add(new SquirrelEnemy(getMapTile(tile[0], tile[1]).getLocation(), Direction.LEFT));
        }
        return enemies;
    }

    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> tiles = new ArrayList<>();
        // {left edge, right edge, row}. The existing platform treats the end
        // coordinate as its right-edge limit, not its final left position.
        int[][] crossings = {{51, 60, 15}, {197, 207, 39}, {275, 285, 49}, {417, 427, 68}};
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

        // Fixed rewards on the tree route, followed by wildlife encounters.
        int[][] pickupTiles = {{45, 9}, {148, 24}, {244, 39}, {431, 62}};
        for (int[] tile : pickupTiles) {
            tiles.add(new HockeyStickPowerup(getMapTile(tile[0], tile[1]).getLocation()));
        }

        // Flat trailhead, well before the right boundary, with room to land.
        tiles.add(new EndLevelBox(getMapTile(494, 74).getLocation()));
        return tiles;
    }
}
