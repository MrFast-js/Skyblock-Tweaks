package mrfast.sbt.config.components.shader

import org.lwjgl.opengl.GL20

class Shader(
    source: String,
    type: Int
) : AutoCloseable {

    val shaderId = GL20.glCreateShader(type)

    init {
        GL20.glShaderSource(shaderId, source)
        GL20.glCompileShader(shaderId)

        if (GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS) == 0) {
            throw RuntimeException("Failed to compile shader: ${GL20.glGetShaderInfoLog(shaderId, 1024)}")
        }
    }

    override fun close() {
        GL20.glDeleteShader(shaderId)
    }

    companion object {
        const val VERTEX = GL20.GL_VERTEX_SHADER
        const val FRAGMENT = GL20.GL_FRAGMENT_SHADER

        val DEFAULT_VERTEX_SHADER = createShaderFromPath("/assets/skyblocktweaks/shaders/default.vsh", Shader.VERTEX)

        fun createShaderFromPath(path: String, type: Int): Shader {
            val source = Shader::class.java.getResource(path)?.readText()
                ?: throw RuntimeException("Failed to load shader source from $path")
            return Shader(source, type)
        }
    }

}