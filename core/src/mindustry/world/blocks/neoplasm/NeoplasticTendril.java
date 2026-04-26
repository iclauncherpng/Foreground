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

public class NeoplasticTendril extends Block {
    public Color heartColor = Color.valueOf("e05438");

    public NeoplasticTendril(String name){
        super(name);
        update = true;
        solid = true;
        size = 1;
        sync = true;
        buildVisibility = BuildVisibility.sandboxOnly;
    }

    public class NeoplasticTendrilBuild extends Building {
        public float pulse = 0f;

        @Override
        public void updateTile(){
            pulse += edelta() * 0.05f;
        }

        @Override
        public void draw(){
            Draw.color(Color.black);
            Fill.square(x, y, size * Vars.tilesize / 2f);

            float s = (size * Vars.tilesize / 2f);
            float p = Mathf.absin(pulse, 1f, 0.15f);

            Draw.color(heartColor);
            float layerS = s * (0.8f + p * 0.2f);
            Fill.square(x, y, layerS);
            Lines.stroke(2f * (0.8f + p));
            for(int i = 0; i < 4; i++){
                Lines.lineAngle(x, y, i * 90f, s * (1.1f + p * 0.2f));
            }
            Draw.blend(Blending.additive);
            Draw.color(heartColor, 0.3f);
            Fill.square(x, y, s * 1.2f * (0.8f + p));
            Draw.blend();

            Draw.reset();
        }
    }
}