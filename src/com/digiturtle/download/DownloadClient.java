package com.digiturtle.download;

import java.net.URI;
import java.util.ArrayList;
import java.util.Stack;

import javax.net.ssl.SSLException;

import com.digiturtle.http.HttpClient;
import com.digiturtle.http.HttpSequencedResponse;
import com.digiturtle.http.JsonResponse;
import com.digiturtle.state.Directory;
import com.digiturtle.state.FileRecord;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;

public class DownloadClient {
	
	private URI indexUri;
	
	private Directory remoteIndex;
	
	public DownloadClient(URI indexUri) {
		this.indexUri = indexUri;
	}
	
	public void readLocalIndex() {
		
	}
	
	public void downloadRemoteIndex() throws InterruptedException, SSLException {
		HttpClient client = new HttpClient(indexUri);
		client.connect();
		HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, indexUri.getPath());
		HttpSequencedResponse response = client.sendHttpRequest(request);
		JsonResponse jsonResponse = new JsonResponse(response);
		if (jsonResponse.getHttpResponseStatus().code() == 200) {
			remoteIndex = jsonResponse.getObject(Directory.class);
		} else {
			throw new IllegalStateException("Invalid HTTP Response: " + jsonResponse.getHttpResponseStatus().code());
		}
	}
	
	public Directory getIndex() {
		return remoteIndex;
	}
	
	public ArrayList<DownloadAction> queueActions() {
		ArrayList<DownloadAction> actions = new ArrayList<>(remoteIndex.size());
		Stack<Directory> directories = new Stack<>();
		queueActions(remoteIndex, actions, directories, 0);
		return actions;
	}
	
	private int queueActions(Directory directory, ArrayList<DownloadAction> actions, Stack<Directory> directories, int offset) {
		StringBuffer prefix = new StringBuffer();
		for (Directory d : directories) {
			prefix.append(d.getName());
			prefix.append('/');
		}
		for (FileRecord record : directory.getFiles()) {
			if (directory.findRecord(record.getGuid()) != null) {
				actions.set(offset, new DownloadAction(record.getGuid(), prefix.toString() + record.getName()));
				offset++;
			}
		}
		for (Directory subDirectory : directory.getSubDirectories()) {
			directories.push(subDirectory);
			offset = queueActions(subDirectory, actions, directories, offset);
			directories.pop();
		}
		return offset;
	}
	
}
