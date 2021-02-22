package io.bankbridge.provider;

import io.bankbridge.model.BankModel;

import java.util.List;

public interface IBanksProvider {
    List<BankModel> getBankDetails();
}
