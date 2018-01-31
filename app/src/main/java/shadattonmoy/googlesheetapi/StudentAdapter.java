package shadattonmoy.googlesheetapi;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Shadat Tonmoy on 2/1/2018.
 */

public class StudentAdapter extends ArrayAdapter<Student> {

    public StudentAdapter(@NonNull Context context, @LayoutRes int resource, @IdRes int         textViewResourceId, @NonNull List<Student> objects) {
            super(context, resource, textViewResourceId, objects);
    }

@NonNull
@Override
    public View getView(int position, @Nullable final View convertView, @NonNull
        ViewGroup parent) {

        View row = convertView;
        if(row==null)
        {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.student_single_row,parent,false);
        }

        /*
        * find views by their IDs
        * */
        final Student student = getItem(position);
        TextView studentIconView = (TextView) row.findViewById(R.id.student_icon);
        TextView studentNameView = (TextView) row.findViewById(R.id.student_name);
        TextView studentRegNoView = (TextView) row.findViewById(R.id.student_reg_no);
        TextView studentEmailView = (TextView) row.findViewById(R.id.student_email);


        /*
        * get the specific attributes for a particular course
        * */
        String studentName = student.getName();
        String studentRegNo = student.getRegNo();
        String studentEmail = student.getEmail();
        String iconText = String.valueOf(studentName.charAt(0));


        /*
        * set the attributes like text or clicklistener to the specific view/textview
        * */
        studentIconView.setText(iconText);
        studentNameView.setText(studentName);
        studentRegNoView.setText(studentRegNo);
        studentEmailView.setText(studentEmail);

        return row;

    }
}