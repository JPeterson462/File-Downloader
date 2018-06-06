package com.digiturtle.http;

import java.util.Stack;

import com.digiturtle.http.HttpClientHandler.HttpTokenizer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.ssl.SslContext;

public class HttpChannelInitializer extends ChannelInitializer<SocketChannel> {
	
	private final SslContext sslCtx;
	
	private HttpTokenizer tokenizer;
	
	private HttpClient client;
	
	public HttpChannelInitializer(SslContext sslCtx, HttpClient client) {
		this.sslCtx = sslCtx;
		this.client = client;
		tokenizer = new HttpTokenizer() {

			private Stack<HttpSequencedResponse> responses = new Stack<>();
			
			@Override
			public void startHttpResponse(HttpHeaders headers, HttpResponseStatus responseStatus) {
				responses.push(new HttpSequencedResponse(headers, responseStatus));
			}

			@Override
			public void httpContent(ByteBuf buf) {
				responses.peek().getData().add(buf);
			}

			@Override
			public void endHttpResponse() {
				HttpSequencedResponse response = responses.pop();
				HttpChannelInitializer.this.client.addResponse(response);
			}
			
		};
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline p = ch.pipeline();
		if (sslCtx != null) {
			p.addLast(sslCtx.newHandler(ch.alloc()));
		}
		p.addLast(new HttpClientCodec());
		p.addLast(new HttpContentDecompressor());
		p.addLast(new HttpClientHandler(tokenizer));
	}

}
