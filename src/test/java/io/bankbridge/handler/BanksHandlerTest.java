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

/**
 * Class to unit tests Bank handler. It will test both cache based and remote
 * based.
 */
public class BanksHandlerTest {

    private static final Logger logger = Logger.getLogger(BanksHandlerTest.class.getName());

    HttpServletRequest httpServletRequest;
    Request requestMock;
    Response responseMock;

    /**
     * Set up will run before executing any test. It initializes necessary http mock
     * objects.
     */
    @Before
    public void setup() {
        requestMock = mock(Request.class); // Spark request
        responseMock = mock(Response.class); // Spark response
        httpServletRequest = mock(HttpServletRequest.class); // Javax servlet
    }

    /**
     * Clean up will run after executing all tests. It will stop the Spark with port
     * 1234.
     */
    @AfterClass
    public static void cleanup() {
        stop(); // Stop the remote mock server
    }

    /**
     * Test method to test cache based handler.
     * 
     * @throws IOException
     */
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

    /**
     * Test method to test cache based handler. It will test the paginationa nd
     * filter too.
     * 
     * @throws IOException
     */
    @Test
    public void test_BanksCacheBasedHandler_Pagination_With_Filter() throws IOException {

        // Set Up
        IBanksProvider banksProvider = new BanksCacheBasedProvider();
        CacheHelper cacheHelper = new CacheHelper();
        ObjectMapper objectMapper = new ObjectMapper();
        String result;
        when(requestMock.raw()).thenReturn(httpServletRequest);
        when(requestMock.queryParams("pagesize")).thenReturn(String.valueOf("2"));
        when(requestMock.queryParams("pagenumber")).thenReturn(String.valueOf("2"));
        when(requestMock.queryParams("countrycode")).thenReturn(String.valueOf("de"));

        BanksRequestHandler banksRequestHandler = new BanksRequestHandler(banksProvider, cacheHelper, objectMapper);

        requestMock.queryParams().add("pagesize");
        requestMock.queryParams().add("pagenumber");
        requestMock.queryParams().add("countrycode");

        // Act
        result = banksRequestHandler.handle(requestMock, responseMock);

        // Assert
        assertNotNull(result);
        assertThat(result, containsString("Soar Credit Union"));
    }

    /**
     * Test method to test the cache based handler with an invalid page.
     * 
     * @throws IOException
     */
    @Test
    public void test_BanksCacheBasedHandler_Invalid_PageNumber() throws IOException {

        // Set Up
        IBanksProvider banksProvider = new BanksCacheBasedProvider();
        CacheHelper cacheHelper = new CacheHelper();
        ObjectMapper objectMapper = new ObjectMapper();
        String result;
        when(requestMock.raw()).thenReturn(httpServletRequest);
        when(requestMock.queryParams("pagesize")).thenReturn(String.valueOf("21"));
        when(requestMock.queryParams("pagenumber")).thenReturn(String.valueOf("2"));
        when(requestMock.queryParams("countrycode")).thenReturn(String.valueOf("de"));

        BanksRequestHandler banksRequestHandler = new BanksRequestHandler(banksProvider, cacheHelper, objectMapper);

        requestMock.queryParams().add("pagesize");
        requestMock.queryParams().add("pagenumber");
        requestMock.queryParams().add("countrycode");

        // Act
        result = banksRequestHandler.handle(requestMock, responseMock);

        // Assert
        assertNotNull(result);
        assertEquals("[]", result);
    }

    /**
     * Test method to test the remote calls with pagination and filter.
     * 
     * @throws IOException
     */
    @Test
    public void test_BanksRemoteCallsBasedHandler_Pagination_With_Filter() throws IOException {

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
        when(requestMock.queryParams("pagesize")).thenReturn(String.valueOf("2"));
        when(requestMock.queryParams("pagenumber")).thenReturn(String.valueOf("2"));
        when(requestMock.queryParams("countrycode")).thenReturn(String.valueOf("se"));

        BanksRequestHandler banksRequestHandler = new BanksRequestHandler(banksProvider, cacheHelper, objectMapper);

        requestMock.queryParams().add("pagesize");
        requestMock.queryParams().add("pagenumber");
        requestMock.queryParams().add("countrycode");

        // Act
        result = banksRequestHandler.handle(requestMock, responseMock);

        // Assert
        assertNotNull(result);
        assertThat(result, containsString("ETSWE19XXX"));
    }

    /**
     * Method to stopo the Spark port 1234 if it is already running. If already an
     * instance is running, we are stopping this and again starting from the remote
     * calls based test method. In this case, our test will never call with
     * 'unreachable' error or 'already an instance running' error.
     */
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
