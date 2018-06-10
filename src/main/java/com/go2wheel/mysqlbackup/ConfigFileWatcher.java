package com.go2wheel.mysqlbackup;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

//@Component
public class ConfigFileWatcher {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private static String APPLICATION_CONFIGFILE = "application.properties";
	private static String APPLICATION_CONFIGFILE_DEV = "application-dev.properties";
	
	private Path fileToWatcher;
	
	private Path dir;
	
	public ConfigFileWatcher() {
		dir = Paths.get("");
		fileToWatcher = dir.resolve(APPLICATION_CONFIGFILE_DEV);
		if (!Files.exists(fileToWatcher)) {
			dir = Paths.get("src", "main", "resources");
			fileToWatcher = dir.resolve(APPLICATION_CONFIGFILE_DEV);
		}
	}

	@Async
	public void watch() {
		if (!Files.exists(fileToWatcher)) {
			logger.info("File {} doesn't exists, watching canceled.", fileToWatcher.toAbsolutePath().toString());
			return;
		}
		try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
			dir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
			while (true) {
				WatchKey key;
				try {
					key = watcher.poll(25, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					return;
				}
				if (key == null) {
					Thread.yield();
					continue;
				}

				for (WatchEvent<?> event : key.pollEvents()) {
					WatchEvent.Kind<?> kind = event.kind();

					@SuppressWarnings("unchecked")
					WatchEvent<Path> ev = (WatchEvent<Path>) event;
					Path filename = dir.resolve(ev.context());

					if (kind == StandardWatchEventKinds.OVERFLOW) {
						Thread.yield();
						continue;
					} else if (kind == java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY
							&& filename.equals(fileToWatcher)) {
						doOnChange();
					}
					boolean valid = key.reset();
					if (!valid) {
						break;
					}
				}
				Thread.yield();
			}
		} catch (Throwable e) {
		}

	}

	private void doOnChange() {
		logger.info("{} changed.", APPLICATION_CONFIGFILE_DEV);
	}

}
