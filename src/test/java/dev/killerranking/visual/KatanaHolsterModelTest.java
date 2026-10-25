package dev.killerranking.visual;

import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class KatanaHolsterModelTest {
    @ParameterizedTest @ValueSource(strings={"pawn6_katana_saya","pawn6_katana_sheathed"})
    void holsterElementsRemainInsideTheActualMinecraftModelParserLimits(String name) throws Exception {
        var data=JsonParser.parseString(Files.readString(Path.of("src/main/resources/assets/killerranking/models/item/"+name+".json"))).getAsJsonObject();
        assertFalse(data.getAsJsonArray("elements").isEmpty());
        for(var element:data.getAsJsonArray("elements")) for(String corner:new String[]{"from","to"})
            for(var coordinate:element.getAsJsonObject().getAsJsonArray(corner))
                assertTrue(coordinate.getAsFloat()>=-16&&coordinate.getAsFloat()<=32,"Minecraft rejects the entire model outside -16..32: "+coordinate);
    }
}
