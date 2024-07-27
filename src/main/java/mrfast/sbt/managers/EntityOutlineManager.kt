package mrfast.sbt.managers

import mrfast.sbt.customevents.RenderEntityOutlineEvent
import mrfast.sbt.mixins.transformers.CustomRenderGlobal
import mrfast.sbt.utils.LocationUtils
import mrfast.sbt.utils.Utils
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.culling.ICamera
import net.minecraft.client.shader.Framebuffer
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.BlockPos
import net.minecraftforge.client.MinecraftForgeClient
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL30
import java.awt.Color
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method


/**
 * Modified from SkyblockAddons under MIT license
 * @link https://github.com/BiscuitDevelopment/SkyblockAddons/blob/master/LICENSE
 * @author BiscuitDevelopment
 */
object EntityOutlineManager {
    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load?) {
        val renderGlobal: CustomRenderGlobal = Utils.mc.renderGlobal as CustomRenderGlobal
        renderGlobal.sbtEntityOutlineFramebuffer().framebufferClear()
    }

    /**
     * Updates the cache at the start of every minecraft tick to improve efficiency.
     * Identifies and caches all entities in the world that should be outlined.
     *
     *
     * Calls to [.shouldRender] are frustum based, rely on partialTicks,
     * and so can't be updated on a per-tick basis without losing information.
     *
     *
     * This works since entities are only updated once per tick, so the inclusion or exclusion of an entity
     * to be outlined can be cached each tick with no loss of data
     *
     * @param event the client tick event
     */
    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase == TickEvent.Phase.START) {
            val mc = Minecraft.getMinecraft()
            val renderGlobal: CustomRenderGlobal = mc.renderGlobal as CustomRenderGlobal
            if (mc.theWorld != null && shouldRenderEntityOutlines()) {
                // These events need to be called in this specific order for the xray to have priority over the no xray
                // Get all entities to render xray outlines
                val xrayOutlineEvent = RenderEntityOutlineEvent(RenderEntityOutlineEvent.Type.XRAY, null)
                MinecraftForge.EVENT_BUS.post(xrayOutlineEvent)
                // Get all entities to render no xray outlines, using pre-filtered entities (no need to test xray outlined entities)
                val noxrayOutlineEvent = RenderEntityOutlineEvent(
                    RenderEntityOutlineEvent.Type.NO_XRAY,
                    xrayOutlineEvent.entitiesToChooseFrom
                )
                MinecraftForge.EVENT_BUS.post(noxrayOutlineEvent)
                // Cache the entities for future use
                entityRenderCache.xrayCache = xrayOutlineEvent.entitiesToOutline
                entityRenderCache.noXrayCache = noxrayOutlineEvent.entitiesToOutline
                entityRenderCache.noOutlineCache = noxrayOutlineEvent.entitiesToChooseFrom

                if (isCacheEmpty) {
                    if (!emptyLastTick) {
                        renderGlobal.sbtEntityOutlineFramebuffer().framebufferClear()
                    }
                    emptyLastTick = true
                } else {
                    emptyLastTick = false
                }
            } else if (!emptyLastTick) {
                entityRenderCache.xrayCache = null
                entityRenderCache.noXrayCache = null
                entityRenderCache.noOutlineCache = null
                if (renderGlobal.sbtEntityOutlineFramebuffer() != null) renderGlobal.sbtEntityOutlineFramebuffer()
                    .framebufferClear()
                emptyLastTick = true
            }
        }
    }

    private class CachedInfo {
        var xrayCache: HashMap<Entity, Int>? = null
        var noXrayCache: HashMap<Entity, Int>? = null
        var noOutlineCache: HashSet<Entity>? = null
    }

    private val entityRenderCache = CachedInfo()
    private var stopLookingForOptifine = false
    private var isFastRender: Method? = null
    private var isShaders: Method? = null
    private var isAntialiasing: Method? = null
    private var swapBuffer: Framebuffer? = null

    /**
     * @return a new framebuffer with the size of the main framebuffer
     */
    private fun initSwapBuffer(): Framebuffer {
        val main = Minecraft.getMinecraft().framebuffer
        val framebuffer = Framebuffer(main.framebufferTextureWidth, main.framebufferTextureHeight, true)
        framebuffer.setFramebufferFilter(GL11.GL_NEAREST)
        framebuffer.setFramebufferColor(0.0f, 0.0f, 0.0f, 0.0f)
        return framebuffer
    }

    private fun updateFramebufferSize() {
        if (swapBuffer == null) {
            swapBuffer = initSwapBuffer()
        }
        val width = Minecraft.getMinecraft().displayWidth
        val height = Minecraft.getMinecraft().displayHeight
        if (swapBuffer!!.framebufferWidth != width || swapBuffer!!.framebufferHeight != height) {
            swapBuffer!!.createBindFramebuffer(width, height)
        }
        val renderGlobal: CustomRenderGlobal = Utils.mc.renderGlobal as CustomRenderGlobal
        val outlineBuffer: Framebuffer = renderGlobal.sbtEntityOutlineFramebuffer()
        if (outlineBuffer.framebufferWidth != width || outlineBuffer.framebufferHeight != height) {
            outlineBuffer.createBindFramebuffer(width, height)
            renderGlobal.sbtEntityOutlineShader().createBindFramebuffers(width, height)
        }
    }

    /**
     * Renders xray and no-xray entity outlines.
     *
     * @param camera       the current camera
     * @param partialTicks the progress to the next tick
     * @param x            the camera x position
     * @param y            the camera y position
     * @param z            the camera z position
     */
    fun renderEntityOutlines(camera: ICamera, partialTicks: Float, x: Double, y: Double, z: Double): Boolean {
        val shouldRenderOutlines = shouldRenderEntityOutlines()
        if (shouldRenderOutlines && !isCacheEmpty && MinecraftForgeClient.getRenderPass() == 0) {
            val mc = Minecraft.getMinecraft()
            val renderGlobal: CustomRenderGlobal = mc.renderGlobal as CustomRenderGlobal
            val renderManager = mc.renderManager
            mc.theWorld.theProfiler.endStartSection("entityOutlines")
            updateFramebufferSize()
            // Clear and bind the outline framebuffer
            renderGlobal.sbtEntityOutlineFramebuffer().framebufferClear()
            renderGlobal.sbtEntityOutlineFramebuffer().bindFramebuffer(false)

            // Vanilla options
            RenderHelper.disableStandardItemLighting()
            GlStateManager.disableFog()
            mc.renderManager.setRenderOutlines(true)

            // SBA options
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL13.GL_COMBINE)
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_RGB, GL11.GL_REPLACE)
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_RGB, GL13.GL_CONSTANT)
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR)
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_ALPHA, GL11.GL_REPLACE)
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_ALPHA, GL11.GL_TEXTURE)
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA)


            // Render x-ray outlines first, ignoring the depth buffer bit
            if (!isXrayCacheEmpty) {

                // Xray is enabled by disabling depth testing
                GlStateManager.depthFunc(GL11.GL_ALWAYS)
                for ((key, value) in entityRenderCache.xrayCache!!) {
                    // Test if the entity should render, given the player's camera position
                    if (shouldRender(camera, key, x, y, z)) {
                        try {
                            if (key is EntityLivingBase) {
                                setLivingColor(Color(value))
                            } else {
                                setNonLivingColor(value)
                            }
                            renderManager.renderEntityStatic(key, partialTicks, false)
                        } catch (ignored: Exception) {
                        }
                    }
                }
                // Reset depth function
                GlStateManager.depthFunc(GL11.GL_LEQUAL)
            }
            // Render no-xray outlines second, taking into consideration the depth bit
            if (!isNoXrayCacheEmpty) {
                if (!isNoOutlineCacheEmpty) {
                    // Render other entities + terrain that may occlude an entity outline into a depth buffer
                    swapBuffer!!.framebufferClear()
                    copyBuffers(mc.framebuffer, swapBuffer, GL11.GL_DEPTH_BUFFER_BIT)
                    swapBuffer!!.bindFramebuffer(false)

                    // Copy terrain + other entities depth into outline frame buffer to now switch to no-xray outlines
                    val cache = entityRenderCache.noOutlineCache!!
                    for (entity in cache) {
                        // Test if the entity should render, given the player's instantaneous camera position
                        if (shouldRender(camera, entity, x, y, z)) {
                            try {
                                renderManager.renderEntityStatic(entity, partialTicks, true)
                            } catch (ignored: Exception) {
                            }
                        }
                    }

                    // Copy the entire depth buffer of everything that might occlude outline to outline framebuffer
                    copyBuffers(swapBuffer, renderGlobal.sbtEntityOutlineFramebuffer(), GL11.GL_DEPTH_BUFFER_BIT)
                    renderGlobal.sbtEntityOutlineFramebuffer().bindFramebuffer(false)
                } else {
                    copyBuffers(
                        mc.framebuffer,
                        renderGlobal.sbtEntityOutlineFramebuffer(),
                        GL11.GL_DEPTH_BUFFER_BIT
                    )
                }

                // Xray disabled by re-enabling traditional depth testing
                for ((key, value) in entityRenderCache.noXrayCache!!) {
                    // Test if the entity should render, given the player's instantaneous camera position
                    if (shouldRender(camera, key, x, y, z)) {
                        try {
                            if (key is EntityLivingBase) {
                                setLivingColor(Color(value))
                            } else {
                                setNonLivingColor(value)
                            }
                            GlStateManager.color(1f,0f,0f)
                            GL11.glColor4f(1f,0f,0f,1f)
                            setNonLivingColor(value)
                            setLivingColor(Color(value))

                            renderManager.renderEntityStatic(key, partialTicks, true)
                        } catch (ignored: Exception) {
                            ignored.printStackTrace()
                        }
                    }
                }
            }

            // SBA options
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE)
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_RGB, GL11.GL_MODULATE)
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_RGB, GL11.GL_TEXTURE)
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR)
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_ALPHA, GL11.GL_MODULATE)
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_ALPHA, GL11.GL_TEXTURE)
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA)

            // Vanilla options
            RenderHelper.enableStandardItemLighting()
            mc.renderManager.setRenderOutlines(false)

            // Load the outline shader
            GlStateManager.depthMask(false)
            renderGlobal.sbtEntityOutlineShader().loadShaderGroup(partialTicks)
            GlStateManager.depthMask(true)

            // Reset GL/framebuffers for next render layers
            GlStateManager.enableLighting()
            mc.framebuffer.bindFramebuffer(false)
            GlStateManager.enableFog()
            GlStateManager.enableBlend()
            GlStateManager.enableColorMaterial()
            GlStateManager.enableDepth()
            GlStateManager.enableAlpha()
        }
        return !shouldRenderOutlines
    }

    private fun setLivingColor(color: Color) {
        GL11.glColor4d(
            (color.red / 255f).toDouble(),
            (color.green / 255f).toDouble(),
            (color.blue / 255f).toDouble(),
            (color.alpha / 255f)
                .toDouble()
        )
        GL11.glDisable(GL11.GL_BLEND)
    }

    /**
     * Caches optifine settings and determines whether outlines should be rendered
     *
     * @return `true` iff outlines should be rendered
     */
    fun shouldRenderEntityOutlines(): Boolean {
        val mc = Minecraft.getMinecraft()
        val renderGlobal: CustomRenderGlobal = mc.renderGlobal as CustomRenderGlobal

        // Vanilla Conditions
        if (renderGlobal.sbtEntityOutlineFramebuffer() == null || renderGlobal.sbtEntityOutlineShader() == null || mc.thePlayer == null) return false

        // Skyblock Conditions
        if (!LocationUtils.inSkyblock) {
            return false
        }

        // Optifine Conditions
        if (!stopLookingForOptifine && isFastRender == null) {
            try {
                val config = Class.forName("Config")
                try {
                    isFastRender = config.getMethod("isFastRender")
                    isShaders = config.getMethod("isShaders")
                    isAntialiasing = config.getMethod("isAntialiasing")
                } catch (ex: Exception) {
                    println("Couldn't find Optifine methods for entity outlines.")
                    stopLookingForOptifine = true
                }
            } catch (ex: Exception) {
                println("Couldn't find Optifine for entity outlines.")
                stopLookingForOptifine = true
            }
        }
        var isFastRenderValue = false
        var isShadersValue = false
        var isAntialiasingValue = false
        if (isFastRender != null) {
            try {
                isFastRenderValue = isFastRender!!.invoke(null) as Boolean
                isShadersValue = isShaders!!.invoke(null) as Boolean
                isAntialiasingValue = isAntialiasing!!.invoke(null) as Boolean
            } catch (ex: IllegalAccessException) {
                println("An error occurred while calling Optifine methods for entity outlines...$ex")
            } catch (ex: InvocationTargetException) {
                println("An error occurred while calling Optifine methods for entity outlines...$ex")
            }
        }
        return !isFastRenderValue && !isShadersValue && !isAntialiasingValue
    }

    /**
     * Apply the same rendering standards as in [net.minecraft.client.renderer.RenderGlobal.renderEntities] lines 659 to 669
     *
     * @param camera the current camera
     * @param entity the entity to render
     * @param x      the camera x position
     * @param y      the camera y position
     * @param z      the camera z position
     * @return whether the entity should be rendered
     */
    private fun shouldRender(camera: ICamera, entity: Entity, x: Double, y: Double, z: Double): Boolean {
        val mc = Minecraft.getMinecraft()
        //if (considerPass && !entity.shouldRenderInPass(MinecraftForgeClient.getRenderPass())) {
        //    return false;
        //}
        // Only render the view entity when sleeping or in 3rd person mode mode
        return if (entity === mc.renderViewEntity &&
            !(mc.renderViewEntity is EntityLivingBase && (mc.renderViewEntity as EntityLivingBase).isPlayerSleeping ||
                    mc.gameSettings.thirdPersonView != 0)
        ) {
            false
        } else mc.theWorld.isBlockLoaded(BlockPos(entity)) && (mc.renderManager.shouldRender(
            entity,
            camera,
            x,
            y,
            z
        ) || entity.riddenByEntity === mc.thePlayer)
        // Only render if renderManager would render and the world is loaded at the entity
    }

    /**
     * Function that copies a portion of a framebuffer to another framebuffer.
     *
     *
     * Note that this requires GL3.0 to function properly
     *
     *
     * The major use of this function is to copy the depth-buffer portion of the world framebuffer to the entity outline framebuffer.
     * This enables us to perform no-xray outlining on entities, as we can use the world framebuffer's depth testing on the outline frame buffer
     *
     * @param frameToCopy   the framebuffer from which we are copying data
     * @param frameToPaste  the framebuffer onto which we are copying the data
     * @param buffersToCopy the bit mask indicating the sections to copy (see [GL11.GL_DEPTH_BUFFER_BIT], [GL11.GL_COLOR_BUFFER_BIT], [GL11.GL_STENCIL_BUFFER_BIT])
     */
    private fun copyBuffers(frameToCopy: Framebuffer?, frameToPaste: Framebuffer?, buffersToCopy: Int) {
        if (OpenGlHelper.isFramebufferEnabled()) {
            OpenGlHelper.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, frameToCopy!!.framebufferObject)
            OpenGlHelper.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, frameToPaste!!.framebufferObject)
            GL30.glBlitFramebuffer(
                0, 0, frameToCopy.framebufferWidth, frameToCopy.framebufferHeight,
                0, 0, frameToPaste.framebufferWidth, frameToPaste.framebufferHeight,
                buffersToCopy, GL11.GL_NEAREST
            )
        }
    }

    val isCacheEmpty: Boolean
        get() = isXrayCacheEmpty && isNoXrayCacheEmpty
    private val isXrayCacheEmpty: Boolean
        get() = entityRenderCache.xrayCache == null || entityRenderCache.xrayCache!!.isEmpty()
    private val isNoXrayCacheEmpty: Boolean
        get() = entityRenderCache.noXrayCache == null || entityRenderCache.noXrayCache!!.isEmpty()
    private val isNoOutlineCacheEmpty: Boolean
        get() = entityRenderCache.noOutlineCache == null || entityRenderCache.noOutlineCache!!.isEmpty()
    private var emptyLastTick = false
    private val BUF_FLOAT_4 = BufferUtils.createFloatBuffer(4)
    private fun setNonLivingColor(color: Int) {
        BUF_FLOAT_4.put(0, (color shr 16 and 255).toFloat() / 255.0f)
        BUF_FLOAT_4.put(1, (color shr 8 and 255).toFloat() / 255.0f)
        BUF_FLOAT_4.put(2, (color and 255).toFloat() / 255.0f)
        BUF_FLOAT_4.put(3, 1f)
        GL11.glTexEnv(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_COLOR, BUF_FLOAT_4)
    }
}