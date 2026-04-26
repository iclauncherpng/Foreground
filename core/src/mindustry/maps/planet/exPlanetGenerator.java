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

public class exPlanetGenerator extends PlanetGenerator {
    float scl = 5f;
    float heightScl = 1.15f;

    Block[][] arr = {
            {Blocks.basalt, Blocks.basalt, Blocks.stone, Blocks.darksand, Blocks.darksand, Blocks.darksand, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.darksand, Blocks.stone, Blocks.stone, Blocks.stone},
            {Blocks.basalt, Blocks.stone, Blocks.darksand, Blocks.darksand, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.darksand, Blocks.stone, Blocks.stone, Blocks.stone},
            {Blocks.basalt, Blocks.stone, Blocks.darksand, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.darksand, Blocks.stone, Blocks.stone, Blocks.stone},
            {Blocks.stone, Blocks.darksand, Blocks.darksand, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.darksand, Blocks.stone, Blocks.stone, Blocks.stone, Blocks.stone}
    };

    float rawHeight(Vec3 position) {
        float noise = Simplex.noise3d(seed, 8, 0.5f, 1f/3f, position.x * scl, position.y * scl, position.z * scl);
        return Mathf.pow(noise * heightScl, 2.8f);
    }

    @Override
    public float getHeight(Vec3 position) {
        return Math.max(rawHeight(position), 0.04f);
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

        if (Ridged.noise3d(seed + 1, position.x, position.y, position.z, 2, 22) > 0.31) {
            tile.block = Blocks.air;
        }
    }

    Block getBlock(Vec3 position) {
        float height = rawHeight(position);
        float px = position.x * scl, py = position.y * scl, pz = position.z * scl;

        float temp = Mathf.clamp(Math.abs(py * 1.8f) / scl);
        float tnoise = Simplex.noise3d(seed, 7, 0.56, 1f/3f, px, py + 999f, pz);
        temp = Mathf.lerp(temp, tnoise, 0.4f);

        height = Mathf.clamp(height * 1.2f);
        float tarNoise = Simplex.noise3d(seed + 2, 4, 0.6f, 0.7f, px, py, pz);
        if (tarNoise > 0.72f && height < 0.15f) {
            return Blocks.tar;
        }

        return arr[Mathf.clamp((int)(temp * arr.length), 0, arr.length - 1)][Mathf.clamp((int)(height * arr[0].length), 0, arr[0].length - 1)];
    }

    @Override
    protected void generate() {
        cells(4);
        distort(10f, 12f);

        float radius = width / 2.3f;
        int rooms = rand.random(4, 10);

        for (int i = 0; i < rooms; i++) {
            Tmp.v1.trns(rand.random(360f), rand.random(radius));
            erase((int)(width/2f + Tmp.v1.x), (int)(height/2f + Tmp.v1.y), rand.random(10, 30));
        }

        distort(10f, 6f);
        median(2);

        Schematics.placeLaunchLoadout(width/2, height/2);
    }

    @Override
    public boolean isEmissive(){
        return true;
    }

    @Override
    public void getEmissiveColor(Vec3 position, Color out){
        out.set(Color.clear);
        float cityNoise = Simplex.noise3d(seed + 500, 8, 0.6f, 1.4f, position.x, position.y, position.z);
        if(cityNoise > 0.82f){
            float h = rawHeight(position);
            if(h > 0.2f && h < 0.45f){
                float detail = Simplex.noise3d(seed + 123, 10, 0.9f, 4.5f, position.x, position.y, position.z);

                if(detail > 0.5f){
                    out.set(Color.valueOf("feb35a"));
                    float blink = 0.7f + Mathf.absin(Time.time + position.x * 100f, 10f, 0.3f);
                    out.mul(blink * (cityNoise * 1.5f));
                }
            }
        }
    }
}