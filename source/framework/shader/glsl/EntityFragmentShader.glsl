#version 410 core

const int lightAmount = 20;

in vec2 pass_textureCoords;
in vec3 surfaceNormal;
in vec3 toLightVector[lightAmount];
in vec3 toCameraVector;
in float visibility;

layout(location = 0) out vec4 out_Color;

uniform sampler2D modelTexture;
uniform vec3 attenuation[lightAmount];
uniform vec3 lightColor[lightAmount];
uniform float shineDamper;
uniform float reflectivity;
uniform vec3 skyColor;

// make this uniform below:
const float levels = 255;
const int pcfCount = 4;
const float totalTexels = pow((pcfCount * 2 + 1), 2);
const float mapSize = 2048;

void main(void) {

    vec3 unitNormal = normalize(surfaceNormal); // normalize makes the size of the vector = 1. Only direction of the vector matters here. Size is irrelevant
    vec3 unitVectorToCamera = normalize(toCameraVector);
    vec3 totalDiffuse = vec3(0.0);
    vec3 totalSpecular = vec3(0.0);

    for(int i = 0; i < lightAmount; i++)
    {
        float distance = length(toLightVector[i]);
        float setFactor = attenuation[i].x + (attenuation[i].y * distance) + (attenuation[i].z * pow(distance, 2));

        vec3 unitLightVector = normalize(toLightVector[i]);
        float nDotl = dot(unitNormal, unitLightVector);// dot product calculation of 2 vectors. nDotl is how bright this pixel should be. difference of the position and normal vector to the light source

        // cel-shading technique (brightness)
        float brightness = max(nDotl, 0); // clamp the brightness result value to between 0 and 1. values less than 0 are clamped to 0.2. to leave a little more diffuse light
        float level = floor(brightness * levels);
        brightness = level / levels;

        vec3 lightDirection = -unitLightVector; // light direction vector is the opposite of the toLightVector
        vec3 reflectedLightDirection = reflect(lightDirection, unitNormal);// specular reflected light vector
        float specularFactor = dot(reflectedLightDirection, unitVectorToCamera);// determines how bright the specular light should be relative to the "camera" by taking the dot product of the two vectors
        specularFactor = max(specularFactor, 0.0);

        // cel-shading technique (darkness)
        float dampedFactor = pow(specularFactor, shineDamper);// raise specularFactor to the power of the shineDamper value. makes the low specular values even lower but doesnt effect the high specular values too much
        level = floor(dampedFactor * levels);
        dampedFactor = level / levels;

        totalDiffuse = totalDiffuse + (brightness * lightColor[i]) / setFactor;// calculate final color of this pixel by how much light it has
        totalSpecular = totalSpecular + (dampedFactor * reflectivity * lightColor[i]) / setFactor;
    }
    // for shadows later :))
    //totalDiffuse = max(totalDiffuse * lightFactor, 0.3);
    totalDiffuse = max(totalDiffuse, 0.5);


    vec4 textureColor = texture(modelTexture, pass_textureCoords);

    if (textureColor.a < 0.5) {
        discard;
    }

    out_Color = vec4(totalDiffuse, 1.0) *  textureColor + vec4(totalSpecular, 1.0); // returns color of the pixel from the texture at specified texture coordinates
    out_Color = mix(vec4(skyColor, 1.0), out_Color, visibility);
}