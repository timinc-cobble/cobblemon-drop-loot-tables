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
class NatureCondition(
    val targetPokemon: ResourceLocation = FOCUS_POKEMON,
    val natures: List<ResourceLocation>,
) : LootItemCondition {
    companion object {
        val CODEC: MapCodec<NatureCondition> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                DropLootTables.RESOURCE_LOCATION_CODEC.optionalFieldOf("target_pokemon", FOCUS_POKEMON)
                    .forGetter(NatureCondition::targetPokemon),
                COBBLEMON_RESOURCE_LOCATION_CODEC.listOf().fieldOf("natures")
                    .forGetter(NatureCondition::natures),
            ).apply(instance, ::NatureCondition)
        }
    }

    override fun getType(): LootItemConditionType = DropLootTables.LootItemConditionTypes.NATURE_CONDITION

    override fun test(ctx: LootContext): Boolean {
        val pokemon = PokemonParamExtractor.getFrom(ctx, targetPokemon) ?: return false
        return natures.contains(pokemon.nature.name)
    }
}