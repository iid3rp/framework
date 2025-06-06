#version 410 core

const int lightAmount = 20;

in vec3 position;
in vec2 textureCoords;
in vec3 normal;

out vec2 pass_textureCoords;
out vec3 surfaceNormal;
out vec3 toLightVector[lightAmount];
out vec3 toCameraVector;
out float visibility;
out vec4 shadowCoords;

uniform mat4 transformationMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform vec3 lightPosition[lightAmount];
uniform vec4 plane;
uniform mat4 toShadowMapSpace;

const float fogDensity = 1 / 100000;
const float fogGradient = .2;

void main(void)
{
    vec4 worldPosition = transformationMatrix * vec4(position, 1.0);
    shadowCoords = toShadowMapSpace * worldPosition;

    gl_ClipDistance[1] = dot(worldPosition, plane);

    vec4 positionRelativeToCamera = viewMatrix * worldPosition;
    gl_Position = projectionMatrix * positionRelativeToCamera;
    pass_textureCoords = textureCoords;

    surfaceNormal = (transformationMatrix * vec4(normal, 0.0)).xyz; // swizzle it! get the xyz components from the resulting 4d vector

    for(int i = 0; i < lightAmount; i++)
    {
        toLightVector[i] = lightPosition[i] - worldPosition.xyz;  // world position is a 4d vector. again, use a swizzle to get a 3d vector from it
    }

    // This shader does not have the "camera" position, but the view matrix position is the inverse of the camera, so the inverse of the view matrix is the camera position
    // multiply this matrix by an  empty 4d matrix and subract the worldPosition of the vertex gives the distance between them
    toCameraVector = (inverse(viewMatrix) * vec4(0.0, 0.0, 0.0, 1.0)).xyz - worldPosition.xyz;

    // Calculate fog
    float distanceFromCamera = length(positionRelativeToCamera.xyz);
    visibility = exp(-pow((distanceFromCamera * fogDensity), fogGradient));
    visibility = clamp(visibility, 0.0, 1.0);
}
