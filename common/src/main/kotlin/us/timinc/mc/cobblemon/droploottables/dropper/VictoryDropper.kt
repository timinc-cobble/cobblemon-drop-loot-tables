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
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity
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
        val winner: Pokemon,
        val winnerTrainer: LivingEntity?,
        val winnerTrainerPlayer: ServerPlayer?,
        val losers: List<Pokemon>,
        val losersTrainers: Set<LivingEntity>,
        val facedLosers: List<Pokemon>,
        val facedLosersTrainers: Set<LivingEntity>,
        val team: List<Pokemon>,
        val teamTrainers: Set<LivingEntity>,
        val battle: PokemonBattle,
    ) : DropContext {
        override fun toLootParams(): LootParams {
            val pos = winner.entity?.let(PokemonEntity::position)
                ?: (battle.losers + battle.winners).flatMap(
                    BattleActor::pokemonList
                ).firstNotNullOfOrNull(BattlePokemon::entity)?.position()
                ?: battle.players.firstOrNull()?.position()

            val params = mutableMapOf<LootContextParam<out Any>, Any>(
                DropLootTables.LootParams.FOCUS_POKEMON to winner,
                DropLootTables.LootParams.DEFEATED_TEAM to losers,
                DropLootTables.LootParams.FACED_DEFEATED_TEAM to facedLosers,
                DropLootTables.LootParams.DEFEATED_TEAM_TRAINERS to losersTrainers,
                DropLootTables.LootParams.FACED_DEFEATED_TEAM_TRAINERS to facedLosersTrainers,
                DropLootTables.LootParams.FOCUS_TEAMMATE_TRAINERS to teamTrainers,
                DropLootTables.LootParams.FOCUS_TEAMMATES to team,
            )
            winner.entity?.let { params[LootContextParams.THIS_ENTITY] = it }
            pos?.let { params[LootContextParams.ORIGIN] = it }
            winnerTrainer?.let { params[DropLootTables.LootParams.FOCUS_TRAINER] = it }
            winnerTrainerPlayer?.let { params[DropLootTables.LootParams.FOCUS_PLAYER] = it }

            return LootParams(
                level,
                params,
                mapOf(),
                winnerTrainerPlayer?.luck ?: 0F
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