package com.digiturtle.download;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.digiturtle.files.FileClient;

public class DownloadAction {

	private String guid;
	
	private String path;
	
	private boolean started, finished;
	
	public DownloadAction(String guid, String path) {
		this.guid = guid;
		this.path = path;
		started = false;
		finished = false;
	}
	
	public String getGuid() {
		return guid;
	}
	
	public String getPath() {
		return path;
	}
	
	public void start() {
		started = true;
	}
	
	public void finish() {
		finished = true;
	}
	
	public boolean isInProgress() {
		return started && !finished;
	}
	
	public boolean isComplete() {
		return started && finished;
	}
	
	public void downloadFile(FileClient client) throws FileNotFoundException, IOException {
		start();
		client.downloadFile(guid, new FileOutputStream(getPath()));
		finish();
	}
	
}
