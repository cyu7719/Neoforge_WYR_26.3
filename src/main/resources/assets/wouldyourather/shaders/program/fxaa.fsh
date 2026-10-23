#version 330

uniform sampler2D DiffuseSampler;

in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform EffectConfig {
    float SpanMax;
    float ReduceMul;
    float SubPixelShift;
};

out vec4 fragColor;

#define FxaaTex(t, p) texture(t, p)
#define OffsetVec(a, b) ivec2(a, b)
#define FxaaTexOff(t, p, o, r) textureOffset(t, p, o)

vec3 FxaaPixelShader(
  vec4 posPos,
  sampler2D tex,
  vec2 rcpFrame)
{

    #define FXAA_REDUCE_MIN   (1.0/128.0)

    vec3 rgbNW = FxaaTex(tex, posPos.zw).xyz;
    vec3 rgbNE = FxaaTexOff(tex, posPos.zw, OffsetVec(1,0), rcpFrame.xy).xyz;
    vec3 rgbSW = FxaaTexOff(tex, posPos.zw, OffsetVec(0,1), rcpFrame.xy).xyz;
    vec3 rgbSE = FxaaTexOff(tex, posPos.zw, OffsetVec(1,1), rcpFrame.xy).xyz;

    vec3 rgbM  = FxaaTex(tex, posPos.xy).xyz;

    vec3 luma = vec3(0.299, 0.587, 0.114);
    float lumaNW = dot(rgbNW, luma);
    float lumaNE = dot(rgbNE, luma);
    float lumaSW = dot(rgbSW, luma);
    float lumaSE = dot(rgbSE, luma);
    float lumaM  = dot(rgbM,  luma);

    float lumaMin = min(lumaM, min(min(lumaNW, lumaNE), min(lumaSW, lumaSE)));
    float lumaMax = max(lumaM, max(max(lumaNW, lumaNE), max(lumaSW, lumaSE)));

    vec2 dir;
    dir.x = -((lumaNW + lumaNE) - (lumaSW + lumaSE));
    dir.y =  ((lumaNW + lumaSW) - (lumaNE + lumaSE));

    float dirReduce = max(
        (lumaNW + lumaNE + lumaSW + lumaSE) * (0.25 * ReduceMul),
        FXAA_REDUCE_MIN);
    float rcpDirMin = 1.0/(min(abs(dir.x), abs(dir.y)) + dirReduce);
    dir = min(vec2( SpanMax,  SpanMax),
          max(vec2(-SpanMax, -SpanMax),
          dir * rcpDirMin)) * rcpFrame.xy;

    vec3 rgbA = (1.0/2.0) * (
        FxaaTex(tex, posPos.xy + dir * vec2(1.0/3.0 - 0.5)).xyz +
        FxaaTex(tex, posPos.xy + dir * vec2(2.0/3.0 - 0.5)).xyz);
    vec3 rgbB = rgbA * (1.0/2.0) + (1.0/4.0) * (
        FxaaTex(tex, posPos.xy + dir * vec2(0.0/3.0 - 0.5)).xyz +
        FxaaTex(tex, posPos.xy + dir * vec2(3.0/3.0 - 0.5)).xyz);

    float lumaB = dot(rgbB, luma);

    if ((lumaB < lumaMin) || (lumaB > lumaMax)) {
        return rgbA;
    } else {
        return rgbB;
    }
}

void main() {
    vec4 posPos;
    posPos.xy = texCoord;
    posPos.zw = texCoord - (0.5 / OutSize);

    fragColor = vec4(FxaaPixelShader(posPos, DiffuseSampler, 1.0 / OutSize), 1.0);
}
