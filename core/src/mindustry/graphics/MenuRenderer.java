package mindustry.graphics;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.graphics.g3d.*;

import static mindustry.Vars.*;

public class MenuRenderer implements Disposable{
    private PlanetRenderer renderer;
    private PlanetParams params;
    private float time = 0f;

    public MenuRenderer(){
        renderer = new PlanetRenderer();
        params = new PlanetParams();
        params.planet = Planets.verilus;
        params.drawSkybox = true;
        params.drawUi = false;
    }

    public void render(){
        time += Time.delta;
        float dist = params.planet.radius + (mobile ? 12.0f : 1.5f);
        Tmp.v31.set(params.planet.position).nor();
        params.camPos.set(Tmp.v31).scl(dist);
        params.camPos.add(
                Mathf.sin(time, 150f, 0.2f),
                0.5f, // Приподнимаем камеру
                Mathf.cos(time, 150f, 0.2f)
        );
        params.camUp.set(Vec3.Y);
        renderer.render(params);
        Draw.proj().setOrtho(0, 0, Core.graphics.getWidth(), Core.graphics.getHeight());
        Draw.color();
    }

    @Override
    public void dispose(){
        renderer.dispose();
    }
}