/*
 * Copyright (c) 2017 eilslabs.
 *
 * Distributed under the MIT License (license terms are at https://www.github.com/eilslabs/Roddy/LICENSE.txt).
 */

package de.dkfz.roddy.tools

/**
 * An Integer value representing sizes for e.g. files or memory units.
 * The values are internally aligned to Byte and use the BufferUnit to align the input value.
 * However, the default input BU is GigaByte, the default output BufferUnit is MegaByte (e.g. when no unit could be parsed)
 *
 * Values lower than M (k) will still be displayed as e.g. 3K as there is no support for floating point.
 * However floating point values can be parsed and will be rounded to their Integers.
 * Created by heinold on 10.11.15.
 */
class BufferValue {

/**
     * The stored value aligned to KiloByte
     */
    private Long alignedValue;

    private BufferUnit baseUnit = BufferUnit.K;

    public BufferValue(String inputValue, BufferUnit defaultInputUnit = BufferUnit.G, BufferUnit baseUnit = BufferUnit.K) {
        this.baseUnit = baseUnit;
        if (inputValue.size() == 0) {
            inputValue = "1024M";
        }

        //Handle to short strings containing only a unit
        if (inputValue.size() == 1 && !inputValue.isNumber())
            inputValue = "1" + inputValue;

        // Check, if the last character is not a number
        if (inputValue[-1].isNumber()) {
            inputValue += defaultInputUnit.name();
        }

        // Check, if only one character is there.
        if (!inputValue[0..-2].isNumber()) {
            // Uh oh, that is the worst case... Raise an error
            throw new NumberFormatException("$inputValue is not a correct number string. There must be a number followed by at maximum one unit character.")
        }

        String inputUnit = inputValue[-1];
        Float inputNumber = inputValue[0..-2].toFloat();

        def collectionOfBufferValueNames = BufferUnit.values().collect { it -> it.name() };

        if (!collectionOfBufferValueNames.contains(inputUnit)) {
            throw new NumberFormatException("$inputValue is not a correct number string. The unit of the number is not known.")
        }

        alignedValue = inputNumber * ((BufferUnit) inputUnit).multiplier / baseUnit.multiplier;
    }

    public BufferValue(Integer value, BufferUnit unit = BufferUnit.G, BufferUnit baseUnit = BufferUnit.K) {
        this.alignedValue = (value * unit.multiplier / baseUnit.multiplier) as Long;
        this.baseUnit = baseUnit;
    }

    public BufferValue(Float value, BufferUnit unit = BufferUnit.G, BufferUnit baseUnit = BufferUnit.K) {
        this.alignedValue = (value * unit.multiplier / baseUnit.multiplier) as Long;
        this.baseUnit = baseUnit;
    }

    String toString(BufferUnit unit = BufferUnit.M) {
        return toStringWithUnit(unit)
    }

    String toStringWithUnit(BufferUnit unit = BufferUnit.M) {
        if (alignedValue * baseUnit.multiplier < unit.multiplier) {
            return "" + alignedValue + baseUnit.name();
        }
        return "" + (alignedValue * baseUnit.multiplier / unit.multiplier as Long) + unit.name()
    }

    String toResourceStringWithoutUnit(BufferUnit unit = BufferUnit.M) {
        if (alignedValue * baseUnit.multiplier < unit.multiplier) {
            return "" + alignedValue
        }
        return "" + (alignedValue * baseUnit.multiplier / unit.multiplier as Long)
    }

    public Long toLong(BufferUnit unit = BufferUnit.M) {
        if (alignedValue < unit.multiplier)
            return alignedValue
        return alignedValue * baseUnit.multiplier / unit.multiplier as Long
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        BufferValue that = (BufferValue) o

        if (alignedValue != that.alignedValue) return false
        if (baseUnit != that.baseUnit) return false

        return true
    }

    int hashCode() {
        int result
        result = (alignedValue != null ? alignedValue.hashCode() : 0)
        result = 31 * result + (baseUnit != null ? baseUnit.hashCode() : 0)
        return result
    }
}
