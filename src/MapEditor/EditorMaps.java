package MapEditor;

import Level.Map;
import Maps.DebugMap;
import Maps.Level1Map;
import Maps.TestMap;
import Maps.TitleScreenMap;

import java.util.ArrayList;

public class EditorMaps {
    public static ArrayList<String> getMapNames() {
        return new ArrayList<String>() {{
            add("Level1Map");
            add("TestMap");
            add("TitleScreen");
            add("DebugMap");
        }};
    }

    public static Map getMapByName(String mapName) {
        return switch (mapName) {
            case "Level1Map" -> new Level1Map();
            case "TestMap" -> new TestMap();
            case "TitleScreen" -> new TitleScreenMap();
            case "DebugMap" -> new DebugMap();
            default -> throw new RuntimeException("Unrecognized map name");
        };
    }
}
