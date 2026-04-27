package mindustry.maps.planet;

import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.noise.*;
import mindustry.content.*;
import mindustry.graphics.*;
import mindustry.maps.generators.*;
import mindustry.type.*;
import mindustry.world.*;

public class TantrosMoonGenerator extends PlanetGenerator {
    Color 
        cLow = Color.valueOf("3a4b62"),
        cMid = Color.valueOf("6d85a4"),
        cHigh = Color.valueOf("ffffff"),
        cSpec = Color.valueOf("84f4ff");

    {
        baseSeed = 2; 
    }

    @Override
    public float getHeight(Vec3 position) {
        float noise = Ridged.noise3d(seed, position.x, position.y, position.z, 2, 1.5f);
        return Math.max(0, noise);
    }

    @Override
    public void getColor(Vec3 position, Color out) {
        float h = Simplex.noise3d(seed, 6, 0.5f, 1.2f, position.x, position.y, position.z);
        float crystal = Simplex.noise3d(seed + 1, 4, 0.6f, 3.0f, position.x, position.y, position.z);

        if(h > 0.7f) {
            out.set(cHigh);
        } else if(h > 0.3f) {
            out.set(cMid).lerp(cHigh, (h - 0.3f) * 2.5f);
        } else {
            out.set(cLow).lerp(cMid, h * 3f);
        }
        if(crystal > 0.75f) {
            out.lerp(cSpec, (crystal - 0.75f) * 4f);
        }
    }

    @Override
    public void genTile(Vec3 position, TileGen tile) {
        float h = Simplex.noise3d(seed, 6, 0.5f, 1.0f, position.x, position.y, position.z);
        
        if(h > 0.6f) {
            tile.floor = Blocks.ice;
            tile.block = Blocks.iceWall;
        } else if(h > 0.3f) {
            tile.floor = Blocks.snow;
        } else {
            tile.floor = Blocks.carbonStone;
        }

        if(tile.floor == Blocks.ice && rand.chance(0.02)) {
            tile.block = Blocks.crystalCluster;
        }
    }

    @Override
    protected void generate() {
        pass((x, y) -> {
            floor = Blocks.snow;
            if(Mathf.chance(0.05)) block = Blocks.iceWall;
        });

        cells(4);
        distort(10f, 12f);
        median(2);
    }

    @Override
    public boolean isEmissive() {
        return true;
    }
}