package com.github.ka4ok85.config.client.jwt.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.config.client.ConfigClientProperties;
import org.springframework.cloud.config.client.ConfigServicePropertySourceLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.github.ka4ok85.config.client.jwt.models.LoginRequest;
import com.github.ka4ok85.config.client.jwt.models.Token;

@Configuration
@Order(Ordered.LOWEST_PRECEDENCE)
public class ConfigClientBootstrapConfiguration {

	private static Log logger = LogFactory.getLog(ConfigClientBootstrapConfiguration.class);

	@Value("${spring.cloud.config.username}")
	private String jwtUsername;

	@Value("${spring.cloud.config.password}")
	private String jwtPassword;

	@Value("${spring.cloud.config.endpoint}")
	private String jwtEndpoint;

	private String jwtToken;

	@Autowired
	private ConfigurableEnvironment environment;

	@PostConstruct
	public void init() {
		RestTemplate restTemplate = new RestTemplate();

		LoginRequest loginBackend = new LoginRequest();
		loginBackend.setUsername(jwtUsername);
		loginBackend.setPassword(jwtPassword);

		String url = jwtEndpoint;
		Token token;
		try {
			token = restTemplate.postForObject(url, loginBackend, Token.class);
			if (token.getToken() == null) {
				throw new Exception();
			}

			setJwtToken(token.getToken());
		} catch (Exception e) {
			logger.error("Can not fetch JWT from Config Server");
		}
	}

	public String getJwtToken() {
		return jwtToken;
	}

	public void setJwtToken(String jwtToken) {
		this.jwtToken = jwtToken;
	}

	@Bean
	public ConfigClientProperties configClientProperties() {
		ConfigClientProperties client = new ConfigClientProperties(this.environment);
		client.setEnabled(false);

		return client;
	}

	@Bean
	public ConfigServicePropertySourceLocator configServicePropertySourceLocator() {
		ConfigClientProperties clientProperties = configClientProperties();
		ConfigServicePropertySourceLocator configServicePropertySourceLocator = new ConfigServicePropertySourceLocator(
				clientProperties);
		configServicePropertySourceLocator.setRestTemplate(customRestTemplate(clientProperties));

		return configServicePropertySourceLocator;
	}

	private RestTemplate customRestTemplate(ConfigClientProperties clientProperties) {
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", "Bearer:" + jwtToken);
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setReadTimeout((60 * 1000 * 3) + 5000); // TODO 3m5s make
																// configurable?
		RestTemplate template = new RestTemplate(requestFactory);
		if (!headers.isEmpty()) {
			template.setInterceptors(
					Arrays.<ClientHttpRequestInterceptor> asList(new GenericRequestHeaderInterceptor(headers)));
		}

		return template;
	}

	public static class GenericRequestHeaderInterceptor implements ClientHttpRequestInterceptor {

		private final Map<String, String> headers;

		public GenericRequestHeaderInterceptor(Map<String, String> headers) {
			this.headers = headers;
		}

		@Override
		public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
				throws IOException {
			for (Entry<String, String> header : headers.entrySet()) {
				request.getHeaders().add(header.getKey(), header.getValue());
			}
			return execution.execute(request, body);
		}
	}
}
