package mindustry.type.devent;

import arc.graphics.Color;
import arc.math.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.type.*;
import mindustry.entities.bullet.*;

public class MeteorRain extends DynamicEvent {

    public MeteorRain(String name){
        super(name);
    }

    @Override
    public void onStart(){
        Call.sendMessage("[orange]Meteor Rain has started! Look at the sky.");
    }

    @Override
    public void update(float progress){
        float intensity = Mathf.sin(progress * Mathf.PI);
        if(Mathf.chanceDelta(intensity * 0.25f)){
            float targetX = Mathf.random(Vars.world.unitWidth());
            float targetY = Mathf.random(Vars.world.unitHeight());
            Fx.sparkExplosion.at(targetX, targetY + 150f);
            Time.run(70f, () -> {
                Bullets.fireball.create(null, targetX, targetY, 0f, 120f, 1f);
                Fx.impactReactorExplosion.at(targetX, targetY);
                Fx.blastExplosion.at(targetX, targetY);
                Sounds.explosion.at(targetX, targetY);
            });
        }
    }

    @Override
    public void draw(float progress){
        if(Vars.renderer != null){
            Drawf.light(Vars.player.x, Vars.player.y, 1000f, Color.orange, 0.4f * Mathf.sin(progress * Mathf.PI));
        }
    }

    @Override
    public void onEnd(){
        Call.sendMessage("[green]Meteor Rain has ended.");
    }
}