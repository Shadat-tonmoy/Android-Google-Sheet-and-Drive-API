package shadattonmoy.googlesheetapi;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.CheckedOutputStream;

import static java.security.AccessController.getContext;

/**
 * Created by Shadat Tonmoy on 2/3/2018.
 */

public class SpreadSheetAdapter extends BaseAdapter {

    public class ViewHolder {
        TextView spreadSheetTitle;
        ImageView spreadSheetIcon;
    }

    private Context context;
    private List<SpreadSheet> spreadSheets;
    SpreadSheetAdapter(Context context, List<SpreadSheet> spreadSheets)
    {
        this.context = context;
        this.spreadSheets = spreadSheets;
    }
    @Override
    public int getCount() {
        return spreadSheets.size();
    }

    @Override
    public Object getItem(int position) {
        return spreadSheets.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View cell = convertView;
        ViewHolder viewHolder;
        if(cell==null)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            cell = inflater.inflate(R.layout.spreadsheet_single_row,parent,false);
            viewHolder = new ViewHolder();
            viewHolder.spreadSheetTitle= (TextView) cell.findViewById(R.id.spread_title);
            viewHolder.spreadSheetIcon = (ImageView) cell.findViewById(R.id.spread_sheet_logo);
            cell.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        String spreadSheetTitle = spreadSheets.get(position).getName();
        if(spreadSheetTitle.length()>10)
        {
            spreadSheetTitle = spreadSheetTitle.substring(0,8)+"....";
        }
        viewHolder.spreadSheetIcon.setImageResource(R.drawable.spreadsheeticon);
        viewHolder.spreadSheetTitle.setText(spreadSheetTitle);

        return cell;
    }
}
