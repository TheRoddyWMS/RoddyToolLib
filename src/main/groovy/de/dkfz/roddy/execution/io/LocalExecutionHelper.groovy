/*
 * Copyright (c) 2017 eilslabs.
 *
 * Distributed under the MIT License (license terms are at https://www.github.com/eilslabs/Roddy/LICENSE.txt).
 */

package de.dkfz.roddy.execution.io

import de.dkfz.roddy.core.InfoObject
import de.dkfz.roddy.tools.LoggerWrapper
import groovy.transform.CompileStatic

import java.lang.reflect.Field
import java.nio.charset.Charset
import java.time.Duration
import java.util.concurrent.*
import java.util.function.Supplier
import java.util.stream.Collectors

@CompileStatic
class LocalExecutionHelper {

    private static final LoggerWrapper logger = LoggerWrapper.getLogger(LocalExecutionHelper.class.name);

    private static final ExecutorService executorService = Executors.newCachedThreadPool()

    static Integer getProcessID(Process process) {
        Field f = process.getClass().getDeclaredField("pid");
        f.setAccessible(true);
        Integer processID = f.get(process) as Integer
        return processID
    }

    /**
     * Use ExecutionResult instead. Deprecated and kept for compatibility.
     */
    @Deprecated
    static class ExtendedProcessExecutionResult extends InfoObject {
        final int exitValue;
        final String processID;
        final List<String> lines = [];

        ExtendedProcessExecutionResult(int exitValue, String processID, List<String> lines) {
            this.exitValue = exitValue
            this.processID = processID
            this.lines = lines
        }

        boolean isSuccessful() {
            return exitValue == 0
        }
    }

    static String executeSingleCommand(String command) {
        //Process process = Roddy.getLocalCommandSet().getShellExecuteCommand(command).execute();
        Process process = (["bash", "-c", "${command}"]).execute();

        final String separator = System.getProperty("line.separator");
        process.waitFor();
        if (process.exitValue()) {
            throw new RuntimeException("Process could not be run" + separator + "\tCommand: bash -c " +
                                       command + separator + "\treturn code is: " + process.exitValue())
        }

        def text = process.text
        return chomp(text) // Cut off trailing "\n"
    }

    static String chomp(String text) {
        text.length() >= 2 ? text[0..-2] : text
    }

    static List<String> readStringStream(InputStream inputStream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))
        return reader.lines().collect(Collectors.toList())
    }

    /** Read from an InputStream asynchronously using the executorService.
     *  May throw an UncheckedIOException.
     */
    static CompletableFuture<List<String>> asyncReadStringStream(
            InputStream inputStream,
            ExecutorService executorService) {
        return CompletableFuture.supplyAsync({
            readStringStream(inputStream)
        }, executorService)
    }

    static List<String> readStringStream(
            InputStream inputStream,
            OutputStream outputStream) {
        // Make sure the same charset is used for input and output. This is what
        // InputStreamReader(InputStream) uses internally.
        String charsetName = Charset.defaultCharset().name()
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charsetName))
        List<String> result = []
        reader.eachLine {line ->
            result.add(line)
            // The reader removes the newlines, so we add them back here. UNIX only.
            byte[] bytes = (line + "\n").getBytes(charsetName)
            outputStream.write(bytes, 0, bytes.size())
        }
        return result
    }

    /** This method is like asyncReadStringStream, but additionally copies the inputStream content
     *  to the outputStream.
     */
    static CompletableFuture<List<String>> asyncReadStringStream(
            InputStream inputStream,
            ExecutorService executorService,
            OutputStream outputStream) {
        return CompletableFuture.supplyAsync({
            readStringStream(inputStream, outputStream)
        } as Supplier<List<String>>, executorService)
    }

    /**
     * TODO Remove this backwards-compatibility function in version 3. Set default waitFor=true
     *      on new signature function.
     */
    @Deprecated
    static ExecutionResult executeCommandWithExtendedResult(
            String command,
            OutputStream outputStream = null,
            ExecutorService executorService = executorService) {
        return executeCommandWithExtendedResult(
                command, true, Duration.ZERO, outputStream, executorService)
    }


    /**
     * Execute a command using the local command interpreter Bash (currently fixed).
     *
     * If outputStream is set, the full output is going to this stream. Otherwise it is stored
     * in the returned object.
     */
    static ExecutionResult executeCommandWithExtendedResult(
            String command,
            Boolean waitFor,
            Duration timeout = Duration.ZERO,
            OutputStream outputStream = null,
            ExecutorService executorService = executorService) {
        List<String> bashCommand = ["bash", "-c", command]
        logger.postRareInfo("Executing command locally: ${bashCommand}")
        ProcessBuilder processBuilder = new ProcessBuilder(bashCommand)
        Process process = processBuilder.start()
        CompletableFuture<List<String>> stdoutF
        if (outputStream == null) {
            stdoutF = asyncReadStringStream(process.inputStream, executorService)
        } else {
            stdoutF = asyncReadStringStream(process.inputStream, executorService, outputStream)
        }
        CompletableFuture<List<String>> stderrF =
                asyncReadStringStream(process.errorStream, executorService)
        CompletableFuture<Integer> exitCodeF = CompletableFuture.supplyAsync({
            Integer result
            if (timeout != Duration.ZERO) {
                // To ensure the command is actually terminated and for better error reporting
                // a TimeoutException is raised here, rather than creating a timeout at the level
                // of the future.
                Boolean finished = process.waitFor(timeout.toNanos(), TimeUnit.NANOSECONDS)
                if (finished) {
                    result = process.exitValue()
                } else {
                    // Supplier.get() sucks. It doesn't declare throwing exceptions. Without the
                    // wrapping one will get an UndeclaredThrowableException. Let's do this
                    // explicitly into an unchecked Exception, instead.
                    throw new RuntimeException(new TimeoutException("Command execution timed out: ${bashCommand}"))
                }
            } else {
                result = process.waitFor()
            }
            return result
        }, executorService)

        AsyncExecutionResult result = new AsyncExecutionResult(
                bashCommand,
                getProcessID(process),
                exitCodeF, stdoutF, stderrF)
        if (waitFor) {
            return result.asSynchronousExecutionResult()
        } else {
            return result
        }
    }

    static Process executeNonBlocking(String command) {
        Process process = ("sleep 1; " + command).execute();
        return process;
    }


    static String execute(String cmd) {
        def proc = cmd.execute();
        int res = proc.waitFor();
        if (res == 0) {
            return proc.in.text;
        }
        return "";
    }
}
