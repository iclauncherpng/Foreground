package mindustry.world.blocks.diplomacy;

import arc.math.*;
import arc.math.geom.Geometry;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.blocks.distribution.StackConveyor;

import static mindustry.Vars.*;

public class TradeConveyor extends StackConveyor {
    public TradeConveyor(String name){
        super(name);
    }

    public class TradeConveyorBuild extends StackConveyorBuild {
        @Override
        public boolean acceptItem(Building source, Item item){
            if(this == source) return items.total() < itemCapacity && (!items.any() || items.has(item));
            if(cooldown > recharge - 1f) return false;

            if(source.team != team && !(source instanceof TradeConveyorBuild)) return false;
            return items.total() < getMaximumAccepted(item) && (!items.any() || items.has(item));
        }

        @Override
        public void onProximityUpdate(){
            super.onProximityUpdate();
            Building forward = world.build(tile.x + Geometry.d4x[rotation], tile.y + Geometry.d4y[rotation]);

            if(forward instanceof TradeConveyorBuild){
                state = stateMove;

                if(!headless){
                    blendprox |= 1;
                }
            }

            Building back = world.build(tile.x - Geometry.d4x[rotation], tile.y - Geometry.d4y[rotation]);
            if(back instanceof TradeConveyorBuild && back.rotation == rotation){
                if(!headless) blendprox |= 4;
                if(state == stateLoad) state = stateMove;
            }
        }

        @Override
        public void updateTile(){
            float eff = enabled ? (efficiency + baseEfficiency) : 1f;

            if(cooldown > 0f) cooldown = Mathf.clamp(cooldown - speed * eff * delta(), 0f, recharge);

            if(link == -1) return;
            if(cooldown > 0f) return;

            if(lastItem == null || !items.has(lastItem)){
                lastItem = items.first();
            }

            if(!enabled) return;

            if(state == stateUnload){
                while(lastItem != null && (!outputRouter ? moveForward(lastItem) : dump(lastItem))){
                    if(!outputRouter) items.remove(lastItem, 1);
                    if(!items.has(lastItem)){
                        poofOut();
                        lastItem = null;
                        break;
                    }
                }
            }else{
                if(state != stateLoad || (items.total() >= getMaximumAccepted(lastItem))){
                    Building forward = world.build(tile.x + Geometry.d4x[rotation], tile.y + Geometry.d4y[rotation]);

                    if(forward instanceof TradeConveyorBuild e && e.link == -1){
                        e.items.add(items);
                        e.lastItem = lastItem;
                        e.link = tile.pos();

                        link = -1;
                        items.clear();

                        cooldown = recharge;
                        e.cooldown = 1f;
                    }
                }
            }
        }
    }
}