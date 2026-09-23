package us.timinc.mc.cobblemon.droploottables.mixin;

import com.cobblemon.mod.common.api.drop.DropEntry;
import com.cobblemon.mod.common.api.drop.DropTable;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.entity.pokemon.PokemonServerDelegate;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import us.timinc.mc.cobblemon.droploottables.handler.BaseDropCause;
import us.timinc.mc.cobblemon.droploottables.handler.BaseDropCauseScope;

@Mixin(PokemonServerDelegate.class)
public abstract class PokemonDeathBaseDropCause {
    @WrapOperation(
        method = "doDeathDrops",
        at = @At(
            value = "INVOKE",
            target = "Lcom/cobblemon/mod/common/api/drop/DropTable;postLootDroppedEvent(Ljava/util/List;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/server/level/ServerPlayer;)V"
        ),
        require = 1
    )
    private void markPokemonDeathBaseDrops(
        DropTable table,
        List<DropEntry> drops,
        LivingEntity entity,
        ServerLevel level,
        Vec3 position,
        ServerPlayer player,
        Operation<Void> original
    ) {
        PokemonEntity pokemonEntity = (PokemonEntity) entity;
        BaseDropCause cause = new BaseDropCause.PokemonDeath(
            table,
            pokemonEntity.getPokemon().getUuid(),
            pokemonEntity.getBattle() != null
        );
        BaseDropCauseScope.runWithCause(
            cause,
            () -> original.call(table, drops, entity, level, position, player)
        );
    }
}
