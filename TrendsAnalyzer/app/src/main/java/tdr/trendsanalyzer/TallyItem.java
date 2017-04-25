package tdr.trendsanalyzer;

/**
 * Created by A on 4/19/2017.
 */

public class TallyItem implements Comparable<TallyItem>{
    private String key;
    private int value;

    public TallyItem(String key, int value) {
        this.key = key;
        this.value = value;
    }

    public void increment() {
        value++;
    }

    public int compareTo(TallyItem item)
    {
        return Integer.compare(value, item.getValue());
    }

    public String getKey() {
        return key;
    }

    public int getValue() {
        return value;
    }
}
