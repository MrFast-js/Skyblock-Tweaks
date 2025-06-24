package mrfast.sbt.guis.components

import gg.essential.elementa.components.UIImage
import gg.essential.universal.UMatrixStack
import java.awt.image.BufferedImage
import java.util.concurrent.CompletableFuture

class RotatingUIImage(
    imageFuture: CompletableFuture<BufferedImage>
) : UIImage(imageFuture) {

    override fun draw(matrixStack: UMatrixStack) {
        // Update rotation
        updateRotation()

        val x = this.getLeft().toDouble()
        val y = this.getTop().toDouble()
        val width = this.getWidth().toDouble()
        val height = this.getHeight().toDouble()
        val color = this.getColor()

        if (color.alpha == 0) {
            return super.draw(matrixStack)
        }

        val centerX = x + width / 2
        val centerY = y + height / 2

        matrixStack.push()

        matrixStack.translate(centerX.toFloat(), centerY.toFloat(), 0f)
        matrixStack.rotate(angle, 0f, 0f, 1f, true)
        matrixStack.translate(-centerX.toFloat(), -centerY.toFloat(), 0f)

        super.draw(matrixStack)

        matrixStack.pop()
    }

    private var angle = 0f
    private var targetAngle = 0f
    private var rotationSpeed = 540f // degrees per second
    private var lastUpdateTime = System.nanoTime()

    fun setTargetAngle(degrees: Float) {
        targetAngle = ((degrees % 360f) + 360f) % 360f // Normalize to [0, 360)
    }

    private fun updateRotation() {
        val currentTime = System.nanoTime()
        val deltaTimeSec = (currentTime - lastUpdateTime) / 1_000_000_000f
        lastUpdateTime = currentTime

        val diff = ((((targetAngle - angle + 540f) % 360f) - 180f)) // shortest path
        val distance = kotlin.math.abs(diff)

        if (distance < 0.1f) {
            angle = targetAngle
            return
        }

        // Define how much angle before target to start easing (e.g., last 10 degrees)
        val easeStartDistance = 10f

        val step: Float = if (distance > easeStartDistance) {
            // Far from target, rotate at full speed
            rotationSpeed * deltaTimeSec
        } else {
            // Close to target, ease out using smoothstep
            val t = (distance / easeStartDistance).coerceIn(0f, 1f) // from 0 (at target) to 1 (start easing)
            val easeFactor = t * t * (3 - 2 * t) // smoothstep easing
            rotationSpeed * deltaTimeSec * easeFactor
        }

        val actualStep = kotlin.math.min(step, distance)
        angle = (angle + kotlin.math.sign(diff) * actualStep + 360f) % 360f
    }

    companion object {
        val defaultResourceCache = CustomImageCache(50)

        @JvmStatic
        fun ofResourceCached(path: String): RotatingUIImage {
            return ofResourceCached(path, defaultResourceCache)
        }

        @JvmStatic
        fun ofResourceCached(path: String, resourceCache: CustomImageCache): RotatingUIImage {
            val image = resourceCache.getImage(path) ?: throw IllegalStateException("Image not found: $path")
            return RotatingUIImage(CompletableFuture.completedFuture(image))
        }
    }
}
