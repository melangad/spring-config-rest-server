package io.github.melangad.spring.config.server;

import io.github.melangad.spring.config.server.model.ClientFeedback;

public interface ClientFeedbackHandler {
	
	public void onClientFeedback(ClientFeedback clientFeedback);

}
