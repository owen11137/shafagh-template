package com.misc.shafagh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * این کلاس عمداً در پکیج ریشه (com.misc.shafagh) است تا component scan و
 * Entity/Repository scan همه‌ی ماژول‌ها را پوشش دهد.
 */
@SpringBootApplication
public class ShafaghApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShafaghApplication.class, args);
    }
}
