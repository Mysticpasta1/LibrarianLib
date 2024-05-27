package com.teamwizardry.librarianlib.core.client

import com.teamwizardry.librarianlib.core.LibrarianLib
import com.teamwizardry.librarianlib.core.client.commands.ClientCommands
import com.teamwizardry.librarianlib.core.common.LibCommonProxy
import com.teamwizardry.librarianlib.features.forgeevents.CustomWorldRenderEvent
import com.teamwizardry.librarianlib.features.helpers.VariantHelper
import com.teamwizardry.librarianlib.features.helpers.vec
import com.teamwizardry.librarianlib.features.kotlin.minus
import com.teamwizardry.librarianlib.features.kotlin.times
import com.teamwizardry.librarianlib.features.kotlin.unaryMinus
import com.teamwizardry.librarianlib.features.math.AllocationDisplay
import com.teamwizardry.librarianlib.features.methodhandles.MethodHandleHelper
import com.teamwizardry.librarianlib.features.particlesystem.GameParticleSystems
import com.teamwizardry.librarianlib.features.shader.LibShaders
import com.teamwizardry.librarianlib.features.shader.ShaderHelper
import com.teamwizardry.librarianlib.features.sprite.SpritesMetadataSection
import com.teamwizardry.librarianlib.features.sprite.SpritesMetadataSectionSerializer
import com.teamwizardry.librarianlib.features.sprite.Texture
import com.teamwizardry.librarianlib.features.tesr.TileRendererRegisterProcessor
import com.teamwizardry.librarianlib.features.text.Fonts
import com.teamwizardry.librarianlib.features.utilities.client.ClientRunnable
import com.teamwizardry.librarianlib.features.utilities.client.F3Handler
import com.teamwizardry.librarianlib.features.utilities.client.ScissorUtil
import games.thecodewarrior.bitfont.typesetting.AttributedString
import games.thecodewarrior.bitfont.typesetting.TypesetString
import games.thecodewarrior.bitfont.utils.ExperimentalBitfont
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.resources.I18n
import net.minecraft.client.resources.IReloadableResourceManager
import net.minecraft.client.resources.IResourceManager
import net.minecraft.client.resources.data.MetadataSerializer
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.client.resource.IResourceType
import net.minecraftforge.client.resource.ISelectiveResourceReloadListener
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.function.Predicate

/**
 * Prefixed with Lib so code suggestion in dependent projects doesn't suggest it
 */
@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(value = [Side.CLIENT], modid = LibrarianLib.MODID)
class LibClientProxy : LibCommonProxy(), ISelectiveResourceReloadListener {

    init {
        if (!Minecraft.getMinecraft().framebuffer.isStencilEnabled)
            Minecraft.getMinecraft().framebuffer.enableStencil()
    }

    override fun pre(e: FMLPreInitializationEvent) {
        super.pre(e)

        F3Handler
        ScissorUtil
        LibShaders
        GameParticleSystems
        AllocationDisplay

        val s = MethodHandleHelper.wrapperForGetter(Minecraft::class.java, "metadataSerializer", "field_110452_an")(Minecraft.getMinecraft()) as MetadataSerializer
        s.registerMetadataSectionType(SpritesMetadataSectionSerializer(), SpritesMetadataSection::class.java)
        SpritesMetadataSection.registered = true

        Texture.register()

        if (LibrarianLib.DEV_ENVIRONMENT)
            TextureMapExporter
    }

    override fun init(e: FMLInitializationEvent) {
        super.init(e)

        if(ClientCommands.root.subCommands.isNotEmpty())
            ClientCommandHandler.instance.registerCommand(ClientCommands.root)
    }

    override fun latePre(e: FMLPreInitializationEvent) {
        super.latePre(e)

        GlowingHandler.init()
        TileRendererRegisterProcessor.register()

        (Minecraft.getMinecraft().resourceManager as IReloadableResourceManager).registerReloadListener(this)
        onResourceManagerReload(Minecraft.getMinecraft().resourceManager)

        // typeset a simple ASCII string. This does two things:
        // - initializes the atlas with the basic ASCII characters
        // - loads ICU and related files
        // This process seems to take around a second, so frontloading this process prevents stutters when first
        // opening a GUI that uses Bitfont
        @OptIn(ExperimentalBitfont::class)
        TypesetString(Fonts.classic, AttributedString(('\u0020'..'\u007E').joinToString("")))
    }

    override fun lateInit(e: FMLInitializationEvent) {
        super.lateInit(e)
        ModelHandler.init()
    }

    override fun latePost(e: FMLPostInitializationEvent) {
        super.latePost(e)
        ShaderHelper.init()
    }

    override fun translate(s: String, vararg format: Any?): String {
        return I18n.format(s, *format)
    }

    override fun canTranslate(s: String): Boolean {
        return I18n.hasKey(s)
    }

    override fun getResource(modId: String, path: String): InputStream? {
        val fixedModId = modId.toLowerCase(Locale.ROOT)
        val fixedPath = VariantHelper.pathToSnakeCase(path).removePrefix("/")

        val resourceManager = Minecraft.getMinecraft().resourceManager
        return try {
            resourceManager.getResource(ResourceLocation(fixedModId, fixedPath)).inputStream
        } catch (e: IOException) {
            null
        }
    }

    override fun runIfClient(clientRunnable: ClientRunnable) = clientRunnable.runIfClient()

    private val reloadHandlers = mutableListOf<ClientRunnable>()

    override fun addReloadHandler(clientRunnable: ClientRunnable) {
        reloadHandlers.add(clientRunnable)
    }

    override fun getClientPlayer(): EntityPlayer = Minecraft.getMinecraft().player

    override fun getDataFolder(): File = Minecraft.getMinecraft().gameDir

    // Custom events

    override fun onResourceManagerReload(resourceManager: IResourceManager, type: Predicate<IResourceType>) {
        for (it in reloadHandlers) ClientRunnable.run(it)
    }

    companion object {
        @SubscribeEvent
        @JvmStatic
        fun renderWorldEvent(e: RenderWorldLastEvent) {
            GlStateManager.pushMatrix()
            val player = Minecraft.getMinecraft().player

            val partialTicks = if (Minecraft.getMinecraft().isGamePaused)
                Minecraft.getMinecraft().renderPartialTicksPaused
            else
                Minecraft.getMinecraft().timer.renderPartialTicks

            val lastPos = vec(player.lastTickPosX, player.lastTickPosY, player.lastTickPosZ)
            val partialOffset = (player.positionVector - lastPos) * (1 - partialTicks)

            val globalize = -(player.positionVector - partialOffset)
            GlStateManager.translate(globalize.x, globalize.y, globalize.z)


            GlStateManager.disableTexture2D()
            GlStateManager.color(1f, 1f, 1f, 1f)

            MinecraftForge.EVENT_BUS.post(CustomWorldRenderEvent(Minecraft.getMinecraft().world, e.context, partialTicks))

            GlStateManager.enableTexture2D()
            GlStateManager.popMatrix()
        }
    }
}

private val Minecraft.renderPartialTicksPaused by MethodHandleHelper.delegateForReadOnly<Minecraft, Float>(Minecraft::class.java, "renderPartialTicksPaused", "field_193996_ah")
private val Minecraft.timer by MethodHandleHelper.delegateForReadOnly<Minecraft, net.minecraft.util.Timer>(Minecraft::class.java, "timer", "field_71428_T")
