package net.landzero.xlog.example.controller;

import net.landzero.xlog.XLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
public class WelcomeController {

    private static final Logger logger = LoggerFactory.getLogger(WelcomeController.class);

    @RequestMapping(value = "/hello", method = {RequestMethod.POST}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public String hello_json(@RequestBody HelloBody hello) {
        logger.info("hello " + XLog.keyword(hello.getHello()));
        return "hello " + hello.getHello();
    }

    @RequestMapping(value = "/hello", method = {RequestMethod.POST}, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String hello_form(@RequestParam("hello") String hello) {
        logger.info("hello " + XLog.keyword(hello));
        return "hello " + hello;
    }

    @RequestMapping(value = "/hello", method = {RequestMethod.GET})
    public String hello_get(@RequestParam("hello") String hello) {
        logger.info("hello " + XLog.keyword(hello));
        return "hello " + hello;
    }


    public static class HelloBody {

        public String hello = "";

        public String getHello() {
            return hello;
        }

        public void setHello(String hello) {
            this.hello = hello;
        }
    }

}
