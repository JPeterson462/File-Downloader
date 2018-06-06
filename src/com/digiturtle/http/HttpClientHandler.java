package com.digiturtle.http;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;

public class HttpClientHandler extends SimpleChannelInboundHandler<HttpObject> {
	
	public interface HttpTokenizer {
		
		public void startHttpResponse(HttpHeaders headers, HttpResponseStatus responseStatus);
		
		public void httpContent(ByteBuf buf);
		
		public void endHttpResponse();
		
	}
	
	private HttpTokenizer tokenizer;
	
	public HttpClientHandler(HttpTokenizer tokenizer) {
		this.tokenizer = tokenizer;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
		if (msg instanceof HttpResponse) {
			HttpResponse response = (HttpResponse) msg;
			tokenizer.startHttpResponse(response.headers(), response.getStatus());
		}
		if (msg instanceof HttpContent) {
			HttpContent content = (HttpContent) msg;
			tokenizer.httpContent(content.content().retain());
			if (content instanceof LastHttpContent) {
				tokenizer.endHttpResponse();
				ctx.close();
			}
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

}
