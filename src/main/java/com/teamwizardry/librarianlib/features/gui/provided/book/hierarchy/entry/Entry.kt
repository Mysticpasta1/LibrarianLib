package com.teamwizardry.librarianlib.features.gui.provided.book.hierarchy.entry

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.teamwizardry.librarianlib.core.LibrarianLog
import com.teamwizardry.librarianlib.features.gui.component.GuiComponent
import com.teamwizardry.librarianlib.features.gui.provided.book.ComponentEntryPage
import com.teamwizardry.librarianlib.features.gui.provided.book.IBookGui
import com.teamwizardry.librarianlib.features.gui.provided.book.hierarchy.IBookElement
import com.teamwizardry.librarianlib.features.gui.provided.book.hierarchy.category.Category
import com.teamwizardry.librarianlib.features.gui.provided.book.hierarchy.entry.criterion.ICriterion
import com.teamwizardry.librarianlib.features.gui.provided.book.hierarchy.entry.criterion.game.EntryUnlockedEvent
import com.teamwizardry.librarianlib.features.gui.provided.book.hierarchy.page.Page
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

/**
 * @author WireSegal
 * Created at 10:19 PM on 2/17/18.
 */
class Entry(val category: Category, rl: String, json: JsonObject) : IBookElement {

    val pages: List<Page>
    val titleKey: String
    val descKey: String
    val icon: JsonElement

    val criterion: ICriterion?

    var isValid = false

    override val bookParent: IBookElement?
        get() = category

    init {
        ENTRIES[ResourceLocation(rl)] = this

        val pages = mutableListOf<Page>()
        val baseKey = category.book.location.resourceDomain + "." +
                category.book.location.resourcePath + "." +
                rl.replace("^.*/(?=\\w+)".toRegex(), "")
        var titleKey = baseKey + ".title"
        var descKey = baseKey + ".description"
        var icon: JsonElement = JsonObject()
        var criterion: ICriterion? = null
        try {
            if (json.has("title"))
                titleKey = json.getAsJsonPrimitive("title").asString
            if (json.has("description"))
                descKey = json.getAsJsonPrimitive("description").asString
            icon = json.get("icon")
            val allPages = json.getAsJsonArray("content")
            allPages.mapNotNullTo(pages) { Page.fromJson(this, it) }
            if (json.has("criteria"))
                criterion = ICriterion.fromJson(json.get("criteria"))
            isValid = true
        } catch (exception: Exception) {
            LibrarianLog.error(exception, "Failed trying to parse an entry component")
        }

        this.pages = pages
        this.titleKey = titleKey
        this.descKey = descKey
        this.icon = icon
        this.criterion = criterion
    }

    fun isUnlocked(player: EntityPlayer): Boolean {
        if (criterion == null) return true

        val event = EntryUnlockedEvent(player, this)
        MinecraftForge.EVENT_BUS.post(event)

        return criterion.isUnlocked(player, event.result)
    }

    @SideOnly(Side.CLIENT)
    override fun createComponent(book: IBookGui): GuiComponent {
        return ComponentEntryPage(book, this)
    }

    companion object {
        val ENTRIES: MutableMap<ResourceLocation, Entry> = mutableMapOf()
    }
}
