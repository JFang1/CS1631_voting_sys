package tdr.admincontrol;

/**
 * Created by A on 3/10/2017.
 */

public class VoteItem implements Comparable<VoteItem> {

    private String ID;
    private int tally;

    public VoteItem(String id)
    {
        ID = id;
        tally = 0;
    }

    public void add()
    {
        tally++;
    }

    public int compareTo(VoteItem item)
    {
       return Integer.compare(tally, item.getTally());
    }

    public String getID()
    {
        return ID;
    }

    public int getTally()
    {
        return tally;
    }
}
