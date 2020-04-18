package io.github.melangad.spring.config.server.model;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfigDetailDAO {
	private int version;
	private Map<String, ConfigMetaDAO> configData = new HashMap<String, ConfigMetaDAO>();

}
