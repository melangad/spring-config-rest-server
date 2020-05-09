package io.github.melangad.spring.config.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import io.github.melangad.spring.config.server.model.ClientFeedback;
import io.github.melangad.spring.config.server.model.ConfigDetailDAO;
import io.github.melangad.spring.config.server.model.ConfigMetaDAO;
import io.github.melangad.spring.config.server.model.ErrorDAO;

@RestController
@RequestMapping("/config")
@CrossOrigin("*")
public class ConfigController {

	@Autowired
	private ConfigService configService;

	@Autowired
	ConfigPushService configPushService;
	
	@GetMapping("/")
	public ResponseEntity<?> getLabelList() {
		List<String> labelList = new ArrayList<String>();

		labelList.addAll(this.configService.getLabelList());
		labelList = labelList.stream().map(String::toUpperCase).collect(Collectors.toList());
		Collections.sort(labelList);
		

		return ResponseEntity.ok(labelList);
	}

	@GetMapping("/{label}")
	public ResponseEntity<?> getConfig(@PathVariable String label) {
		ResponseEntity<?> response = ResponseEntity.notFound().build();

		Optional<ConfigDetailDAO> data = this.configService.getConfig(label);

		if (data.isPresent()) {
			response = ResponseEntity.ok(data.get());
		} else {
			response = ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDAO("Label Not Found"));
		}

		return response;
	}

	@PostMapping("/{label}")
	public ResponseEntity<?> createConfig(@PathVariable String label, @RequestBody List<ConfigMetaDAO> configs) {
		ResponseEntity<?> response = ResponseEntity.badRequest().build();

		try {
			Optional<ConfigDetailDAO> data = this.configService.createConfig(label, configs);
			if (data.isPresent()) {
				response = ResponseEntity.ok(data.get());
			}
		} catch (LabelAlreadyExisitException e) {
			response = ResponseEntity.badRequest().body(new ErrorDAO("Label Already Exisit"));
		} catch (InvalidConfigException e) {
			response = ResponseEntity.badRequest().body(new ErrorDAO("Invalid Config Provided"));
		} catch (DuplicateKeysException de) {
			response = ResponseEntity.badRequest().body(new ErrorDAO("Duplicate Keys: " + de.getMessage()));
		}

		return response;
	}

	@PatchMapping("/{label}")
	public ResponseEntity<?> patchConfig(@PathVariable String label, @RequestBody List<ConfigMetaDAO> configs) {
		ResponseEntity<?> response = ResponseEntity.notFound().build();

		try {
			ConfigDetailDAO data = this.configService.patchConfig(label, configs);
			if (null != data) {
				response = ResponseEntity.ok(data);
			}
		} catch (InvalidLabelException e) {
			response = ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDAO("Label Not Found"));
		} catch (InvalidConfigException e) {
			response = ResponseEntity.badRequest().body(new ErrorDAO("Invalid Config Provided"));
		} catch (DuplicateKeysException de) {
			response = ResponseEntity.badRequest().body(new ErrorDAO("Duplicate Keys: " + de.getMessage()));
		}

		return response;
	}

	@PutMapping("/{label}")
	public ResponseEntity<?> updateConfig(@PathVariable String label, @RequestBody List<ConfigMetaDAO> configs) {
		ResponseEntity<?> response = ResponseEntity.notFound().build();

		try {
			ConfigDetailDAO data = this.configService.updateConfig(label, configs);
			if (null != data) {
				response = ResponseEntity.ok(data);
			}
		} catch (InvalidLabelException e) {
			response = ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDAO("Label Not Found"));
		} catch (InvalidConfigException e) {
			response = ResponseEntity.badRequest().body(new ErrorDAO("Invalid Config Provided"));
		} catch (DuplicateKeysException de) {
			response = ResponseEntity.badRequest().body(new ErrorDAO("Duplicate Keys: " + de.getMessage()));
		}

		return response;
	}

	@PostMapping("/feedback")
	public ResponseEntity<?> getFeedback(@RequestBody ClientFeedback clientFeedback) {

		this.configService.processClientFeedback(clientFeedback);

		return ResponseEntity.ok().build();
	}

	@GetMapping("/notification/{label}")
	public ResponseEntity<SseEmitter> doNotify(@PathVariable String label) throws InterruptedException, IOException {
		final SseEmitter emitter = new SseEmitter();

		configPushService.addEmitter(label, emitter);
		emitter.onCompletion(() -> {
			configPushService.removeEmitter(label, emitter);
			System.out.println("Removing Emitter");
		});
		emitter.onTimeout(() -> configPushService.removeEmitter(label, emitter));
		return new ResponseEntity<>(emitter, HttpStatus.OK);
	}

}
