package mindustry.entities.abilities;

import arc.graphics.*;
import arc.math.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.gen.*;

public class SukunaCutAbility extends Ability {
    public float damage = 50f;
    public float reload = 20f;
    public float range = 220f;
    public int cuts = 115;
    public float worldCutChance = 0.05f;

    protected float timer = 0f;

    public SukunaCutAbility(float damage, float reload, float range){
        this.damage = damage;
        this.reload = reload;
        this.range = range;
    }

    public SukunaCutAbility(){}

    @Override
    public void update(Unit unit){
        timer += Time.delta;

        if(unit.isShooting && timer >= reload){
            
            boolean isWorldCut = Mathf.chance(worldCutChance);

            int currentCuts = isWorldCut ? cuts * 2 : cuts;
            float currentRange = isWorldCut ? range * 3f : range;
            float currentDamage = isWorldCut ? damage * 10f : damage;

            if(isWorldCut){
                Effect.shake(6f, 6f, unit); 
                if(!Vars.headless) Sounds.explosionNavanax.at(unit);
            }

            for(int i = 0; i < currentCuts; i++){
                
                float angle = Mathf.random(360f);

                Time.run(i * 1.5f, () -> {
                    
                    Fx.cleaveEffect.at(
                            unit.x + Angles.trnsx(angle, Mathf.random(currentRange)),
                            unit.y + Angles.trnsy(angle, Mathf.random(currentRange)),
                            angle
                    );

                    
                    if(isWorldCut && Mathf.chance(0.2)){
                        Fx.instBomb.at(unit.x + Angles.trnsx(angle, Mathf.random(currentRange)),
                                unit.y + Angles.trnsy(angle, Mathf.random(currentRange)));
                    }

                    
                    for(float j = 0; j < currentRange; j += 25f){
                        Damage.damage(unit.team,
                                unit.x + Angles.trnsx(angle, j),
                                unit.y + Angles.trnsy(angle, j),
                                isWorldCut ? 40f : 15f, 
                                currentDamage
                        );
                    }

                    if(!Vars.headless) {
                        Sounds.chargeVela.at(unit, isWorldCut ? 0.5f : Mathf.random(0.8f, 1.2f));
                    }
                });
            }

            timer = 0f;
        }
    }
}