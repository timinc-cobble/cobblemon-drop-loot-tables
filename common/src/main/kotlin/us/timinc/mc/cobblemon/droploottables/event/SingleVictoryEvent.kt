package us.timinc.mc.cobblemon.droploottables.event

import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor
import com.cobblemon.mod.common.api.events.battles.BattleVictoryEvent
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity

class SingleVictoryEvent(
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
) {
    companion object {
        fun from(evt: BattleVictoryEvent): Array<SingleVictoryEvent> =
            evt.winners.flatMap(BattleActor::pokemonList)
                .map { winnerBattlePokemon ->
                    val winner = winnerBattlePokemon.effectedPokemon
                    val winnerTrainer = winner.getOwnerEntity()
                    val winnerTrainerPlayer = winnerTrainer as? ServerPlayer
                    val losers = evt.losers.flatMap(BattleActor::pokemonList).map(BattlePokemon::effectedPokemon)
                    val losersTrainers = losers.mapNotNull { it.getOwnerEntity() }.toSet()
                    val facedLosers = winnerBattlePokemon.facedOpponents.map(BattlePokemon::effectedPokemon)
                    val facedLosersTrainers = facedLosers.mapNotNull { it.getOwnerEntity() }.toSet()
                    val team =
                        evt.winners.flatMap(BattleActor::pokemonList).filter { it.uuid != winnerBattlePokemon.uuid }
                            .map(BattlePokemon::effectedPokemon)
                    val teamTrainers = team.mapNotNull { it.getOwnerEntity() }.toSet()

                    SingleVictoryEvent(
                        winner,
                        winnerTrainer,
                        winnerTrainerPlayer,
                        losers,
                        losersTrainers,
                        facedLosers,
                        facedLosersTrainers,
                        team,
                        teamTrainers,
                        evt.battle
                    )
                }.toTypedArray()
    }
}