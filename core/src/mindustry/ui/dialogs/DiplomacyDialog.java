package mindustry.ui.dialogs;

import arc.*;
import arc.graphics.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.game.*;
import mindustry.ui.*;
import mindustry.graphics.*;

import static mindustry.Vars.*;

public class DiplomacyDialog extends BaseDialog {
    private float updateTimer = 0f;

    public DiplomacyDialog() {
        super("Diplomacy Hub");
        addCloseButton();

        shown(this::updateContent);

        update(() -> {
            if(isShown() && state.isGame()){
                updateTimer += Time.delta;
                if(updateTimer >= 60f){
                    updateContent();
                    updateTimer = 0f;
                }
            }
        });
    }

    public void updateContent() {
        cont.clear();
        Table table = new Table();
        table.margin(20).top();

        for (Teams.TeamData data : state.teams.present) {
            Team other = data.team;
            if (other == player.team() || other == Team.derelict || !data.active()) continue;

            table.table(Styles.grayPanel, t -> {
                t.margin(12).left();

                t.table(nameTable -> {
                    nameTable.add(other.coloredName()).fontScale(1.2f).left().row();
                    nameTable.label(() -> "[lightgray]Players: " + Groups.player.copy().select(p -> p.team() == other).size).left().fontScale(0.8f);
                }).width(200f).left();

                int myId = player.team().id;
                int otherId = other.id;
                byte relByte = Team.teamRelations[myId][otherId];
                Team.Relation rel = Team.Relation.values()[relByte];

                Color relColor = rel == Team.Relation.ally ? Pal.accent :
                        rel == Team.Relation.enemy ? Pal.remove : Color.white;

                t.add(rel.name().toUpperCase()).color(relColor).width(120f).center();

                t.table(btns -> {
                    btns.defaults().size(50).pad(4);

                    btns.button(Icon.add, Styles.cleari, () -> requestChange(other, Team.Relation.ally)).color(Pal.accent);
                    btns.button(Icon.move, Styles.cleari, () -> requestChange(other, Team.Relation.neutral));
                    btns.button(Icon.cancel, Styles.cleari, () -> requestChange(other, Team.Relation.enemy)).color(Pal.remove);
                }).right().growX();

            }).pad(4).width(600f).row();
        }

        ScrollPane pane = new ScrollPane(table);
        cont.add(pane).grow();
    }

    private void requestChange(Team other, Team.Relation rel) {
        Call.sendChatMessage("/diplomacy " + other.id + " " + rel.name());
        updateContent();
    }
}