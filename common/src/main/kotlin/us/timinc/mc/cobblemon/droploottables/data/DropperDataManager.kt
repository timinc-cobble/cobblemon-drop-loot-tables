package us.timinc.mc.cobblemon.droploottables.data

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.mojang.serialization.JsonOps
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.util.profiling.ProfilerFiller
import us.timinc.mc.cobblemon.droploottables.api.DropContext
import us.timinc.mc.cobblemon.droploottables.api.Dropper
import us.timinc.mc.cobblemon.timcore.AbstractReloadListener

object DropperDataManager : AbstractReloadListener(Gson(), "drop/dropper") {
    private val droppers: MutableMap<ResourceLocation, MutableList<Dropper<out DropContext>>> = mutableMapOf()

    override fun apply(
        objectMap: MutableMap<ResourceLocation, JsonElement>,
        resourceManager: ResourceManager,
        profilerFiller: ProfilerFiller,
    ) {
        droppers.clear()
        objectMap.forEach { (id, json) ->
            val dropper = Dropper.CODEC.parse(JsonOps.INSTANCE, json).orThrow
            dropper.id = id
            droppers.getOrPut(dropper.trigger, ::mutableListOf) += dropper
        }
    }

    fun <C : DropContext, T : Dropper<C>> getValidDroppers(id: ResourceLocation?, context: C): List<T>? =
        droppers[id]?.mapNotNull {
            @Suppress("UNCHECKED_CAST")
            it as? T
        }?.filter { it.canDrop(context) }
}