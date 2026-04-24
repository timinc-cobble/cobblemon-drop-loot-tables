package us.timinc.mc.cobblemon.droploottables.dropper

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.playSoundServer
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition
import net.minecraft.world.phys.Vec3
import us.timinc.mc.cobblemon.droploottables.DropLootTables
import us.timinc.mc.cobblemon.droploottables.api.DropContext
import us.timinc.mc.cobblemon.droploottables.api.Dropper
import us.timinc.mc.cobblemon.droploottables.api.Dropper.Companion.CodecPieces
import us.timinc.mc.cobblemon.droploottables.api.DropperType
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

class TickedDropper(
    override val trigger: ResourceLocation,
    override val lootTables: List<ResourceLocation>,
    override val conditions: List<LootItemCondition>,
    override val dropTarget: ResourceLocation?,
    val ticks: Int,
    val isWild: Boolean? = null,
    val sound: SoundDescription? = null,
) : Dropper<TickedDropper.Context>() {
    companion object {
        val CODEC: MapCodec<TickedDropper> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                CodecPieces.getTrigger(TickedDropper::trigger),
                CodecPieces.getTables(TickedDropper::lootTables),
                CodecPieces.getConditions(TickedDropper::conditions),
                CodecPieces.getDropTarget(TickedDropper::dropTarget),
                Codec.INT.fieldOf("ticks").forGetter(TickedDropper::ticks),
                Codec.BOOL.optionalFieldOf("is_wild").forGetter { Optional.ofNullable(it.isWild) },
                SoundDescription.CODEC.optionalFieldOf("sound").forGetter { Optional.ofNullable(it.sound) }
            ).apply(instance) { trigger, lootTables, conditions, dropTarget, ticks, isWild, sound ->
                if (ticks <= 0) throw Exception("Ticks must be a positive number.")
                TickedDropper(
                    trigger,
                    lootTables,
                    conditions,
                    dropTarget.getOrNull(),
                    ticks,
                    isWild.getOrNull(),
                    sound.getOrNull(),
                )
            }
        }

        val DROPPER_TYPE = DropperType(CODEC)
    }

    override fun getType(): DropperType<*, *> = DropLootTables.DropperTypes.TICKED
    override fun canDrop(context: Context): Boolean =
        !context.pokemonEntity.isBusy
                && context.pokemonEntity.ticksLived % ticks == 0
                && (isWild?.let { it == context.focusPokemon.isWild() } ?: true)
                && super.canDrop(context)

    class Context(
        override val level: ServerLevel,
        val focusPokemon: Pokemon,
        val pokemonEntity: PokemonEntity,
    ) : DropContext {
        companion object {
            fun fromEntity(entity: PokemonEntity): Context = Context(
                entity.level() as ServerLevel,
                entity.pokemon,
                entity
            )
        }

        override fun toLootParams(): LootParams = LootParams(
            level,
            mapOf(
                LootContextParams.ORIGIN to pokemonEntity.position(),
                LootContextParams.THIS_ENTITY to pokemonEntity,
                DropLootTables.LootParams.FOCUS_POKEMON to focusPokemon
            ),
            mapOf(),
            0F
        )
    }

    data class SoundDescription(
        val sound: ResourceLocation,
        val volume: Float = 1F,
        val pitch: Float = 1F,
        val source: String = SoundSource.NEUTRAL.name,
    ) {
        companion object {
            val CODEC: Codec<SoundDescription> = RecordCodecBuilder.create { instance ->
                instance.group(
                    ResourceLocation.CODEC.fieldOf("sound").forGetter(SoundDescription::sound),
                    Codec.FLOAT.optionalFieldOf("volume", 1F).forGetter(SoundDescription::volume),
                    Codec.FLOAT.optionalFieldOf("pitch", 1F).forGetter(SoundDescription::pitch),
                    Codec.STRING.optionalFieldOf("source", SoundSource.NEUTRAL.name).forGetter(SoundDescription::source)
                ).apply(instance, ::SoundDescription)
            }
        }

        fun emit(level: ServerLevel, position: Vec3) {
            val soundEvent = SoundEvent.createVariableRangeEvent(sound)
            level.playSoundServer(
                position,
                soundEvent,
                SoundSource.valueOf(source),
                volume,
                pitch,
            )
        }
    }
}
