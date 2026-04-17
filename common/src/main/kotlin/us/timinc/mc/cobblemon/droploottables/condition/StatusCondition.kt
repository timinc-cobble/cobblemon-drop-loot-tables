package us.timinc.mc.cobblemon.droploottables.condition

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType
import us.timinc.mc.cobblemon.droploottables.COBBLEMON_RESOURCE_LOCATION_CODEC
import us.timinc.mc.cobblemon.droploottables.DropLootTables
import us.timinc.mc.cobblemon.droploottables.DropLootTables.DataKeys.LootParamKeys.FOCUS_POKEMON
import us.timinc.mc.cobblemon.droploottables.paramextractor.PokemonParamExtractor

@Deprecated("Use the newly improved pokemon_matcher condition, it now accommodates this.")
class StatusCondition(
    val targetPokemon: ResourceLocation = FOCUS_POKEMON,
    val statuses: List<ResourceLocation>,
) : LootItemCondition {
    companion object {
        val CODEC: MapCodec<StatusCondition> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                DropLootTables.RESOURCE_LOCATION_CODEC.optionalFieldOf("target_pokemon", FOCUS_POKEMON)
                    .forGetter(StatusCondition::targetPokemon),
                COBBLEMON_RESOURCE_LOCATION_CODEC.listOf().fieldOf("status")
                    .forGetter(StatusCondition::statuses),
            ).apply(instance, ::StatusCondition)
        }
    }

    override fun getType(): LootItemConditionType = DropLootTables.LootItemConditionTypes.STATUS_CONDITION

    override fun test(ctx: LootContext): Boolean {
        val pokemon = PokemonParamExtractor.getFrom(ctx, targetPokemon) ?: return false
        val pokemonStatus = pokemon.status ?: return false
        return statuses.contains(pokemonStatus.status.name)
    }
}