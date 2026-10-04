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
import kotlin.jvm.optionals.getOrNull

class VictoryDropper(
    override val trigger: ResourceLocation,
    override val lootTables: List<ResourceLocation>,
    override val conditions: List<LootItemCondition>,
    override val dropTarget: ResourceLocation?,
    val battleTypes: List<String> = listOf("pvw"),
) : Dropper<VictoryDropper.Context>() {
    companion object {
        val CODEC: MapCodec<VictoryDropper> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                CodecPieces.getTrigger(VictoryDropper::trigger),
                CodecPieces.getTables(VictoryDropper::lootTables),
                CodecPieces.getConditions(VictoryDropper::conditions),
                CodecPieces.getDropTarget(VictoryDropper::dropTarget),
                Codec.STRING.listOf().optionalFieldOf("battle_types", listOf("pvw"))
                    .forGetter(VictoryDropper::battleTypes),
            ).apply(instance) { trigger, lootTables, conditions, dropTarget, battleTypes ->
                VictoryDropper(
                    trigger,
                    lootTables,
                    conditions,
                    dropTarget.getOrNull(),
                    battleTypes,
                )
            }
        }

        val DROPPER_TYPE = DropperType(CODEC)
    }

    override fun getType(): DropperType<*, *> = DropLootTables.DropperTypes.VICTORY

    class Context(
        override val level: ServerLevel,
        val focusPokemon: Pokemon,
        val defeatedPokemon: Pokemon,
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
                DropLootTables.LootParams.DEFEATED_POKEMON to defeatedPokemon,
            )
            val focusSide = battle.actors.firstOrNull { actor ->
                actor.pokemonList.any { it.effectedPokemon.uuid == focusPokemon.uuid }
            }?.getSide()
            focusSide?.actors?.flatMap(BattleActor::pokemonList)?.map(BattlePokemon::effectedPokemon)?.let {
                params[DropLootTables.LootParams.FOCUS_TEAM] = it
            }
            focusSide?.getOppositeSide()?.actors?.flatMap(BattleActor::pokemonList)
                ?.map(BattlePokemon::effectedPokemon)?.let {
                    params[DropLootTables.LootParams.DEFEATED_TEAM] = it
                }
            focusPokemon.entity?.let { params[LootContextParams.THIS_ENTITY] = it }
            focusPokemon.getOwnerPlayer()?.let { params[DropLootTables.LootParams.FOCUS_PLAYER] = it }
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
            && super.canDrop(context)
}
