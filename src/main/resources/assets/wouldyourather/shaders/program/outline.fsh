#version 330

uniform sampler2D DiffuseSampler;

in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

out vec4 fragColor;

void main(){
    vec2 oneTexel = 1.0 / InSize;
    vec4 center = texture(DiffuseSampler, texCoord);
    vec4 up     = texture(DiffuseSampler, texCoord + vec2(        0.0, -oneTexel.y));
    vec4 down   = texture(DiffuseSampler, texCoord + vec2( oneTexel.x,         0.0));
    vec4 left   = texture(DiffuseSampler, texCoord + vec2(-oneTexel.x,         0.0));
    vec4 right  = texture(DiffuseSampler, texCoord + vec2(        0.0,  oneTexel.y));
    vec4 uDiff = center - up;
    vec4 dDiff = center - down;
    vec4 lDiff = center - left;
    vec4 rDiff = center - right;
    vec4 sum = uDiff + dDiff + lDiff + rDiff;
    vec3 clamped = clamp(center.rgb - sum.rgb, 0.0, 1.0);
    fragColor = vec4(clamped, 1.0);
}
