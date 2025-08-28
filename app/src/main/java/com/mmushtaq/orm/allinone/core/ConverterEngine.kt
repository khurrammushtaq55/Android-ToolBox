package com.mmushtaq.orm.allinone.core

import kotlin.math.round
import kotlin.math.roundToLong

/** Categories we support (purely offline). */
enum class UnitCategory {
    LENGTH, MASS, TEMPERATURE, AREA, VOLUME, SPEED, TIME, DATA, ANGLE
}

/** Unit description. For linear units, we convert via factor to a base (SI-ish). */
data class UnitDef(
    val key: String,
    val label: String,
    val symbol: String,
    val category: UnitCategory,
    val isLinear: Boolean = true,
    val toBase: (Double) -> Double,   // value in this unit -> value in base unit
    val fromBase: (Double) -> Double  // value in base unit -> value in this unit
)

/** Registry of all units. Base chosen per category:
 * LENGTH: meter, MASS: kilogram, TEMPERATURE: celsius (non-linear), AREA: m², VOLUME: liter,
 * SPEED: m/s, TIME: second, DATA: byte, ANGLE: degree
 */
object ConverterEngine {

    val units: List<UnitDef> = buildList {
        // LENGTH (base: meter)
        addAll(listOf(
            linear("mm", "Millimeter", "mm", UnitCategory.LENGTH, 1e-3),
            linear("cm", "Centimeter", "cm", UnitCategory.LENGTH, 1e-2),
            linear("m",  "Meter", "m", UnitCategory.LENGTH, 1.0),
            linear("km", "Kilometer", "km", UnitCategory.LENGTH, 1e3),
            linear("in", "Inch", "in", UnitCategory.LENGTH, 0.0254),
            linear("ft", "Foot", "ft", UnitCategory.LENGTH, 0.3048),
            linear("yd", "Yard", "yd", UnitCategory.LENGTH, 0.9144),
            linear("mi", "Mile", "mi", UnitCategory.LENGTH, 1609.344),
        ))

        // MASS (base: kilogram)
        addAll(listOf(
            linear("mg", "Milligram", "mg", UnitCategory.MASS, 1e-6),
            linear("g",  "Gram", "g", UnitCategory.MASS, 1e-3),
            linear("kg", "Kilogram", "kg", UnitCategory.MASS, 1.0),
            linear("t",  "Metric Ton", "t", UnitCategory.MASS, 1e3),
            linear("oz", "Ounce", "oz", UnitCategory.MASS, 0.028349523125),
            linear("lb", "Pound", "lb", UnitCategory.MASS, 0.45359237)
        ))

        // TEMPERATURE (base: °C; non-linear)
        addAll(listOf(
            nonlinear("c", "Celsius", "°C", UnitCategory.TEMPERATURE,
                toBase = { it }, fromBase = { it }),
            nonlinear("f", "Fahrenheit", "°F", UnitCategory.TEMPERATURE,
                toBase = { (it - 32.0) * 5.0/9.0 }, fromBase = { it * 9.0/5.0 + 32.0 }),
            nonlinear("k", "Kelvin", "K", UnitCategory.TEMPERATURE,
                toBase = { it - 273.15 }, fromBase = { it + 273.15 })
        ))

        // AREA (base: m²)
        addAll(listOf(
            linear("mm2", "Square Millimeter", "mm²", UnitCategory.AREA, 1e-6),
            linear("cm2", "Square Centimeter", "cm²", UnitCategory.AREA, 1e-4),
            linear("m2",  "Square Meter", "m²", UnitCategory.AREA, 1.0),
            linear("km2", "Square Kilometer", "km²", UnitCategory.AREA, 1e6),
            linear("in2", "Square Inch", "in²", UnitCategory.AREA, 0.00064516),
            linear("ft2", "Square Foot", "ft²", UnitCategory.AREA, 0.09290304),
            linear("yd2", "Square Yard", "yd²", UnitCategory.AREA, 0.83612736),
            linear("ac",  "Acre", "ac", UnitCategory.AREA, 4046.8564224),
            linear("ha",  "Hectare", "ha", UnitCategory.AREA, 10000.0)
        ))

        // VOLUME (base: liter)
        addAll(listOf(
            linear("ml", "Milliliter", "mL", UnitCategory.VOLUME, 1e-3),
            linear("l",  "Liter", "L", UnitCategory.VOLUME, 1.0),
            linear("m3", "Cubic Meter", "m³", UnitCategory.VOLUME, 1000.0),
            linear("in3","Cubic Inch", "in³", UnitCategory.VOLUME, 0.016387064),
            linear("ft3","Cubic Foot", "ft³", UnitCategory.VOLUME, 28.316846592),
            linear("gal","US Gallon", "gal", UnitCategory.VOLUME, 3.785411784),
            linear("qt", "US Quart", "qt", UnitCategory.VOLUME, 0.946352946),
            linear("pt", "US Pint", "pt", UnitCategory.VOLUME, 0.473176473),
            linear("cup","US Cup", "cup", UnitCategory.VOLUME, 0.2365882365)
        ))

        // SPEED (base: m/s)
        addAll(listOf(
            linear("mps", "Meters/second", "m/s", UnitCategory.SPEED, 1.0),
            linear("kmph", "Kilometers/hour", "km/h", UnitCategory.SPEED, 1000.0/3600.0),
            linear("mph", "Miles/hour", "mph", UnitCategory.SPEED, 1609.344/3600.0),
            linear("kn", "Knot", "kn", UnitCategory.SPEED, 1852.0/3600.0)
        ))

        // TIME (base: second)
        addAll(listOf(
            linear("ms", "Millisecond", "ms", UnitCategory.TIME, 1e-3),
            linear("s",  "Second", "s", UnitCategory.TIME, 1.0),
            linear("min","Minute", "min", UnitCategory.TIME, 60.0),
            linear("h",  "Hour", "h", UnitCategory.TIME, 3600.0),
            linear("d",  "Day", "d", UnitCategory.TIME, 86400.0)
        ))

        // DATA (base: byte)
        addAll(listOf(
            linear("b",   "Bit", "b", UnitCategory.DATA, 1.0/8.0),
            linear("B",   "Byte", "B", UnitCategory.DATA, 1.0),
            linear("KB",  "Kilobyte (1000)", "KB", UnitCategory.DATA, 1000.0),
            linear("KiB", "Kibibyte (1024)", "KiB", UnitCategory.DATA, 1024.0),
            linear("MB",  "Megabyte (1000)", "MB", UnitCategory.DATA, 1_000_000.0),
            linear("MiB", "Mebibyte (1024)", "MiB", UnitCategory.DATA, 1024.0*1024),
            linear("GB",  "Gigabyte (1000)", "GB", UnitCategory.DATA, 1_000_000_000.0),
            linear("GiB", "Gibibyte (1024)", "GiB", UnitCategory.DATA, 1024.0*1024*1024)
        ))

        // ANGLE (base: degree)
        addAll(listOf(
            linear("deg", "Degree", "°", UnitCategory.ANGLE, 1.0),
            linear("rad", "Radian", "rad", UnitCategory.ANGLE, 180.0/Math.PI),
            linear("grad","Gradian", "gon", UnitCategory.ANGLE, 0.9)
        ))
    }

    val unitsByCategory: Map<UnitCategory, List<UnitDef>> =
        units.groupBy { it.category }

    fun convert(category: UnitCategory, value: Double, fromKey: String, toKey: String): Double {
        val from = units.first { it.key == fromKey && it.category == category }
        val to = units.first { it.key == toKey && it.category == category }
        val base = from.toBase(value)
        return to.fromBase(base)
    }

    private fun linear(
        key: String,
        label: String,
        symbol: String,
        category: UnitCategory,
        toBaseFactor: Double
    ) = UnitDef(
        key, label, symbol, category, isLinear = true,
        toBase = { it * toBaseFactor },
        fromBase = { it / toBaseFactor }
    )

    private fun nonlinear(
        key: String,
        label: String,
        symbol: String,
        category: UnitCategory,
        toBase: (Double) -> Double,
        fromBase: (Double) -> Double
    ) = UnitDef(key, label, symbol, category, isLinear = false, toBase = toBase, fromBase = fromBase)
}
