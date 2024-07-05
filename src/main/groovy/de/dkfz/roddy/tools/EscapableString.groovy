/*
 * Copyright (c) 2024 German Cancer Research Center (DKFZ).
 *
 * Distributed under the MIT License (license terms are at https://www.github.com/eilslabs/Roddy/LICENSE.txt).
 */
package de.dkfz.roddy.tools

import com.google.common.base.Preconditions
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.PackageScope
import org.apache.commons.text.StringEscapeUtils
import org.jetbrains.annotations.NotNull


/** EscapableString implements a Composite pattern. **/
@CompileStatic
abstract class EscapableString {

    /** Compose escapable strings into a tree-like structure. **/
    ConcatenatedString plus(@NotNull EscapableString other) {
        Preconditions.checkArgument(other != null)
        new ConcatenatedString([this, other]).simplify()
    }

    /** Convenience method to automatically cast left operands of `+` to UnescapedString. **/
    ConcatenatedString plus(@NotNull String other) {
        Preconditions.checkArgument(other != null)
        this + new UnescapedString(other)
    }

    /** Call `someEscapable.escaped` to add a level of escaping. **/
    abstract EscapableString getEscaped()

    /** Get the raw size (i.e. without any escapes that may get added. */
    abstract int size()

    /** Interpret the string using an interpreter. **/
    abstract String interpreted(EscapableStringInterpreter interpreter)

    /** Simplify. This is mostly to reduce nesting with ConcatenatedString. The identity of
     *  UnescapedString and EscapedString will not be changed. Therefore, the default is
     *  *not* to do any simplification.
     */
    protected EscapableString simplify() {
        this
    }

    /** Nested class with only static methods get a light-weight DSL-like syntax. **/
    class Shortcuts {

        // Composition functions.

        // e = escaped string
        static EscapableString e(@NotNull EscapableString value) {
            Preconditions.checkArgument(value != null)
            value.escaped
        }


        static EscapedString e(@NotNull String value) {
            u(value).escaped
        }

        // u = unescaped string
        static EscapableString u(@NotNull EscapableString value) {
            Preconditions.checkArgument(value != null)
            // No-op
            value
        }

        static UnescapedString u(@NotNull String value) {
            new UnescapedString(value)
        }

        // c = concatenate
        static ConcatenatedString c(@NotNull List<EscapableString> values = []) {
            Preconditions.checkArgument(values != null)
            new ConcatenatedString(values).simplify()
        }

        static ConcatenatedString c(@NotNull EscapableString... values) {
            c(values as List<EscapableString>)
        }

        /** This is just a workaround. Ideally, there would be a `.join()` method attached to
         *  List<EscapableString>. As Groovy does not have type classes, the only option
         *  seems to be some compile-time metaprogramming, and honestly, it's currently not worth
         *  the effort.
         */
        static ConcatenatedString join(List<EscapableString> list, EscapableString separator) {
            list.inject(c(), { acc, v ->
                if (acc == c()) {
                    c(v)
                } else if (v == c()) {
                    acc
                } else {
                    acc + separator + v
                }
            })
        }
        static ConcatenatedString join(List<EscapableString> list, String separator) {
            join(list, u(separator))
        }

        static String forBash(EscapableString string) {
            string.interpreted(BashInterpreter.instance)
        }

    }

}

@CompileStatic
@EqualsAndHashCode(includeFields = true)
class UnescapedString extends EscapableString {

    private String value

    UnescapedString(@NotNull String value) {
        Preconditions.checkArgument(value != null)
        this.value = value
    }

    String getValue() {
        return value
    }

    @Override
    String toString() {
        "u($value)"
    }

    @Override
    String interpreted(EscapableStringInterpreter interpreter) {
        interpreter.from(this)
    }

    @Override
    EscapedString getEscaped() {
        new EscapedString(this)
    }

    @Override
    int size() {
        value.size()
    }

}

@CompileStatic
@EqualsAndHashCode(includeFields = true)
class EscapedString extends EscapableString {

    private EscapableString value

    // The EscapableStringInterpreter needs access to the value. For now package private access
    // is sufficient.
    @PackageScope
    EscapableString get_value() {
        value
    }

    EscapedString(@NotNull EscapableString value) {
        Preconditions.checkArgument(value != null)
        this.value = value
    }

    @Override
    String toString() {
        "e($value)"
    }

    @Override
    String interpreted(EscapableStringInterpreter interpreter) {
        interpreter.from(this)
    }

    @Override
    EscapedString getEscaped() {
        // Stack escapes.
        new EscapedString(this)
    }

    @Override
    int size() {
        value.size()
    }

}

@CompileStatic
@EqualsAndHashCode(includeFields = true)
class ConcatenatedString extends EscapableString {

    private List<EscapableString> values

    @PackageScope
    List<EscapableString> get_values() {
        values
    }

    ConcatenatedString(@NotNull List<EscapableString> values) {
        Preconditions.checkArgument(values != null)
        this.values = values
    }

    @Override
    ConcatenatedString getEscaped() {
        // Escape all values.
        new ConcatenatedString(values.collect { it.escaped })
    }

    @Override
    int size() {
        values.collect { it.size() }.
                inject(0, { acc, v -> acc + v })
    }

    @Override
    String interpreted(EscapableStringInterpreter interpreter) {
        interpreter.from(this)
    }

    @Override
    String toString() {
        "c($values)"
    }

    @Override
    protected ConcatenatedString simplify() {
        List<EscapableString> newValues = values.
                collect { it.simplify() }.
                collect {
                    if (it instanceof ConcatenatedString) {
                        if (it.size() == 0) {
                            null
                        } else {
                            it.values
                        }
                    } else {
                        [it]
                    }
                }.flatten().findAll { it != null } as List<EscapableString>
        new ConcatenatedString(newValues)
    }

}


@CompileStatic
abstract class EscapableStringInterpreter {
    public abstract String interpret(EscapableString string)
    protected abstract String from(UnescapedString string)
    protected abstract String from(EscapedString string)
    protected abstract String from(ConcatenatedString string)
}


@CompileStatic
class BashInterpreter extends EscapableStringInterpreter {

    BashInterpreter() {}

    /** For convenience, we create a singleton instance and a static interpret method. */
    final static BashInterpreter instance = new BashInterpreter()

    @Override
    String interpret(EscapableString string) {
        // This makes it possible to use the single, dynamic dispatch usable.
        string.interpreted(this)
    }

    @Override
    String from(UnescapedString string) {
        string.value
    }

    @Override
    String from(EscapedString string) {
        StringEscapeUtils.escapeXSI(interpret(string._value).
            replace("\n", "\\n")).
            replace("!", "'!'")
    }

    @Override
    String from(ConcatenatedString string) {
        string._values.collect { interpret(it) }.join("")
    }

}