package io.bankbridge.model;

import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** Model is used for populating generic bank details. */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class BankModel {
	private String bic;
	private String name;
	private String countryCode;
	private String auth;
	private ArrayList<String> products;
}
