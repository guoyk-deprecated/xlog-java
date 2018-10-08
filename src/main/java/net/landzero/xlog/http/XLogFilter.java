package net.landzero.xlog.http;

import net.landzero.xlog.XLog;
import net.landzero.xlog.constants.Constants;
import net.landzero.xlog.utils.Requests;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This class provides a servlet Filter, setup correlation id, outputs structured events
 * <p>
 * MDC added:
 * cridMark - standard correlation id mark, i.e. "CRID[xxxxxxxxxxxxxxxx]"
 * crid - correlation id
 */
public class XLogFilter implements Filter {

    /**
     * header name for correlation id
     * <p>
     * CRID 的 HTTP Header 名
     * <p>
     * xlog 1.11 版本使用Constants.CRID_HEADER_NAME替代。
     */
    @Deprecated
    public static final String CRID_HEADER_NAME = Constants.HTTP_CRID_HEADER;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        request = wrapRequest(request);
        setupXLog(request, response);
        AccessEventBuilder event = new AccessEventBuilder().setServletRequest(request);
        try {
            chain.doFilter(request, response);
            event.setServletResponse(response);
        } finally {
            event.commit();
            resetXLog();
        }
    }

    private void setupXLog(ServletRequest request, ServletResponse response) {
        if (request instanceof HttpServletRequest) {
            XLog.setPath(((HttpServletRequest) request).getRequestURI());
            XLog.setCrid(((HttpServletRequest) request).getHeader(CRID_HEADER_NAME));
        }
        if (response instanceof HttpServletResponse) {
            ((HttpServletResponse) response).setHeader(CRID_HEADER_NAME, XLog.crid());
        }
        MDC.put(Constants.MDC_CRID_KEY, XLog.crid());
        MDC.put(Constants.MDC_CRID_MARK_KEY, XLog.cridMark());
    }

    @NotNull
    private ServletRequest wrapRequest(@NotNull ServletRequest request) throws IOException {
        if (request instanceof HttpServletRequest) {
            if (Requests.hasJsonBody((HttpServletRequest) request)) {
                return new XLogHttpServletRequestWrapper((HttpServletRequest) request);
            }
        }

        return request;
    }

    private void resetXLog() {
        XLog.clearCrid();
        XLog.clearPath();
        MDC.remove(Constants.MDC_CRID_KEY);
        MDC.remove(Constants.MDC_CRID_MARK_KEY);
    }

    @Override
    public void destroy() {
    }

}
