package us.timinc.mc.cobblemon.droploottables.dropper

import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.level.storage.loot.parameters.LootContextParam
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition
import us.timinc.mc.cobblemon.droploottables.DropLootTables
import us.timinc.mc.cobblemon.droploottables.api.DropContext
import us.timinc.mc.cobblemon.droploottables.api.Dropper
import us.timinc.mc.cobblemon.droploottables.api.Dropper.Companion.CodecPieces
import us.timinc.mc.cobblemon.droploottables.api.DropperType
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

class DefeatedDropper(
    override val trigger: ResourceLocation,
    override val lootTables: List<ResourceLocation>,
    override val conditions: List<LootItemCondition>,
    override val dropTarget: ResourceLocation?,
    val battleTypes: List<String> = listOf("pvw"),
    val preserveBaseDrops: Boolean = false,
    val isWild: Boolean? = null,
) : Dropper<DefeatedDropper.Context>() {
    companion object {
        val CODEC: MapCodec<DefeatedDropper> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                CodecPieces.getTrigger(DefeatedDropper::trigger),
                CodecPieces.getTables(DefeatedDropper::lootTables),
                CodecPieces.getConditions(DefeatedDropper::conditions),
                CodecPieces.getDropTarget(DefeatedDropper::dropTarget),
                Codec.STRING.listOf().optionalFieldOf("battle_types", listOf("pvw"))
                    .forGetter(DefeatedDropper::battleTypes),
                Codec.BOOL.optionalFieldOf("preserve_base_drops", false)
                    .forGetter(DefeatedDropper::preserveBaseDrops),
                Codec.BOOL.optionalFieldOf("is_wild").forGetter { Optional.ofNullable(it.isWild) },
            ).apply(instance) { trigger, lootTables, conditions, dropTarget, battleTypes, preserveBaseDrops, isWild ->
                DefeatedDropper(
                    trigger,
                    lootTables,
                    conditions,
                    dropTarget.getOrNull(),
                    battleTypes,
                    preserveBaseDrops,
                    isWild.getOrNull(),
                )
            }
        }

        val DROPPER_TYPE = DropperType(CODEC)
    }

    override fun getType(): DropperType<*, *> = DropLootTables.DropperTypes.DEFEATED

    class Context(
        override val level: ServerLevel,
        val focusPokemon: Pokemon,
        val defeatingPokemon: Pokemon,
        val battle: PokemonBattle,
    ) : DropContext {
        override fun toLootParams(): LootParams {
            val pos = focusPokemon.entity?.let(PokemonEntity::position)
                ?: (battle.losers + battle.winners).flatMap(
                    BattleActor::pokemonList
                ).firstNotNullOfOrNull(BattlePokemon::entity)?.position()
                ?: battle.players.firstOrNull()?.position()

            val params = mutableMapOf<LootContextParam<out Any>, Any>(
                DropLootTables.LootParams.FOCUS_POKEMON to focusPokemon,
                DropLootTables.LootParams.DEFEATING_POKEMON to defeatingPokemon,
            )
            focusPokemon.entity?.let { params[LootContextParams.THIS_ENTITY] = it }
            pos?.let { params[LootContextParams.ORIGIN] = it }

            return LootParams(
                level,
                params,
                mapOf(),
                focusPokemon.getOwnerPlayer()?.luck ?: 0F
            )
        }
    }

    override fun canDrop(context: Context): Boolean = (
            (battleTypes.contains("pvw") && context.battle.isPvW)
                    || (battleTypes.contains("pvn") && context.battle.isPvN)
                    || (battleTypes.contains("pvp") && context.battle.isPvP)
            )
            && (isWild?.let { it == context.focusPokemon.isWild() } ?: true)
            && super.canDrop(context)
}