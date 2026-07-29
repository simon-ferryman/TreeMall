package com.treemall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication

public class TreemallApplication {
    /**
     * TreeMall家电商城
     * SpringBoot 启动类
    * */
    public static void main(String[]args){
        SpringApplication.run(TreemallApplication.class,args);
        System.out.println("========================================");
        System.out.println("  TreeMall Server 启动成功！");
        System.out.println("  访问: http://localhost:8080");
        System.out.println("========================================");
    }
}
