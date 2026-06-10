package mindustry.entities.bullet;

import arc.graphics.*;
import arc.math.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.type.*;
import mindustry.world.blocks.diplomacy.TradeMassDriver.*;

import static mindustry.Vars.*;

public class TradeMassDriverBolt extends BasicBulletType{

    public TradeMassDriverBolt(){
        super(1f, 75);
        collidesTiles = false;
        lifetime = 1f;
        width = 11f;
        height = 13f;
        shrinkY = 0f;
        sprite = "shell";
        despawnEffect = Fx.smeltsmoke;
        hitEffect = Fx.hitBulletBig;

        lightRadius = 55f;
        lightOpacity = 0.9f;
    }

    @Override
    public void init(Bullet b){
        super.init(b);

        if(b.data() instanceof TradeDriverBulletData data){
            updateBulletColor(b, data);
        }
    }

    @Override
    public void update(Bullet b){
        super.update(b);

        //data MUST be an instance of DriverBulletData
        if(!(b.data() instanceof TradeDriverBulletData data)){
            hit(b);
            return;
        }

        float hitDst = 15f;

        //if the target is dead, just keep flying until the bullet explodes
        if(data.to.dead()){
            return;
        }

        float baseDst = data.from.dst(data.to);
        float dst1 = b.dst(data.from);
        float dst2 = b.dst(data.to);

        boolean intersect = false;

        //bullet has gone past the destination point: but did it intersect it?
        if(dst1 > baseDst){
            float angleTo = b.angleTo(data.to);
            float baseAngle = data.to.angleTo(data.from);

            //if angles are nearby, then yes, it did
            if(Angles.near(angleTo, baseAngle, 2f)){
                intersect = true;
                //snap bullet position back; this is used for low-FPS situations
                b.set(data.to.x + Angles.trnsx(baseAngle, hitDst), data.to.y + Angles.trnsy(baseAngle, hitDst));
            }
        }

        //if on course and it's in range of the target
        if(Math.abs(dst1 + dst2 - baseDst) < 4f && dst2 <= hitDst){
            intersect = true;
        } //else, bullet has gone off course, does not get received.

        if(intersect){
            data.to.handlePayload(b, data);
        }

        updateBulletColor(b, data);
    }

    @Override
    public void draw(Bullet b){
        if(lightRadius > 0 && lightOpacity > 0 && lightColor != null){
            Drawf.light(b.x, b.y, lightRadius, lightColor, lightOpacity);
        }
        super.draw(b);
    }

    @Override
    public void despawned(Bullet b){
        super.despawned(b);

        if(!(b.data() instanceof TradeDriverBulletData data)) return;

        for(int i = 0; i < data.items.length; i++){
            int amountDropped = Mathf.random(0, data.items[i]);
            if(amountDropped > 0){
                float angle = b.rotation() + Mathf.range(100f);
                Fx.dropItem.at(b.x, b.y, angle, Color.white, content.item(i));
            }
        }
    }

    @Override
    public void hit(Bullet b, float hitx, float hity, boolean createFrags){
        super.hit(b, hitx, hity, createFrags);
        despawned(b);
        if(b.data() instanceof TradeDriverBulletData data){
            float explosiveness = 0f;
            float flammability = 0f;
            float power = 0f;
            for(int i = 0; i < data.items.length; i++){
                Item item = content.item(i);
                explosiveness += item.explosiveness * data.items[i];
                flammability += item.flammability * data.items[i];
                power += item.charge * Mathf.pow(data.items[i], 1.1f) * 25f;
            }
            Damage.dynamicExplosion(b.x, b.y, flammability / 10f, explosiveness / 10f, power, 1f, state.rules.damageExplosions);
        }
    }

    private void updateBulletColor(Bullet b, TradeDriverBulletData data){
        if(data == null || data.items == null) return;

        Item primaryItem = null;
        int maxAmount = 0;
        int totalItems = 0;

        for(int i = 0; i < data.items.length; i++){
            int amount = data.items[i];
            if(amount > 0){
                totalItems += amount;
                if(amount > maxAmount){
                    maxAmount = amount;
                    primaryItem = content.item(i);
                }
            }
        }

        if(primaryItem == null || totalItems == 0) return;

        Color bulletColor = primaryItem.color;

        this.backColor = bulletColor;
        this.frontColor = bulletColor;
        this.hitColor = bulletColor;
        this.trailColor = bulletColor;
        this.lightRadius = 55f;
        this.lightOpacity = 0.9f;
        this.lightColor = bulletColor;

        if(this.lightColor.r + this.lightColor.g + this.lightColor.b < 0.1f){
            this.lightColor = Color.white;
        }
    }
}