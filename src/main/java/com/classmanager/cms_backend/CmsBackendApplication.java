package com.classmanager.cms_backend;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
public class CmsBackendApplication {

	private static final Logger log = LogManager.getLogger(CmsBackendApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(CmsBackendApplication.class, args);
		log.info("Cms Application Started");
	}
}
