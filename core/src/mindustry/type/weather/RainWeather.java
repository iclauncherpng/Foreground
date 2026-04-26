package mindustry.type.weather;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.Vars;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.type.*;

public class RainWeather extends Weather {
    public float yspeed = 6.5f, xspeed = 1.2f, padding = 16f, density = 1100f, stroke = 0.75f, sizeMin = 8f, sizeMax = 40f, splashTimeScale = 22f;
    public Liquid liquid = Liquids.water;
    public TextureRegion[] splashes = new TextureRegion[12];
    public Color color = Color.valueOf("7a95eaff");

    public RainWeather(String name){
        super(name);
    }

    @Override
    public void load(){
        super.load();
        for(int i = 0; i < splashes.length; i++){
            splashes[i] = Core.atlas.find("splash-" + i);
        }
    }

    @Override
    public void update(WeatherState state){
        super.update(state);

        if(state.intensity > 0.2f){
            Groups.fire.each(fire -> {
                fire.lifetime -= 1.5f * state.intensity * Time.delta;
            });
        }
    }

    @Override
    public void drawOver(WeatherState state){
        Draw.color(color);
        drawRain(sizeMin, sizeMax, xspeed, yspeed, density, state.intensity, stroke, color);
        Draw.reset();
    }

    @Override
    public void drawUnder(WeatherState state){
        drawSplashes(splashes, sizeMax, density * 0.8f, state.intensity, state.opacity, splashTimeScale, stroke, color, liquid);
    }
}