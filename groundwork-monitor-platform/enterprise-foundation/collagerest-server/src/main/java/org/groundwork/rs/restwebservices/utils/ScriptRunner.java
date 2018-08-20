package org.groundwork.rs.restwebservices.utils;

import com.google.common.util.concurrent.SimpleTimeLimiter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class ScriptRunner implements AutoCloseable, Runnable {

    private Log log = LogFactory.getLog(this.getClass());

    private String command;
    private String input;
    private int timeoutDuration;
    private TimeUnit timeoutUnit;

    private Integer exitValue = -1;
    private StringBuffer output = new StringBuffer();
    private StringBuffer error = new StringBuffer();
    private Process process;

    public ScriptRunner(String command, String input, int timeoutDuration, TimeUnit timeoutUnit) {
        this.command = command;
        this.input = input;
        this.timeoutDuration = timeoutDuration;
        this.timeoutUnit = timeoutUnit;
    }

    public void run() {
        Callable callable = new Callable<Integer>() {
            @Override
            public Integer call() throws InterruptedException, IOException {
                process = Runtime.getRuntime().exec(command);
                // Input/output is from the java perspective.  We output to the scripts stdin, etc.
                try (OutputStream stdin = process.getOutputStream();
                     InputStream stdout = process.getInputStream();
                     InputStream stderr = process.getErrorStream()) {
                    StreamWriter stdinWriter = new StreamWriter(stdin);
                    StreamReader stdoutReader = new StreamReader(stdout, output);
                    StreamReader stderrReader = new StreamReader(stderr, error);
                    stdinWriter.start();
                    stdoutReader.start();
                    stderrReader.start();
                    process.waitFor();

                    // Join the readers/writers to ensure that they have completed their tasks prior to returning to
                    // the caller.
                    stdinWriter.join();
                    stdoutReader.join();
                    stderrReader.join();

                    return process.exitValue();
                }
            }
        };

        try {
            if (log.isDebugEnabled()) log.debug("Start running command " + command);
            exitValue = (Integer) new SimpleTimeLimiter().callWithTimeout(callable, timeoutDuration, timeoutUnit, false);
            if (log.isDebugEnabled()) log.debug("Done running command " + command);
        } catch (Exception e) {
            log.error("Exception running command " + command + ": " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void close() {
        process.destroy();
    }

    public String getOutput() {
        return output.toString();
    }

    public String getError() {
        return error.toString();
    }

    public int getExitValue() {
        return exitValue;
    }

    class StreamWriter extends Thread {
        OutputStream os;

        StreamWriter(OutputStream os) {
            this.os = os;
        }

        public void run() {
            try (OutputStreamWriter osw = new OutputStreamWriter(os);
                 BufferedWriter bw = new BufferedWriter(osw)) {
                bw.write(input);
            } catch (IOException e) {
                log.error("Unable to write to output stream: " + e);
            }
        }
    }

    class StreamReader extends Thread {
        InputStream is;
        StringBuffer buffer;

        StreamReader(InputStream is, StringBuffer buffer) {
            this.is = is;
            this.buffer = buffer;
        }

        public void run() {
            try (InputStreamReader isr = new InputStreamReader(is);
                 BufferedReader br = new BufferedReader(isr)) {
                for (String line = br.readLine(); line != null; line = br.readLine()) {
                    // br.readLine() strips the trailing newline, so we must put it back.
                    buffer.append(line);
                    buffer.append('\n');
                }
            } catch (IOException e) {
                log.error("Unable to read from input stream: " + e);
            }
        }
    }

}
