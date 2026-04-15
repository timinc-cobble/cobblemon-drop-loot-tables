package us.timinc.mc.cobblemon.droploottables

import com.cobblemon.mod.common.util.math.FloatRange

fun toFloatRange(str: String): FloatRange {
    val (start, end) = str.split("..")

    return try {
        val actualStart = when (start.lowercase()) {
            "min" -> Float.MIN_VALUE
            else -> start.toFloat()
        }
        val actualEnd = when (end.lowercase()) {
            "max" -> Float.MAX_VALUE
            else -> end.toFloat()
        }
        FloatRange(actualStart, actualEnd)
    } catch (e: NumberFormatException) {
        throw IllegalArgumentException("'$start' and/or '$end' is/are not Floats", e)
    }
}