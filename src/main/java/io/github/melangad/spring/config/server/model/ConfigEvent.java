package io.github.melangad.spring.config.server.model;

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class ConfigEvent implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@NonNull
	private String eventId;
	
	@NonNull
	private String label;
	
	@NonNull
	private ConfigEventType eventType;
	
	
	private Date eventDate;

}
