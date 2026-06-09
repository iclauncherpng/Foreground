package mindustry.ui.dialogs;

import arc.graphics.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import arc.scene.style.*;

public class RoadMapDialog extends BaseDialog {
    private final float totalProgress = 0.05f;

    public RoadMapDialog() {
        super("@roadmaptitle");
        addCloseButton();
        setup();
    }

    void setup() {
        cont.clear();

        Table timeline = new Table();
        timeline.center();

        timeline.table(t -> {
            t.defaults().width(240f).center().padLeft(6f).padRight(6f);

            t.table(p -> {
                p.background(Tex.pane);
                p.margin(10f);
                p.add("[yellow]PACK 1[]").center().row();
                p.add("[accent]v1.1.0 Diplomacy rework Update[]").wrap().center().growX().get().setFontScale(0.80f);
            }).width(240f).center();

            t.table(p -> {
                p.background(Tex.pane);
                p.margin(10f);
                p.add("[orange]PACK 2[]").center().row();
                p.add("[gray]v1.2.0 Quality of Life Update[]").wrap().center().growX().get().setFontScale(0.80f);
            }).width(240f).center();

            t.table(p -> {
                p.background(Tex.pane);
                p.margin(10f);
                p.add("[#ff9266]PACK 3[]").center().row();
                p.add("[gray]v1.3.0 Erekir Content Update[]").wrap().center().growX().get().setFontScale(0.80f);
            }).width(240f).center();

            t.table(p -> {
                p.background(Tex.pane);
                p.margin(10f);
                p.add("[#70baff]AND MORE...[]").center().row();
                p.add("[gray]v1.4.0 Tantros Early Access[]").wrap().center().growX().get().setFontScale(0.80f);
            }).width(240f).center();

        }).center().row();

        timeline.table(line -> {
            line.center();
            line.add(new Bar(
                    () -> "",
                    () -> totalProgress <= 0.03f ? Pal.accent.cpy().a(1f) : Pal.accent,
                    () -> totalProgress
            )).size(960f, 8f);
        }).width(960f).center().padTop(15f).padBottom(20f).row();

        timeline.table(content -> {
            content.center();
            content.defaults().width(240f).top().padTop(6f).padBottom(6f).padLeft(6f).padRight(6f);

            content.table(Styles.grayPanel, card -> {
                card.margin(12f).top().left();
                addBullet(card, "New distribution types");
                addBullet(card, "Territory system");
                addBullet(card, "Minimap layers");
            });

            content.table(Styles.grayPanel, card -> {
                card.margin(12f).top().left();
                addBullet(card, "Sandbox control panel");
                addBullet(card, "Optimizations");
                addBullet(card, "New modding opportunities");
                addBullet(card, "Fixes");
            });

            content.table(Styles.grayPanel, card -> {
                card.margin(12f).top().left();
                addBullet(card, "Erekir expansion");
                addBullet(card, "Distribution overhaul");
                addBullet(card, "Logic blocks");
                addBullet(card, "AI Changes");
            });

            content.table(Styles.grayPanel, card -> {
                card.margin(12f).top().left();
                addBullet(card, "Tantros Planet");
                addBullet(card, "Too many factories!");
                addBullet(card, "New energy transmission system");
                addBullet(card, "Multiblocks?");
            });

        }).center().row();

        ScrollPane pane = new ScrollPane(timeline);
        pane.setScrollingDisabled(false, true);

        cont.add(pane).grow().pad(20f);
    }

    private void addBullet(Table table, String text) {
        table.table(row -> {
            row.add("[lightgray]- ").top();
            row.add(text).wrap().growX().left().fontScale(0.85f);
        }).growX().padBottom(4f).left().row();
    }
}
