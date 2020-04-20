package io.github.melangad.spring.config.server.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import io.github.melangad.spring.config.server.entity.Config;

public interface ConfigRepository extends CrudRepository<Config, Integer> {

	List<Config> findByLabel(String label);

}
