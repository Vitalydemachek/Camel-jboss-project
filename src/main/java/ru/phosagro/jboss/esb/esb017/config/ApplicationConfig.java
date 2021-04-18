package ru.phosagro.jboss.esb.esb017.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;


public class ApplicationConfig  {

	public static final ObjectMapper objectMapper;
	public static final MessageSourceAccessor msg;


	static {
		objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		msg = new MessageSourceAccessor(messageSource());
	}

	private static MessageSource messageSource() {
		ReloadableResourceBundleMessageSource messageSource
				= new ReloadableResourceBundleMessageSource();

		messageSource.setBasenames("classpath:base-messages", "classpath:messages");
		messageSource.setDefaultEncoding("UTF-8");
		return messageSource;
	}

 /*@Produces
 @ApplicationScoped
 @Named
 public MessageSourceAccessor msg() {
     return new MessageSourceAccessor(messageSource());
 }*/

/* @Produces
 @ApplicationScoped
 @Named
 public ObjectMapper objectMapper() {
     ObjectMapper objectMapper = new ObjectMapper();
     objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
     return objectMapper;
 }*/
}
