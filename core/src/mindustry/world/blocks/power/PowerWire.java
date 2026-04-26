package mindustry.world.blocks.power;

import arc.func.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.annotations.Annotations.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.input.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.meta.*;
import arc.Core;
import mindustry.ui.Bar;
import mindustry.core.UI;

import static mindustry.Vars.*;

public class PowerWire extends Block implements Autotiler {
    public @Load(value = "@-#1-#2", lengths = {7, 4}) TextureRegion[][] regions;

    public PowerWire(String name){
        super(name);
        update = true;
        destructible = true;
        hasPower = true;
        outputsPower = true;
        consumesPower = false;
        rotate = true;
        quickRotate = true;
        conveyorPlacement = true;
    }

    @Override
    public void load(){
        super.load();
        region = regions[0][0];
    }

    @Override
    public void setBars(){
        super.setBars();
        addBar("power", entity -> new Bar(() ->
                Core.bundle.format("bar.powerbalance", ((entity.power.graph.getPowerBalance() >= 0 ? "+" : "") + UI.formatAmount((long)(entity.power.graph.getPowerBalance() * 60)))),
                () -> Pal.powerBar,
                () -> Mathf.clamp(entity.power.graph.getLastPowerProduced() / entity.power.graph.getLastPowerNeeded())
        ));
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        int[] bits = getTiling(plan, list);
        if(bits == null) return;

        TextureRegion region = regions[bits[0]][0];
        Draw.rect(region, plan.drawx(), plan.drawy(), region.width * bits[1] * region.scl(), region.height * bits[2] * region.scl(), plan.rotation * 90);
    }

    @Override
    public boolean blends(Tile tile, int rotation, int otherx, int othery, int otherrot, Block otherblock){
        return otherblock.hasPower;
    }

    public class PowerWireBuild extends Building {
        public int blendbits, blending;
        public int blendsclx = 1, blendscly = 1;

        @Override
        public void onProximityUpdate(){
            super.onProximityUpdate();
            int[] bits = buildBlending(tile, rotation, null, true);
            blendbits = bits[0];
            blendsclx = bits[1];
            blendscly = bits[2];
            blending = bits[4];
        }

        @Override
        public void draw(){
            for(int i = 0; i < 4; i++){
                if((blending & (1 << i)) != 0){
                    int dir = rotation - i;
                    float rot = i == 0 ? rotation * 90 : (dir) * 90;
                    Draw.rect(sliced(regions[0][0], i != 0 ? SliceMode.bottom : SliceMode.top), x + Geometry.d4x(dir) * tilesize * 0.75f, y + Geometry.d4y(dir) * tilesize * 0.75f, rot);
                }
            }
            Draw.rect(regions[blendbits][0], x, y, tilesize * blendsclx, tilesize * blendscly, rotation * 90);
        }
    }
}