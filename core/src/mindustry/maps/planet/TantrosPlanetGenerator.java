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
import arc.math.Mathf;
import arc.struct.*;
import arc.math.*;
import arc.math.geom.*;
import mindustry.world.*;
import mindustry.content.*;
import mindustry.type.*;
import mindustry.ai.*;

import static mindustry.Vars.*;

public class TantrosPlanetGenerator extends PlanetGenerator{
    //Color c1 = Color.valueOf("5057a6"), c2 = Color.valueOf("272766");
    Color cDeep = Color.valueOf("0f0f2d"),
            cMid = Color.valueOf("272766"),
            cShall = Color.valueOf("5057a6"),
            cCoral = Color.valueOf("a65050");

    Block[][] arr = {
    {Blocks.redmat, Blocks.redmat, Blocks.darksand, Blocks.bluemat, Blocks.bluemat}
    };

    {
        baseSeed = 1;
    }

    @Override
    public float getHeight(Vec3 position){
        float noise = Simplex.noise3d(seed, 6, 0.5f, 1.3f, position.x, position.y, position.z);
        return Math.max(0, noise - 0.1f) * 0.06f;
    }

    @Override
    public void getColor(Vec3 position, Color out){
        float depth = Simplex.noise3d(seed, 7, 0.45f, 1.1f, position.x, position.y, position.z);
        float bio = Simplex.noise3d(seed + 1, 5, 0.55f, 2.2f, position.x, position.y, position.z);
        if(depth > 0.65f){
            out.set(cMid).lerp(cDeep, (depth - 0.65f) * 2.8f);
        }else if(depth > 0.35f){
            out.set(cShall).lerp(cMid, (depth - 0.35f) * 3.3f);
        }else{
            out.set(cShall);
            if(bio > 0.5f){
                out.lerp(cCoral, (bio - 0.5f) * 1.2f);
            }
        }
        float light = 0.9f + Mathf.clamp(1f - depth) * 0.15f;
        out.mul(light);
    }

    @Override
    public float getSizeScl(){
        return 2000;
    }

    @Override
    public void addWeather(Sector sector, Rules rules){
        //no weather... yet
    }

    @Override
    public void genTile(Vec3 position, TileGen tile){
        tile.floor = getBlock(position);

        if(tile.floor == Blocks.redmat && rand.chance(0.1)){
            tile.block = Blocks.redweed;
        }

        if(tile.floor == Blocks.bluemat && rand.chance(0.03)){
            tile.block = Blocks.purbush;
        }

        if(tile.floor == Blocks.bluemat && rand.chance(0.002)){
            tile.block = Blocks.yellowCoral;
        }
    }

    @Override
    protected void generate() {
        cells(4);
        distort(10f, 12f);

        class Room {
            int x, y, radius;
            ObjectSet<Room> connected = new ObjectSet<>();

            Room(int x, int y, int radius) {
                this.x = Mathf.clamp(x, 0, width - 1);
                this.y = Mathf.clamp(y, 0, height - 1);
                this.radius = radius;
                connected.add(this);
            }

            void join(int x1, int y1, int x2, int y2) {
                float nscl = rand.random(100f, 140f) * 6f;
                int stroke = rand.random(3, 9);
                var path = pathfind(x1, y1, x2, y2, tile ->
                                (tile.solid() ? 50f : 0f) + noise(tile.x, tile.y, 2, 0.4f, 1f / nscl) * 500,
                        Astar.manhattan
                );

                brush(path, stroke);
            }

            void connect(Room to) {
                if(!connected.add(to) || to == this) return;

                Vec2 midpoint = Tmp.v1.set(to.x, to.y).add(x, y).scl(0.5f);
                midpoint.add(Tmp.v2.setToRandomDirection(rand).scl(Tmp.v1.dst(x, y) * 0.5f));
                int mx = Mathf.clamp((int)midpoint.x, 0, width - 1);
                int my = Mathf.clamp((int)midpoint.y, 0, height - 1);

                join(x, y, mx, my);
                join(mx, my, to.x, to.y);
            }
        }

        float biomeScl = 120f;

        pass((x, y) -> {
            floor = Blocks.bluemat;
            block = Blocks.shaleWall;

            float v = noise(x, y, 3, 0.5f, biomeScl);
            if(v > 0.55f){
                floor = Blocks.redmat;
                if(block == Blocks.shaleWall) block = Blocks.redStoneWall;
            }
        });
        Seq<Room> roomseq = new Seq<>();
        int roomCount = rand.random(10, 15);
        float circleRad = width / 2.5f;

        for(int i = 0; i < roomCount; i++) {
            Tmp.v1.trns(rand.random(360f), rand.random(circleRad));
            roomseq.add(new Room((int)(width/2 + Tmp.v1.x), (int)(height/2 + Tmp.v1.y), rand.random(10, 25)));
        }

        Room spawn = new Room(width / 2, height / 2, 20);
        roomseq.add(spawn);

        for(Room room : roomseq) {
            erase(room.x, room.y, room.radius);
        }

        for(int i = 0; i < roomseq.size; i++) {
            roomseq.get(i).connect(roomseq.random(rand));
        }

        for(Room room : roomseq) {
            spawn.connect(room);
        }

        distort(10f, 6f);
        median(2);


        Schematics.placeLaunchLoadout(spawn.x, spawn.y);


    }

    float rawHeight(Vec3 position){
        return Simplex.noise3d(seed, 8, 0.7f, 1f, position.x, position.y, position.z);
    }

    Block getBlock(Vec3 position){
        float height = rawHeight(position);
        Tmp.v31.set(position);
        position = Tmp.v33.set(position).scl(2f);
        float temp = Simplex.noise3d(seed, 8, 0.6, 1f/2f, position.x, position.y + 99f, position.z);
        height *= 1.2f;
        height = Mathf.clamp(height);

        //float tar = (float)noise.octaveNoise3D(4, 0.55f, 1f/2f, position.x, position.y + 999f, position.z) * 0.3f + Tmp.v31.dst(0, 0, 1f) * 0.2f;

        return arr[Mathf.clamp((int)(temp * arr.length), 0, arr[0].length - 1)][Mathf.clamp((int)(height * arr[0].length), 0, arr[0].length - 1)];
    }
}
