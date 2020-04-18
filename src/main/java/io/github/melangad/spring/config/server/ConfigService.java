package io.github.melangad.spring.config.server;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.melangad.spring.config.server.entity.Config;
import io.github.melangad.spring.config.server.entity.ConfigHistory;
import io.github.melangad.spring.config.server.model.ConfigDetailDAO;
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

	/**
	 * Get Config DAO from the Database
	 * 
	 * @param application Application ID
	 * @return Config details
	 */
	public Optional<ConfigDetailDAO> getConfig(String application) {

		ConfigDetailDAO configDetails = null;

		final List<Config> list = configRepository.findByApplication(application);

		if (list.stream().findFirst().isPresent()) {
			final Config config = list.stream().findFirst().get();

			configDetails = this.convertToConfigDetailDAO(config);

		}

		return Optional.ofNullable(configDetails);
	}

	/**
	 * Create new configuration
	 * 
	 * @param application Application ID
	 * @param configs     Config map
	 * @return Config Details DAO
	 * @throws ApplicationAlreadyExisitException
	 * @throws JsonProcessingException
	 */
	public Optional<ConfigDetailDAO> createConfig(String application, Map<String, ConfigMetaDAO> configs)
			throws ApplicationAlreadyExisitException, InvalidConfigException {

		final Config config = new Config();
		config.setApplication(application);
		config.setConfigVersion(1);

		try {
			config.setValue(this.mapper.writeValueAsString(configs));
		} catch (JsonProcessingException e) {
			log.error(e.getMessage());
			throw new InvalidConfigException();
		}

		try {
			this.configRepository.save(config);
		} catch (Exception se) {
			if (se instanceof SQLIntegrityConstraintViolationException || se instanceof DataIntegrityViolationException) {
				throw new ApplicationAlreadyExisitException();
			} else {
				log.error(se.getMessage(), se);
			}
			
		}

		return Optional.ofNullable(this.convertToConfigDetailDAO(config));

	}

	/**
	 * Update Config. this will also add a history record and bump up the version
	 * 
	 * @param application Application ID
	 * @param configs     Config Map
	 * @return updated Config details
	 * @throws InvalidApplicationException
	 * @throws InvalidConfigException
	 */
	public ConfigDetailDAO updateConfig(String application, Map<String, ConfigMetaDAO> configs)
			throws InvalidApplicationException, InvalidConfigException {
		Config config = null;
		final List<Config> list = configRepository.findByApplication(application);

		if (list.stream().findFirst().isPresent()) {
			config = list.stream().findFirst().get();

			//Create History Object
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
			} catch (Exception se) {
				log.error(se.getMessage(), se);
			}

		} else {
			throw new InvalidApplicationException();
		}

		return this.convertToConfigDetailDAO(config);

	}

	private Map<String, ConfigMetaDAO> getConfigMap(String json) throws JsonMappingException, JsonProcessingException {
		return mapper.readValue(json, new TypeReference<Map<String, ConfigMetaDAO>>() {
		});
	}

	private ConfigDetailDAO convertToConfigDetailDAO(Config config) {
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
		history.setApplication(config.getApplication());
		history.setConfigVersion(config.getConfigVersion());
		history.setValue(config.getValue());
		history.setUpdateTime(config.getUpdateTime());
		
		return history;
	}

}
