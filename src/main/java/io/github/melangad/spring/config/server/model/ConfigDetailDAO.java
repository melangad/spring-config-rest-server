package io.github.melangad.spring.config.server.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfigDetailDAO {
	private int version;
	private List<ConfigMetaDAO> configData = new ArrayList<>();

}
