package us.timinc.mc.cobblemon.droploottables.condition

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType
import us.timinc.mc.cobblemon.droploottables.DropLootTables
import us.timinc.mc.cobblemon.droploottables.DropLootTables.DataKeys.LootParamKeys.FOCUS_POKEMON
import us.timinc.mc.cobblemon.droploottables.paramextractor.PokemonParamExtractor

@Deprecated("Use the newly improved pokemon_matcher condition, it now accommodates this.")
class NicknameCondition(
    val targetPokemon: ResourceLocation = FOCUS_POKEMON,
    val name: String,
    val caseInsensitive: Boolean = false,
) : LootItemCondition {
    companion object {
        val CODEC: MapCodec<NicknameCondition> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                DropLootTables.RESOURCE_LOCATION_CODEC.optionalFieldOf("target_pokemon", FOCUS_POKEMON)
                    .forGetter(NicknameCondition::targetPokemon),
                Codec.STRING.fieldOf("name")
                    .forGetter(NicknameCondition::name),
                Codec.BOOL.fieldOf("case_insensitive").orElse(false)
                    .forGetter(NicknameCondition::caseInsensitive)
            ).apply(instance, ::NicknameCondition)
        }
    }

    override fun getType(): LootItemConditionType = DropLootTables.LootItemConditionTypes.NICKNAME_CONDITION

    override fun test(ctx: LootContext): Boolean {
        val pokemon = PokemonParamExtractor.getFrom(ctx, targetPokemon) ?: return false
        val nickname = pokemon.nickname ?: return false
        return if (caseInsensitive) nickname.string.equals(name, ignoreCase = true) else nickname.string == name
    }
}