package io.bankbridge.model;

import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Model to return the v1(Cache based) preferred properties. */
@Getter
@AllArgsConstructor
public class BanksCacheBasedModel {
	private String id;
	private String name;
	private String countryCode;
	private ArrayList<String> products;
}
