package io.bankbridge.handler;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static spark.Spark.stop;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import io.bankbridge.MockRemotes;
import io.bankbridge.provider.BanksCacheBasedProvider;
import io.bankbridge.provider.BanksRemoteCallsProvider;
import io.bankbridge.provider.IBanksProvider;
import io.bankbridge.seedwork.CacheHelper;

import spark.Request;
import spark.Response;

public class BanksHandlerTest {

    private static final Logger logger = Logger.getLogger(BanksHandlerTest.class.getName());

    HttpServletRequest httpServletRequest;
    Request requestMock;
    Response responseMock;

    @Before
    public void setup() {
        requestMock = mock(Request.class); // Spark request
        responseMock = mock(Response.class); // Spark response
        httpServletRequest = mock(HttpServletRequest.class); // Javax servlet
    }

    @AfterClass
    public static void cleanup() {
        stop(); // Stop the remote mock server
    }

    @Test
    public void test_BanksCacheBasedHandler() throws IOException {

        // Set Up
        IBanksProvider banksProvider = new BanksCacheBasedProvider();
        CacheHelper cacheHelper = new CacheHelper();
        ObjectMapper objectMapper = new ObjectMapper();
        String result;
        when(requestMock.raw()).thenReturn(httpServletRequest);
        when(requestMock.queryParams("pagesize")).thenReturn(String.valueOf("10"));
        when(requestMock.queryParams("pagenumber")).thenReturn(String.valueOf("1"));
        when(requestMock.queryParams("id")).thenReturn(String.valueOf("ULLAMCOSP16XXX"));

        BanksRequestHandler banksRequestHandler = new BanksRequestHandler(banksProvider, cacheHelper, objectMapper);

        requestMock.queryParams().add("pagesize");
        requestMock.queryParams().add("pagenumber");
        requestMock.queryParams().add("id");

        // Act
        result = banksRequestHandler.handle(requestMock, responseMock);

        // Assert
        assertNotNull(result);
        assertThat(result, containsString("ULLAMCOSP16XXX"));
    }

    @Test
    public void test_BanksRemoteCallsBasedHandler() throws IOException {

        // Set Up
        // Invoke mock main method or use the JUNIT BeforeClass with empty args in
        // main()
        // Make sure that no port with 1234 is already running. Otherwise it may give
        // error with "Alreday in use port"
        StopSparkWithPort1234();
        MockRemotes.main(null);

        IBanksProvider banksProvider = new BanksRemoteCallsProvider();
        CacheHelper cacheHelper = new CacheHelper();
        ObjectMapper objectMapper = new ObjectMapper();
        String result;
        when(requestMock.raw()).thenReturn(httpServletRequest);
        when(requestMock.queryParams("pagesize")).thenReturn(String.valueOf("10"));
        when(requestMock.queryParams("pagenumber")).thenReturn(String.valueOf("1"));
        when(requestMock.queryParams("countrycode")).thenReturn(String.valueOf("de"));

        BanksRequestHandler banksRequestHandler = new BanksRequestHandler(banksProvider, cacheHelper, objectMapper);

        requestMock.queryParams().add("pagesize");
        requestMock.queryParams().add("pagenumber");
        requestMock.queryParams().add("countrycode");

        // Act
        result = banksRequestHandler.handle(requestMock, responseMock);

        // Assert
        assertNotNull(result);
        assertThat(result, containsString("ANIMDEU7XXX"));
    }

    private void StopSparkWithPort1234() {
        try {
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec("cmd /c netstat -ano | findstr 1234");

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String s = null;
            if ((s = stdInput.readLine()) != null) {
                int index = s.lastIndexOf(" ");
                String sc = s.substring(index, s.length());

                rt.exec("cmd /c Taskkill /PID" + sc + " /T /F");

            }
            logger.info("Server Stopped");
            // JOptionPane.showMessageDialog(null, "Server Stopped");
        } catch (Exception e) {
            logger.info("Something Went wrong with server");
        }
    }
}
