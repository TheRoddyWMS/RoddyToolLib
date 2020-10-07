/*
 * Copyright (c) 2016 eilslabs.
 *
 * Distributed under the MIT License (license terms are at https://www.github.com/eilslabs/Roddy/LICENSE.txt).
 */

package de.dkfz.roddy.execution.io;

import de.dkfz.roddy.core.InfoObject;

import java.util.List;

/**
 * Stores the result of a command execution.
 * Commands can i.e. be executed via ssh or on the local command line.
 * @author michael
 */
public class ExecutionResult extends InfoObject {

    /**
     * This can hold some sort of process id for a process
     */
    protected final String processID;

    /**
     * Successful or not?
     */
    protected final boolean successful;
    /**
     * All result lines.
     */
    protected final List<String> resultLines;
    /**
     * First line of the result array.
     * Null if no entries are in the array.
     */
    protected final String firstLine;

    protected final int exitCode;

    /** Contain the results of an execution. Note that the usage of this class is quite inconsistent.
     *
     * @param successful   Whether the execution was successful. Some tools have exit code == 0 but are yet not successful.
     * @param exitCode     The actual exit code. (Warning: Often this is zero, because waitFor == false).
     * @param resultLines  This should be the standard output and error (interleaved). (Warning: Often this is only stdout).
     * @param processID    The process ID, if available.
     */
    public ExecutionResult(boolean successful, int exitCode, List<String> resultLines, String processID) {

        this.processID = processID;
        this.successful = successful;
        this.exitCode = exitCode;
        this.resultLines = resultLines;
        if(resultLines.size() > 0)
            firstLine = resultLines.get(0);
        else
            firstLine = null;
    }

    public String getProcessID() {
        return processID;
    }

    public int getExitCode() {
        return exitCode;
    }

    public String getFirstLine() {
        return firstLine;
    }

    public List<String> getResultLines() {
        return resultLines;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public boolean getSuccessful() {
        return isSuccessful();
    }

    @Deprecated
    public int getErrorNumber() { return exitCode; }
}
