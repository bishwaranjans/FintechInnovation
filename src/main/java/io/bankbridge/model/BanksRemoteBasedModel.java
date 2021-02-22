package io.bankbridge.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Model to return the v2(remote calls based) preferred properties. */
@Getter
@AllArgsConstructor
public class BanksRemoteBasedModel {
	private String id;
	private String name;
	private String countryCode;
	private String auth;
}
