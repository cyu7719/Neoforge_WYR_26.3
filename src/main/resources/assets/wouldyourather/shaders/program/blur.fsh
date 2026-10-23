#version 330

uniform sampler2D DiffuseSampler;

in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform EffectConfig {
    vec2 BlurDir;
    float Radius;
};

out vec4 fragColor;

void main() {
    vec2 oneTexel = 1.0 / InSize;
    vec4 blurred = vec4(0.0);
    float totalStrength = 0.0;
    for(float r = -Radius; r <= Radius; r += 1.0) {
        vec4 sample = texture(DiffuseSampler, texCoord + oneTexel * r * BlurDir);
        float strength = 1.0 - abs(r / Radius);
        totalStrength = totalStrength + strength;
        blurred = blurred + sample * strength;
    }
    fragColor = vec4(blurred.rgb / totalStrength, 1.0);
}
