package io.github.melangad.spring.config.server;

import io.github.melangad.spring.config.server.model.ConfigEvent;

public interface ConfigEventDispatcher {
	
	public void dispatchEvent(ConfigEvent configEvent);

}
