package com.go2wheel.mysqlbackup;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

@Configuration
@EnableWebMvc
public class WebMvcConfigMine implements WebMvcConfigurer {
	
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/api/**");
			}
		};
	}
	
	
    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
//        registry.enableContentNegotiation(new MappingJackson2JsonView());
//        registry.freeMarker().cache(false);
    }

//    @Bean
//    public FreeMarkerConfigurer freeMarkerConfigurer() {
//        FreeMarkerConfigurer configurer = new FreeMarkerConfigurer();
//        configurer.setTemplateLoaderPath("/freemarker");
//        return configurer;
//    }
	
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