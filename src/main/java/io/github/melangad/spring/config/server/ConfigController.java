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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.melangad.spring.config.server.model.ClientFeedback;
import io.github.melangad.spring.config.server.model.ConfigDetailDAO;
import io.github.melangad.spring.config.server.model.ConfigMetaDAO;
import io.github.melangad.spring.config.server.model.ErrorDAO;

@RestController
@RequestMapping("/config")
public class ConfigController {

	@Autowired
	private ConfigService configService;

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
	public ResponseEntity<?> createConfig(@PathVariable String label,
			@RequestBody Map<String, ConfigMetaDAO> configs) {
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
		}

		return response;
	}

	@PatchMapping("/{label}")
	public ResponseEntity<?> patchConfig(@PathVariable String label,
			@RequestBody Map<String, ConfigMetaDAO> configs) {
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
		}

		return response;
	}

	@PutMapping("/{label}")
	public ResponseEntity<?> updateConfig(@PathVariable String label,
			@RequestBody Map<String, ConfigMetaDAO> configs) {
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
		}

		return response;
	}

	@PostMapping("/feedback")
	public ResponseEntity<?> getFeedback(@RequestBody ClientFeedback clientFeedback) {
		
		this.configService.processClientFeedback(clientFeedback);

		return ResponseEntity.ok().build();
	}

}
