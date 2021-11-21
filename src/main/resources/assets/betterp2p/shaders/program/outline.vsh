#version 120

uniform mat4 rotate_matrix;

attribute vec4 position;

void main() {
    gl_Position = rotate_matrix * position;
}
