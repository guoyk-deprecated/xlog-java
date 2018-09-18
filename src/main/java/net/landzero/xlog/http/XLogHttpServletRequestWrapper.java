package net.landzero.xlog.http;

import net.landzero.xlog.utils.Requests;
import org.jetbrains.annotations.NotNull;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class XLogHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] body;

    public XLogHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        this.body = Requests.readBody(request);
    }

    @Override
    public ServletInputStream getInputStream() {
        return new InputStreamWrapper();
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @NotNull
    public String getBody() {
        return new String(body);
    }

    public class InputStreamWrapper extends ServletInputStream {

        private final ByteArrayInputStream inputStream;

        public InputStreamWrapper() {
            this.inputStream = new ByteArrayInputStream(XLogHttpServletRequestWrapper.this.body);
        }

        @Override
        public boolean isFinished() {
            return this.inputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            try {
                // InputStreamWrapper is always ready
                readListener.onDataAvailable();
            } catch (IOException ignored) {
            }
        }

        @Override
        public int read() {
            return this.inputStream.read();
        }
    }

}
