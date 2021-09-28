package me.srinjoy.graphics;

import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
//import static org.lwjgl.opengl.GL45.glBindTextureUnit;
import static org.lwjgl.stb.STBImage.*;

@SuppressWarnings("unused")
public class Texture {
    private final int TEXTURE;
    public final int SLOT;

    public Texture(final int slot, final byte[] buffer) {
        TEXTURE = glGenTextures();
        SLOT = slot;
        glActiveTexture(GL_TEXTURE0 + slot);
        glBindTexture(GL_TEXTURE_2D, TEXTURE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        stbi_set_flip_vertically_on_load(true);
        final int[] width = { 0 };
        final int[] height = { 0 };
        final int[] channel = { 0 };
        final var byteBuffer = MemoryUtil.memCalloc(buffer.length);
        byteBuffer.put(buffer).flip();
        final var image = stbi_load_from_memory(byteBuffer, width, height, channel, 4);
        if (image != null) {
            glTexImage2D(
                GL_TEXTURE_2D,
                0,
                GL_RGBA,
                width[0],
                height[0],
                0,
                GL_RGBA,
                GL_UNSIGNED_BYTE,
                image
            );
            glGenerateMipmap(GL_TEXTURE_2D);
            stbi_image_free(image);
        } else System.err.println("Failed to load image!!!");
        MemoryUtil.memFree(byteBuffer);
    }
    public void delete() {
        glDeleteTextures(TEXTURE);
    }
    public void bind() {
        glActiveTexture(GL_TEXTURE0 + SLOT);
        glBindTexture(GL_TEXTURE_2D, TEXTURE);
//        glBindTextureUnit(SLOT, TEXTURE);
    }
}
