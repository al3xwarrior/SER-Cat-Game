package Tilesets;

import Builders.FrameBuilder;
import Builders.MapTileBuilder;
import Engine.ImageLoader;
import GameObject.Frame;
import Level.TileType;
import Level.Tileset;
import Utils.SlopeTileLayoutUtils;

import java.util.ArrayList;

// Tileset for Level 2 (Whitney Ave, Hamden), defined in the WhitneyAveTileset.png file
// Tiles are laid out in the image 8 per row, in the same order they are added here, so a tile's index matches its spot in the image
public class WhitneyAveTileset extends Tileset {
    private static final int COLUMNS = 8;

    public WhitneyAveTileset() {
        super(ImageLoader.load("WhitneyAveTileset.png"), 16, 16, 3);
    }

    @Override
    public ArrayList<MapTileBuilder> defineTiles() {
        ArrayList<MapTileBuilder> mapTiles = new ArrayList<>();

        // ground
        mapTiles.add(tile(mapTiles, TileType.NOT_PASSABLE)); // 0 grass
        mapTiles.add(tile(mapTiles, TileType.NOT_PASSABLE)); // 1 dirt
        mapTiles.add(tile(mapTiles, TileType.NOT_PASSABLE)); // 2 sidewalk
        mapTiles.add(tile(mapTiles, TileType.NOT_PASSABLE)); // 3 lake bed
        mapTiles.add(tile(mapTiles, TileType.NOT_PASSABLE)); // 4 stone wall

        // slopes
        mapTiles.add(tile(mapTiles, TileType.SLOPE) // 5 grass slope going down to the right
                .withTileLayout(SlopeTileLayoutUtils.createRight45SlopeLayout(spriteWidth, (int) tileScale)));
        mapTiles.add(tile(mapTiles, TileType.SLOPE) // 6 grass slope going up to the right
                .withTileLayout(SlopeTileLayoutUtils.createLeft45SlopeLayout(spriteWidth, (int) tileScale)));
        mapTiles.add(tile(mapTiles, TileType.SLOPE) // 7 underwater mud slope going up to the right
                .withTileLayout(SlopeTileLayoutUtils.createLeft45SlopeLayout(spriteWidth, (int) tileScale)));

        // Lake Whitney
        mapTiles.add(tile(mapTiles, TileType.PASSABLE)); // 8 water top
        mapTiles.add(tile(mapTiles, TileType.WATER)); // 9 water

        // buildings: red brick (Dickerman's), dark brick (Haven Beer), tan brick (Eli's)
        mapTiles.add(tile(mapTiles, TileType.NOT_PASSABLE)); // 10 red brick wall
        mapTiles.add(tile(mapTiles, TileType.NOT_PASSABLE)); // 11 red brick window
        mapTiles.add(tile(mapTiles, TileType.NOT_PASSABLE)); // 12 dark brick wall
        mapTiles.add(tile(mapTiles, TileType.NOT_PASSABLE)); // 13 dark brick window
        mapTiles.add(tile(mapTiles, TileType.NOT_PASSABLE)); // 14 tan brick wall
        mapTiles.add(tile(mapTiles, TileType.NOT_PASSABLE)); // 15 tan brick window
        mapTiles.add(tile(mapTiles, TileType.NOT_PASSABLE)); // 16 roof cornice
        mapTiles.add(tile(mapTiles, TileType.NOT_PASSABLE)); // 17 storefront glass
        mapTiles.add(tile(mapTiles, TileType.NOT_PASSABLE)); // 18 door
        mapTiles.add(tile(mapTiles, TileType.NOT_PASSABLE)); // 19 chimney
        mapTiles.add(tile(mapTiles, TileType.NOT_PASSABLE)); // 20 rooftop AC unit
        mapTiles.add(tile(mapTiles, TileType.NOT_PASSABLE)); // 21 beer keg
        mapTiles.add(tile(mapTiles, TileType.NOT_PASSABLE)); // 22 wooden crate

        // jump through platforms
        mapTiles.add(platform(mapTiles, 2, 4)); // 23 green awning
        mapTiles.add(platform(mapTiles, 2, 4)); // 24 navy awning
        mapTiles.add(platform(mapTiles, 2, 4)); // 25 red awning
        mapTiles.add(platform(mapTiles, 5, 3)); // 26 fire escape
        mapTiles.add(platform(mapTiles, 6, 4)); // 27 dock

        // scenery
        mapTiles.add(tile(mapTiles, TileType.PASSABLE)); // 28 lamp post pole
        mapTiles.add(tile(mapTiles, TileType.PASSABLE)); // 29 lamp post top
        mapTiles.add(tile(mapTiles, TileType.PASSABLE)); // 30 tree trunk
        mapTiles.add(tile(mapTiles, TileType.PASSABLE)); // 31 tree leaves
        mapTiles.add(tile(mapTiles, TileType.PASSABLE)); // 32 fence
        mapTiles.add(tile(mapTiles, TileType.PASSABLE)); // 33 bush
        mapTiles.add(tile(mapTiles, TileType.PASSABLE)); // 34 sign pole
        mapTiles.add(tile(mapTiles, TileType.PASSABLE)); // 35 patio post
        mapTiles.add(tile(mapTiles, TileType.PASSABLE)); // 36 bench

        // signs, each one is several tiles wide and read left to right
        for (int i = 0; i < 5; i++) {
            mapTiles.add(tile(mapTiles, TileType.PASSABLE)); // 37-41 WHITNEY AVE street sign
        }
        for (int i = 0; i < 5; i++) {
            mapTiles.add(tile(mapTiles, TileType.NOT_PASSABLE)); // 42-46 HAVEN BEER
        }
        for (int i = 0; i < 3; i++) {
            mapTiles.add(tile(mapTiles, TileType.NOT_PASSABLE)); // 47-49 ELI'S
        }
        for (int i = 0; i < 5; i++) {
            mapTiles.add(tile(mapTiles, TileType.NOT_PASSABLE)); // 50-54 DICKERMAN'S
        }

        mapTiles.add(tile(mapTiles, TileType.PASSABLE)); // 55 sky

        return mapTiles;
    }

    // builds the tile that sits at the next free spot in the image
    private MapTileBuilder tile(ArrayList<MapTileBuilder> mapTiles, TileType tileType) {
        int index = mapTiles.size();
        Frame frame = new FrameBuilder(getSubImage(index / COLUMNS, index % COLUMNS))
                .withScale(tileScale)
                .build();
        return new MapTileBuilder(frame).withTileType(tileType);
    }

    // a jump through platform whose standable part starts top pixels down and is height pixels tall
    private MapTileBuilder platform(ArrayList<MapTileBuilder> mapTiles, int top, int height) {
        int index = mapTiles.size();
        Frame frame = new FrameBuilder(getSubImage(index / COLUMNS, index % COLUMNS))
                .withScale(tileScale)
                .withBounds(0, top, 16, height)
                .build();
        return new MapTileBuilder(frame).withTileType(TileType.JUMP_THROUGH_PLATFORM);
    }
}
