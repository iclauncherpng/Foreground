package mindustry.ui;

import arc.Core;
import arc.graphics.Color;
import arc.scene.ui.layout.*;
import arc.struct.*;
import mindustry.*;
import mindustry.core.*;
import mindustry.type.*;
import mindustry.world.blocks.storage.CoreBlock.*;

import static mindustry.Vars.*;

public class CoreItemsDisplay extends Table{
    private final ObjectSet<Item> usedItems = new ObjectSet<>();
    private CoreBuild core;
    private int lastCoreCount = -1;

    public CoreItemsDisplay(){
        rebuild();
    }

    public void resetUsed(){
        usedItems.clear();
        lastCoreCount = -1;
        background(null);
    }

    void rebuild(){
        clear();
        if(usedItems.size > 0){
            background(Styles.black6);
            margin(4);
        }

        update(() -> {
            core = Vars.player.team().core();

            boolean needsRebuild = false;

            if(content.items().contains(item -> core != null && core.items.get(item) > 0 && usedItems.add(item))){
                needsRebuild = true;
            }

            int currentCoreCount = player.team().data().cores.size;
            if(currentCoreCount != lastCoreCount){
                needsRebuild = true;
            }

            if(needsRebuild){
                rebuild();
            }
        });

        int i = 0;

        for(Item item : content.items()){
            if(usedItems.contains(item)){
                image(item.uiIcon).size(iconSmall).padRight(3).tooltip(t -> t.background(Styles.black6).margin(4f).add(item.localizedName).style(Styles.outlineLabel));
                //TODO leaks garbage
                label(() -> core == null ? "0" : UI.formatAmount(core.items.get(item))).padRight(3).minWidth(52f).left().tooltip(t -> t.background(Styles.black6).margin(4f).label(() -> core == null ? "0" : core.items.get(item) + "").style(Styles.outlineLabel));

                if(++i % 4 == 0){
                    row();
                }
            }
        }

        var teamRule = state.rules.teams.get(player.team());
        if(state.rules.canBuildCores && teamRule.maxCores > 0){
            if(i % 6 != 0) row();

            add(new Bar(
                    () -> Core.bundle.format("uibar.core-limit", player.team().data().cores.size, teamRule.maxCores),
                    () -> Color.valueOf("e08122"),
                    () -> teamRule.maxCores <= 0 ? 0f : (float)player.team().data().cores.size / teamRule.maxCores
            )).colspan(6).growX().fillX().height(18f).pad(2).center();
        }
    }
}