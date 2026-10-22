package MapEditor;

import Level.Map;
import Maps.DebugMap;
import Maps.Level1Map;
import Maps.Level2Map;
import Maps.TitleScreenMap;

import java.util.ArrayList;

public class EditorMaps {
    public static ArrayList<String> getMaps() {
        return new ArrayList<>() {{
            add("Level1Map");
            add("Level2Map");
            add("TitleScreen");
            add("DebugMap");
        }};
    }

    public static Map getMapByName(String mapName) {
        return switch (mapName) {
            case "Level1Map" -> new Level1Map();
            case "Level2Map" -> new Level2Map();
            case "TitleScreen" -> new TitleScreenMap();
            case "DebugMap" -> new DebugMap();
            default -> throw new RuntimeException("Unrecognized map name");
        };
    }
}
