package mrfast.sbt.mixins.transformers;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.ShaderGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Copied Verbatim from Skyhanni under GNU LGPL v2.1 license
 *
 * @link https://github.com/hannibal002/SkyHanni/blob/beta/LICENSE
 */
@Mixin(RenderGlobal.class)
public interface CustomRenderGlobal {
    @Accessor("entityOutlineFramebuffer")
    Framebuffer sbtEntityOutlineFramebuffer();

    @Accessor("entityOutlineShader")
    ShaderGroup sbtEntityOutlineShader();
}