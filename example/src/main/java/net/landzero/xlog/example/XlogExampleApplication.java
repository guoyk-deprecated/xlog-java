package net.landzero.xlog.example;

import net.landzero.xlog.http.XLogFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

@SpringBootApplication
public class XlogExampleApplication {

    @Bean
    public FilterRegistrationBean xlogFilter() {
        FilterRegistrationBean<XLogFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new XLogFilter());
        bean.addUrlPatterns("/*");
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return bean;
    }

    public static void main(String[] args) {
        SpringApplication.run(XlogExampleApplication.class, args);
    }
}
