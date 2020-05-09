package io.github.melangad.spring.config.server;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;

import io.github.melangad.spring.config.server.model.ConfigPushEvent;

/**
 * <h1>Config Push Service</h1> Push message service for configuration management.
 * Push messages are based on Server Sent Event and Config Push Service manage
 * all Server Sent Event emitters relate to specific label
 * 
 * @author melanga
 *
 */

@Service
public class ConfigPushService {

	private Map<String, List<SseEmitter>> clientMap = new ConcurrentHashMap<String, List<SseEmitter>>();

	
	/**
	 * Register a client which listen to Server Sent Emitter under given label
	 * @param label is a label
	 * @param emitter is a Server Sent Emitter client connection
	 */
	public void addEmitter(final String label, final SseEmitter emitter) {
		List<SseEmitter> list = new CopyOnWriteArrayList<SseEmitter>();

		if (null != this.clientMap.get(label)) {
			list = this.clientMap.get(label);
		} else {
			this.clientMap.put(label, list);
		}

		list.add(emitter);
	}

	/**
	 * De-register a client from the Push nootification service
	 * @param label is a label
	 * @param emitter is a Server Sent Emitter client connection
	 */
	public void removeEmitter(final String label, final SseEmitter emitter) {
		if (!this.clientMap.get(label).isEmpty()) {
			List<SseEmitter> list = this.clientMap.get(label);
			list.remove(emitter);
		}
	}

	/**
	 * Notify all registered clients under given label with regard to configuration update
	 * @param label is a label
	 */
	@Async
	public void doNotify(final String label) {
		String id = UUID.randomUUID().toString();
		ConfigPushEvent configEvent = new ConfigPushEvent();
		configEvent.setConfigVersion(3);

		SseEventBuilder event = SseEmitter.event().id(id).data(configEvent).name("CONFIG-UPDATE-EVENT")
				.reconnectTime(5000);

		List<SseEmitter> list = this.clientMap.get(label);
		if (null != list) {
			List<SseEmitter> stalelList = new CopyOnWriteArrayList<SseEmitter>();
			list.stream().forEach(emitter -> {
				try {
					emitter.send(event);
				} catch (IOException e) {
					stalelList.add(emitter);
				}
			});

			stalelList.stream().forEach(e -> {
				list.remove(e);
			});
		}
	}

}
