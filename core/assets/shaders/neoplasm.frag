#define HIGHP

#define C_ORANGE vec3(249.0, 143.0, 74.0) / 255.0
#define C_RED    vec3(224.0, 84.0, 56.0) / 255.0
#define C_DEEP   vec3(158.0, 23.0, 44.0) / 255.0
#define C_BRIGHT vec3(255.0, 220.0, 150.0) / 255.0

#define NSCALE 90.0

uniform sampler2D u_texture;
uniform sampler2D u_noise;

uniform vec2 u_campos;
uniform vec2 u_resolution;
uniform float u_time;

varying vec2 v_texCoords;

void main(){
    vec2 coords = v_texCoords * u_resolution + u_campos;
    float btime = u_time / 4000.0;

    vec2 noiseUV = coords / 140.0;
    vec2 d1 = texture2D(u_noise, noiseUV + vec2(btime * 0.2)).rg;
    vec2 d2 = texture2D(u_noise, noiseUV * 0.5 - vec2(btime * 0.1)).rg;
    vec2 displacement = (d1 + d2 - 1.0) * 20.0;

    vec2 finalCoords = coords + displacement;

    float n1 = texture2D(u_noise, (finalCoords / NSCALE) + vec2(btime * 0.3, btime * 0.1)).r;
    float n2 = texture2D(u_noise, (finalCoords / (NSCALE * 1.2)) - vec2(btime * 0.2, btime * 0.4)).r;
    float noise = (n1 + n2) * 0.5;

    vec3 finalCol;
    if(noise > 0.65) {
        finalCol = C_BRIGHT;
    } else if(noise > 0.52) {
        finalCol = C_ORANGE;
    } else if(noise > 0.40) {
        finalCol = C_RED;
    } else {
        finalCol = C_DEEP;
    }

    vec4 tex = texture2D(u_texture, v_texCoords);

    gl_FragColor = vec4(finalCol, tex.a);
}