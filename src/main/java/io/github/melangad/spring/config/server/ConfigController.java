package io.github.melangad.spring.config.server;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.github.melangad.spring.config.server.model.ClientFeedback;
import io.github.melangad.spring.config.server.model.ConfigDetailDAO;
import io.github.melangad.spring.config.server.model.ConfigMetaDAO;
import io.github.melangad.spring.config.server.model.ErrorDAO;

@RestController
public class ConfigController {

	@Autowired
	private ConfigService configService;

	@GetMapping("/config/{application}")
	public ResponseEntity<?> getConfig(@PathVariable String application) {
		ResponseEntity<?> response = ResponseEntity.notFound().build();

		Optional<ConfigDetailDAO> data = this.configService.getConfig(application);

		if (data.isPresent()) {
			response = ResponseEntity.ok(data.get());
		} else {
			response = ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDAO("Application ID Not Found"));
		}

		return response;
	}

	@PostMapping("/config/{application}")
	public ResponseEntity<?> createConfig(@PathVariable String application,
			@RequestBody Map<String, ConfigMetaDAO> configs) {
		ResponseEntity<?> response = ResponseEntity.badRequest().build();

		try {
			Optional<ConfigDetailDAO> data = this.configService.createConfig(application, configs);
			if (data.isPresent()) {
				response = ResponseEntity.ok(data.get());
			}
		} catch (ApplicationAlreadyExisitException e) {
			response = ResponseEntity.badRequest().body(new ErrorDAO("Application ID Already Exisit"));
		} catch (InvalidConfigException e) {
			response = ResponseEntity.badRequest().body(new ErrorDAO("Invalid Config Provided"));
		}

		return response;
	}

	@PatchMapping("/config/{application}")
	public ResponseEntity<?> patchConfig(@PathVariable String application,
			@RequestBody Map<String, ConfigMetaDAO> configs) {
		ResponseEntity<?> response = ResponseEntity.notFound().build();

		try {
			ConfigDetailDAO data = this.configService.patchConfig(application, configs);
			if (null != data) {
				response = ResponseEntity.ok(data);
			}
		} catch (InvalidApplicationException e) {
			response = ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDAO("Application ID Not Found"));
		} catch (InvalidConfigException e) {
			response = ResponseEntity.badRequest().body(new ErrorDAO("Invalid Config Provided"));
		}

		return response;
	}

	@PutMapping("/config/{application}")
	public ResponseEntity<?> updateConfig(@PathVariable String application,
			@RequestBody Map<String, ConfigMetaDAO> configs) {
		ResponseEntity<?> response = ResponseEntity.notFound().build();

		try {
			ConfigDetailDAO data = this.configService.updateConfig(application, configs);
			if (null != data) {
				response = ResponseEntity.ok(data);
			}
		} catch (InvalidApplicationException e) {
			response = ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDAO("Application ID Not Found"));
		} catch (InvalidConfigException e) {
			response = ResponseEntity.badRequest().body(new ErrorDAO("Invalid Config Provided"));
		}

		return response;
	}

	@PostMapping("/config/feedback")
	public ResponseEntity<?> getFeedback(@RequestBody ClientFeedback clientFeedback) {
		
		this.configService.processClientFeedback(clientFeedback);

		return ResponseEntity.ok().build();
	}

}
