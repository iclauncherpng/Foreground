package mindustry.graphics.g3d;

import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.noise.*;
import mindustry.graphics.*;
import mindustry.type.*;

public class GiantSunMesh extends HexMesh {

    public GiantSunMesh(Planet planet, int divisions, double octaves, double persistence, double scl, double pow, double mag, float colorScale, Color... colors) {
        super(planet, new HexMesher() {

            @Override
            public float getHeight(Vec3 position) {
                double noise = Simplex.noise3d(planet.id, octaves, persistence, scl, position.x, position.y, position.z);

                float threshold = 0.75f;
                float fNoise = (float)noise;

                if(fNoise > threshold){
                    return (float)Math.pow(fNoise - threshold, pow) * (float)mag * 15f;
                }

                return 0f;
            }

            @Override
            public void getColor(Vec3 position, Color out) {
                double noise = Simplex.noise3d(planet.id, octaves, persistence, scl, position.x, position.y, position.z);

                float h = (float)Math.pow(noise, pow);

                int index = Mathf.clamp((int)(h * colors.length), 0, colors.length - 1);
                out.set(colors[index]).mul(colorScale);

                float threshold = 0.75f;
                if(noise > threshold){
                    out.lerp(Color.white, (float)(noise - threshold) * 2f);
                }
            }
        }, divisions, Shaders.unlit);
    }
}