package us.timinc.mc.cobblemon.droploottables.condition

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition
import us.timinc.mc.cobblemon.droploottables.DropLootTables
import us.timinc.mc.cobblemon.droploottables.paramextractor.PokemonParamExtractor
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType
import us.timinc.mc.cobblemon.droploottables.DropLootTables.DataKeys.LootParamKeys.FOCUS_POKEMON

@Deprecated ("Use the newly improved pokemon_matcher condition, it now accommodates this.")
class GmaxCondition(
    val targetPokemon: ResourceLocation = FOCUS_POKEMON,
    val value: Boolean = true,
) : LootItemCondition {
    companion object {
        val CODEC: MapCodec<GmaxCondition> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                ResourceLocation.CODEC.optionalFieldOf("target_pokemon", FOCUS_POKEMON)
                    .forGetter(GmaxCondition::targetPokemon),
                Codec.BOOL.fieldOf("value").orElse(true)
                    .forGetter(GmaxCondition::value)
            ).apply(instance, ::GmaxCondition)
        }
    }

    override fun getType(): LootItemConditionType = DropLootTables.LootItemConditionTypes.GMAX_CONDITION

    override fun test(ctx: LootContext): Boolean {
        val pokemon = PokemonParamExtractor.getFrom(ctx, targetPokemon) ?: return false
        return pokemon.gmaxFactor == value
    }
}