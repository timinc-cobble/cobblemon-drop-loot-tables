package us.timinc.mc.cobblemon.droploottables.condition

import com.mojang.brigadier.StringReader
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.commands.arguments.item.ItemParser
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType
import us.timinc.mc.cobblemon.droploottables.DropLootTables
import us.timinc.mc.cobblemon.droploottables.DropLootTables.DataKeys.LootParamKeys.FOCUS_POKEMON
import us.timinc.mc.cobblemon.droploottables.paramextractor.PokemonParamExtractor

@Deprecated ("Use the newly improved pokemon_matcher condition, it now accommodates this.")
class HeldItemCondition(
    val targetPokemon: ResourceLocation = FOCUS_POKEMON,
    val item: String,
) : LootItemCondition {
    companion object {
        val CODEC: MapCodec<HeldItemCondition> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                ResourceLocation.CODEC.optionalFieldOf("target_pokemon", FOCUS_POKEMON)
                    .forGetter(HeldItemCondition::targetPokemon),
                Codec.STRING.fieldOf("item")
                    .forGetter(HeldItemCondition::item)
            ).apply(instance, ::HeldItemCondition)
        }
    }

    override fun getType(): LootItemConditionType = DropLootTables.LootItemConditionTypes.HELD_ITEM_CONDITION

    override fun test(ctx: LootContext): Boolean {
        val pokemon = PokemonParamExtractor.getFrom(ctx, targetPokemon) ?: return false

        val heldItem = pokemon.heldItem()
        if (heldItem.isEmpty) return false

        val parser = ItemParser(ctx.level.server.registryAccess())
        val result = parser.parse(StringReader(item))

        if (!heldItem.`is`(result.item)) return false

        for (entry in result.components.entrySet()) {
            val targetPropValue = heldItem.get(entry.key)
            if (targetPropValue != entry.value.get()) return false
        }

        return true
    }
}