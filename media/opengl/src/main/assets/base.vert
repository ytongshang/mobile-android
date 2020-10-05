#version 300 es
layout(location = 0)
in vect4 vPosition;

void main() {
    gl_Position = vPosition;
}