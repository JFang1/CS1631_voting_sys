package tdr.trendsanalyzer;

import java.util.ArrayList;

/**
 * Created by A on 4/18/2017.
 */

public class TableList {

    private ArrayList<String> categories;
    private ArrayList<KeyValueList> votes;

    public TableList() {
        votes = new ArrayList<>();
        categories = new ArrayList<>();
    }

    public int count() {
        return categories.size() * (1 + votes.size());
    }

    public int size() {
        return votes.size();
    }

    public void add(KeyValueList kvList) {
        votes.add(kvList);
    }

    public String get(int rowNum, int colNum) {
        if (rowNum < 0 || rowNum >= votes.size())
            return "";
        return votes.get(rowNum).getValue(getCategory(colNum));
    }

    public String get(int rowNum, String col) {
        if (rowNum < 0 || rowNum >= votes.size())
            return "";
        return votes.get(rowNum).getValue(col);
    }

    public void addCategory(String str) {
        categories.add(str);
    }

    public int categories() {
        return categories.size();
    }

    public String getCategory(int index) {
        if (index < 0 || index >= categories.size())
            return "";
        return categories.get(index);
    }

    public boolean contains(String str) {
        return categories.contains(str);
    }

    public void clear() {
        votes = new ArrayList<>();
        categories = new ArrayList<>();
    }
}
