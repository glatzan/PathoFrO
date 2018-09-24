package com.patho.main.util.version;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VersionContainer {

	private List<Version> versions;

	private String currentVersion;

	public VersionContainer() {

	}

	public VersionContainer(List<Version> versions) {
		this.versions = versions;

		if (versions != null)
			currentVersion = versions.get(0).getVersion();
	}
}
