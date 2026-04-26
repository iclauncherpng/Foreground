package mindustry.maps.planet;

import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.noise.*;
import mindustry.content.*;
import mindustry.game.*;
import mindustry.maps.generators.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;

import static mindustry.Vars.*;

public class exMoonGenerator extends PlanetGenerator {
    float scl = 2.5f;
    float water = 0.01f;

    Block[] rocks = {Blocks.basalt, Blocks.stone, Blocks.stone, Blocks.basalt, Blocks.hotrock, Blocks.basalt};

    @Override
    public float getHeight(Vec3 position) {
        float noise = Simplex.noise3d(seed, 6, 0.5f, 1f/3f, position.x * scl, position.y * scl, position.z * scl);
        
        float height = Mathf.pow(noise, 2.5f);

        float craterNoise = Simplex.noise3d(seed + 1, 2, 0.4f, 1.2f, position.x * 2f, position.y * 2f, position.z * 2f);
        if (craterNoise > 0.75f) {
            height *= 0.15f;
        }

        return Math.max(height, 0.05f);
    }

    @Override
    public void getColor(Vec3 position, Color out) {
        Block block = getBlock(position);
        out.set(block.mapColor).a(1f - block.albedo);
    }

    @Override
    public void genTile(Vec3 position, TileGen tile) {
        tile.floor = getBlock(position);
        tile.block = tile.floor.asFloor().wall;

        if (Ridged.noise3d(seed + 2, position.x, position.y, position.z, 2, 25) > 0.45f) {
            tile.block = Blocks.air;
        }
    }

    Block getBlock(Vec3 position) {
        float height = getHeight(position);
        float craterNoise = Simplex.noise3d(seed + 1, 2, 0.4f, 1.2f, position.x * 2f, position.y * 2f, position.z * 2f);

        if (craterNoise > 0.75f && craterNoise < 0.78f) {
            return Blocks.basalt;
        }

        if (height < 0.1f) return Blocks.stone;
        if (height < 0.25f) return Blocks.basalt;
        
        float v = Simplex.noise3d(seed + 3, 5, 0.5f, 1.5f, position.x * 4f, position.y * 4f, position.z * 4f);
        return rocks[Mathf.clamp((int)(v * rocks.length), 0, rocks.length - 1)];
    }

    @Override
    protected void generate() {
        cells(3);
        distort(8f, 5f);

        float radius = width / 2.5f;
        int rooms = rand.random(2, 6);

        for (int i = 0; i < rooms; i++) {
            Tmp.v1.trns(rand.random(360f), rand.random(radius));
            erase((int)(width/2f + Tmp.v1.x), (int)(height/2f + Tmp.v1.y), rand.random(8, 20));
        }

        median(2);
        Schematics.placeLaunchLoadout(width/2, height/2);
    }

    @Override
    public boolean isEmissive() {
        return false;
    }
}