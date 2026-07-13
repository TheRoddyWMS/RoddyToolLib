package de.dkfz.roddy.tools

import spock.lang.Specification

import static de.dkfz.roddy.tools.EscapableString.Shortcuts.*

class BashInterpreterTest extends Specification {
    def "Interpret"() {
        given:
        BashInterpreter i = BashInterpreter.instance
        expect:
        i.interpret(c()) == ""
        i.interpret(u(" ")) == " "
        i.interpret(e(" ")) == "\\ "
        i.interpret(c(u(" "), e(";"))) == " \\;"
        i.interpret(c(e(" "), u(";"))) == "\\ ;"
    }
}
