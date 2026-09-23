package us.timinc.mc.cobblemon.droploottables.mixin;

import com.cobblemon.mod.common.api.drop.DropTable;
import com.cobblemon.mod.common.api.pokemon.evolution.Evolution;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import kotlin.ranges.IntRange;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import us.timinc.mc.cobblemon.droploottables.handler.BaseDropCause;
import us.timinc.mc.cobblemon.droploottables.handler.BaseDropCauseScope;

@Mixin(Evolution.class)
public interface EvolutionBaseDropCause {
    @WrapOperation(
        method = "evolutionMethod",
        at = @At(
            value = "INVOKE",
            target = "Lcom/cobblemon/mod/common/api/drop/DropTable;drop$default(Lcom/cobblemon/mod/common/api/drop/DropTable;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/server/level/ServerPlayer;Lkotlin/ranges/IntRange;Lcom/cobblemon/mod/common/pokemon/Pokemon;ILjava/lang/Object;)V"
        ),
        require = 1
    )
    private void markEvolutionBaseDrops(
        DropTable table,
        LivingEntity entity,
        ServerLevel level,
        Vec3 position,
        ServerPlayer player,
        IntRange amount,
        Pokemon pokemon,
        int mask,
        Object marker,
        Operation<Void> original
    ) {
        BaseDropCause cause = new BaseDropCause.Evolution(table, pokemon.getUuid(), player.getUUID());
        BaseDropCauseScope.runWithCause(
            cause,
            () -> original.call(table, entity, level, position, player, amount, pokemon, mask, marker)
        );
    }
}
