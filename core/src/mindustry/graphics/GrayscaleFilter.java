package mindustry.graphics;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.Draw;
import arc.graphics.gl.*;
import arc.util.*;
import mindustry.game.EventType.*;

import static arc.Core.*;

public class GrayscaleFilter implements Disposable {
    private FrameBuffer buffer = new FrameBuffer();
    private Shader shader;
    public boolean active = false;

    public GrayscaleFilter() {
        shader = new Shader(
                files.internal("shaders/grayscale.vert"),
                files.internal("shaders/grayscale.frag")
        );

        active = settings.getBool("grayscale", false);

        Events.on(Trigger.class, t -> {
            if(t == Trigger.enableGrayscale) active = true;
            else if(t == Trigger.disableGrayscale) active = false;

            if(!active) return;

            if(t == Trigger.universeDrawBegin) begin();
            else if(t == Trigger.universeDrawEnd) end();
        });
    }

    public void begin() {
        int w = graphics.getWidth(), h = graphics.getHeight();
        if(buffer.getWidth() != w || buffer.getHeight() != h) buffer.resize(w, h);
        buffer.begin(Color.clear);
    }

    public void end() {
        buffer.end();
        Draw.proj().setOrtho(0, 0, graphics.getWidth(), graphics.getHeight());
        shader.bind();
        buffer.blit(shader);
    }

    @Override
    public void dispose() {
        buffer.dispose();
        shader.dispose();
    }
}