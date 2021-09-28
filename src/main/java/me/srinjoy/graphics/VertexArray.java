package me.srinjoy.graphics;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL30.*;

record Element(int count, long offset) {}

public class VertexArray {
    private final int VAO;
    public void delete() {
        glDeleteVertexArrays(VAO);
    }
    public VertexArray() {
        VAO = glGenVertexArrays();
        bind();
    }
    public void addLayout(VertexBuffer vbo, Layout layout) {
        bind();
        vbo.bind();
        var elements = layout.elements;
        for (int i = 0; i < elements.size(); i++) {
            var element = elements.get(i);
            glVertexAttribPointer(i, element.count(), GL_FLOAT, false, layout.stride, element.offset());
            glEnableVertexAttribArray(i);
        }
    }
    public void bind() {
        glBindVertexArray(VAO);
    }
    static class Layout {
        protected final ArrayList<Element> elements = new ArrayList<>();
        protected int stride = 0;
        public void add(final int count, final long offset) {
            elements.add(new Element(count, offset));
            stride += count * Float.BYTES;
        }
    }
}
