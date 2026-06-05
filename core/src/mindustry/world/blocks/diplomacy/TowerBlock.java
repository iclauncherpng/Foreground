package mindustry.world.blocks.diplomacy;

import arc.Core;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import arc.util.io.*;
import arc.scene.ui.layout.*;
import mindustry.Vars;
import mindustry.content.*;
import mindustry.core.UI;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.*;
import mindustry.world.consumers.*;
import mindustry.game.*;
import mindustry.entities.bullet.*;
import mindustry.type.*;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.StatValues;
import mindustry.annotations.Annotations.*;

import static mindustry.Vars.*;

public class TowerBlock extends Block {
    public int tier = 1;
    public BulletType shootBullet = Bullets.fireball;

    public float shieldHealth = 1000f;
    public float baseShieldRadius = 80f;
    public float shieldRegen = 2f;

    public TowerBlock(String name){
        super(name);
        update = true;
        solid = true;
        sync = true;
        configurable = true;
        saveData = true;
        buildCostMultiplier = 2f;
    }

    @Override
    public void load(){
        super.load();
        this.teamRegion = Core.atlas.find(name + "-team");
    }

    @Override
    public TextureRegion[] getGeneratedIcons(){
        return new TextureRegion[]{
                Core.atlas.find(name),
                Core.atlas.find(name + "-team-sharded", teamRegion)
        };
    }


    @Override
    public void setStats(){
        super.setStats();
        stats.add(Stat.neutralwl, t -> t.add("@drnl.buffer"));
    }

    @Override
    public void init(){
        if(tier == 4){
            hasItems = true;
            itemCapacity = 10;
            consume(new ConsumeItemFilter(i -> i == Items.silicon));
        }
        super.init();
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation) {
        if(!super.canPlaceOn(tile, team, rotation)) return false;
        float range = TerritorySystem.territoryBlocks.get(this, 0f);
        if(range <= 1f) return true;
        float side = range * 0.7071f;
        int tileOffset = (int)(side / 8f);
        for(int dx = -tileOffset; dx <= tileOffset; dx += Math.max(tileOffset * 2, 1)){
            for(int dy = -tileOffset; dy <= tileOffset; dy += Math.max(tileOffset * 2, 1)){
                if(TerritorySystem.isEnemyTerritory(tile.x + dx, tile.y + dy, team)){
                    return false;
                }
            }
        }
        return !TerritorySystem.isEnemyTerritory(tile.x, tile.y, team);
    }

    public class TowerBuild extends Building {
        public float reload = 0f;
        public float smoothRadius = 0f;
        public float hit = 0f;
        public float buildup = 0f;
        public boolean broken = false;

        @Override
        public void buildConfiguration(Table table){
            Block next = getNextTierBlock();
            if(next == null) return;
            table.table(all -> {
                all.button(Icon.upOpen, () -> {
                    upgrade();
                    deselect();
                }).size(50f).disabled(b -> !canUpgrade()).tooltip("@dtower.upgrade");

                all.table(t -> {
                    t.background(Tex.pane);
                    t.margin(6f);
                    for(ItemStack stack : next.requirements){
                        t.table(s -> {
                            s.image(stack.item.uiIcon).size(18f).padRight(4f);

                            int current = (team.core() != null ? team.core().items.get(stack.item) : 0) + getProximityItems(stack.item);

                            s.add(UI.formatAmount(stack.amount)).fontScale(0.9f).color(
                                    current >= stack.amount ? Color.white : Color.scarlet
                            );
                        }).left().pad(1f);
                        t.row();
                    }
                }).padLeft(6f);
            });
        }

        private int getProximityItems(Item item){
            int sum = 0;
            for(Building b : proximity){
                if(b.team == team && b.items != null) sum += b.items.get(item);
            }
            return sum;
        }

        public boolean canUpgrade(){
            Block next = getNextTierBlock();
            return next != null && team.core() != null && team.core().items.has(next.requirements);
        }

        public void upgrade() {
            Block next = getNextTierBlock();
            if(next == null || !canUpgrade()) return;
            team.core().items.remove(next.requirements);
            Fx.arcShieldBreak.at(x, y);
            Fx.upgradeCore.at(x, y);
            Fx.smeltsmoke.at(x, y);
            tile.setBlock(next, team);
        }

        public Block getNextTierBlock(){
            if(tier == 2) return Blocks.dipTowerTear3;
            return null;
        }



        @Override
        public void updateTile(){
            super.updateTile();
            if(broken){
                buildup -= edelta() * shieldRegen;
                if(buildup <= 0) broken = false;
            }
            boolean active = !broken && ((tier == 5) || (tier == 3 && power != null && power.status > 0.001f));
            float targetRadius = active ? (tier == 5 ? 120f : baseShieldRadius) : 0f;
            smoothRadius = Mathf.lerpDelta(smoothRadius, targetRadius, 0.05f);
            if(hit > 0) hit -= Time.delta / 10f;
            if(smoothRadius > 1f) handleShield(smoothRadius);
            if(tier == 4 && items.has(Items.silicon)){
                reload += edelta();
                if(reload >= 90f){
                    Unit target = Units.closestEnemy(team, x, y, 200f, u -> !u.dead);
                    if(target != null){
                        items.remove(Items.silicon, 1);
                        shootBullet.create(this, team, x, y, Mathf.angle(target.x - x, target.y - y));
                        reload = 0f;
                    }
                }
            }
        }

        protected void handleShield(float radius){
            Units.nearbyEnemies(team, x, y, radius + 10f, unit -> {
                float overlap = (unit.hitSize / 2f + radius) - unit.dst(x, y);
                if(overlap > 0){
                    if(overlap > unit.hitSize * 1.5f) unit.kill();
                    else unit.move(Tmp.v1.set(unit).sub(x, y).setLength(overlap + 0.01f));
                    unit.vel.setZero();
                    if(Mathf.chanceDelta(0.1f)) Fx.circleColorSpark.at(unit.x, unit.y, team.color);
                }
            });
            Groups.bullet.intersect(x - radius, y - radius, radius * 2, radius * 2, bullet -> {
                if(bullet.team != team && bullet.type.absorbable && bullet.within(x, y, radius)){
                    bullet.absorb();
                    Fx.absorb.at(bullet.x, bullet.y);
                    hit = 1f;
                    buildup += bullet.damage();
                    if(buildup >= shieldHealth){
                        broken = true;
                        buildup = shieldHealth;
                        Fx.shieldBreak.at(x, y, radius, team.color);
                    }
                }
            });
        }

        @Override
        public void draw(){
            super.draw();
            if(smoothRadius > 1f){
                Draw.draw(Layer.shields, () -> {
                    Draw.color(team.color, Color.white, Mathf.clamp(hit));
                    if(renderer.animateShields){
                        Fill.poly(x, y, 24, smoothRadius);
                    }else{
                        Lines.stroke(1.5f);
                        Draw.alpha(0.09f + Mathf.clamp(0.08f * hit));
                        Fill.poly(x, y, 24, smoothRadius);
                        Draw.alpha(1f);
                        Lines.poly(x, y, 24, smoothRadius);
                    }
                    Draw.reset();
                });
            }
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(buildup);
            write.bool(broken);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            buildup = read.f();
            broken = read.bool();
        }
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        Tile tile = world.tile(x, y);
        if(tile == null) return;
        if(!canPlaceOn(tile, player.team(), rotation)){
            String message;
            if(checkTerritoryConflict(tile, player.team())){
                message = Core.bundle.get("diplomacy.territoryconflict", "Territory Conflict");
            } else {
                message = Core.bundle.get("bar.invalid");
            }

            drawPlaceText(message, x, y, valid);
        }
        float range = TerritorySystem.territoryBlocks.get(this, 0f);
        if (range > 1f) {
            float side = range * 0.7071f;
            float centerX = x * 8f + offset;
            float centerY = y * 8f + offset;
            if(checkTerritoryConflict(tile, player.team())){
                Draw.color(Vars.player.team().color);
            } else {
                Draw.color(Vars.player.team().color);
            }

            Lines.stroke(1.5f);
            Lines.rect(centerX - side, centerY - side, side * 2, side * 2);
            Draw.alpha(0.05f);
            Fill.rect(centerX, centerY, side * 2, side * 2);
            Draw.reset();
        }
    }
    private boolean checkTerritoryConflict(Tile tile, Team team){
        float range = TerritorySystem.territoryBlocks.get(this, 0f);
        if(range <= 1f) return false;

        float side = range * 0.7071f;
        int tileOffset = (int)(side / 8f);

        for(int dx = -tileOffset; dx <= tileOffset; dx += Math.max(tileOffset * 2, 1)){
            for(int dy = -tileOffset; dy <= tileOffset; dy += Math.max(tileOffset * 2, 1)){
                if(TerritorySystem.isEnemyTerritory(tile.x + dx, tile.y + dy, team)) return true;
            }
        }
        return TerritorySystem.isEnemyTerritory(tile.x, tile.y, team);
    }
}