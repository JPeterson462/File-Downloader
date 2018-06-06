package com.digiturtle.http;

import java.util.ArrayList;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

public class HttpSequencedResponse {
	
	private HttpHeaders headers;
	
	private HttpResponseStatus responseStatus;
	
	private ArrayList<ByteBuf> data = new ArrayList<>();
	
	public HttpSequencedResponse(HttpHeaders headers, HttpResponseStatus responseStatus) {
		this.headers = headers;
		this.responseStatus = responseStatus;
	}

	public HttpHeaders getHeaders() {
		return headers;
	}
	
	public HttpResponseStatus getResponseStatus() {
		return responseStatus;
	}
	
	public ArrayList<ByteBuf> getData() {
		return data;
	}
	
}
