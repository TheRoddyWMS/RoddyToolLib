package de.dkfz.roddy.execution.io

import spock.lang.Specification

class LocalExecutionHelperSpec extends Specification {

    def "getProcessID returns the OS process id of a running process"() {
        given:
        Process process = ["bash", "-c", "sleep 1"].execute()

        when:
        Integer pid = LocalExecutionHelper.getProcessID(process)

        then:
        pid > 0
        pid == process.pid() as Integer

        cleanup:
        process.destroy()
    }
}
