package us.timinc.mc.cobblemon.droploottables.dropper

import com.cobblemon.mod.common.util.party
import com.cobblemon.mod.common.pokemon.Pokemon
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.level.storage.loot.parameters.LootContextParam
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition
import us.timinc.mc.cobblemon.droploottables.DropLootTables
import us.timinc.mc.cobblemon.droploottables.api.DropContext
import us.timinc.mc.cobblemon.droploottables.api.Dropper
import us.timinc.mc.cobblemon.droploottables.api.Dropper.Companion.CodecPieces
import us.timinc.mc.cobblemon.droploottables.api.DropperType
import kotlin.jvm.optionals.getOrNull

class ResurrectedDropper(
    override val trigger: ResourceLocation,
    override val lootTables: List<ResourceLocation>,
    override val conditions: List<LootItemCondition>,
    override val dropTarget: ResourceLocation?,
) : Dropper<ResurrectedDropper.Context>() {
    companion object {
        val CODEC: MapCodec<ResurrectedDropper> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                CodecPieces.getTrigger(ResurrectedDropper::trigger),
                CodecPieces.getTables(ResurrectedDropper::lootTables),
                CodecPieces.getConditions(ResurrectedDropper::conditions),
                CodecPieces.getDropTarget(ResurrectedDropper::dropTarget),
            ).apply(instance) { trigger, lootTables, conditions, dropTarget ->
                ResurrectedDropper(
                    trigger,
                    lootTables,
                    conditions,
                    dropTarget.getOrNull(),
                )
            }
        }

        val DROPPER_TYPE = DropperType(CODEC)
    }

    override fun getType(): DropperType<*, *> = DropLootTables.DropperTypes.RESURRECTED

    class Context(
        override val level: ServerLevel,
        val focusPokemon: Pokemon,
        val focusPlayer: ServerPlayer?,
    ) : DropContext {
        override fun toLootParams(): LootParams {
            val params = mutableMapOf<LootContextParam<*>, Any>(
                DropLootTables.LootParams.FOCUS_POKEMON to focusPokemon
            )
            val origin = focusPokemon.entity?.position() ?: focusPlayer?.position()
            origin?.let { params[LootContextParams.ORIGIN] = it }
            focusPlayer?.let {
                params[DropLootTables.LootParams.FOCUS_PLAYER] = it
                params[DropLootTables.LootParams.FOCUS_TEAM] = it.party().toList()
            }
            return LootParams(level, params, mapOf(), focusPlayer?.luck ?: 0F)
        }
    }
}
