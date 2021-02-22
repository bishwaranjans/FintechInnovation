package io.bankbridge.provider;

import io.bankbridge.model.BankModel;
import io.bankbridge.model.BankModelList;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/** Class to provide Cache based bank details. */
public class BanksCacheBasedProvider implements IBanksProvider {

    private static final Logger logger = Logger.getLogger(BanksCacheBasedProvider.class.getName());
    private final List<BankModel> cachedBanksList;

    /**
     * Constructor for initializing cache based bankd details. For any IO error, it
     * will log the error and then will throw an exception.
     * 
     * @throws IOException
     */
    public BanksCacheBasedProvider() throws IOException {
        try {
            BankModelList bankModelList = new ObjectMapper().readValue(
                    Thread.currentThread().getContextClassLoader().getResource("banks-v1.json"), BankModelList.class);
            cachedBanksList = bankModelList.getBanks();
        } catch (Exception e) {
            logger.info(e.getMessage());
            throw e;
        }
    }

    /** Method to get the cached bank details. */
    @Override
    public List<BankModel> getBankDetails() {
        return cachedBanksList;
    }
}
