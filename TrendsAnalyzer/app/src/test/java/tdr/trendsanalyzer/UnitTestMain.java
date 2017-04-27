package tdr.trendsanalyzer;

import org.junit.Rule;
import org.junit.Test;

import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.HashMap;

import static junit.framework.Assert.assertEquals;

public class UnitTestMain {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Test
    public void organizeResultsTest() throws Exception {
        HashMap<String, String> candidateMap = MainActivity.candidateMap;
        TableList table = MainActivity.table;
        table.clear();
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

        KeyValueList kvList1 = new KeyValueList();
        kvList1.putPair("CandidateID", "A");
        KeyValueList kvList2 = new KeyValueList();
        kvList2.putPair("CandidateID", "A");
        KeyValueList kvList3 = new KeyValueList();
        kvList3.putPair("CandidateID", "K");
        table.add(kvList1); // add votes to table
        table.add(kvList2);
        table.add(kvList3);
        table.addCategory("CandidateID");
        table.addCategory("Candidate Type");

        String expectedStr = "Most Popular Candidate: ID A; Gaming type with 2 votes out of 3\n";

        assertEquals(expectedStr, MainActivity.organizeResults(false));

        // vote for O 3 times
        KeyValueList kvList4 = new KeyValueList();
        kvList4.putPair("CandidateID", "O");
        KeyValueList kvList5 = new KeyValueList();
        kvList5.putPair("CandidateID", "O");
        KeyValueList kvList6 = new KeyValueList();
        kvList6.putPair("CandidateID", "O");
        table.add(kvList4);
        table.add(kvList5);
        table.add(kvList6);
        String expectedStrO = "Most Popular Candidate: ID O; Embedded Systems type with 3 votes out of 6\n";
        assertEquals(expectedStrO, MainActivity.organizeResults(false));
    }

    @Test
    public void mostPopularCategoryTest() throws Exception {
        HashMap<String, String> candidateMap = MainActivity.candidateMap;
        TableList table = MainActivity.table;
        table.clear();
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
//        table.addCategory("Gaming");
//        table.addCategory("Security");
//        table.addCategory("Embedded Systems");
        // add vote for A
        KeyValueList kvList1 = new KeyValueList();
        kvList1.putPair("CandidateID", "A");
        table.add(kvList1);
        // add another vote for A
        KeyValueList kvList2 = new KeyValueList();
        kvList2.putPair("CandidateID", "A");
        table.add(kvList2);
        // add vote for B
        KeyValueList kvList3 = new KeyValueList();
        kvList3.putPair("CandidateID", "B");
        table.add(kvList3);
        // ~C
        KeyValueList kvList4 = new KeyValueList();
        kvList4.putPair("CandidateID", "C");
        table.add(kvList4);
        // ~D
        KeyValueList kvList5 = new KeyValueList();
        kvList5.putPair("CandidateID", "D");
        table.add(kvList5);
        // ~E
        KeyValueList kvList6 = new KeyValueList();
        kvList6.putPair("CandidateID", "E");
        table.add(kvList6);

        table.addCategory("CandidateID");
        table.addCategory("Candidate Type");

        String expectedStr = "Most Popular Gaming out of 5: A with 2 votes out of 6\n";
        assertEquals(expectedStr, MainActivity.mostPopularCategory("Gaming"));

        // add vote for K
        KeyValueList kvList7 = new KeyValueList();
        kvList7.putPair("CandidateID", "K");
        table.add(kvList7);
        String expectedStrK = "Most Popular Embedded Systems out of 1: K with 1 votes out of 1\n";
        assertEquals(expectedStrK, MainActivity.mostPopularCategory("Embedded Systems"));
    }



//    @Test
//    public void generateReplyMessageTest() throws Exception {
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