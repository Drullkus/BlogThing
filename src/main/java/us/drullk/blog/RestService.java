package us.drullk.blog;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.function.Consumer;

@Service
public class RestService {

	private final RestTemplate rest;

	public RestService(RestTemplateBuilder builder) {
		this.rest = builder.build();
	}

	public JsonNode get(String url) {
		return this.rest.getForObject(url, JsonNode.class);
	}

	public JsonNode post(String url, Consumer<HttpHeaders> head, JsonNode body) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		head.accept(headers);
		HttpEntity<JsonNode> entity = new HttpEntity<>(body, headers);
		return this.rest.postForEntity(url, entity, JsonNode.class).getBody();
	}

}
