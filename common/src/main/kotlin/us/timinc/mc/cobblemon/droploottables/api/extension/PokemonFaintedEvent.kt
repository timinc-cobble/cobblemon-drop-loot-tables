package us.timinc.mc.cobblemon.droploottables.api.extension

import com.cobblemon.mod.common.api.events.pokemon.PokemonFaintedEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.damagesource.DamageSource

fun PokemonFaintedEvent.getDamageSource(): DamageSource? = pokemon.entity?.lastDamageSource

fun PokemonFaintedEvent.getPlayerKillCredit(): ServerPlayer? = pokemon.entity?.killCredit as? ServerPlayer
