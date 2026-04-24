package us.timinc.mc.cobblemon.droploottables.api

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition
import java.util.Optional

abstract class Dropper<T : DropContext> {
    companion object {
        val CODEC: Codec<Dropper<*>> =
            DropperType.REGISTRY.byNameCodec()
                .dispatch("trigger", Dropper<*>::getType) {
                    @Suppress("UNCHECKED_CAST")
                    it.codec as MapCodec<Dropper<*>>
                }

        object CodecPieces {
            fun <T : Dropper<*>> getTrigger(getter: (dropper: T) -> ResourceLocation): RecordCodecBuilder<T, ResourceLocation> =
                ResourceLocation.CODEC.fieldOf("trigger").forGetter(getter)

            fun <T : Dropper<*>> getTables(getter: (dropper: T) -> List<ResourceLocation>): RecordCodecBuilder<T, List<ResourceLocation>> =
                ResourceLocation.CODEC.listOf().optionalFieldOf("tables", emptyList()).forGetter(getter)

            fun <T : Dropper<*>> getConditions(getter: (dropper: T) -> List<LootItemCondition>): RecordCodecBuilder<T, List<LootItemCondition>> =
                LootItemCondition.DIRECT_CODEC.listOf().optionalFieldOf("conditions", emptyList()).forGetter(getter)

            fun <T : Dropper<*>> getDropTarget(getter: (dropper: T) -> ResourceLocation?): RecordCodecBuilder<T, Optional<ResourceLocation>> =
                ResourceLocation.CODEC.optionalFieldOf("drop_target").forGetter { Optional.ofNullable(getter(it)) }
        }
    }

    var id: ResourceLocation? = null

    abstract val trigger: ResourceLocation
    abstract val lootTables: List<ResourceLocation>
    abstract val conditions: List<LootItemCondition>
    abstract val dropTarget: ResourceLocation?
    abstract fun getType(): DropperType<*, *>
    open fun canDrop(context: T): Boolean = conditions.all {
        it.test(LootContext.Builder(context.toLootParams()).create(Optional.empty()))
    }
}