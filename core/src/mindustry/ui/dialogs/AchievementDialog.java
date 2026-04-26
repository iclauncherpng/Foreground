package mindustry.ui.dialogs;

import arc.graphics.Color;
import arc.scene.ui.layout.Table;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.ui.Styles;
import mindustry.service.Achievement;

import static mindustry.Vars.*;

public class AchievementDialog extends BaseDialog {
    private Table listTable = new Table();
    private String activeFilter = "all";

    public AchievementDialog() {
        super("@achievements.title");
        addCloseButton();
        setup();
    }

    void setup() {
        cont.clear();

        cont.table(t -> {
            t.margin(10f);
            t.add("@achievements.progress").left().padRight(10f);

            t.add(new Bar(() -> {
                int done = 0;
                for(Achievement a : Achievement.values()) if(a.isAchieved()) done++;
                return done + " / " + Achievement.values().length;
            }, () -> Pal.accent, () -> {
                float done = 0;
                for(Achievement a : Achievement.values()) if(a.isAchieved()) done++;
                return (float)done / Achievement.values().length;
            })).growX().height(24f);
        }).width(620f).padBottom(15f).row();

        cont.table(tabs -> {
            tabs.defaults().width(140f).height(45f).pad(4f);
            String[] filters = {"all", "unlocked", "locked"};
            for(String f : filters){
                tabs.button("@achievements." + f, Styles.togglet, () -> {
                    activeFilter = f;
                    rebuild();
                }).checked(b -> activeFilter.equals(f));
            }
        }).row();

        cont.pane(listTable).grow().scrollX(false).padTop(15f);

        rebuild();
    }

    void rebuild() {
        listTable.clear();
        listTable.top();

        int i = 0;
        for(Achievement ach : Achievement.values()){
            boolean done = ach.isAchieved();
            if(activeFilter.equals("unlocked") && !done) continue;
            if(activeFilter.equals("locked") && done) continue;

            addAchievement(ach);

            if(++i % 2 == 0) listTable.row();
        }
    }

    private void addAchievement(Achievement ach) {
        boolean done = ach.isAchieved();

        listTable.table(Styles.grayPanel, t -> {
            t.margin(12f).left();
            t.image(done ? Icon.ok : Icon.lock)
                    .size(24f)
                    .color(done ? Pal.accent : Color.gray)
                    .padRight(12f);

            t.table(info -> {
                info.left();

                info.add("@achievement." + ach.name())
                        .color(done ? Color.white : Color.gray)
                        .fontScale(1f).left()
                        .width(190f).wrap().row();

                info.add("@achievement." + ach.name() + ".description")
                        .color(Color.lightGray)
                        .fontScale(0.8f).left()
                        .width(190f).wrap();
            }).grow();

        }).width(290f).height(85f).pad(4f);
    }
}