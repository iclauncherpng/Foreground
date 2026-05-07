package mindustry.graphics;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;

import static mindustry.Vars.*;

public class LightRenderer {
    private static final int scaling = 4;
    private float[] vertices = new float[24];
    private FrameBuffer buffer = new FrameBuffer();
    private Seq<CircleLight> circles = new Seq<>(CircleLight.class);
    private Seq<ImageLight> images = new Seq<>(ImageLight.class);
    private Seq<LineLight> lines = new Seq<>(LineLight.class);

    private int circleIndex = 0, imageIndex = 0, lineIndex = 0;

    private TextureRegion circleRegion, ledgeRegion, lmidRegion;

    private void checkRegions() {
        if(circleRegion == null) circleRegion = Core.atlas.find("circle-shadow");
        if(ledgeRegion == null) ledgeRegion = Core.atlas.find("circle-end");
        if(lmidRegion == null) lmidRegion = Core.atlas.find("circle-mid");
    }

    
    public void add(float x, float y, float radius, Color color, float opacity) {
        if(!enabled() || radius <= 0f) return;
        float res = Color.toFloatBits(color.r, color.g, color.b, opacity);
        if(circles.size <= circleIndex) circles.add(new CircleLight());
        circles.items[circleIndex++].set(x, y, res, radius);
    }

    
    public void add(float x, float y, TextureRegion region, float rotation, Color color, float opacity) {
        if(!enabled()) return;
        if(images.size <= imageIndex) images.add(new ImageLight());
        images.items[imageIndex++].set(x, y, region, rotation, color.toFloatBits(), opacity, Draw.xscl, Draw.yscl);
    }

    public void add(float x, float y, TextureRegion region, Color color, float opacity) {
        add(x, y, region, 0f, color, opacity);
    }

    
    public void line(float x, float y, float x2, float y2, float stroke, Color tint, float alpha) {
        if(!enabled()) return;
        if(lines.size <= lineIndex) lines.add(new LineLight());
        lines.items[lineIndex++].set(x, y, x2, y2, stroke, tint.toFloatBits(), alpha);
    }

    public boolean enabled() {
        return state.rules.lighting && state.rules.ambientLight.a > 0.0001f && renderer.drawLight;
    }

    public void draw() {
        if(!Vars.enableLight) {
            clearIndices();
            return;
        }
        checkRegions();
        buffer.resize(Core.graphics.getWidth() / scaling, Core.graphics.getHeight() / scaling);
        buffer.begin(Color.clear);
        Draw.sort(false);
        Gl.blendEquationSeparate(Gl.funcAdd, Gl.max);
        Blending.normal.apply();
        
        for(int i = 0; i < circleIndex; i++) {
            var cir = circles.items[i];
            Draw.color(cir.color);
            Draw.rect(circleRegion, cir.x, cir.y, cir.radius * 2, cir.radius * 2);
        }

        for(int i = 0; i < imageIndex; i++) {
            var img = images.items[i];
            Draw.color(img.colorPacked);
            Draw.alpha(img.opacity);
            Draw.scl(img.xscl, img.yscl);
            Draw.rect(img.region, img.x, img.y, img.rotation);
        }
        Draw.scl();

        for(int i = 0; i < lineIndex; i++) {
            renderLine(lines.items[i]);
        }

        Draw.reset();
        Draw.sort(true);
        buffer.end();
        Gl.blendEquationSeparate(Gl.funcAdd, Gl.funcAdd);
        Draw.color();
        Shaders.light.ambient.set(state.rules.ambientLight);
        buffer.blit(Shaders.light);
        clearIndices();
    }

    private void renderLine(LineLight l) {
        float rot = Mathf.angleExact(l.x2 - l.x, l.y2 - l.y);
        float color = Color.white.toFloatBits(); 
        Draw.color(l.colorPacked);
        Draw.alpha(l.opacity);
        Vec2 v1 = Tmp.v1.trnsExact(rot + 90f, l.stroke);
        float lx1 = l.x - v1.x, ly1 = l.y - v1.y,
                lx2 = l.x + v1.x, ly2 = l.y + v1.y,
                lx3 = l.x2 + v1.x, ly3 = l.y2 + v1.y,
                lx4 = l.x2 - v1.x, ly4 = l.y2 - v1.y;
    }

    private void clearIndices() {
        circleIndex = 0;
        imageIndex = 0;
        lineIndex = 0;
    }

    
    static class CircleLight {
        float x, y, color, radius;
        void set(float x, float y, float color, float radius) {
            this.x = x; this.y = y; this.color = color; this.radius = radius;
        }
    }

    static class ImageLight {
        float x, y, rotation, colorPacked, opacity, xscl, yscl;
        TextureRegion region;
        void set(float x, float y, TextureRegion reg, float rot, float col, float op, float xs, float ys) {
            this.x = x; this.y = y; this.region = reg; this.rotation = rot;
            this.colorPacked = col; this.opacity = op; this.xscl = xs; this.yscl = ys;
        }
    }

    static class LineLight {
        float x, y, x2, y2, stroke, colorPacked, opacity;
        void set(float x, float y, float x2, float y2, float str, float col, float op) {
            this.x = x; this.y = y; this.x2 = x2; this.y2 = y2;
            this.stroke = str; this.colorPacked = col; this.opacity = op;
        }
    }
}