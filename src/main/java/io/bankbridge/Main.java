package io.bankbridge;

import static spark.Spark.get;
import static spark.Spark.port;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.bankbridge.handler.BanksRequestHandler;
import io.bankbridge.provider.BanksCacheBasedProvider;
import io.bankbridge.provider.BanksRemoteCallsProvider;
import io.bankbridge.seedwork.CacheHelper;

public class Main {

	public static void main(String[] args) throws Exception {

		port(8080);

		BanksRequestHandler banksCacheBasedHandler = new BanksRequestHandler(new BanksCacheBasedProvider(),
				new CacheHelper(), new ObjectMapper());

		BanksRequestHandler banksRemoteCallsHandler = new BanksRequestHandler(new BanksRemoteCallsProvider(),
				new CacheHelper(), new ObjectMapper());

		get("/v1/banks/all", (request, response) -> banksCacheBasedHandler.handle(request, response));
		get("/v2/banks/all", (request, response) -> banksRemoteCallsHandler.handle(request, response));
	}
}