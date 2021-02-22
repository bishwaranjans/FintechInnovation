package io.bankbridge.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/** Model List for containing bank details. */
@Getter
@Setter
public class BankModelList {
	private List<BankModel> banks;
}
