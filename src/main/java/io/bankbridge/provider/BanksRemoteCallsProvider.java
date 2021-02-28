package io.bankbridge.provider;

import io.bankbridge.model.BankModel;
import io.bankbridge.seedwork.Constants;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.asynchttpclient.*;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * Class to provide remote calls based bank details. The remote calls detail
 * will be retrieved from a config file. We are assuming that the remote API is
 * up and running. If not, it will log the error.
 */
public class BanksRemoteCallsProvider implements IBanksProvider {

    private static final Logger logger = Logger.getLogger(BanksRemoteCallsProvider.class.getName());
    private final Map<String, String> config;
    private final AsyncHttpClient client;
    private final ObjectMapper objectMapper;

    /**
     * Constructor to initialize the HTTP client,object mapper and reads out the
     * remote calls config details. For the HTTP client config, a timeout and configurable retry option has been set.
     * 
     * @throws IOException
     */
    public BanksRemoteCallsProvider() throws IOException {
        DefaultAsyncHttpClientConfig.Builder clientBuilder = Dsl.config().setConnectTimeout(500)
                .setMaxRequestRetry(Constants.DEFAULT_RETRY_COUNT);
        client = Dsl.asyncHttpClient(clientBuilder);
        objectMapper = new ObjectMapper();

        try {
            config = new ObjectMapper()
                    .readValue(Thread.currentThread().getContextClassLoader().getResource("banks-v2.json"), Map.class);
        } catch (Exception e) {
            logger.info(e.getMessage());
            throw e;
        }
    }

    /** Method to get all the bank details from the remote calls. */
    @Override
    public List<BankModel> getBankDetails() {
        List<BankModel> banksList = new ArrayList<BankModel>();
        for (Map.Entry<String, String> entry : config.entrySet()) {
            getRemoteBankDetails(entry.getKey(), entry.getValue(), banksList);
        }
        return banksList;
    }

    /**
     * Method to get the bank details by doing an HTTP clinet call. We are doing a
     * async call now, but waiting for it to finish. Once ready, we can apply the
     * pagination and filter only.
     * 
     * @param name
     * @param url
     * @param result
     */
    private void getRemoteBankDetails(String name, String url, List<BankModel> result) {

        CompletableFuture<Response> whenResponse = client.prepareGet(url).execute().toCompletableFuture();

        whenResponse = whenResponse.thenApply(response -> {
            String responseText = response.getResponseBody();
            try {
                BankModel bank = objectMapper.readValue(responseText, BankModel.class);
                /** Check for unique id of bank. Add to list if it is unique */
                if (!result.stream().anyMatch(r -> r.getBic().equalsIgnoreCase(bank.getBic()))) {
                    result.add(bank);
                } else {
                    logger.info("Duplicate banks found with id: " + bank.getBic()
                            + ". Please map unique name with unique API end point.");
                }

            } catch (Exception ex) {
                logger.info("Error occurred while parsing. Error details: " + ex.getMessage() + ". Response Text: "
                        + responseText);
            }
            return response;
        }).exceptionally(p -> {
            logger.info(
                    "Error occurred while retrieving details for Bank: " + name + ". Error details: " + p.getMessage());
            return null;
        });

        whenResponse.join(); // wait for completion
    }
}
