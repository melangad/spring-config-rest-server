package io.github.melangad.spring.config.server;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.github.melangad.spring.config.server.model.ConfigMetaDAO;

public class DuplicateKeysException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	List<ConfigMetaDAO> duplicateList = new ArrayList<ConfigMetaDAO>();

	public DuplicateKeysException(List<ConfigMetaDAO> duplicateList) {
		this.duplicateList = duplicateList;
	}

	@Override
	public String getMessage() {

		return this.duplicateList.stream().map(c -> c.getKey()).collect(Collectors.joining(","));

	}

}
