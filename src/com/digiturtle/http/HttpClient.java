package com.digiturtle.http;

import java.net.URI;
import java.util.Stack;

import javax.net.ssl.SSLException;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

public class HttpClient {
	
	private String scheme, host;
	
	private int port;
	
	private SslContext sslCtx;
	
	private EventLoopGroup group;
	
	private Bootstrap b;
	
	private Channel ch;
	
	private Stack<HttpSequencedResponse> responses = new Stack<>();
	
	public HttpClient(URI uri) throws SSLException {
		scheme = uri.getScheme() == null ? "http" : uri.getScheme();
		host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();
		port = uri.getPort();
		if (port < 0) {
			if (scheme.equalsIgnoreCase("http")) {
				port = 80;
			}
			else if (scheme.equalsIgnoreCase("https")) {
				port = 443;
			}
		}
		if (!(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
			throw new IllegalArgumentException("HttpClient only supports http and https protocols");
		}
		if (scheme.equalsIgnoreCase("https")) {
			sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
		} else {
			sslCtx = null;
		}
	}
	
	public void addResponse(HttpSequencedResponse response) {
		responses.add(response);
	}
	
	public void connect() throws InterruptedException {
		group = new NioEventLoopGroup();
		b = new Bootstrap();
		b.group(group).channel(NioSocketChannel.class).handler(new HttpChannelInitializer(sslCtx, this));
		ch = b.connect(host, port).sync().channel();
	}
	
	public HttpSequencedResponse sendHttpRequest(HttpRequest request) {
		ch.writeAndFlush(request);
		while (responses.empty()) {
			// Wait
		}
		return responses.pop();
	}
	
	public void disconnect() throws InterruptedException {
		try {
			ch.closeFuture().sync();
		} finally {
			group.shutdownGracefully();
		}
	}

}
