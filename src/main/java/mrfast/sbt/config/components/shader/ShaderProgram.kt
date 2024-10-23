package mrfast.sbt.config.components.shader

import org.lwjgl.opengl.GL20
import java.nio.FloatBuffer

abstract class ShaderProgram(
    vararg shaders: Shader,
) : AutoCloseable {

    private val programId = GL20.glCreateProgram()

    init {
        for (shader in shaders) {
            GL20.glAttachShader(programId, shader.shaderId)
        }

        GL20.glLinkProgram(programId)
        if (GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS) == 0) {
            throw RuntimeException("Failed to link program: ${GL20.glGetProgramInfoLog(programId, 1024)}")
        }

        GL20.glValidateProgram(programId)
        if (GL20.glGetProgrami(programId, GL20.GL_VALIDATE_STATUS) == 0) {
            throw RuntimeException("Failed to validate program: ${GL20.glGetProgramInfoLog(programId, 1024)}")
        }
    }

    abstract fun drawShader(block: () -> Unit)

    fun activate() {
        GL20.glUseProgram(programId)
    }

    fun deactivate() {
        GL20.glUseProgram(0)
    }

    fun use(block: ShaderProgram.() -> Unit) {
        activate()
        block()
        deactivate()
    }

    operator fun get(uniform: String): Int = GL20.glGetUniformLocation(programId, uniform)

    operator fun set(uniform: String, value: Int) {
        GL20.glUniform1i(GL20.glGetUniformLocation(programId, uniform), value)
    }

    operator fun set(uniform: String, value: Float) {
        GL20.glUniform1f(GL20.glGetUniformLocation(programId, uniform), value)
    }

    operator fun set(uniform: String, value: FloatArray) {
        when (value.size) {
            1 -> GL20.glUniform1f(GL20.glGetUniformLocation(programId, uniform), value[0])
            2 -> GL20.glUniform2f(GL20.glGetUniformLocation(programId, uniform), value[0], value[1])
            3 -> GL20.glUniform3f(GL20.glGetUniformLocation(programId, uniform), value[0], value[1], value[2])
            4 -> GL20.glUniform4f(GL20.glGetUniformLocation(programId, uniform), value[0], value[1], value[2], value[3])
            else -> throw IllegalArgumentException("Invalid array length: ${value.size}")
        }
    }

    operator fun set(uniform: String, value: FloatBuffer) {
        GL20.glUniform1(GL20.glGetUniformLocation(programId, uniform), value)
    }

    operator fun set(uniform: String, value: IntArray) {
        when (value.size) {
            1 -> GL20.glUniform1i(GL20.glGetUniformLocation(programId, uniform), value[0])
            2 -> GL20.glUniform2i(GL20.glGetUniformLocation(programId, uniform), value[0], value[1])
            3 -> GL20.glUniform3i(GL20.glGetUniformLocation(programId, uniform), value[0], value[1], value[2])
            4 -> GL20.glUniform4i(GL20.glGetUniformLocation(programId, uniform), value[0], value[1], value[2], value[3])
            else -> throw IllegalArgumentException("Invalid array length: ${value.size}")
        }
    }

    override fun close() {
        GL20.glDeleteProgram(programId)
    }

}