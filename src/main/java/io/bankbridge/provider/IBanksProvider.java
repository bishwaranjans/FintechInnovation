package io.bankbridge.provider;

import io.bankbridge.model.BankModel;

import java.util.List;

/**
 * Interface to get bank details. The concreate implementation needs to inherit
 * this and implement their own logic.
 */
public interface IBanksProvider {
    List<BankModel> getBankDetails();
}
