package ru.phosagro.jboss.esb.esb017.routes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.sql.stored.SqlStoredComponent;
import org.apache.camel.component.undertow.UndertowComponent;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestParamType;
import org.apache.camel.model.rest.RestPropertyDefinition;
import org.springframework.context.support.MessageSourceAccessor;
import org.wildfly.extension.camel.CamelAware;
import ru.phosagro.jboss.esb.esb017.dto.ResponseDto;
import ru.phosagro.jboss.esb.esb017.error.ErrorService;

import javax.annotation.Resource;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jms.*;
import javax.servlet.annotation.WebServlet;
import javax.sql.DataSource;
import javax.ws.rs.core.MediaType;
import java.text.MessageFormat;
import java.util.List;
import java.util.logging.Logger;

import static ru.phosagro.jboss.esb.esb017.config.ApplicationConfig.*;

@Startup
@ApplicationScoped
@CamelAware
//@ContextName("esb017-rest-camel-context")
public class Routes extends RouteBuilder {
	private final static Logger LOGGER = Logger.getLogger(Routes.class.toString());

	private static final int cores;

	static {
		cores = Runtime.getRuntime().availableProcessors();
	}

	/*private @Inject
	ObjectMapper objectMapper;

	private @Inject
	MessageSourceAccessor msg;*/

	@Resource(lookup = "java:jboss/datasources/OracleTestR12DB")
	private DataSource ds;


	@Resource(mappedName = "java:/ConnectionFactory")
	private ConnectionFactory connectionFactory;

	@Inject
	private ErrorService errorService;

	public Routes() throws JMSException {
	}

	@Produces
	@Named("dataSource")
	public DataSource getDataSource() {
		return ds;
	}

	@Override
	public void configure() throws Exception {
		restConfiguration()
				.contextPath("/esb017")
				//.port(8443)
				//.scheme("https")
				.component("undertow")
				.componentProperty("matchOnUriPrefix", "true")
				.bindingMode(RestBindingMode.auto);

		getTasksByProject();
	}

	private void getTasksByProject() {
		getContext().getGlobalOptions().put("CamelJacksonEnableTypeConverter", "true");
		getContext().getGlobalOptions().put("CamelJacksonTypeConverterToPojo", "true");

		JmsComponent jmsComponent = new JmsComponent();
		jmsComponent.setConnectionFactory(connectionFactory);

		SqlStoredComponent sqlStCmp = new SqlStoredComponent();
		sqlStCmp.setDataSource(ds);

		getContext().addComponent("jms", jmsComponent);
		getContext().addComponent("sqlStCmp", sqlStCmp);

		rest("/api/v1/")
				.get("/getTasksByProject")
				.outType(ResponseDto.class)
				.param().name("projectCode")
				.type(RestParamType.header)
				.required(true)
				.description("The projectCode to get business trip limits task")
				.dataType("String")
				.endParam()
				.produces(MediaType.APPLICATION_JSON)
				.consumes(MediaType.APPLICATION_JSON)
				.route()
				.log(LoggingLevel.INFO,
						log
						, MessageFormat.format(msg.getMessage("GetTasksByProjectProcedureHandler")
								, "Service called with parameters | projectCode:${header.projectCode}"))
				//.bean(SpringSecurityContextLoader.class)
				//.policy("authenticated")
				.choice()
				.when()
				.simple("${header.projectCode} == null || ${header.projectCode} == ' ' ")
				.bean(errorService, "wrongProjectCodError")
				.otherwise()
				.to("seda:incoming");

		from("seda:incoming")
				.doTry()
				.setBody(simple("${header.projectCode}"))
				.to(ExchangePattern.InOnly, "jms:queue:ESB17_FromWssToOebs")
				.doCatch(java.lang.Exception.class)
				.bean(errorService, "customExceptionHandling")
				.log(LoggingLevel.ERROR
						, log
						, MessageFormat.format(msg.getMessage("GetTasksByProjectProcedureHandler")
								, "Message supply to ESB17_FromWssToOebs-queue finished with error: ${header.HTTP_RESPONSE_CODE}"))
				.marshal()
				.json(JsonLibrary.Jackson)
				.end();

		from("jms:queue:ESB17_FromWssToOebs?concurrentConsumers=" + cores)
				.doTry()
				.process(new Processor() {
					@Override
					public void process(Exchange exchange) throws Exception {

						ImmutableMap<Object, Object> outResult = ImmutableMap
								.of("projectCode", exchange.getIn().getHeader("projectCode"));

						exchange.getIn().setBody(outResult);

						LOGGER.info(MessageFormat.format(msg.getMessage("GetTasksByProjectProcedureHandler")
								, "Data-base-request sent with params | system id: '"
										+ exchange.getExchangeId() + "'"
										+ " | sql params: '"
										+ outResult.toString()
										+ "'"));
					}
				})
				.to("sqlStCmp:APPS.XXPHA_BC574_PKG.GetProjectList(VARCHAR :#projectCode,OUT VARCHAR responseJson)")
				.process(new Processor() {
					@Override
					public void process(Exchange exchange) throws Exception {

						ResponseDto outResult = objectMapper
								.convertValue(exchange.getIn().getBody()
										, new TypeReference<ResponseDto>() {
										});

						exchange.getIn().setBody(outResult.responseJson());

						LOGGER.info(MessageFormat.format(msg.getMessage("GetTasksByProjectProcedureHandler")
								, "Database response is successful | system id: '"
										+ exchange.getExchangeId() + "'"
										+ " | project code: '"
										+ exchange.getIn().getHeader("projectCode")
										+ "' | response: '"
										+ outResult.responseJson()
										+ "'"));
					}
				})
				.doCatch(java.lang.Exception.class)
				.bean(errorService, "customExceptionHandling")
				.log(LoggingLevel.ERROR
						, log
						, MessageFormat.format(msg.getMessage("GetTasksByProjectProcedureHandler")
								, String.format("Message supplied to database finished with error: %s", "${header.HTTP_RESPONSE_CODE}")))
				.marshal()
				.json(JsonLibrary.Jackson)
				.end()
				.log("Esb017_GetTasksByProjectProcedure event TEST: ${body}")
		;
	}

}

