package com.go2wheel.mysqlbackup;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfigMine {
	
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/api/**");
			}
		};
	}
	
//	@Bean(name="localeResolver")
//	public LocaleResolver localMissingEndeResolver() {
//		CookieLocaleResolver clr = new CookieLocaleResolver();
//		clr.setDefaultLocale(Locale.ENGLISH); //Locale.US result en_US.properties.
//		return clr;
//	}
//	
//	@Bean
//	public LocaleChangeInterceptor localeChangeInterceptor() {
//		return new LocaleChangeInterceptor();
//	}
//
//	@Override
//	public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("/static/**")
//        .addResourceLocations("classpath:/static/");
////        .setCacheControl(CacheControl.maxAge(1000, TimeUnit.DAYS).cachePublic());
//	}
//	
//	@Override
//	public void addInterceptors(InterceptorRegistry registry) {
//		registry.addInterceptor(localeChangeInterceptor());
//	}
	
	
}