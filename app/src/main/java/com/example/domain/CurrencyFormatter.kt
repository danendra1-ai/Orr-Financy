package com.example.domain

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * High-precision Indian Rupee (INR) formatter for paise (1 Rupee = 100 Paise).
 * Formats numbers according to Indian numbering grouping:
 * e.g., ₹1,00,000.00, ₹25,500.00, ₹500.00
 */
object CurrencyFormatter {

    /**
     * Formats exact paise into Indian Rupee format.
     * @param paise Amount in integer paise (e.g. 10000000L = ₹1,00,000.00)
     * @param includeSymbol Whether to prefix with ₹ symbol (default true)
     * @param showDecimals Whether to show two decimal places (default true)
     */
    fun formatPaise(
        paise: Long,
        includeSymbol: Boolean = true,
        showDecimals: Boolean = true
    ): String {
        val isNegative = paise < 0
        val absPaise = kotlin.math.abs(paise)
        val rupees = absPaise / 100
        val remainderPaise = absPaise % 100

        val formattedRupees = formatIndianGrouping(rupees)
        val decimalPart = if (showDecimals) {
            String.format(Locale.US, ".%02d", remainderPaise)
        } else ""

        val prefix = if (includeSymbol) "₹" else ""
        val sign = if (isNegative) "-" else ""

        return "$sign$prefix$formattedRupees$decimalPart"
    }

    /**
     * Formats integer rupees with Indian comma grouping:
     * e.g., 100000 -> 1,00,000
     * Last 3 digits grouped, then groups of 2.
     */
    fun formatIndianGrouping(value: Long): String {
        val s = value.toString()
        if (s.length <= 3) return s

        val lastThree = s.substring(s.length - 3)
        val rest = s.substring(0, s.length - 3)

        val sb = StringBuilder()
        var count = 0
        for (i in rest.length - 1 downTo 0) {
            sb.append(rest[i])
            count++
            if (count % 2 == 0 && i > 0) {
                sb.append(',')
            }
        }
        return sb.reverse().toString() + "," + lastThree
    }

    /**
     * Parses user string input (e.g. "1500", "1500.50", "1,500") into exact paise.
     * Returns null if input is invalid or negative.
     */
    fun parseToPaise(input: String): Long? {
        val clean = input.replace("₹", "").replace(",", "").trim()
        if (clean.isEmpty()) return null

        return try {
            val parts = clean.split(".")
            if (parts.size > 2) return null

            val wholeRupees = parts[0].toLongOrNull() ?: return null
            if (wholeRupees < 0) return null

            val paisePart = if (parts.size == 2) {
                val frac = parts[1]
                when {
                    frac.isEmpty() -> 0L
                    frac.length == 1 -> frac.toLong() * 10L
                    frac.length == 2 -> frac.toLong()
                    else -> frac.substring(0, 2).toLong()
                }
            } else 0L

            (wholeRupees * 100L) + paisePart
        } catch (_: Exception) {
            null
        }
    }
}
