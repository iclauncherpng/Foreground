package mindustry.graphics;

import arc.*;
import arc.func.*;
import arc.fx.*;
import arc.fx.filters.*;
import arc.graphics.*;
import arc.graphics.Pixmap.*;
import arc.graphics.g2d.*;
import arc.graphics.g3d.*;
import arc.graphics.gl.GLVersion.*;
import arc.input.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.core.*;
import mindustry.graphics.g3d.*;

import static arc.Core.*;

public class LoadRenderer implements Disposable{
    private static final Color color = new Color(Pal.accent).lerp(Color.black, 0.5f);
    private static final Color colorRed = Pal.breakInvalid.cpy().lerp(Color.black, 0.3f);
    private static final String red = "[#" + colorRed + "]";
    private static final String orange = "[#" + color + "]";
    private static final FloatSeq floats = new FloatSeq();
    private static final boolean preview = false;

    private float testprogress = 0f;
    private StringBuilder assetText = new StringBuilder();
    private Bar[] bars;
    private Mesh mesh = MeshBuilder.buildPlanetGrid(PlanetGrid.create(2), Color.cyan, 1f);
    private Camera3D cam = new Camera3D();
    private int lastLength = -1;
    private FxProcessor fx;
    private WindowedMean renderTimes = new WindowedMean(20);
    private BloomFilter bloom;
    private boolean renderStencil = true;
    private long lastFrameTime;

    {
        try{
            fx = new FxProcessor(Format.rgba8888, 2, 2, false, true);
        }catch(Exception e){
            try{
                fx = new FxProcessor(Format.rgb565, 2, 2, false, true);
            }catch(Exception awful){
                renderStencil = false;
                fx = new FxProcessor(Format.rgba8888, 2, 2, false, false);
            }
        }

        fx.addEffect(bloom = new BloomFilter());

        bars = new Bar[]{
                new Bar("s_proc#", OS.cores / 16f, OS.cores < 4),
                new Bar("c_aprog", () -> assets != null, () -> assets.getProgress(), () -> false),
                new Bar("g_vtype", graphics.getGLVersion().type == GlType.GLES ? 0.5f : 1f, graphics.getGLVersion().type == GlType.GLES),
                new Bar("s_mem#", () -> true, () -> Core.app.getJavaHeap() / 1024f / 1024f / 200f, () -> Core.app.getJavaHeap() > 1024 * 1024 * 110),
                new Bar("v_ver#", () -> !Version.build.equals("0"), () -> Version.build.equals("-1") ? 0.3f : (Strings.parseInt(Version.build, 103) - 103f) / 10f, () -> !Version.modifier.equals("release")),
                new Bar("s_osv", OS.isWindows ? 0.35f : OS.isLinux ? 0.9f : OS.isMac ? 0.5f : 0.2f, OS.isMac),
                new Bar("v_worlds#", () -> Vars.control != null && Vars.control.saves != null, () -> Vars.control.saves.getSaveSlots().size / 30f, () -> Vars.control.saves.getSaveSlots().size > 30),
                new Bar("c_datas#", () -> settings.keySize() > 0, () -> settings.keySize() / 50f, () -> settings.keySize() > 20),
                new Bar("v_alterc", () -> Vars.mods != null, () -> (Vars.mods.list().size + 1) / 6f, () -> Vars.mods.list().size > 0),
                new Bar("g_vcomp#", (graphics.getGLVersion().majorVersion + graphics.getGLVersion().minorVersion / 10f) / 4.6f, !graphics.getGLVersion().atLeast(3, 2)),
        };
    }

    @Override
    public void dispose(){
        mesh.dispose();
        fx.dispose();
        bloom.dispose();
    }

    public void draw(){

        if(!preview){
            if(lastFrameTime == 0) lastFrameTime = Time.millis();
            float timespace = Time.timeSinceMillis(lastFrameTime) / 1000f;
            renderTimes.add(timespace);
            lastFrameTime = Time.millis();
        }

        Core.graphics.clear(Color.black);

        float w = Core.graphics.getWidth(), h = Core.graphics.getHeight(), s = Scl.scl();
        float progress = assets.getProgress();
        //float progress = 0.5f;
        boolean isMobile = Core.app.isMobile() || w < h;

        Draw.proj().setOrtho(0, 0, w, h);

        if(assets.isLoaded("tech")){
            Font font = assets.get("tech");
            font.getData().markupEnabled = true;

            float pad = (isMobile ? 20f : 32f) * s;
            float lineH = (isMobile ? 14f : 22f) * s;
            font.getData().setScale(isMobile ? 0.4f * s : s);


            if(isMobile){
                font.setColor(Pal.accent);
                font.draw("Foreground", w / 2f, h / 2f + lineH, Align.center);
                font.setColor(Color.white);
                font.draw(Version.buildString() + Version.signString(), w / 2f, h / 2f - lineH / 2f, Align.center);
                if(assets.getCurrentLoading() != null){
                    String name = assets.getCurrentLoading().fileName;
                    if(name.length() > 35) name = "..." + name.substring(name.length() - 32);
                    font.setColor(Color.lightGray);
                    font.draw("INIT::" + name, w / 2f, h / 2f - 90f * s, Align.center);
                }
            }else{
                font.setColor(Pal.accent);
                font.draw(">> WELCOME BACK, " + OS.username, pad, h - pad);
                font.draw(">> CORE: [white]" + Version.combined(), pad, h - pad - lineH);

                font.setColor(Color.white);
                font.draw("BUILD_NUM: " + Version.buildNumber(), w - pad, h - pad, Align.right);

                Lines.stroke(2f * s);
                Draw.color(Pal.accent, 0.4f);
                Lines.line(pad, h - pad - lineH * 2.8f, w - pad, h - pad - lineH * 2.8f);

                float logAreaY = h - pad - lineH * 4.2f;
                float winW = 350f * s;
                float winH = 250f * s;

                int maxLines = (int)((logAreaY - pad * 4f) / lineH);
                Seq<String> names = assets.getAssetNames();

                int start = Math.max(0, names.size - maxLines);
                for(int i = start; i < names.size; i++){
                    float y = logAreaY - (i - start) * lineH;
                    String name = names.get(i).replace(OS.username, "USER");
                    if(name.length() > 50) name = "..." + name.substring(name.length() - 47);

                    font.setColor(Tmp.c1.set(Color.white).a(0.6f));
                    font.draw("[#666666]INIT::[white]" + name, pad, y);
                }

                float winX = w - winW - pad;
                float winY = pad + 120f * s;

                Draw.color(Color.black);
                Fill.rect(winX + winW / 2f, winY + winH / 2f, winW, winH);
                Draw.color(Color.cyan);
                Lines.stroke(2f * s);
                Lines.rect(winX, winY, winW, winH);
                Fill.rect(winX + winW / 2f, winY + winH + 12.5f * s, winW, 25f * s);
                font.setColor(Color.black);
                font.draw("TARGET: TANTROS", winX + 8f * s, winY + winH + 18f * s);

                Draw.flush();
                if(winW > 0 && winH > 0){
                    Gl.clear(Gl.depthBufferBit);
                    Gl.enable(Gl.depthTest);
                    Gl.viewport((int)winX, (int)winY, (int)winW, (int)winH);
                    cam.position.set(2.8f, 1f, 2.8f);
                    cam.resize(winW, winH);
                    cam.lookAt(0, 0, 0);
                    cam.fov = 35f;
                    cam.update();
                    Shaders.mesh.bind();
                    Shaders.mesh.setUniformMatrix4("u_proj", cam.combined.val);
                    mesh.render(Shaders.mesh, Gl.lines);
                    Gl.disable(Gl.depthTest);
                    Gl.viewport(0, 0, (int)w, (int)h);
                }
                Draw.proj().setOrtho(0, 0, w, h);
            }

            float barW = isMobile ? w - pad * 4f : w - pad * 2f;
            float barH = (isMobile ? 10f : 20f) * s;
            float barX = isMobile ? pad * 2f : pad;
            float barY = isMobile ? h / 2f - 60f * s : pad + 20f * s;

            Draw.color(Color.darkGray, 0.3f);
            Lines.stroke(1f * s);
            Lines.rect(barX, barY, barW, barH);

            int segments = isMobile ? 20 : 45;
            float segW = barW / segments;
            int filled = (int)(progress * segments);

            Draw.color(Pal.accent);
            for(int i = 0; i < filled; i++){
                Fill.rect(barX + i * segW + segW / 2f, barY + barH / 2f, segW - 1.5f * s, barH - 4f * s);
            }

            font.setColor(Color.white);
            if(isMobile){
                font.draw((int)(progress * 100) + "%", w / 2f, barY - 10f * s, Align.center);
            }else{
                font.draw("BOOT: " + (int)(progress * 100) + "%", barX, barY + barH + 18f * s);
                if(assets.getCurrentLoading() != null){
                    font.draw("FILE: " + assets.getCurrentLoading().fileName, w - pad, barY + barH + 18f * s, Align.right);
                }
            }

            font.getData().setScale(s);
        }
        Draw.flush();
    }

    static class Bar{
        final Floatp value;
        final Boolp red, valid;
        final String text;

        public Bar(String text, float value, boolean red){
            this.value = () -> value;
            this.red = () -> red;
            this.valid = () -> true;
            this.text = text;
        }

        public Bar(String text, Boolp valid, Floatp value, Boolp red){
            this.valid = valid;
            this.value = value;
            this.red = red;
            this.text = text;
        }

        boolean valid(){
            return valid.get();
        }

        boolean red(){
            return red.get();
        }

        float value(){
            return Mathf.clamp(value.get());
        }
    }
}