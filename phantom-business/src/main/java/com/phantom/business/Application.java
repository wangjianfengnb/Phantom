package com.phantom.business;

import com.phantom.business.db.DataSourceConfig;
import jdk.nashorn.internal.ir.debug.ObjectSizeCalculator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * IM业务系统
 *
 * @author Jianfeng Wang
 * @since 2019/11/12 11:06
 */
@Import(DataSourceConfig.class)
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
