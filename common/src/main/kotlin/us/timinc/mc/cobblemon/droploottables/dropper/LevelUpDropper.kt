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

class LevelUpDropper(
    override val trigger: ResourceLocation,
    override val lootTables: List<ResourceLocation>,
    override val conditions: List<LootItemCondition>,
    override val dropTarget: ResourceLocation?,
    val level: Int,
) : Dropper<LevelUpDropper.Context>() {
    companion object {
        val CODEC: MapCodec<LevelUpDropper> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                CodecPieces.getTrigger(LevelUpDropper::trigger),
                CodecPieces.getTables(LevelUpDropper::lootTables),
                CodecPieces.getConditions(LevelUpDropper::conditions),
                CodecPieces.getDropTarget(LevelUpDropper::dropTarget),
                Codec.INT.fieldOf("level").forGetter(LevelUpDropper::level)
            ).apply(instance) { trigger, lootTables, conditions, dropTarget, level ->
                LevelUpDropper(
                    trigger,
                    lootTables,
                    conditions,
                    dropTarget.getOrNull(),
                    level,
                )
            }
        }

        val DROPPER_TYPE = DropperType(CODEC)
    }

    override fun getType(): DropperType<*, *> = DropLootTables.DropperTypes.LEVELED

    class Context(
        override val level: ServerLevel,
        val fromLevel: Int,
        val toLevel: Int,
        val focusPokemon: Pokemon,
        val focusPlayer: ServerPlayer,
    ) : DropContext {
        override fun toLootParams(): LootParams = LootParams(
            level,
            mapOf(
                LootContextParams.ORIGIN to focusPlayer.position(),
                DropLootTables.LootParams.FOCUS_POKEMON to focusPokemon,
                DropLootTables.LootParams.FOCUS_PLAYER to focusPlayer,
                DropLootTables.LootParams.FOCUS_TEAM to focusPlayer.party().toList(),
            ),
            mapOf(),
            focusPlayer.luck
        )
    }

    override fun canDrop(context: Context): Boolean =
        super.canDrop(context)
                && level > context.fromLevel
                && level <= context.toLevel
}