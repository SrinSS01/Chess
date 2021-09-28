package me.srinjoy.graphics;

import static java.lang.System.exit;
import static org.lwjgl.opengl.GL20.*;

@SuppressWarnings("unused")
public class Shader {
    private final int PROGRAM;
    public Shader(String vertexSrc, String fragmentSrc) {
        PROGRAM = create(vertexSrc, fragmentSrc);
    }
    private int compile(int type, String src) {
        var shader = glCreateShader(type);
        glShaderSource(shader, src);
        glCompileShader(shader);
        var success = glGetShaderi(shader, GL_COMPILE_STATUS);
        if (success == 0) {
            var msg = glGetShaderInfoLog(shader);
            System.err.printf("unable to compile %s shader\n%s\n", type == GL_VERTEX_SHADER? "vertex": "fragment", msg);
            exit(1);
        }
        return shader;
    }
    private void checkError(int program, int pName) {
        var success = glGetProgrami(program, pName);
        if (success == 0) {
            var msg = glGetProgramInfoLog(program);
            System.err.println(msg);
            exit(1);
        }
    }
    private int create(String vertexSrc, String fragmentSrc) {
        var program = glCreateProgram();
        var vertex = compile(GL_VERTEX_SHADER, vertexSrc);
        var fragment = compile(GL_FRAGMENT_SHADER, fragmentSrc);
        glAttachShader(program, vertex);
        glAttachShader(program, fragment);
        glLinkProgram(program);
        checkError(program, GL_LINK_STATUS);
        glValidateProgram(program);
        checkError(program, GL_VALIDATE_STATUS);
        glDeleteShader(vertex);
        glDeleteShader(fragment);
        return program;
    }
    public void active() {
        glUseProgram(PROGRAM);
    }
    public void setUniform1f(String name, float f) {
        glUniform1f(glGetUniformLocation(PROGRAM, name), f);
    }
    public void setUniform1i(String name, int i) {
        glUniform1i(glGetUniformLocation(PROGRAM, name), i);
    }
    public void setUniform1iv(String name, int[] intArray) {
        glUniform1iv(glGetUniformLocation(PROGRAM, name), intArray);
    }
    public void setUniformMat4(String name, float[] mat) {
        glUniformMatrix4fv(glGetUniformLocation(PROGRAM, name), false, mat);
    }
    public void delete() {
        glDeleteProgram(PROGRAM);
    }
}
