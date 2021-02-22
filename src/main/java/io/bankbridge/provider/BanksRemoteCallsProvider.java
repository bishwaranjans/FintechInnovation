package io.bankbridge.provider;

import io.bankbridge.model.BankModel;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.asynchttpclient.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Logger;

public class BanksRemoteCallsProvider implements IBanksProvider {

    private static final Logger logger = Logger.getLogger(BanksRemoteCallsProvider.class.getName());
    private final Map<String, String> config;
    private final AsyncHttpClient client;
    private final ObjectMapper objectMapper;

    public BanksRemoteCallsProvider() throws IOException {
        DefaultAsyncHttpClientConfig.Builder clientBuilder = Dsl.config().setConnectTimeout(500);
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

    @Override
    public List<BankModel> getBankDetails() {
        List<BankModel> banksList = new ArrayList<BankModel>();
        for (Map.Entry<String, String> entry : config.entrySet()) {
            getRemoteBankDetails(entry.getKey(), entry.getValue(), banksList);
        }
        return banksList;
    }

    public void getRemoteBankDetails(String name, String url, List<BankModel> result) {
        Request unboundRequest = Dsl.get(url).build();
        Future<Response> responseFuture = client.executeRequest(unboundRequest);

        try {
            Response response = responseFuture.get();
            String responseText = response.getResponseBody();
            BankModel bank = objectMapper.readValue(responseText, BankModel.class);
            result.add(bank);
        } catch (Exception e) {
            logger.info("Error occurred while retrieving details for Bank: " + name);
        }
    }
}
