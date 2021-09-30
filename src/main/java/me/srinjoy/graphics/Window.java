package me.srinjoy.graphics;

import me.srinjoy.Box;
import me.srinjoy.Button;
import me.srinjoy.Main;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.exit;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11C.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;

@SuppressWarnings("unused")
public class Window {
    private final long GLFW_WINDOW;
    private final VertexArray VAO;
    private final VertexBuffer VBO;
    private final Shader SHADER;
    public final Shader FONT_SHADER;
    private final ArrayList<Box<?>> BOXES = new ArrayList<>();
    public static final HashMap<String, Texture> TEXTURES = new HashMap<>();
    public static final Map<Character, String> PIECES_MAP = new HashMap<>();
    static {
        PIECES_MAP.put('r', "br");
        PIECES_MAP.put('n', "bn");
        PIECES_MAP.put('b', "bb");
        PIECES_MAP.put('q', "bq");
        PIECES_MAP.put('k', "bk");
        PIECES_MAP.put('R', "wr");
        PIECES_MAP.put('N', "wn");
        PIECES_MAP.put('B', "wb");
        PIECES_MAP.put('Q', "wq");
        PIECES_MAP.put('K', "wk");
        PIECES_MAP.put('p', "bp");
        PIECES_MAP.put('P', "wp");
        PIECES_MAP.put('d', "dot");
        PIECES_MAP.put('t', "target");
    }
    private final int WIDTH, HEIGHT;
    private final int[] TEX = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 };
    public Window(int width, int height, String title) {
        WIDTH = width;
        HEIGHT = height;
        if (!glfwInit()) {
            System.err.println("[ERROR] unable to init glfw");
            exit(1);
        }
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
        glfwSetErrorCallback((e, msg) -> System.err.printf("[ERROR] (%d): %s", e, msg));
        GLFW_WINDOW = glfwCreateWindow(width, height, title, 0L, 0L);
        if (GLFW_WINDOW == 0L) {
            System.err.println("[ERROR] unable to create window");
            glfwTerminate();
            exit(1);
        }
        glfwMakeContextCurrent(GLFW_WINDOW);
        glfwSwapInterval(1);
        glfwShowWindow(GLFW_WINDOW);
        GL.createCapabilities();
        glViewport(0, 0, width, height);
        glfwSetKeyCallback(GLFW_WINDOW, (win, key, _0, action, _1)-> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(win, true);
            }
        });
        var iconStream = Main.class.getResourceAsStream("chess_com.png");
        if (iconStream != null) {
            try {
                var iconBuffer = iconStream.readAllBytes();
                var nativeBuffer = MemoryUtil.memCalloc(iconBuffer.length);
                nativeBuffer.put(iconBuffer).flip();
                int[] iconWidth = { 0 }, iconHeight = { 0 }, channel = { 0 };
                var icon = stbi_load_from_memory(nativeBuffer, iconWidth, iconHeight, channel, 4);
                if (icon != null) {
                    var glfwImage = GLFWImage.create(1);
                    glfwImage.width(iconWidth[0]);
                    glfwImage.height(iconHeight[0]);
                    glfwImage.pixels(icon);
                    glfwSetWindowIcon(GLFW_WINDOW, glfwImage);
                    stbi_image_free(icon);
                }
                MemoryUtil.memFree(nativeBuffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        var vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        if (vidMode != null) {
            glfwSetWindowPos(GLFW_WINDOW, (vidMode.width() - width) / 2, (vidMode.height() - height) / 2);
        }
        glfwSetMouseButtonCallback(GLFW_WINDOW, (win, button, action, _0) -> {
            var pos = getCursorPos(win);
            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                BOXES.stream().filter(it -> it.isActive).forEach(it -> it.click(pos.first, pos.second, action));
            }
        });
        glfwSetCursorPosCallback(GLFW_WINDOW, (window, xpos, ypos) -> BOXES.stream().filter(it -> it.isActive).forEach(it -> it.hover(xpos, 400 - ypos)));
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        VAO = new VertexArray();
        VBO = new VertexBuffer(16000 * Float.BYTES);
        FONT_SHADER = new Shader(
        """
                #version 410 core
                layout(location = 0) in vec4 a_pos;
                layout(location = 1) in vec4 a_colour;
                layout(location = 2) in vec2 a_texCords;
                uniform mat4 mvp;
                out vec2 v_texCords;
                out vec4 v_colour;
                void main() {
                    gl_Position = mvp * a_pos;
                    v_texCords = a_texCords;
                    v_colour = a_colour;
                }
                """,
      """
                #version 410 core
                in vec2 v_texCords;
                in vec4 v_colour;
                uniform sampler2D tex;
                out vec4 colour;
                void main() {
                    vec4 sampled = vec4(1.0, 1.0, 1.0, texture(tex, v_texCords).r);
                    colour = v_colour * sampled;
                }
                """
        );
        SHADER = new Shader(
                """
                    #version 410 core
                    layout(location = 0) in vec4 a_pos;
                    layout(location = 1) in vec4 a_colour;
                    layout(location = 2) in vec2 a_texCords;
                    layout(location = 3) in float a_tex_index;
                    uniform mat4 mvp;
                    out vec2 v_texCords;
                    out vec4 v_colour;
                    out float v_tex_index;
                    void main() {
                        gl_Position = mvp * a_pos;
                        v_texCords = a_texCords;
                        v_colour = a_colour;
                        v_tex_index = a_tex_index;
                    }
                """,
                """
                    #version 410 core
                    in vec2 v_texCords;
                    in vec4 v_colour;
                    in float v_tex_index;
                    uniform sampler2D tex[16];
                    out vec4 colour;
                    void main() {
                        int index = int(v_tex_index);
                        if (index == 0) colour = v_colour;
                        else colour = texture(tex[index], v_texCords);
                    }
                """
        );
        var layout = new VertexArray.Layout();
        layout.add(2, 0L);
        layout.add(4, 2L * Float.BYTES);
        layout.add(2, 6L * Float.BYTES);
        layout.add(1, 8L * Float.BYTES);
        VAO.addLayout(VBO, layout);
    }
    public <T extends Button<T>> void addBox(Box<T> box) {
        BOXES.add(box);
    }
    private Pair<Double, Double> getCursorPos(long win) {
        var x = new double[] { 0 };
        var y = new double[] { 0 };
        glfwGetCursorPos(win, x, y);
        return new Pair<>(x[0], 400 - y[0]);
    }
    public boolean shouldClose() {
        return glfwWindowShouldClose(GLFW_WINDOW);
    }
    public void render() {
        render(() -> {});
    }
    public void render(IRenderCallback callback) {
        glClear(GL_COLOR_BUFFER_BIT);
        VAO.bind();
        SHADER.setUniform1iv("tex", TEX);
        var mvp = new Matrix4f().identity().ortho2D(0F, WIDTH * 1F, 0F, HEIGHT * 1F).get(new float[16]);
        SHADER.setUniformMat4("mvp", mvp);
        callback.invoke();
        if (!BOXES.isEmpty()) {
            for (var box: BOXES) {
                SHADER.active();
                box.render();
            }
        }
        glfwSwapBuffers(GLFW_WINDOW);
        glfwPollEvents();
    }
    public void delete() {
        VAO.delete();
        VBO.delete();
        SHADER.delete();
        TEXTURES.forEach((s, texture) -> texture.delete());
        glfwDestroyWindow(GLFW_WINDOW);
        glfwTerminate();
        glfwSetErrorCallback(null);
    }
    public static void loadTextures(int slot, String... names) throws IOException {
        for (var name: names) {
            var image = Main.class.getResourceAsStream(String.format("textures/%s.png", name));
            if (image != null) {
                TEXTURES.put(name, new Texture(slot, image.readAllBytes()));
            } else System.err.printf("Failed to load image at textures/%s.png\n", name);
        }
    }

    @FunctionalInterface
    public interface IRenderCallback {
        void invoke();
    }
    public static record Pair<K, V>(K first, V second) {}
}
