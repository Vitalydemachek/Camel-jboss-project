package ru.phosagro.jboss.esb.esb017.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Component;
import ru.phosagro.jboss.esb.esb017.dto.ResponseDto;

import static ru.phosagro.jboss.esb.esb017.config.ApplicationConfig.*;

import javax.inject.Inject;
import java.text.MessageFormat;

public class ErrorService {
	private static final Logger LOG = Logger.getLogger(ErrorService.class);
	/*private @Inject
	ObjectMapper objectMapper;
	private @Inject
	MessageSourceAccessor msg;*/

	public void wrongProjectCodError(Exchange exchange) throws Exception {
		Object projectCode = exchange.getIn().getHeader("projectCode");
		exchange.getIn().setBody(objectMapper.writeValueAsString(new ResponseDto()
				.status("400")));
		exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "text/plain");
		exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 400);
	}

	public void customExceptionHandling(Exchange exchange) {
		String body = exchange.getIn().getBody(String.class);
		Exception e = exchange.getProperty(Exchange.EXCEPTION_CAUGHT,
				Exception.class);
		StringBuilder sb = new StringBuilder();
		sb.append("ERROR: ");
		sb.append(e.getMessage());
		sb.append("\nBODY: ");
		sb.append(body);
		exchange.getIn().setBody(new ResponseDto()
				.status("503"));
		exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "text/plain; charset=utf-8");
		exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 503);

		LOG.error(MessageFormat.format(msg.getMessage("GetTasksByProjectProcedureHandler")
				, sb.toString()));


	}
}
