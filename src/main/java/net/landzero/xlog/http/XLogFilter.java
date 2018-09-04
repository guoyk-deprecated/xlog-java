package net.landzero.xlog.http;

import net.landzero.xlog.XLog;
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
     */
    public static final String CRID_HEADER_NAME = "X-Correlation-ID";

    /**
     * MDC key for correlation id
     * <p>
     * CRID 的 MDC 键值
     */
    public static final String MDC_CRID_KEY = "crid";

    /**
     * MDC key for correlation id mark
     * <p>
     * CRID 标记 的 MDC 键值
     */
    public static final String MDC_CRID_MARK_KEY = "cridMark";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        setupXLog(request, response);
        AccessEventBuilder event = new AccessEventBuilder().setServletRequest(request);
        try {
            chain.doFilter(request, response);
            event.setServletResponse(response);
        } finally {
            XLog.appendEvent(event.build());
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
        MDC.put(MDC_CRID_KEY, XLog.crid());
        MDC.put(MDC_CRID_MARK_KEY, XLog.cridMark());
    }

    private void resetXLog() {
        XLog.clearCrid();
        XLog.clearPath();
        MDC.remove(MDC_CRID_KEY);
        MDC.remove(MDC_CRID_MARK_KEY);
    }

    @Override
    public void destroy() {
    }

}
