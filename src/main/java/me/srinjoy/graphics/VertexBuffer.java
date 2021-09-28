package me.srinjoy.graphics;

import static org.lwjgl.opengl.GL15.*;

@SuppressWarnings("unused")
public class VertexBuffer {
    private final int VBO;
    public VertexBuffer(int size) {
        VBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, size, GL_DYNAMIC_DRAW);
    }
    public VertexBuffer(float[] buffer, int usage) {
        VBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, buffer, usage);
    }

    public void bind() {
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
    }
    public void delete() {
        glDeleteBuffers(VBO);
    }
}
