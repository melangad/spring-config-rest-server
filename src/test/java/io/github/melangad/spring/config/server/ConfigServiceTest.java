package io.github.melangad.spring.config.server;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit4.SpringRunner;

import io.github.melangad.spring.config.server.entity.Config;
import io.github.melangad.spring.config.server.model.ConfigDetailDAO;
import io.github.melangad.spring.config.server.model.ConfigMetaDAO;
import io.github.melangad.spring.config.server.repository.ConfigHistoryRepository;
import io.github.melangad.spring.config.server.repository.ConfigRepository;

@RunWith(SpringRunner.class)
public class ConfigServiceTest {

	@TestConfiguration
	static class ConfigServiceTestContextConfiguration {

		@Bean
		public ConfigService configService() {
			return new ConfigService();
		}
	}

	@Autowired
	private ConfigService configService;

	@MockBean
	private ConfigRepository configRepository;

	@MockBean
	private ConfigHistoryRepository configHistoryRepository;

	@Test
	public void createConfigSuccessful() throws ApplicationAlreadyExisitException, InvalidConfigException {

		Config config = new Config();

		Mockito.when(configRepository.save(Mockito.any())).thenReturn(config);

		ConfigMetaDAO meta = new ConfigMetaDAO();
		meta.setValue("val1");
		meta.setDescription("desc1");

		Map<String, ConfigMetaDAO> configMap = new HashMap<String, ConfigMetaDAO>();
		configMap.put("SOME-KEY", meta);

		Optional<ConfigDetailDAO> result = configService.createConfig("APP1", configMap);

		assertThat(result.get()).isNotNull();
		assertThat(result.get().getVersion()).isEqualTo(1);
		assertThat(result.get().getConfigData().size()).isEqualTo(1);
		assertThat(result.get().getConfigData().get("SOME-KEY").getValue()).isEqualTo("val1");
		assertThat(result.get().getConfigData().get("SOME-KEY").getDescription()).isEqualTo("desc1");
	}
	
	@Test(expected = ApplicationAlreadyExisitException.class)
	public void createConfigFailExistingApplication() throws ApplicationAlreadyExisitException, InvalidConfigException {

		Mockito.when(configRepository.save(Mockito.any())).thenThrow(DataIntegrityViolationException.class);
		
		ConfigMetaDAO meta = new ConfigMetaDAO();
		meta.setValue("val1");
		meta.setDescription("desc1");

		Map<String, ConfigMetaDAO> configMap = new HashMap<String, ConfigMetaDAO>();
		configMap.put("SOME-KEY", meta);

		configService.createConfig("APP1", configMap);
	}


	@Test
	public void getConfigSuccessful() {

		String json = "{\"SOME-KEY\":{\"value\":\"val1\",\"description\":\"desc1\"}}";

		Config config = new Config();
		config.setConfigVersion(3);
		config.setApplication("APP1");
		config.setValue(json);
		
		List<Config> list = new ArrayList<Config>();
		list.add(config);

		Mockito.when(configRepository.findByApplication("APP1")).thenReturn(list);

		Optional<ConfigDetailDAO> result = configService.getConfig("APP1");

		assertThat(result.get()).isNotNull();
		assertThat(result.get().getVersion()).isEqualTo(3);
		assertThat(result.get().getConfigData().size()).isEqualTo(1);
		assertThat(result.get().getConfigData().get("SOME-KEY").getValue()).isEqualTo("val1");
		assertThat(result.get().getConfigData().get("SOME-KEY").getDescription()).isEqualTo("desc1");
	}
	
	@Test
	public void getConfigInvalidApplication() {
		
		Mockito.when(configRepository.findByApplication("APP1")).thenReturn(Collections.emptyList());

		Optional<ConfigDetailDAO> result = configService.getConfig("APP1");

		assertThat(result.isPresent()).isFalse();
	}
	
	
	

}
