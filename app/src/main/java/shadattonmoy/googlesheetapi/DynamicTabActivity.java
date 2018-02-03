package shadattonmoy.googlesheetapi;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.Dialog;
import android.app.LocalActivityManager;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AnalogClock;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class DynamicTabActivity extends AppCompatActivity
        implements EasyPermissions.PermissionCallbacks{

    private TextView debugView,nothingFoundMsg;
    private Button mCallApiButton;
    ProgressDialog mProgress;
    private GridView spreadSheetList;
    private String sheetId;
    private ListView spreadSheetDataList;
    private TextView totalSheetView;
    private TabHost tabs;
    private List<Sheet> sheetList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_tab);
        getSupportActionBar().hide();
        sheetId = getIntent().getStringExtra("ID");
        //sheetId = "1kv3frDlUjR0uAiYFY3Fo5pgm_vxpU-yoq_0yUpP5POs";
        init();
        getResultsFromApi();

    }



    public void init()
    {

        spreadSheetDataList= (ListView) findViewById(R.id.spread_sheet_data_list);
        nothingFoundMsg = (TextView) findViewById(R.id.nothing_found_msg);
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Please Wait....");
        mProgress.setCancelable(false);
    }

    public void createTab(List<SpreadSheetData> spreadSheetDataList)
    {
        tabs=(TabHost)findViewById(R.id.tabhost);
        tabs.setup();
        Log.e("Number of Sheets ","S : "+sheetList.size());
        for(int i=0;i<sheetList.size();i++)
        {
            String title = sheetList.get(i).getProperties().getTitle();
            TabHost.TabSpec spec=tabs.newTabSpec(title);

            List<Student> studentList = spreadSheetDataList.get(i).getStudentList();
            Toast.makeText(DynamicTabActivity.this,"List Size is "+studentList.size()+" For i = "+i,Toast.LENGTH_SHORT).show();
            if(studentList.size()>0)
                spec.setContent(new TabCreator(studentList));
            else {
                nothingFoundMsg.setVisibility(View.VISIBLE);
                nothingFoundMsg.setText("Sorry!!! Nothing Found.");
                spec.setContent(R.id.nothing_found_msg);
            }

            spec.setIndicator(title);
            tabs.addTab(spec);
        }
        tabs.setCurrentTab(0);
    }

    class TabCreator implements TabHost.TabContentFactory{
        private List<Student> studentList;
        TabCreator(List<Student> studentList)
        {
            this.studentList = studentList;
        }

        @Override
        public View createTabContent(String tag) {
            ListView listView = new ListView(DynamicTabActivity.this);
            StudentAdapter studentAdapter = new StudentAdapter(DynamicTabActivity.this,R.layout.student_single_row,R.id.student_icon,  studentList);
            listView.setAdapter(studentAdapter);
            return listView;
        }
    }


    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void getResultsFromApi() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (MainActivity.mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
            debugView.setText("No network connection available.");
        } else {
            new DynamicTabActivity.MakeRequestTask(MainActivity.mCredential).execute();
        }
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(MainActivity.REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(MainActivity.PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                MainActivity.mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        MainActivity.mCredential.newChooseAccountIntent(),
                        MainActivity.REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    MainActivity.REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case MainActivity.REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
//                    debugView.setText(
//                            "This app requires Google Play Services. Please install " +
//                                    "Google Play Services on your device and relaunch this app.");
                } else {
                    getResultsFromApi();
                }
                break;
            case MainActivity.REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(MainActivity.PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        MainActivity.mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case MainActivity.REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     * @param requestCode The request code passed in
     *     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                DynamicTabActivity.this,
                connectionStatusCode,
                MainActivity.REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * An asynchronous task that handles the Google Sheets API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, List<SpreadSheetData> > {
        private Sheets sheetService = null;
        protected Drive driveService = null;

        private Exception mLastError = null;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            sheetService = new com.google.api.services.sheets.v4.Sheets.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Sheets API Android Quickstart")
                    .build();
            driveService = new com.google.api.services.drive.Drive.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Drive API Android Quickstart")
                    .build();
        }

        /**
         * Background task to call Google Sheets API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<SpreadSheetData> doInBackground(Void... params) {
            try {
                return getStudent(sheetId);
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }


        private List<SpreadSheetData> getStudent(String spreadSheetId) throws IOException {
            List<Sheet> sheetList1 = this.sheetService.spreadsheets().get(spreadSheetId).execute().getSheets();
            sheetList = sheetList1;
            List<SpreadSheetData> spreadSheetDataList = new ArrayList<SpreadSheetData>();
            for(int k=0;k<sheetList.size();k++)
            {
                String sheetName = sheetList.get(k).getProperties().getTitle();
                Log.e("Sheet Name ",sheetName);
                SpreadSheetData spreadSheetData = new SpreadSheetData();
                String range = sheetName+"!A2:C";
                List<Student> results = new ArrayList<Student>();
                ValueRange response = this.sheetService.spreadsheets().values()
                        .get(spreadSheetId, range)
                        .execute();
                List<List<Object>> values = response.getValues();
                if (values != null) {
                    for (List row : values) {
                        String name = "Name not available!";
                        String regNo = "Reg. No not available";
                        String email = "Email not available";
                        for(int i=0;i<row.size();i++)
                        {
                            if(i==0)
                                regNo = row.get(i).toString();
                            else if(i==1)
                                name = row.get(i).toString();
                            else if(i==2)
                                email = row.get(i).toString();
                        }
                        results.add(new Student(name,regNo,email));
                    }
                }
                //spreadSheetData.setSheetList(sheetList);
                spreadSheetData.setStudentList(results);
                spreadSheetDataList.add(spreadSheetData);
            }
            return spreadSheetDataList;

        }



        @Override
        protected void onPreExecute() {
//            debugView.setText("");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<SpreadSheetData> spreadSheetData) {
            mProgress.hide();
            if (spreadSheetData == null || spreadSheetData.size() == 0) {
                Toast.makeText(DynamicTabActivity.this,"Null",Toast.LENGTH_SHORT).show();
                nothingFoundMsg.setText("No results returned.");
            } else {
                createTab(spreadSheetData);
            }
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                } else {
                    nothingFoundMsg.setText("The following error occurred:\n"
                           + mLastError.getMessage());
                }
            } else {
                nothingFoundMsg.setText("Request cancelled.");
            }
        }
    }






}
