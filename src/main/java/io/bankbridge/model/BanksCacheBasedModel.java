package io.bankbridge.model;

import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BanksCacheBasedModel {
	private String id;
	private String name;
	private String countryCode;
	private ArrayList<String> products;
}
