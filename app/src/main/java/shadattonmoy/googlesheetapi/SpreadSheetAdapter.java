package shadattonmoy.googlesheetapi;

import android.content.Context;
import android.provider.ContactsContract;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Shadat Tonmoy on 2/1/2018.
 */

public class SpreadSheetAdapter extends ArrayAdapter<SpreadSheet> {

    private TextView spreadSheetTitleView,spreadSheetPropertiesView;
    private ImageView spreadSheetLogoView;
    public SpreadSheetAdapter(@NonNull Context context, @LayoutRes int resource, @IdRes int textViewResourceId, @NonNull List<SpreadSheet> objects) {
        super(context, resource, textViewResourceId, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable final View convertView, @NonNull ViewGroup parent) {

        View row = convertView;
        if(row==null)
        {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.spreadsheet_single_row,parent,false);
        }

        /*
        * find views by their IDs
        * */
        final SpreadSheet spreadSheet= getItem(position);
        spreadSheetTitleView = (TextView) row.findViewById(R.id.spread_title);
        spreadSheetLogoView = (ImageView) row.findViewById(R.id.spread_sheet_logo);
        //spreadSheetPropertiesView = (TextView) row.findViewById(R.id.properties);



        /*
        * get the specific attributes for a particular course
        * */
        String spreadSheetTitle = spreadSheet.getName();
        if(spreadSheetTitle.length()>10)
        {
            spreadSheetTitle = spreadSheetTitle.substring(0,8)+"....";
        }
        //String properties = spreadSheet.getProperties();


        /*
        * set the attributes like text or clicklistener to the specific view/textview
        * */
        spreadSheetTitleView.setText(spreadSheetTitle);
        spreadSheetLogoView.setImageResource(R.drawable.spreadsheeticon);
        //spreadSheetPropertiesView.setText(properties);
        return row;
    }
}
