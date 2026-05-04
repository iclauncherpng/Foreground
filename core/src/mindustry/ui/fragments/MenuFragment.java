package mindustry.ui.fragments;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.*;
import arc.scene.actions.*;
import arc.scene.event.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.ImageButton.*;
import arc.scene.ui.TextButton.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.core.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.AboutDialog;
import mindustry.ui.dialogs.AchievementDialog;
import mindustry.ui.dialogs.CLDialog;

import static mindustry.Vars.*;
import static mindustry.gen.Tex.*;

public class MenuFragment{
    private Table container, submenu;
    private Button currentMenu;
    private MenuRenderer renderer;
    private Seq<MenuButton> customButtons = new Seq<>();
    public Seq<MenuButton> desktopButtons = null;

    public void build(Group parent){
        renderer = new MenuRenderer();

        Group group = new WidgetGroup();
        group.setFillParent(true);
        group.visible(() -> !ui.editor.isShown());
        parent.addChild(group);

        parent = group;

        parent.fill((x, y, w, h) -> renderer.render());

        parent.fill(c -> {
            c.pane(Styles.noBarPane, cont -> {
                container = cont;
                cont.name = "menu container";

                if(!mobile){
                    c.left();
                    buildDesktop();
                    Events.on(ResizeEvent.class, event -> buildDesktop());
                }else{
                    buildMobile();
                    Events.on(ResizeEvent.class, event -> buildMobile());
                }
            }).with(pane -> {
                pane.setOverscroll(false, false);
            }).grow();
        });

        if(mobile){
            parent.fill((x, y, w, h) -> {
                x = 0f;
                y = 0f;
                w = Core.graphics.getWidth();
                h = Core.graphics.getHeight();
                if(Core.scene.marginLeft > 0){
                    paneRight.draw(x, y, Core.scene.marginLeft, h);
                }

                if(Core.scene.marginRight > 0){
                    paneLeft.draw(x + w - Core.scene.marginRight, y, Core.scene.marginRight, h);
                }

                if(Core.scene.marginBottom > 0){
                    Tex.paneTop.draw(Core.scene.marginLeft, 0, Core.graphics.getWidth() - Core.scene.marginRight - Core.scene.marginLeft, Core.scene.marginBottom);
                }
            });

            parent.fill(c -> {
                c.bottom().left();
                c.button(Icon.terminal, () -> ui.consolefrag.toggleMobile()).visible(() -> !ui.consolefrag.shown() && Core.settings.getBool("console")).pad(4f).size(60f).left().row();

                c.button("", new TextButtonStyle(){{
                    font = Fonts.def;
                    fontColor = Color.white;
                    up = infoBanner;
                }}, ui.about::show).size(84, 45).visible(() -> !ui.consolefrag.shown()).name("info");
            });
        }else if(becontrol.active()){
            parent.fill(c -> c.bottom().right().button("@be.check", Icon.refresh, () -> {
                ui.loadfrag.show();
                becontrol.checkUpdate(result -> {
                    ui.loadfrag.hide();
                    if(!result){
                        ui.showInfo("@be.noupdates");
                    }
                });
            }).size(200, 60).name("becheck").update(t -> {
                t.getLabel().setColor(becontrol.isUpdateAvailable() ? Tmp.c1.set(Color.white).lerp(Pal.accent, Mathf.absin(5f, 1f)) : Color.white);
            }));
        }

        //String versionText = ((Version.build == -1) ? "[#fc8140aa]" : "[#ffffffba]") + Version.combined() + "\n[#70baffba]Tantros Preview";
        String versionText = /*"[#f54260ba]Overhaul Update\n" +*/ ((Version.build.equals("-1")) ? "[#fc8140aa]" : "[#ffffffba]") + Version.combined();
        parent.fill((x, y, w, h) -> {
            TextureRegion logo = Core.atlas.find("logo");
            float width = Core.graphics.getWidth(), height = Core.graphics.getHeight() - Core.scene.marginTop;
            float logoscl = Scl.scl(1) * logo.scale;
            float logow = Math.min(logo.width * logoscl, Core.graphics.getWidth() - Scl.scl(20));
            float logoh = logow * (float)logo.height / logo.width;

            float fx = (int)(width / 2f);
            float fy = (int)(height - 6 - logoh) + logoh / 2 - (Core.graphics.isPortrait() ? Scl.scl(30f) : 0f);
            if(Core.settings.getBool("macnotch") ){
                fy -= Scl.scl(macNotchHeight);
            }

            Draw.color();
            Draw.rect(logo, fx, fy, logow, logoh);

            Fonts.outline.setColor(Color.white);
            Fonts.outline.draw(versionText, fx, fy - logoh/2f - Scl.scl(2f), Align.center);
        }).touchable = Touchable.disabled;
    }

    private void buildMobile(){
        container.clearChildren();
        container.name = "buttons";

        container.color.a = 0f;
        container.setScale(0.95f);
        container.actions(Actions.parallel(
                Actions.fadeIn(0.2f, Interp.fade),
                Actions.scaleTo(1f, 1f, 0.2f, Interp.pow3Out)
        ));

        boolean portrait = Core.graphics.isPortrait();
        float size = 120f;
        container.defaults().size(size).pad(5).padTop(4f);
        if(!portrait) container.marginTop(60f);

        Runnable showMain = this::buildMobile;

        MobileButton playMenu = new MobileButton(Icon.play, "@play", () -> {
            container.actions(Actions.fadeOut(0.1f, Interp.fade), Actions.run(() -> {
                container.clearChildren();

                container.add(new MobileButton(Icon.play, "@campaign", () -> checkPlay(() -> ui.planet.show())));
                container.add(new MobileButton(Icon.add, "@joingame", () -> checkPlay(() -> ui.join.show())));
                if(portrait) container.row();
                container.add(new MobileButton(Icon.rightOpenOut, "@customgame", () -> checkPlay(() -> ui.custom.show())));
                container.add(new MobileButton(Icon.download, "@loadgame", () -> checkPlay(() -> ui.load.show())));

                if(portrait){
                    container.row();
                    container.add(new MobileButton(Icon.left, "@back", showMain))
                            .colspan(2)
                            .fillX()
                            .width((size * 2) + (5f * 2)) 
                            .height(size / 2f); 
                }else{
                    container.add(new MobileButton(Icon.left, "@back", showMain));
                }

                container.actions(Actions.fadeIn(0.15f, Interp.fade));
            }));
        });

        MobileButton infoMenu = new MobileButton(Icon.book, "@database.button", () -> {
            container.actions(Actions.fadeOut(0.1f, Interp.fade), Actions.run(() -> {
                container.clearChildren();

                container.add(new MobileButton(Icon.paste, "@schematics", () -> ui.schematics.show()));
                container.add(new MobileButton(Icon.book, "@database", () -> ui.database.show()));

                if(portrait) container.row();

                container.add(new MobileButton(Icon.logic, "@achievement", () -> new AchievementDialog().show()));

                if(portrait){
                    if(container.getChildren().size % 2 != 0) container.add();
                    container.row();
                    container.add(new MobileButton(Icon.left, "@back", showMain))
                            .colspan(2)
                            .fillX()
                            .width((size * 2) + (5f * 2)) 
                            .height(size / 2f); 
                }else{
                    container.add(new MobileButton(Icon.left, "@back", showMain));
                }

                container.actions(Actions.fadeIn(0.15f, Interp.fade));
            }));
        });

        MobileButton editor = new MobileButton(Icon.terrain, "@editor", () -> checkPlay(() -> ui.maps.show()));
        MobileButton settings = new MobileButton(Icon.settings, "@settings", () -> ui.settings.show());
        MobileButton changelog = new MobileButton(Icon.infoCircle, "@changelogb", () -> new CLDialog().show());
        MobileButton exit = new MobileButton(Icon.exit, "@quit", () -> Core.app.exit());
        MobileButton about = new MobileButton(Icon.info, "@about.button", () -> ui.about.show());

        container.add(playMenu);
        container.add(infoMenu);
        if(portrait) container.row();

        container.add(editor);
        container.add(settings);
        if(portrait) container.row();

        container.add(changelog);
        container.add(ios ? about : exit);

        for(int i = 0; i < customButtons.size; i++){
            if(portrait && container.getChildren().size % 2 == 0) container.row();
            var b = customButtons.get(i);
            container.add(new MobileButton(b.icon, b.text, b.runnable));
        }
    }

    private void buildDesktop(){
        container.clearChildren();
        container.setSize(Core.graphics.getWidth(), Core.graphics.getHeight());

        float width = 230f;
        Drawable background = Styles.black6;

        container.left();
        container.add().width(Core.graphics.getWidth()/10f);
        container.table(background, t -> {
            t.defaults().width(width).height(70f);
            t.name = "buttons";

            if(desktopButtons == null){
                desktopButtons = Seq.with(
                        new MenuButton("@play", Icon.play,
                                new MenuButton("@campaign", Icon.play, () -> checkPlay(() -> ui.planet.show())),
                                new MenuButton("@joingame", Icon.add, () -> checkPlay(() -> ui.join.show())),
                                new MenuButton("@customgame", Icon.terrain, () -> checkPlay(() -> ui.custom.show())),
                                new MenuButton("@loadgame", Icon.download, () -> checkPlay(() -> ui.load.show()))
                        ),
                        new MenuButton("@database.button", Icon.menu,
                                new MenuButton("@schematics", Icon.paste, () -> ui.schematics.show()),
                                new MenuButton("@database", Icon.book, () -> ui.database.show()),
                                new MenuButton("@achievement", Icon.logic, () -> new AchievementDialog().show())
                        ),
                        new MenuButton("@editor", Icon.terrain, () -> checkPlay(() -> ui.maps.show())), steam ? new MenuButton("@workshop", Icon.steam, () -> platform.openWorkshop()) : null,
                        new MenuButton("@changelogb", Icon.infoCircle, () -> new CLDialog().show()),
                        new MenuButton("@about", Icon.arrowNote, () -> new AboutDialog().show()),
                        new MenuButton("@settings", Icon.settings, () -> ui.settings.show())
                );
            }

            buttons(t, desktopButtons.toArray(MenuButton.class));
            buttons(t, customButtons.toArray(MenuButton.class));
            buttons(t, new MenuButton("@quit", Icon.exit, () -> Core.app.exit()));
        }).width(width).growY();

        container.table(background, t -> {
            submenu = t;
            t.name = "submenu";
            t.color.a = 0f;
            t.top();
            t.defaults().width(width).height(70f);
            t.visible(() -> !t.getChildren().isEmpty());

        }).width(width).growY();
    }

    private void checkPlay(Runnable run){
        if(!mods.hasContentErrors()){
            run.run();
        }else{
            ui.showInfo("@mod.noerrorplay");
        }
    }

    private void fadeInMenu(){
        submenu.clearActions();
        submenu.actions(Actions.alpha(1f, 0.15f, Interp.fade));
    }

    private void fadeOutMenu(){
        if(submenu.getChildren().isEmpty()){
            return;
        }

        submenu.clearActions();
        submenu.actions(Actions.alpha(1f), Actions.alpha(0f, 0.2f, Interp.fade), Actions.run(() -> submenu.clearChildren()));
    }

    private void buttons(Table t, MenuButton... buttons){
        for(MenuButton b : buttons){
            if(b == null) continue;
            Button[] out = {null};
            out[0] = t.button(b.text, b.icon, Styles.flatToggleMenut, () -> {
                if(currentMenu == out[0]){
                    currentMenu = null;
                    fadeOutMenu();
                }else{
                    if(b.submenu != null && b.submenu.any()){
                        currentMenu = out[0];
                        submenu.clearChildren();
                        fadeInMenu();
                        submenu.add().height((Core.graphics.getHeight() - Core.scene.marginTop - Core.scene.marginBottom - out[0].getY(Align.topLeft)) / Scl.scl(1f));
                        submenu.row();
                        buttons(submenu, b.submenu.toArray());
                    }else{
                        currentMenu = null;
                        fadeOutMenu();
                        b.runnable.run();
                    }
                }
            }).marginLeft(11f).get();
            out[0].update(() -> out[0].setChecked(currentMenu == out[0]));
            t.row();
        }
    }

    public void addButton(String text, Drawable icon, Runnable callback){
        addButton(new MenuButton(text, icon, callback));
    }

    public void addButton(String text, Runnable callback){
        addButton(text, Styles.none, callback);
    }

    public void addButton(MenuButton button){
        customButtons.add(button);
    }

    public static class MenuButton{
        public final Drawable icon;
        public final String text;
        public final Runnable runnable;
        public final @Nullable Seq<MenuButton> submenu;

        public MenuButton(String text, Drawable icon, Runnable runnable){
            this.icon = icon;
            this.text = text;
            this.runnable = runnable;
            this.submenu = null;
        }

        public MenuButton(String text, Drawable icon, Runnable runnable, MenuButton... submenu){
            this.icon = icon;
            this.text = text;
            this.runnable = runnable;
            this.submenu = submenu != null ? Seq.with(submenu) : null;
        }

        MenuButton(String text, Drawable icon, MenuButton... submenu){
            this.icon = icon;
            this.text = text;
            this.runnable = () -> {};
            this.submenu = submenu != null ? Seq.with(submenu) : null;
        }
    }
}