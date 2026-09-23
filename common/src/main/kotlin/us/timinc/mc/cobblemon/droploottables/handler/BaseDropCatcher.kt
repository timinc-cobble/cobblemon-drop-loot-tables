package us.timinc.mc.cobblemon.droploottables.handler

import com.cobblemon.mod.common.api.events.drops.LootDroppedEvent
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import us.timinc.mc.cobblemon.timcore.AbstractHandler

object BaseDropCatcher : AbstractHandler<LootDroppedEvent>() {
    override fun handle(evt: LootDroppedEvent) {
        when (val cause = BaseDropCauseScope.current()) {
            is BaseDropCause.Evolution -> {
                if (evt.table !== cause.table || evt.player?.uuid != cause.playerUuid) return
                val pokemonEntity = evt.entity
                if (pokemonEntity != null &&
                    (pokemonEntity !is PokemonEntity || pokemonEntity.pokemon.uuid != cause.pokemonUuid)
                ) return

                EvolvedHandler.baseDrops[cause.pokemonUuid] = evt.drops
                evt.cancel()
            }

            is BaseDropCause.PokemonDeath -> {
                val pokemonEntity = evt.entity as? PokemonEntity ?: return
                if (evt.table !== cause.table || pokemonEntity.pokemon.uuid != cause.pokemonUuid) return

                if (cause.inBattle) {
                    DefeatedHandler.baseDrops[cause.pokemonUuid] = evt.drops
                } else {
                    KilledHandler.baseDrops[cause.pokemonUuid] = evt.drops
                }
                evt.cancel()
            }

            null -> return
        }
    }
}
