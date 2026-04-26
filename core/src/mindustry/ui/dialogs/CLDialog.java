package mindustry.ui.dialogs;

import arc.graphics.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import arc.scene.style.*;

public class CLDialog extends BaseDialog {
    private Table listTable;
    private String activeTab = "Alpha";

    public CLDialog() {
        super("@changelogtitle");
        addCloseButton();
        setup();
    }

    void setup() {
        cont.clear();

        cont.table(tabs -> {
            tabs.defaults().growX().height(50f).pad(4f);

            String[] types = {"Release", "Beta", "Alpha", "BE", "Legacy"};

            for(String type : types){
                boolean isAvailable = type.equals("Alpha") || type.equals("Legacy");

                var cell = tabs.button(type, Styles.togglet, () -> {
                    activeTab = type;
                    rebuild();
                }).checked(b -> activeTab.equals(type)).disabled(!isAvailable);

                if(!isAvailable){
                    cell.get().color.a = 0.5f;
                }
            }
        }).growX().padBottom(10f).row();

        listTable = new Table();
        cont.pane(listTable).grow().scrollX(false).get().setFadeScrollBars(false);

        rebuild();
    }

    void rebuild() {
        listTable.clear();
        listTable.top().left().margin(12f);
        listTable.defaults().left().growX();

        if (activeTab.equals("Alpha")) {
            //addSpecialRow(listTable, "[scarlet]⚠ Important:[] Mods do NOT work in this [orange]build[]! Don't try to activate them.");
            addSpecialRow(listTable, "[orange]⚠ Warning:[] This is [red]indev[] build! Do not report crashes and errors.");
            //addSpecialRow(listTable, "[#42b3f5]⚠ Info:[] The finalization of the diplomatic mode is in progress");

            addRelease(listTable, "v1.0.0 [#f54242]Alpha", "indev", 0.50f, new String[]{
                    "[#42b3f5]!!!!ALPHA!!!!",
                    "Diplomacy rework",
                    "Render fixes",
                    "Some UI changes",
                    "Mobile MainMenu changes",
                    "Improved gaming experience for mobile devices",
                    "Added console to mobile",
                    "Achievements added",
                    "Added phase duct to [#ff9266]Erekir[]",
                    "Big sandbox overhaul",
                    "New features",
                    "Experiments",
                    "Diplomacy additions. Tower blocks and territory system",
                    "Diplomacy Fixes",
                    "Territories are now displayed on the minimap",
                    "The first part of the diplomacy rework is complete",
                    "Render optimization",
                    "Menu background changes",
                    "Tech tree appearance rework",
                    "Big Optimizations",
                    "New Settings for territory layer",
                    "Arc modifications (TerritoryRender.java)",
                    "Weather rework",
            });
        }

        if (activeTab.equals("Legacy")) {
            addSpecialRow(listTable, "[#42b3f5]⚠ Info:[] This update branch is complete. Experimental build changes will appear here occasionally.");

            addRelease(listTable, "v8-160.6 [#9b34eb]Legacy BE", "05.04.2026", new String[]{
                    "Mods finally fixed",
                    "Now the planets move smoothly in orbit",
            });

            addRelease(listTable, "v8-160.5 [#9b34eb]Legacy BE", "04.04.2026", new String[]{
                    "Mods are back",
                    "Sandbox Additions",
                    "Planet render changes [buggy]",
                    "Now planets have a real orbit",
                    "[#ff9266]Erekir[] small changes",
                    "Gamma Buff",
                    "New fun setting",
                    "Bugs added",
            });

            addRelease(listTable, "v8-160.4 [#9b34eb]Legacy BE", "29.03.2026", new String[]{
                    "[#70baff]Tantros[] core changed to evacuation capsule",
                    "Core unit now is Dagger Type A",
                    "Power Wire changes",
                    "Pressure first testing",
                    "New turrets",
                    "[#70baff]Tantros[] tech tree changes",
                    "Aluminium Content",
                    "[#70baff]Tantros[] Landing sector rework",
                    "Change log dialog updated",
                    "Loading screen changed",
                    "Mod loading logic changed",
                    "Many bug fixes",
            });

            addRelease(listTable, "v8-160.3 [#9b34eb]Legacy BE", "21.03.2026", new String[]{
                    "Diplomacy gamemode added",
                    "Diplomacy Early Alpha Testing",
                    "Bug Fixes",
                    "Servers list changed",
                    "Mod Repository changed",
                    "Max map size increased to 1000x1000",
            });

            addRelease(listTable, "v8-160.2 [#9b34eb]Legacy BE", "18.03.2026", new String[]{
                    "New cool visual setting",
                    "Underwater Drill Reworks",
                    "Return to the Serpulo style",
                    "Huge logistic reworks",
                    "Core Rework",
                    "Sector [#70baff]Landing [white]reworked",
                    "Sector [#70baff]Forerunners Temple [white]preview",
                    "Pressure testing",
                    "Change Log updated again",
            });

            addRelease(listTable, "v8-160.1 [#9b34eb]Legacy BE", "16.03.2026", new String[]{
                    "Some fixes",
                    "Low-end mobile optimization settings",
                    "New logistic (Router, bridge, duct v2)",
                    "New resources (2 items + 1 liquid)",
                    "Sector [#70baff]Landing [white]rework in progress",
                    "First factories",
                    "Removed 99.9% mindustry core code",
                    "Change Log updated",
            });

            addRelease(listTable, "v8-160 [#9b34eb]Legacy BE", "15.03.2026", new String[]{
                    "Added planet [#70baff]Tantros",
                    "Added first sector to [#70baff]Tantros",
                    "Added some content to [#70baff]Tantros",
                    "Added placeholder content",
                    "First build",
                    "Sector Preview",
                    "Added change log",
                    "Removed evil-router"
            });
        }
    }

    private void addRelease(Table table, String version, String date, String[] changes) {
        addRelease(table, version, date, -1f, changes);
    }

    private void addRelease(Table table, String version, String date, float progress, String[] changes) {
        table.table(t -> {
            t.add("[accent]" + version).fontScale(1.1f).left();
            t.add("[lightgray] (" + date + ")").bottom().padLeft(8f);
        }).padTop(20).row();

        table.image().height(2f).fillX().color(Pal.accent).padBottom(10).row();

        if(progress >= 0){
            table.table(t -> {
                t.add(new Bar(() -> "Progress: " + (int)(progress * 100) + "%", () -> Pal.accent, () -> progress))
                        .height(18f).growX();
            }).padBottom(10).padLeft(10).padRight(10).row();
        }

        for (int i = 0; i < changes.length; i++) {
            String change = changes[i];
            float bottomPad = (i == changes.length - 1) ? 35f : 4f;

            table.table(item -> {
                item.add("[lightgray] • ").top();
                item.add(change).wrap().growX();
            }).padLeft(10).padBottom(bottomPad).row();
        }
    }

    private void addSpecialRow(Table table, String text) {
        table.table(Styles.grayPanel, t -> {
            t.add(text).wrap().growX();
        }).pad(4).padLeft(10).padRight(10).margin(8f).row();
    }
}