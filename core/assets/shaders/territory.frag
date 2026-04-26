#define HIGHP

#define ALPHA 0.18
#define step 2.0

uniform sampler2D u_texture;
uniform vec2 u_texsize;
uniform vec2 u_invsize;

varying vec2 v_texCoords;

void main(){
    vec2 T = v_texCoords.xy;
    vec4 color = texture2D(u_texture, T);
    vec2 v = u_invsize;
    vec4 maxed = max(max(max(
        texture2D(u_texture, T + vec2(0, step) * v),
        texture2D(u_texture, T + vec2(0, -step) * v)),
        texture2D(u_texture, T + vec2(step, 0) * v)),
        texture2D(u_texture, T + vec2(-step, 0) * v)
    );

    if(color.a < 0.9 && maxed.a > 0.9){
        gl_FragColor = vec4(maxed.rgb, 1.0);
    } else if(color.a > 0.0){
        gl_FragColor = vec4(color.rgb, ALPHA);
    } else {
        gl_FragColor = vec4(0.0);
    }
}