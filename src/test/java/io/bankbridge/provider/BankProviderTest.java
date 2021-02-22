package io.bankbridge.provider;

import io.bankbridge.MockRemotes;
import io.bankbridge.model.BankModel;

import org.junit.AfterClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;
import java.util.List;

import static org.junit.Assert.*;

import static spark.Spark.stop;

public class BankProviderTest {

    private static final Logger logger = Logger.getLogger(BankProviderTest.class.getName());

    @AfterClass
    public static void cleanup() {
        stop(); // Stop the remote mock server
    }

    @Test
    public void test_BanksCacheBasedProvider() throws IOException {

        // Set Up
        IBanksProvider bankProvider = new BanksCacheBasedProvider();

        // Act
        List<BankModel> banksList = bankProvider.getBankDetails();

        // Assert
        assertEquals(20, banksList.size());
        assertEquals("Banco de espiritu santo", banksList.stream().findFirst().get().getName());
    }

    @Test
    public void test_BanksRemoteCallsProvider() throws IOException {

        // Set Up
        // Invoke mock main method or use the JUNIT BeforeClass with empty args in
        // main()
        // Make sure that no port with 1234 is already running. Otherwise it may give
        // error with "Alreday in use port"
        StopSparkWithPort1234();
        MockRemotes.main(null);

        IBanksProvider bankProvider = new BanksRemoteCallsProvider();

        // Act
        List<BankModel> banksList = bankProvider.getBankDetails();

        // Assert
        assertEquals(20, banksList.size());
        assertEquals("PARIATURDEU0XXX", banksList.stream().filter(b -> b.getName().equals("Banco de espiritu santo"))
                .findFirst().get().getBic());
        assertEquals("GB", banksList.stream().filter(b -> b.getName().equals("Banco de espiritu santo")).findFirst()
                .get().getCountryCode());
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
