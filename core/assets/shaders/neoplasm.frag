#define HIGHP
#define C_ORANGE vec3(249.0, 143.0, 74.0) / 255.0  // #f98f4a (Активные зоны)
#define C_RED    vec3(224.0, 84.0, 56.0) / 255.0  // #e05438 (Средний слой)
#define C_DEEP   vec3(158.0, 23.0, 44.0) / 255.0   // #9e172c (Глубокая база/Вены)
#define NSCALE 35.0
#define WSCALE 100.0

uniform sampler2D u_texture;
uniform sampler2D u_noise;

uniform vec2 u_campos;
uniform vec2 u_resolution;
uniform float u_time;

varying vec2 v_texCoords;

void main(){
    vec2 c = v_texCoords.xy;
    vec2 coords = vec2(c.x * u_resolution.x + u_campos.x, c.y * u_resolution.y + u_campos.y);
    float btime = u_time / 3000.0;
    coords += sin(coords.yx / WSCALE + btime) * 3.0;
    vec2 noiseUV1 = (coords / NSCALE) + vec2(btime * 0.5, btime * 0.2);
    vec2 noiseUV2 = (coords / (NSCALE * 1.5)) - vec2(btime * 0.1, btime * 0.6);

    float n1 = texture2D(u_noise, noiseUV1).r;
    float n2 = texture2D(u_noise, noiseUV2).r;
    float noise = (n1 + n2) / 2.0;
    float pulse = sin(u_time / 400.0 + coords.x * 0.1) * 0.04;
    noise += pulse;

    vec4 color = texture2D(u_texture, c);
    if(noise > 0.55){
        float m = smoothstep(0.55, 0.65, noise);
        color.rgb = mix(C_ORANGE, vec3(1.0, 0.9, 0.8), m * 0.2);
    } else if(noise > 0.45){
        float m = smoothstep(0.45, 0.55, noise);
        color.rgb = mix(C_RED, C_ORANGE, m);
    } else if(noise > 0.38){
        float m = smoothstep(0.38, 0.45, noise);
        color.rgb = mix(C_DEEP * 1.2, C_RED, m);
    } else {
        color.rgb = mix(C_DEEP, C_DEEP * 0.85, n2);
    }

    gl_FragColor = color;
}