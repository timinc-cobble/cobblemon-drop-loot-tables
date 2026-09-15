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
class TeraTypeCondition(
    val targetPokemon: ResourceLocation = FOCUS_POKEMON,
    val types: List<ResourceLocation>,
) : LootItemCondition {
    companion object {
        val CODEC: MapCodec<TeraTypeCondition> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                DropLootTables.RESOURCE_LOCATION_CODEC.optionalFieldOf("target_pokemon", FOCUS_POKEMON)
                    .forGetter(TeraTypeCondition::targetPokemon),
                COBBLEMON_RESOURCE_LOCATION_CODEC.listOf().fieldOf("type")
                    .forGetter(TeraTypeCondition::types),
            ).apply(instance, ::TeraTypeCondition)
        }
    }

    override fun getType(): LootItemConditionType = DropLootTables.LootItemConditionTypes.TERA_TYPE_CONDITION

    override fun test(ctx: LootContext): Boolean {
        val pokemon = PokemonParamExtractor.getFrom(ctx, targetPokemon) ?: return false
        return types.contains(pokemon.teraType.id)
    }
}