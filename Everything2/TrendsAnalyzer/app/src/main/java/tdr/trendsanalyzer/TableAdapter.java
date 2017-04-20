package tdr.trendsanalyzer;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by A on 4/19/2017.
 */

public class TableAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private TableList table;

    public TableAdapter(TableList table) {
        this.table = table;
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder
    {
        private TextView header;

        public HeaderViewHolder(View v)
        {
            super(v);
            header = (TextView)v.findViewById(R.id.header);
        }

    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder
    {
        private TextView item;

        public ItemViewHolder(View v)
        {
            super(v);
            item = (TextView)v.findViewById(R.id.item);
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (position < table.categories())
            return 0;
        else
            return 1;
    }

    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType)
    {
        if (viewType == 0)
            return new TableAdapter.HeaderViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.table_header, viewGroup, false));
        else
            return new TableAdapter.ItemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.table_item, viewGroup, false));
    }

    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i)
    {
        if (i < table.categories())
            ((HeaderViewHolder)viewHolder).header.setText(table.getCategory(i));
        else if (table.categories() > 0)
        {
            int rowNum = i / table.categories() - 1;
            int colNum = i % table.categories();
            ((ItemViewHolder)viewHolder).item.setText(table.get(rowNum, colNum));
        }
    }

    public int getItemCount()
    {
        return table.count();
    }
}
