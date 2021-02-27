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

/**
 * Class to handle the bank APIs requests. For both the versions, single handler
 * is used as for both same handling mechanism used.
 */
public class BanksRequestHandler {

    private static final Logger logger = Logger.getLogger(BanksRequestHandler.class.getName());
    private final IBanksProvider banksProvider;
    private final ObjectMapper objectMapper;
    private final CacheHelper cacheHelper;
    private final BanksRequestFilter banksRequestFilter;

    /**
     * Constructor for BanksRequestHandler. Necessary initializers are injected via
     * the constructor.
     * 
     * @param banksProvider
     * @param cacheHelper
     * @param objectMapper
     */
    public BanksRequestHandler(IBanksProvider banksProvider, CacheHelper cacheHelper, ObjectMapper objectMapper) {
        this.banksProvider = banksProvider;
        this.cacheHelper = cacheHelper;
        this.objectMapper = objectMapper;

        banksRequestFilter = new BanksRequestFilter();
    }

    /**
     * Method to handle the bank APIs requests. For both v1 and v2, this handler is
     * used. This method will accept the request, will try to apply the filters and
     * then returns the result as a string. On first-time request, it will cached
     * the whole result and on successive requests, it will use the cache. For any
     * error, it will returns an exception.
     * 
     * @param request
     * @param response
     * @return Paginated and Filtered results in a string.
     */
    public String handle(Request request, Response response) {

        BankModelList bankModelList = new BankModelList();
        BankRequestFilterModel requestParam = banksRequestFilter.getRequestFilters(request);

        try {
            /** Populate from cache if available. If not re-try. */
            if (this.cacheHelper.cacheDataList.containsKey(Constants.CACHE_BANKS)) {
                bankModelList = this.cacheHelper.cacheDataList.get(Constants.CACHE_BANKS);
            } else {
                for (int i = 0; i < Constants.DEFAULT_RETRY_COUNT; i++) {
                    bankModelList.setBanks(this.banksProvider.getBankDetails());
                    if (!bankModelList.getBanks().isEmpty()) {
                        this.cacheHelper.putInList(bankModelList);
                        break;
                    }
                }

                if (bankModelList.getBanks().isEmpty()) { /** Returns as there is no data to show and filter. */
                    return "No banks data available! Make sure to restart the ports 8080 & 1234 and retry again.";
                }
            }

            /** Filter the banks */
            List<BankModel> banksList = banksRequestFilter.getFilterBankModels(bankModelList.getBanks(), requestParam);

            /** Returns the desired model as per version */
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
