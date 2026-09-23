package us.timinc.mc.cobblemon.droploottables.handler

import com.cobblemon.mod.common.api.drop.DropTable
import java.util.UUID

sealed interface BaseDropCause {
    val table: DropTable
    val pokemonUuid: UUID

    data class Evolution(
        override val table: DropTable,
        override val pokemonUuid: UUID,
        val playerUuid: UUID,
    ) : BaseDropCause

    data class PokemonDeath(
        override val table: DropTable,
        override val pokemonUuid: UUID,
        val inBattle: Boolean,
    ) : BaseDropCause
}

object BaseDropCauseScope {
    private val currentCause = ThreadLocal<BaseDropCause?>()

    @JvmStatic
    fun current(): BaseDropCause? = currentCause.get()

    @JvmStatic
    fun runWithCause(cause: BaseDropCause, action: Runnable) {
        val previousCause = currentCause.get()
        currentCause.set(cause)
        try {
            action.run()
        } finally {
            if (previousCause == null) {
                currentCause.remove()
            } else {
                currentCause.set(previousCause)
            }
        }
    }
}
