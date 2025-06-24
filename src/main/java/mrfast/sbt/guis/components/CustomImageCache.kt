package mrfast.sbt.guis.components

import java.awt.image.BufferedImage
import java.util.concurrent.ConcurrentHashMap
import javax.imageio.ImageIO

class CustomImageCache(val size: Int = 50) {
    private val cacheMap = ConcurrentHashMap<String, BufferedImage>()

    fun getImage(path: String): BufferedImage {
        if (cacheMap.size > size) cacheMap.clear()

        return cacheMap.computeIfAbsent(path) {
            ImageIO.read(this::class.java.getResourceAsStream(it))
        }
    }
}