package com.go2wheel.mysqlbackup.resulthandler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.result.ResultHandlerConfig;

/**
 * @see ResultHandlerConfig
 * @author admin
 *
 */
@Configuration
public class ResultHandlerConfigMine {

//	@Bean
//	@Qualifier("main")
//	public ResultHandler<?> mainResultHandler() {
//		return new TypeHierarchyResultHandlerMine();
//	}
//	
//	@Bean
//	public IterableResultHandlerMine iterableResultHandler() {
//		return new IterableResultHandlerMine();
//	}

//	@PostConstruct
//	public void wireIterableResultHandler() {
//		iterableResultHandler().setDelegate(mainResultHandler());
//	}

//	@Bean
//	@ConditionalOnClass(TerminalSizeAware.class)
//	public TerminalSizeAwareResultHandler terminalSizeAwareResultHandler() {
//		return new TerminalSizeAwareResultHandler();
//	}

//	@Bean
//	public AttributedCharSequenceResultHandler attributedCharSequenceResultHandler() {
//		return new AttributedCharSequenceResultHandler();
//	}
//
//	@Bean
//	public DefaultResultHandler defaultResultHandler() {
//		return new DefaultResultHandler();
//	}
//
//	@Bean
//	public ParameterValidationExceptionResultHandler parameterValidationExceptionResultHandler() {
//		return new ParameterValidationExceptionResultHandler();
//	}

	@Bean
	public HasErrorIdAndMsgkeyResultHandler hasErrorIdAndMsgkeyResultHandler() {
		return new HasErrorIdAndMsgkeyResultHandler();
	}
	
	@Bean
	public FacadeResultHandler<?> facadeResultHandler() {
		return new FacadeResultHandler<>();
	}

}
