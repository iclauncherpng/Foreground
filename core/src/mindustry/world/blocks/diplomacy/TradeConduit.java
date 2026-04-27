package mindustry.world.blocks.diplomacy;

import arc.math.*;
import arc.math.geom.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.liquid.Conduit;

import static mindustry.Vars.*;

public class TradeConduit extends Conduit {
    public TradeConduit(String name){
        super(name);
    }

    public class TradeConduitBuild extends ConduitBuild {
        
        public int blendbits, xscl = 1, yscl = 1, blending;
        public boolean capped, backCapped = false;
        @Override
        public void onProximityUpdate(){
            super.onProximityUpdate();
            int[] bits = buildBlending(tile, rotation, null, true);
            blendbits = bits[0];
            xscl = bits[1];
            yscl = bits[2];
            blending = bits[4];
            Building next = front();
            Building prev = back();
            capped = next == null || !next.block.hasLiquids;
            backCapped = blendbits == 0 && (prev == null || !prev.block.hasLiquids);
        }

        @Override
        public void updateTile(){
            smoothLiquid = Mathf.lerpDelta(smoothLiquid, liquids.currentAmount() / liquidCapacity, 0.05f);
            if(liquids.currentAmount() > 0.0001f && timer(timerFlow, 1)){
                Building next = front();
                if(next != null && next.block.hasLiquids && next.acceptLiquid(this, liquids.current())){
                    float transfer = Math.min(liquids.currentAmount(), liquidCapacity * delta());
                    next.handleLiquid(this, liquids.current(), transfer);
                    liquids.remove(liquids.current(), transfer);
                } else if(leaks && next == null){
                    Tile target = tile.nearby(rotation);
                    if(target != null) Puddles.deposit(target, liquids.current(), liquids.currentAmount() / 2f);
                    liquids.remove(liquids.current(), liquids.currentAmount() / 2f);
                }
                noSleep();
            } else {
                sleep();
            }
        }
    }
}