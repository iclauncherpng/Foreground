package mindustry.world.blocks.power;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.*;
import mindustry.ui.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class ResonanceReactor extends NuclearReactor {
    public float resonanceSpeed = 0.0002f;
    public float stabilityFall = 0.002f;
    public float stabilityRestore = 0.0006f; 
    public float pulseRadius = 240f;
    public float pulseDamage = 500f;

    public ResonanceReactor(String name){
        super(name);
        configurable = true;

        explosionRadius = 25;
        explosionDamage = 8000;

        explodeEffect = new Effect(60f, e -> {
            Draw.color(Pal.accent);
            Lines.stroke(e.fin() * 5f);
            Lines.circle(e.x, e.y, e.fin() * 150f);

            Draw.color(Color.white);
            Fill.circle(e.x, e.y, e.fout() * 40f);

            for(int i = 0; i < 10; i++){
                float ang = Mathf.random(360f);

                Lightning.create(mindustry.gen.Groups.player.isEmpty() ? Team.sharded : mindustry.gen.Groups.player.first().team(), Pal.accent, 0f, e.x, e.y, ang, 20);
            }
        });
        explodeSound = Sounds.explosionReactor2;

        config(Integer.class, (ResonanceReactorBuild tile, Integer value) -> tile.pulse());
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("resonance", (ResonanceReactorBuild entity) ->
                new Bar("bar.resonance", Pal.accent, () -> entity.resonance));
        addBar("stability", (ResonanceReactorBuild entity) ->
                new Bar("bar.stability", Color.valueOf("feb380"), () -> entity.stability));
    }

    public class ResonanceReactorBuild extends NuclearReactorBuild {
        public float resonance = 0f;
        public float stability = 1f;

        @Override
        public void updateTile() {
            int fuel = items.get(fuelItem);
            float fullness = (float) fuel / itemCapacity;

            if (fuel > 0 && enabled) {
                resonance = Mathf.approachDelta(resonance, 1f, resonanceSpeed * fullness);
                productionEfficiency = fullness * (1f + resonance * 5f);
                heat += fullness * heating * (1f + resonance * 3f) * delta();

                if (resonance > 0.8f) {
                    stability -= stabilityFall * (resonance - 0.7f) * 2f * delta();
                } else {
                    stability = Mathf.approachDelta(stability, 1f, stabilityRestore * delta());
                }

                if (timer(timerFuel, itemDuration / timeScale)) consume();
            } else {
                productionEfficiency = 0f;
                resonance = Mathf.approachDelta(resonance, 0f, resonanceSpeed * 2f * delta());
                stability = Mathf.approachDelta(stability, 1f, stabilityRestore * delta());
            }
            float maxUsed = Math.min(liquids.currentAmount(), heat / coolantPower);
            heat -= maxUsed * coolantPower;
            liquids.remove(liquids.current(), maxUsed);

            heat = Mathf.clamp(heat);
            stability = Mathf.clamp(stability);

            if (resonance > 0.8f && Mathf.chance(0.1f * delta())) {
                Fx.chainLightning.at(x, y, resonance * size * 2f, Pal.accent);

                if (stability < 0.3f && Mathf.chance(0.05f * delta())) {
                    mindustry.entities.Damage.damage(null, x + arc.math.Mathf.range(40f), y + arc.math.Mathf.range(40f), 8f, 20f);
                }
            }

            if (stability <= 0f || heat >= 0.999f) kill();
        }

        public void pulse(){
            if(resonance < 0.2f) return;

            Sounds.explosionNavanax.at(this, 1.2f, 1.2f); 

            Fx.launchAccelerator.at(x, y, pulseRadius, Pal.accent);
            Fx.lancerLaserCharge.at(x, y, resonance * 180f, Pal.accent);

            for(int i = 0; i < 15; i++){
                float angle = Mathf.random(360f);
                float len = Mathf.random(pulseRadius);
                Lightning.create(team, Pal.accent, 0f, x, y, angle, (int)(10 * resonance));
            }

            Units.nearbyEnemies(team, x, y, pulseRadius, unit -> {
                Lightning.create(team, Pal.accent, pulseDamage * resonance, x, y, unit.angleTo(this), 20);
                Fx.lightning.at(unit.x, unit.y, unit.angleTo(this), Pal.accent);
            });

            Effect.shake(resonance * 6f, resonance * 12f, this);

            resonance = 0f;
            stability = Math.min(stability + 0.25f, 1f);
        }

        @Override
        public void draw() {
            float tx = x, ty = y;
            if (resonance > 0.7f) {
                x += Mathf.range(resonance - 0.6f);
                y += Mathf.range(resonance - 0.6f);
            }

            super.draw();

            Draw.color(Pal.accent);
            Draw.alpha(resonance * 0.5f);
            Fill.circle(x, y, resonance * size * tilesize / 2f);

            x = tx; y = ty;
            Draw.reset();
        }

        @Override
        public void buildConfiguration(arc.scene.ui.layout.Table table){
            table.table(Styles.black6, t -> {
                t.margin(12f);

                t.add(Core.bundle.get("res.level")).left();
                t.label(() -> " " + Mathf.round(resonance * 100) + "%").color(Pal.accent).row();
                t.button(b -> {
                    b.image(Icon.upload).padRight(8f);
                    b.label(() -> Core.bundle.get("res.pulse"));
                }, () -> configure(1)).size(200f, 64f).padTop(8f).disabled(b -> resonance < 0.2f);

                t.row();

                t.label(() -> resonance > 0.8f ? "[scarlet]" + Core.bundle.get("res.unstable") + "[]" : "")
                        .padTop(4f);
            });
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(resonance);
            write.f(stability);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            resonance = read.f();
            stability = read.f();
        }
    }
}