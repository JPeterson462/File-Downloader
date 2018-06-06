package com.digiturtle.http;

import java.nio.charset.Charset;

import com.digiturtle.jsonbeans.JSONBeans;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

public class JsonResponse {
	
	private StringBuffer jsonSource;
	
	private HttpHeaders headers;
	
	private HttpResponseStatus responseStatus;
	
	public JsonResponse(HttpSequencedResponse response) {
		headers = response.getHeaders();
		responseStatus = response.getResponseStatus();
		jsonSource = new StringBuffer();
		for (ByteBuf buf : response.getData()) {
			jsonSource.append(buf.toString(Charset.defaultCharset()));
		}
	}
	
	public String getJsonSource() {
		return jsonSource.toString();
	}
	
	public HttpHeaders getHttpHeaders() {
		return headers;
	}
	
	public HttpResponseStatus getHttpResponseStatus() {
		return responseStatus;
	}
	
	public <T> T getObject(Class<T> type) {
		return JSONBeans.readBean(jsonSource.toString().toCharArray(), type, null);
	}

}
