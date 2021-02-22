package io.bankbridge.handler;

import java.util.List;
import java.util.stream.Collectors;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.bankbridge.filter.BanksRequestFilter;
import io.bankbridge.model.BankModel;
import io.bankbridge.model.BankModelList;
import io.bankbridge.model.BanksCacheBasedModel;
import io.bankbridge.model.BanksRemoteBasedModel;
import io.bankbridge.model.BankRequestFilterModel;
import io.bankbridge.provider.BanksCacheBasedProvider;
import io.bankbridge.provider.IBanksProvider;
import io.bankbridge.seedwork.CacheHelper;
import io.bankbridge.seedwork.Constants;

import spark.Request;
import spark.Response;

public class BanksRequestHandler {

    private static final Logger logger = Logger.getLogger(BanksRequestHandler.class.getName());
    private final IBanksProvider banksProvider;
    private final ObjectMapper objectMapper;
    private final CacheHelper cacheHelper;
    private final BanksRequestFilter banksRequestFilter;

    public BanksRequestHandler(IBanksProvider banksProvider, CacheHelper cacheHelper, ObjectMapper objectMapper) {
        this.banksProvider = banksProvider;
        this.cacheHelper = cacheHelper;
        this.objectMapper = objectMapper;

        banksRequestFilter = new BanksRequestFilter();
    }

    public String handle(Request request, Response response) {

        BankModelList bankModelList = new BankModelList();
        BankRequestFilterModel requestParam = banksRequestFilter.getRequestFilters(request);

        try {
            // Populate from cache if available
            if (this.cacheHelper.cacheDataList.containsKey(Constants.CACHE_BANKS)) {
                bankModelList = this.cacheHelper.cacheDataList.get(Constants.CACHE_BANKS);
            } else {
                bankModelList.setBanks(this.banksProvider.getBankDetails());
                this.cacheHelper.putInList(bankModelList);
            }

            // Filter the banks
            List<BankModel> banksList = banksRequestFilter.getFilterBankModels(bankModelList.getBanks(), requestParam);

            // Returns the desired model as per version
            return this.objectMapper.writeValueAsString(this.banksProvider instanceof BanksCacheBasedProvider
                    ? banksList.stream()
                            .map(b -> new BanksCacheBasedModel(b.getBic(), b.getName(), b.getCountryCode(),
                                    b.getProducts()))
                            .collect(Collectors.toList())
                    : banksList.stream().map(
                            b -> new BanksRemoteBasedModel(b.getBic(), b.getName(), b.getCountryCode(), b.getAuth()))
                            .collect(Collectors.toList()));
        } catch (JsonProcessingException e) {
            logger.info(e.getMessage());
            throw new RuntimeException("Error while processing request");
        }
    }
}
