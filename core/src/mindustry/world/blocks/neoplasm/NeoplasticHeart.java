package mindustry.world.blocks.neoplasm;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.Vars;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import mindustry.content.*;

public class NeoplasticHeart extends Block {
    public Color heartColor = Color.valueOf("e05438");
    public float spreadChance = 0.05f;
    public Block tendrilBlock;

    public NeoplasticHeart(String name){
        super(name);
        update = true;
        solid = true;
        size = 3;
        sync = true;
        buildVisibility = BuildVisibility.sandboxOnly;
    }

    public class NeoplasticHeartBuild extends Building {
        public float pulse = 0f;

        @Override
        public void updateTile(){
            pulse += edelta() * 0.05f;

            if(Mathf.chanceDelta(spreadChance)){
                growTowardsVent();
            }
        }

        public void growTowardsVent(){
            Tile targetVent = null;
            int radius = 25;

            outer:
            for(int dx = -radius; dx <= radius; dx++){
                for(int dy = -radius; dy <= radius; dy++){
                    Tile other = Vars.world.tile(tileX() + dx, tileY() + dy);
                    if(other != null && other.block() != null && other.block().name != null){
                        if(other.block().name.toLowerCase().contains("vent")){
                            targetVent = other;
                            break outer;
                        }
                    }
                }
            }

            int tx = tileX();
            int ty = tileY();

            if(targetVent != null){
                int dx = (int)Math.signum(targetVent.x - tileX());
                int dy = (int)Math.signum(targetVent.y - tileY());

                if(Mathf.chance(0.5)){
                    tx += (dx != 0 ? dx : Mathf.random(-1, 1));
                    ty += Mathf.random(-1, 1);
                } else {
                    tx += Mathf.random(-1, 1);
                    ty += (dy != 0 ? dy : Mathf.random(-1, 1));
                }
            } else {
                tx += Mathf.random(-2, 2);
                ty += Mathf.random(-2, 2);
            }

            Tile target = Vars.world.tile(tx, ty);
            if(target != null && target.build == null && !target.floor().isLiquid){
                target.setNet(tendrilBlock != null ? tendrilBlock : block, team, 0);
                Fx.neoplasmHeal.at(target.worldx(), target.worldy(), 0f, heartColor);
            }
        }

        @Override
        public void draw(){
            Draw.color(Color.black);
            Fill.square(x, y, size * Vars.tilesize / 2f);

            float s = (size * Vars.tilesize / 2f);
            float p = Mathf.absin(pulse, 1f, 0.15f);

            Draw.color(heartColor);
            for(int i = 0; i < 3; i++){
                float layerS = s * (1f - i * 0.25f) * (0.85f + p * 0.15f);
                Fill.square(x, y, layerS);
            }
            Lines.stroke(3f * (0.8f + p));
            for(int i = 0; i < 4; i++){
                Lines.lineAngle(x, y, i * 90f, s * (1.1f + p * 0.2f));
            }

            Draw.blend(Blending.additive);
            Draw.color(heartColor, 0.3f);
            Fill.square(x, y, s * 1.3f * (0.8f + p));
            Draw.blend();

            Draw.reset();
        }
    }
}