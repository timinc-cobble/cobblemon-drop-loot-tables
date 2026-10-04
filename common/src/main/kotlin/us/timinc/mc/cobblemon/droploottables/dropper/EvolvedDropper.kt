package us.timinc.mc.cobblemon.droploottables.dropper

import com.cobblemon.mod.common.util.party
import com.cobblemon.mod.common.pokemon.Pokemon
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition
import us.timinc.mc.cobblemon.droploottables.DropLootTables
import us.timinc.mc.cobblemon.droploottables.api.DropContext
import us.timinc.mc.cobblemon.droploottables.api.Dropper
import us.timinc.mc.cobblemon.droploottables.api.Dropper.Companion.CodecPieces
import us.timinc.mc.cobblemon.droploottables.api.DropperType
import kotlin.jvm.optionals.getOrNull

class EvolvedDropper(
    override val trigger: ResourceLocation,
    override val lootTables: List<ResourceLocation>,
    override val conditions: List<LootItemCondition>,
    override val dropTarget: ResourceLocation?,
    val preserveBaseDrops: Boolean = false,
) : Dropper<EvolvedDropper.Context>() {
    companion object {
        val CODEC: MapCodec<EvolvedDropper> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                CodecPieces.getTrigger(EvolvedDropper::trigger),
                CodecPieces.getTables(EvolvedDropper::lootTables),
                CodecPieces.getConditions(EvolvedDropper::conditions),
                CodecPieces.getDropTarget(EvolvedDropper::dropTarget),
                Codec.BOOL.optionalFieldOf("preserve_base_drops", false)
                    .forGetter(EvolvedDropper::preserveBaseDrops),
            ).apply(instance) { trigger, lootTables, conditions, dropTarget, preserveBaseDrops ->
                EvolvedDropper(
                    trigger,
                    lootTables,
                    conditions,
                    dropTarget.getOrNull(),
                    preserveBaseDrops,
                )
            }
        }

        val DROPPER_TYPE = DropperType(CODEC)
    }

    override fun getType(): DropperType<*, *> = DropLootTables.DropperTypes.EVOLVED

    class Context(
        override val level: ServerLevel,
        val focusPokemon: Pokemon,
        val focusPlayer: ServerPlayer,
        val previousPokemon: Pokemon,
    ) : DropContext {
        override fun toLootParams(): LootParams = LootParams(
            level,
            mapOf(
                LootContextParams.ORIGIN to focusPlayer.position(),
                LootContextParams.THIS_ENTITY to focusPokemon.entity,
                DropLootTables.LootParams.FOCUS_POKEMON to focusPokemon,
                DropLootTables.LootParams.FOCUS_PLAYER to focusPlayer,
                DropLootTables.LootParams.FOCUS_TEAM to focusPlayer.party().toList(),
                DropLootTables.LootParams.PREVIOUS_POKEMON to previousPokemon,
            ),
            mapOf(),
            focusPlayer.luck
        )
    }
}
