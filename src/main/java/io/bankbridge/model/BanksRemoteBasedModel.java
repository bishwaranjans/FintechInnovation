package io.bankbridge.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BanksRemoteBasedModel {
	private String id;
	private String name;
	private String countryCode;
	private String auth;
}
