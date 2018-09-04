package net.landzero.xlog.logback;

import ch.qos.logback.core.recovery.ResilientFileOutputStream;
import ch.qos.logback.core.util.FileSize;
import ch.qos.logback.core.util.FileUtil;
import net.landzero.xlog.utils.Strings;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * this appender bypass #{OutputStreamAppender}, provides a more direct way to write xlog specified log files
 */
public class XLogFileAppender extends XLogBaseAppender {

    /*
     * constants
     */

    public static final int CHECK_INTERVAL = 30000;

    public static final FileSize BUFFER_SIZE = new FileSize(8192);

    public static final String DEFAULT_SIGNAL_FILE = "/tmp/xlog.reopen.txt";

    /*
     * configurable variables
     */

    private String signalFile = DEFAULT_SIGNAL_FILE;

    private String dir = null;

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = Strings.normalize(dir);
    }

    public String getSignalFile() {
        return signalFile;
    }

    public void setSignalFile(String signalFile) {
        signalFile = Strings.normalize(signalFile);
        if (signalFile == null) {
            return;
        }
        this.signalFile = signalFile;
    }

    /*
     * internal variables and methods
     */

    private String filename = null;

    private String calculateFilename() {
        if (getEnv() == null || getTopic() == null || getProject() == null)
            return null;
        return getDir() + File.separator + getEnv() + File.separator + getTopic() + File.separator + getProject() + ".log";
    }

    private void initFilename() {
        this.filename = calculateFilename();
    }

    // last time (in milliseconds) cached signal file is modified, 0 for not existed / failed
    private long cachedLastModified = 0;

    private long getLastModified() {
        try {
            return new File(getSignalFile()).lastModified();
        } catch (Exception e) {
            return 0;
        }
    }

    // last time (in milliseconds) signal file is checked
    private long lastChecked = 0;

    private OutputStream outputStream = null;

    private void initOutputStream() throws IOException {
        this.lock.lock();
        try {
            unsafeInitOutputStream();
        } finally {
            this.lock.unlock();
        }
    }

    private void unsafeInitOutputStream() throws IOException {
        unsafeCloseOutputStream();
        File file = new File(this.filename);
        boolean result = FileUtil.createMissingParentDirectories(file);
        if (!result) {
            addError("failed to create parent directories for [" + file.getAbsolutePath() + "]");
        }
        ResilientFileOutputStream fos = new ResilientFileOutputStream(file, true, BUFFER_SIZE.getSize());
        fos.setContext(getContext());
        this.outputStream = fos;
    }

    private void closeOutputStream() throws IOException {
        this.lock.lock();
        try {
            unsafeCloseOutputStream();
        } finally {
            this.lock.unlock();
        }
    }

    private void unsafeCloseOutputStream() throws IOException {
        if (this.outputStream != null) {
            try {
                this.outputStream.flush();
                this.outputStream.close();
            } finally {
                this.outputStream = null;
            }
        }
    }

    private void unsafeReloadOutputStreamIfNeeded() throws IOException {
        // skip if recently checked
        long now = System.currentTimeMillis();
        if (now - this.lastChecked < CHECK_INTERVAL) {
            return;
        }
        this.lastChecked = now; // update lastChecked immediately, prevent continuous failure
        // check lastModified
        long lastModified = getLastModified();
        if (lastModified == this.cachedLastModified) {
            return;
        }
        // reopen file
        unsafeInitOutputStream();
        // cache last modified
        this.cachedLastModified = lastModified;
    }


    /**
     * public methods
     */

    @Override
    public void start() {
        int errors = 0;
        initFilename();
        if (this.filename == null) {
            if (isJsonMode()) {
                addError("failed to calculate fileName, check if 'dir', 'env' or 'project' field is missing");
            } else {
                addError("failed to calculate fileName, check if 'dir', 'env', 'topic' or 'project' field is missing");
            }
            errors++;
        }
        if (errors == 0) {
            try {
                initOutputStream();
            } catch (IOException e) {
                addError("failed to initialize output stream", e);
                errors++;
            }
        }
        if (errors == 0) {
            super.start();
        }
    }

    @Override
    public void stop() {
        try {
            this.closeOutputStream();
        } catch (IOException e) {
            addWarn("failed to close output stream", e);
        }
        super.stop();
    }

    @Override
    protected void appendString(@NotNull String string) {
        this.lock.lock();
        try {
            unsafeReloadOutputStreamIfNeeded();
            this.outputStream.write(string.getBytes());
            this.outputStream.flush();
        } catch (IOException ignored) {
        } finally {
            this.lock.unlock();
        }
    }

}
