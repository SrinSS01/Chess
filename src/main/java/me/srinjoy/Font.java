package me.srinjoy;

import me.srinjoy.graphics.Shader;
import me.srinjoy.graphics.Window;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.HashMap;

import static freetype.FreeType.*;
import static freetype.FreeType.face_glyph_bitmap_width;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBufferSubData;

record Character_(int textureID, Vector2i size, Vector2i bearing, long advance) {}

@SuppressWarnings("unused")
public class Font {
    long ft, face;
    final Shader SHADER;
    final HashMap<Character, Character_> CHARACTERS = new HashMap<>();
    public Font(Shader shader, byte[] font_buffer) {
        SHADER = shader;
        ft = new_library();
        face = new_face();
        init_library(ft, face, font_buffer, font_buffer.length);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        for (byte c = 0; c < 127; c++) {
            if (load_char(face, c) != 0) {
                System.err.println("ERROR: Failed to load Glyph");
                continue;
            }
            int texture = glGenTextures();
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, texture);
            var face_glyph_bitmap_w = face_glyph_bitmap_width(face);
            var face_glyph_bitmap_r = face_glyph_bitmap_rows(face);
            var face_glyph_bitmap_l = face_glyph_bitmap_left(face);
            var face_glyph_bitmap_t = face_glyph_bitmap_top(face);
            nglTexImage2D(
                GL_TEXTURE_2D,
                0,
                GL_RED,
                face_glyph_bitmap_w,
                face_glyph_bitmap_r,
                0,
                GL_RED,
                GL_UNSIGNED_BYTE,
                face_glyph_bitmap_buffer(face)
            );
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            CHARACTERS.put((char) c, new Character_(
                texture,
                new Vector2i(face_glyph_bitmap_w, face_glyph_bitmap_r),
                new Vector2i(face_glyph_bitmap_l, face_glyph_bitmap_t),
                face_glyph_advance_x(face)
            ));
        }
    }
    public void delete() {
        clear(face, ft);
    }
    public void render_text(String text, float x, float y, float scale, Vector3f color) {
        SHADER.active();
        SHADER.setUniform1i("tex", 0);
        var proj = new Matrix4f().identity().ortho2D(0.0f, 600.0f, 0.0f, 400.0f).get(new float[16]);
        SHADER.setUniformMat4("mvp", proj);
        glActiveTexture(GL_TEXTURE0);
        for (var c : text.toCharArray()) {
            var ch = CHARACTERS.get(c);
            float xpos = x + ch.bearing().x * scale;
            float ypos = y - (ch.size().y - ch.bearing().y) * scale;
            float w = ch.size().x * scale;
            float h = ch.size().y * scale;
            float[] vertices = {
                xpos, ypos,            color.x, color.y, color.z, 1.0f,   0.0f, 1.0f,    0.0f,
                xpos + w, ypos,        color.x, color.y, color.z, 1.0f,   1.0f, 1.0f,    0.0f,
                xpos + w, ypos + h,    color.x, color.y, color.z, 1.0f,   1.0f, 0.0f,    0.0f,
                xpos + w, ypos + h,    color.x, color.y, color.z, 1.0f,   1.0f, 0.0f,    0.0f,
                xpos, ypos + h,        color.x, color.y, color.z, 1.0f,   0.0f, 0.0f,    0.0f,
                xpos, ypos,            color.x, color.y, color.z, 1.0f,   0.0f, 1.0f,    0.0f,
            };
            glBindTexture(GL_TEXTURE_2D, ch.textureID());
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);
            glDrawArrays(GL_TRIANGLES, 0, vertices.length);
            x += (ch.advance() >>> 6) * scale;
        }
    }
    public Window.Pair<Float, Float> get_width_and_height(String text, float scale) {
        float width = 0, height = 0;
        for (var c : text.toCharArray()) {
            var ch = CHARACTERS.get(c);
            float h = ch.bearing().y * scale;
            if (height < h) height = h;
            width += (ch.advance() >>> 6) * scale;
        }
        return new Window.Pair<>(width, height);
    }
}
