package io.github.melangad.spring.config.server.model;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfigPushEvent {
	private int configVersion = 0;
	private Date updateTime = new Date();

}
