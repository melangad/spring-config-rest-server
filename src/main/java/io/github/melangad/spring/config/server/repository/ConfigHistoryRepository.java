package io.github.melangad.spring.config.server.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import io.github.melangad.spring.config.server.entity.ConfigHistory;

public interface ConfigHistoryRepository extends CrudRepository<ConfigHistory, Integer> {

	List<ConfigHistory> findByLabel(String application);

	List<ConfigHistory> findByLabelAndConfigVersion(String application, int configVersion);

}
