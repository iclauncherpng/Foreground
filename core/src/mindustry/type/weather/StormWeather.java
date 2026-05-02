package mindustry.type.weather;

import arc.*;
import arc.audio.*;
import arc.graphics.*;
import arc.graphics.Texture.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.Vars;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.game.Team;
import mindustry.gen.*;

public class StormWeather extends RainWeather {
    public boolean drawNoise = true;
    public float noiseScale = 1100f;
    public int noiseLayers = 3;
    public float noiseLayerSpeedM = 2f, noiseLayerAlphaM = 0.7f, noiseLayerSclM = 0.6f;
    public String noisePath = "clouds";
    public Color noiseColor = Color.valueOf("45454566");
    public @Nullable Texture noise;
    public float lightningChance = 0.02f;
    public float strikeDamage = 40f;
    public float explosionRadius = 16f;

    public StormWeather(String name){
        super(name);
    }

    @Override
    public void load(){
        super.load();
        if(drawNoise && Core.assets != null){
            Core.assets.load("sprites/" + noisePath + ".png", Texture.class);
        }
    }

    @Override
    public void update(WeatherState state){
        super.update(state);
        if(Mathf.chance(lightningChance * Time.delta)){
            strike(state);
        }
    }

    private void strike(WeatherState state){
        float tx = Mathf.random(Vars.world.unitWidth());
        float ty = Mathf.random(Vars.world.unitHeight());
        float skyHeight = 200f; 
        for(int i = 0; i < 2; i++){
            Fx.stormLightning.at(tx, ty + skyHeight, 270f, Color.white);
        }
        Lightning.create(Team.derelict, Color.white, strikeDamage * state.intensity, tx, ty, 90f, 20);
        Fx.stormLightning.at(tx, ty, Color.white);
        Damage.damage(Team.derelict, tx, ty, explosionRadius, strikeDamage * state.intensity);
        Fx.blastExplosion.at(tx, ty);
        Sounds.lightning.at(tx, ty, Mathf.random(0.9f, 1.1f));
        if(Vars.renderer != null) Vars.renderer.shake(4f, 4f);
    }

    @Override
    public void drawOver(WeatherState state){
        if(drawNoise){
            if(noise == null && Core.assets.isLoaded("sprites/" + noisePath + ".png", Texture.class)){
                noise = Core.assets.get("sprites/" + noisePath + ".png", Texture.class);
                noise.setWrap(TextureWrap.repeat);
                noise.setFilter(TextureFilter.linear);
            }
            if(noise != null){
                float sspeed = 1f, sscl = 1f, salpha = 1f, offset = 0f;
                float fogAlpha = state.opacity * 0.3f;
                for(int i = 0; i < noiseLayers; i++){
                    drawNoise(noise, noiseColor, noiseScale * sscl, fogAlpha * salpha * opacityMultiplier, sspeed, state.intensity, xspeed, yspeed, offset);
                    sspeed *= noiseLayerSpeedM;
                    salpha *= noiseLayerAlphaM;
                    sscl *= noiseLayerSclM;
                    offset += 0.29f;
                }
            }
        }
        drawRain(sizeMin, sizeMax, xspeed, yspeed, density, state.intensity, state.opacity, color);
    }
}