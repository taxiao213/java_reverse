package com.taxiao.reverse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.SimpleFormatter;

/**
 * @Author: hanqq
 * @Date: 2022/12/30 16:20
 * CSDN:http://blog.csdn.net/yin13753884368/article
 * Github:https://github.com/taxiao213
 */
@SpringBootApplication
@RestController
public class DockerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DockerApplication.class, args);
    }

    @RequestMapping("/test")
    public String test(){
        return "test: "+ new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
    }
}
