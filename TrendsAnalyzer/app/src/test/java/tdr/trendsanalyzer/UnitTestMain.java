package tdr.trendsanalyzer;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.HashMap;

import static junit.framework.Assert.assertEquals;

public class UnitTestMain {
    @Mock
    TableList table;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private HashMap<String, String> candidateMap = new HashMap<>();
    private KeyValueList kvList = new KeyValueList();


    @Test
    public void organizeResultsTest() throws Exception {
        candidateMap.put("A", "Gaming");
        candidateMap.put("B", "Gaming");
        candidateMap.put("C", "Gaming");
        candidateMap.put("D", "Gaming");
        candidateMap.put("E", "Gaming");
        candidateMap.put("F", "Security");
        candidateMap.put("G", "Security");
        candidateMap.put("H", "Security");
        candidateMap.put("I", "Security");
        candidateMap.put("J", "Security");
        candidateMap.put("K", "Embedded Systems");
        candidateMap.put("L", "Embedded Systems");
        candidateMap.put("M", "Embedded Systems");
        candidateMap.put("N", "Embedded Systems");
        candidateMap.put("O", "Embedded Systems");
        table.addCategory("Gaming");
        table.addCategory("Security");
        table.addCategory("Embedded Systems");
//        kvList.putPair("voter1","A");
//        kvList.putPair("voter2","B");
//        kvList.putPair("voter3","A");

        table.add(kvList); // add votes to table?
        String expectedStr = "Most Popular Candidate: ID A; Gaming type with 2 votes out of 3\n";
        assertEquals(expectedStr, MainActivity.organizeResults());

        // vote for O 3 times
        String expectedStrO = "Most Popular Candidate: ID O; Embedded Systems type with 3 votes out of 6\n";
        assertEquals(expectedStrO, MainActivity.organizeResults());
    }

    @Test
    public void mostPopularCategoryTest() throws Exception {
        candidateMap.put("A", "Gaming");
        candidateMap.put("B", "Gaming");
        candidateMap.put("C", "Gaming");
        candidateMap.put("D", "Gaming");
        candidateMap.put("E", "Gaming");
        candidateMap.put("F", "Security");
        candidateMap.put("G", "Security");
        candidateMap.put("H", "Security");
        candidateMap.put("I", "Security");
        candidateMap.put("J", "Security");
        candidateMap.put("K", "Embedded Systems");
        candidateMap.put("L", "Embedded Systems");
        candidateMap.put("M", "Embedded Systems");
        candidateMap.put("N", "Embedded Systems");
        candidateMap.put("O", "Embedded Systems");
        table.addCategory("Gaming");
        table.addCategory("Security");
        table.addCategory("Embedded Systems");
        // add vote for A
        // add another vote for A
        // add vote for B
        // ~C
        // ~D
        // ~E

        table.add(kvList); // add votes to table?
        String expectedStr = "Most Popular Gaming out of 5: A with 2 votes out of 6\n";
        assertEquals(expectedStr, MainActivity.mostPopularCategory("Gaming"));

        // add vote for K
        String expectedStrK = "Most Popular Embedded Systems out of 1: K with 1 votes out of 1\n";
        assertEquals(expectedStrK, MainActivity.mostPopularCategory("Embedded Systems"));
    }

//    @Test
//    public void test() throws Exception {
//
//    }
//
//    @Test
//    public void test() throws Exception {
//
//    }

//    @Test
//    public void handleMessageConnected() throws Exception {
//        Message msg = new Message();
//        Handler hnd = new Handler();
//        msg.obtain(hnd, MainActivity.CONNECTED);
//        assertEquals(MainActivity.CONNECTED, MainActivity.handleMessage(msg));
//    }
//    @Test
//    public void handleMessageDisconnected() throws Exception {
//        Message msg = new Message();
//        Handler hnd = new Handler();
//        msg.obtain(hnd, MainActivity.DISCONNECTED);
//        assertEquals(MainActivity.DISCONNECTED, MainActivity.handleMessage(msg));
//    }
//    @Test
//    public void handleMessageMessageReceived() throws Exception {
//        Message msg = new Message();
//        Handler hnd = new Handler();
//        msg.obtain(hnd, MainActivity.MESSAGE_RECEIVED);
//        assertEquals(MainActivity.MESSAGE_RECEIVED, MainActivity.handleMessage(msg));
//    }
//    @Test
//    public void handleMessageStringAdd() throws Exception {
//        assertEquals.("Add", MainActivity.handleMsgMessageValue("Add"));
//    }
//    @Test
//    public void handleMessageStringNewVote() throws Exception {
//        assertEquals.("New Vote", MainActivity.handleMsgMessageValue("New Vote"));
//    }
//    @Test
//    public void handleMessageStringTable() throws Exception {
//        assertEquals.("Table", MainActivity.handleMsgMessageValue("Table"));
//    }
//    @Test
//    public void handleMessageStringClose() throws Exception {
//        assertEquals.("Close", MainActivity.handleMsgMessageValue("Close"));
//    }
//    @Test
//    public void handleMessageStringOpen() throws Exception {
//        assertEquals.("Open", MainActivity.handleMsgMessageValue("Open"));
//    }
}