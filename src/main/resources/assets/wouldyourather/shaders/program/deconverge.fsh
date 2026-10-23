#version 330

uniform sampler2D DiffuseSampler;

in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform EffectConfig {
    vec3 ConvergeX;
    vec3 ConvergeY;
    vec3 RadialConvergeX;
    vec3 RadialConvergeY;
};

out vec4 fragColor;

void main() {
    vec2 oneTexel = 1.0 / InSize;
    vec3 CoordX = texCoord.x * RadialConvergeX;
    vec3 CoordY = texCoord.y * RadialConvergeY;

    CoordX += ConvergeX * oneTexel.x - (RadialConvergeX - 1.0) * 0.5;
    CoordY += ConvergeY * oneTexel.y - (RadialConvergeY - 1.0) * 0.5;

    float RedValue   = texture(DiffuseSampler, vec2(CoordX.x, CoordY.x)).r;
    float GreenValue = texture(DiffuseSampler, vec2(CoordX.y, CoordY.y)).g;
    float BlueValue  = texture(DiffuseSampler, vec2(CoordX.z, CoordY.z)).b;

    fragColor = vec4(RedValue, GreenValue, BlueValue, 1.0);
}
