package io.github.melangad.spring.config.server.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CONFIG")
@Getter
@Setter
public class Config {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	@Column(unique = true, name = "LABEL")
	private String label;

	@Column(name = "CONFIG_VERSION")
	private Integer configVersion;

	@Column(name = "CONFIG_VALUE")
	private String value;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "UPDATE_TIME")
	private Date updateTime = new Date();

	@PreUpdate
	protected void onUpdate() {
		updateTime = new Date();
	}

	public void increaseVersion() {
		this.configVersion++;
	}

}
