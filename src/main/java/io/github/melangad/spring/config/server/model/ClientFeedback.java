package io.github.melangad.spring.config.server.model;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientFeedback {
	
	private String label;
	private String clientId;
	private int clientVersion;
	private Date lastUpdateTime;

}
