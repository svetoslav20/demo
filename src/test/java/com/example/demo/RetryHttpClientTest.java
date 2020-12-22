package com.example.demo;

import org.apache.http.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;


import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RetryHttpClientTest {

	private static final String GET_URL = "https://httpbin.org/get";
	private static final String STATUS_503_URL = "https://httpbin.org/status/503";

	@Test
	void shouldThrowIOExceptionAfterRetryingThreeTimes() {
		IOException exception = assertThrows(IOException.class, executeRetryingThreeTimes());
		assertEquals("Planned Exception", exception.getMessage());
	}

	@Test
	void testRetriesFor503WithResponseInterceptor()  {
		IOException exception = assertThrows(IOException.class, executeRetryingThreeTimesWith503StatusCode());
		assertEquals("Retry it with 503 status code", exception.getMessage());
	}

	private Executable executeRetryingThreeTimes() {
		return () -> {
			try (CloseableHttpClient httpClient = HttpClients
					.custom()
					.addInterceptorLast(createRequestInterceptorThatThrowsIOException())
					.build()) {
				execute(httpClient, GET_URL);
			}
		};
	}

	private Executable executeRetryingThreeTimesWith503StatusCode() {
		return () -> {
			try (CloseableHttpClient httpClient = HttpClients
					.custom()
					.addInterceptorLast(createRequestInterceptorThatThrowsIOExceptionWhenStatusCodeIs503())
					.build()) {
				execute(httpClient, STATUS_503_URL);
			}
		};
	}

	private HttpRequestInterceptor createRequestInterceptorThatThrowsIOException() {
		return (request, context) -> {
			throw new IOException("Planned Exception");
		};
	}

	private HttpResponseInterceptor createRequestInterceptorThatThrowsIOExceptionWhenStatusCodeIs503() {
		return (response, context) -> {
			if (response.getStatusLine().getStatusCode() == 503) {
				throw new IOException("Retry it with 503 status code");
			}
		};
	}

	private void execute(CloseableHttpClient httpClient, String url) throws IOException {
		final HttpGet httpGet = new HttpGet(url);
		try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
			StatusLine statusLine = response.getStatusLine();
			System.out.println(statusLine.getStatusCode() + " " + statusLine.getReasonPhrase());
			EntityUtils.consumeQuietly(response.getEntity());
		}
	}

}
