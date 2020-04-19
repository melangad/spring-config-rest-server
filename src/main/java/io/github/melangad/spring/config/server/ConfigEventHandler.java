package io.github.melangad.spring.config.server;

import io.github.melangad.spring.config.server.model.ConfigEvent;

public interface ConfigEventHandler {
	
	public void onEvent(ConfigEvent configEvent);

}
