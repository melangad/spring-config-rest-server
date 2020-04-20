package io.github.melangad.spring.config.server;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.melangad.spring.config.server.entity.Config;
import io.github.melangad.spring.config.server.entity.ConfigHistory;
import io.github.melangad.spring.config.server.model.ClientFeedback;
import io.github.melangad.spring.config.server.model.ConfigDetailDAO;
import io.github.melangad.spring.config.server.model.ConfigEvent;
import io.github.melangad.spring.config.server.model.ConfigEventType;
import io.github.melangad.spring.config.server.model.ConfigMetaDAO;
import io.github.melangad.spring.config.server.repository.ConfigHistoryRepository;
import io.github.melangad.spring.config.server.repository.ConfigRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ConfigService {

	@Autowired
	private ConfigRepository configRepository;

	private final ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private ConfigHistoryRepository configHistoryRepository;

	@Autowired(required = false)
	private ConfigEventHandler configEventHandler;

	@Autowired(required = false)
	private ClientFeedbackHandler clientFeedbackHandler;

	/**
	 * Get Config DAO from the Database
	 * 
	 * @param label Label
	 * @return Config details
	 */
	public Optional<ConfigDetailDAO> getConfig(final String label) {

		ConfigDetailDAO configDetails = null;

		final List<Config> list = configRepository.findByLabel(label);

		if (list.stream().findFirst().isPresent()) {
			final Config config = list.stream().findFirst().get();

			configDetails = this.convertToConfigDetailDAO(config);

		}

		return Optional.ofNullable(configDetails);
	}

	/**
	 * Create new configuration
	 * 
	 * @param label Label
	 * @param configs     Config map
	 * @return Config Details DAO
	 * @throws LabelAlreadyExisitException
	 * @throws JsonProcessingException
	 */
	public Optional<ConfigDetailDAO> createConfig(final String label, Map<String, ConfigMetaDAO> configs)
			throws LabelAlreadyExisitException, InvalidConfigException {

		final Config config = new Config();
		config.setLabel(label);
		config.setConfigVersion(1);

		try {
			config.setValue(this.mapper.writeValueAsString(configs));
		} catch (JsonProcessingException e) {
			log.error(e.getMessage());
			throw new InvalidConfigException();
		}

		try {
			this.configRepository.save(config);
			this.dispatchEvent(ConfigEventType.CONFIG_CREATE, label);
		} catch (Exception se) {
			if (se instanceof SQLIntegrityConstraintViolationException
					|| se instanceof DataIntegrityViolationException) {
				throw new LabelAlreadyExisitException();
			} else {
				log.error(se.getMessage(), se);
			}

		}

		return Optional.ofNullable(this.convertToConfigDetailDAO(config));

	}

	/**
	 * Patch Config. this will also add a history record and bump up the version.
	 * This will update provided properties or add new properties (delta update)
	 * 
	 * @param label Label
	 * @param configs     Config Map
	 * @return updated Config details
	 * @throws InvalidLabelException
	 * @throws InvalidConfigException
	 */
	public ConfigDetailDAO patchConfig(final String label, Map<String, ConfigMetaDAO> configs)
			throws InvalidLabelException, InvalidConfigException {
		Config config = null;
		final List<Config> list = configRepository.findByLabel(label);

		if (list.stream().findFirst().isPresent()) {
			config = list.stream().findFirst().get();

			// Create History Object
			ConfigHistory history = this.convertToConfigHistory(config);

			// Update Config data
			config.increaseVersion();

			try {
				Map<String, ConfigMetaDAO> currentConfig = this.getConfigMap(config.getValue());

				configs.forEach((k, v) -> {
					currentConfig.put(k, v);
				});

				config.setValue(this.mapper.writeValueAsString(currentConfig));
			} catch (JsonProcessingException e) {
				log.error(e.getMessage());
				throw new InvalidConfigException();
			}

			try {
				config = this.configRepository.save(config);
				this.configHistoryRepository.save(history);

				this.dispatchEvent(ConfigEventType.CONFIG_PATCH, label);
			} catch (Exception se) {
				log.error(se.getMessage(), se);
			}

		} else {
			throw new InvalidLabelException();
		}

		return this.convertToConfigDetailDAO(config);

	}

	/**
	 * Update Config. this will also add a history record and bump up the version
	 * This would replace existing configurations with provided set
	 * 
	 * @param label Label
	 * @param configs     Config Map
	 * @return updated Config details
	 * @throws InvalidLabelException
	 * @throws InvalidConfigException
	 */
	public ConfigDetailDAO updateConfig(final String label, Map<String, ConfigMetaDAO> configs)
			throws InvalidLabelException, InvalidConfigException {
		Config config = null;
		final List<Config> list = configRepository.findByLabel(label);

		if (list.stream().findFirst().isPresent()) {
			config = list.stream().findFirst().get();

			// Create History Object
			ConfigHistory history = this.convertToConfigHistory(config);

			// Update Config data
			config.increaseVersion();
			try {
				config.setValue(this.mapper.writeValueAsString(configs));
			} catch (JsonProcessingException e) {
				log.error(e.getMessage());
				throw new InvalidConfigException();
			}

			try {
				config = this.configRepository.save(config);
				this.configHistoryRepository.save(history);

				this.dispatchEvent(ConfigEventType.CONFIG_UPDATE, label);
			} catch (Exception se) {
				log.error(se.getMessage(), se);
			}

		} else {
			throw new InvalidLabelException();
		}

		return this.convertToConfigDetailDAO(config);

	}

	/**
	 * Process Client Feedback
	 * 
	 * @param clientFeedback Client Feedback
	 */
	public void processClientFeedback(ClientFeedback clientFeedback) {
		if (null != this.clientFeedbackHandler) {
			this.clientFeedbackHandler.onClientFeedback(clientFeedback);
		}
	}

	@Async
	private void dispatchEvent(ConfigEventType eventType, final String label) {
		if (null != this.configEventHandler) {
			final ConfigEvent event = new ConfigEvent(UUID.randomUUID().toString(), label, eventType);
			event.setEventDate(new Date());

			this.configEventHandler.onEvent(event);
		}
	}

	private Map<String, ConfigMetaDAO> getConfigMap(final String json)
			throws JsonMappingException, JsonProcessingException {
		return mapper.readValue(json, new TypeReference<Map<String, ConfigMetaDAO>>() {
		});
	}

	private ConfigDetailDAO convertToConfigDetailDAO(final Config config) {
		final ConfigDetailDAO configDetails = new ConfigDetailDAO();
		configDetails.setVersion(config.getConfigVersion());
		try {
			configDetails.setConfigData(this.getConfigMap(config.getValue()));
		} catch (JsonProcessingException e) {
			log.error(e.getMessage());
		}

		return configDetails;
	}

	private ConfigHistory convertToConfigHistory(Config config) {
		ConfigHistory history = new ConfigHistory();
		history.setLabel(config.getLabel());
		history.setConfigVersion(config.getConfigVersion());
		history.setValue(config.getValue());
		history.setUpdateTime(config.getUpdateTime());

		return history;
	}

}
