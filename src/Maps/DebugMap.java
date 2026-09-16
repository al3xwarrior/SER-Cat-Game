package Maps;

import Powerups.PlaceholderPowerup;
import java.util.Random;
import Engine.ImageLoader;
import EnhancedMapTiles.EndLevelBox;
import EnhancedMapTiles.HorizontalMovingPlatform;
import GameObject.Rectangle;
import Level.*;
import Tilesets.CommonTileset;
import Utils.Direction;

import java.util.ArrayList;

public class DebugMap extends Map {
    public DebugMap() {
        super("debug_map.txt", new CommonTileset());
        this.playerStartPosition = getMapTile(2, 11).getLocation();
    }

    @Override
    public ArrayList<Enemy> loadEnemies() {
        return null;
    }

    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();

        HorizontalMovingPlatform hmp = new HorizontalMovingPlatform(
                ImageLoader.load("GreenPlatform.png"),
                getMapTile(24, 6).getLocation(),
                getMapTile(27, 6).getLocation(),
                TileType.JUMP_THROUGH_PLATFORM,
                3,
                new Rectangle(0, 6,16,4),
                Direction.RIGHT
        );
        enhancedMapTiles.add(hmp);

        EndLevelBox endLevelBox = new EndLevelBox(getMapTile(32, 7).getLocation());
        enhancedMapTiles.add(endLevelBox);

        int numberOfPowerups = 5;
        Random random = new Random();
        for (int i = 0; i < numberOfPowerups; i++) {
            boolean firstFlatZone = random.nextBoolean();
            int minX = firstFlatZone ? 13 : 30;
            int maxX = firstFlatZone ? 24 : 49;
            int randomX = minX + random.nextInt(maxX - minX + 1);

            int groundY = findGroundLevel(randomX);
            PlaceholderPowerup powerup = new PlaceholderPowerup(getMapTile(randomX, groundY - 1).getLocation());
            enhancedMapTiles.add(powerup);
        }

        return enhancedMapTiles;
    }

    @Override
    public ArrayList<NPC> loadNPCs() {
        return null;
    }

    private int findGroundLevel(int x) {
        for (int y = 0; y < height; y++) {
            TileType tileType = getMapTile(x, y).getTileType();
            if (tileType == TileType.NOT_PASSABLE || tileType == TileType.SLOPE) {
                return y;
            }
        }
        return height - 1;
    }
}
