package com.example.myapplication;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.example.myapplication.arrayList.eventStrings;

public class getTask extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{
    /**
     * Google Calendar API에 접근하기 위해 사용되는 구글 캘린더 API 서비스 객체
     */

    private com.google.api.services.calendar.Calendar API_Service = null;

    /**
     * Google Calendar API 호출 관련 메커니즘 및 AsyncTask을 재사용하기 위해 사용
     */
    private  int mID = 0;


    GoogleAccountCredential googleAccountCredential;

    private TextView textView_DailyCalendar;

    ProgressDialog progressDialog;


    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    String calendar_title;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {CalendarScopes.CALENDAR};

    private static final String TAG="CalendarActivity";
    String month_str, dayofMonth_str;
    String current_t=null, temp_selected_date=null;

    Button search_movielist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.get_task);

        textView_DailyCalendar = (TextView) findViewById(R.id.textView_DailyCalendar);


        Intent intent_toCalendar=getIntent();
        calendar_title=intent_toCalendar.getStringExtra("accountName");
        Log.d(calendar_title, "calendar_title");


        CalendarView calendarView=findViewById(R.id.calendarView);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                String date=(year)+"/"+(month+1)+"/"+dayOfMonth;
                month=(month+1);
                Log.d(TAG, "onSelectedDayChange: date: "+date);
                if(month<10){
                    month_str="0"+month;
                }
                else{
                    month_str=""+month;
                }
                if(dayOfMonth<10){
                    dayofMonth_str="0"+dayOfMonth;
                }
                else{
                    dayofMonth_str=""+dayOfMonth;
                }
                Log.d(month_str, "month_str");
                Log.d(dayofMonth_str, "dayofMonth_str");
                mID = 3;        //이벤트 가져오기
                getResultsFromApi();
                Log.d(textView_DailyCalendar.getText().toString(), "result 화면");

            }
        });

        search_movielist=findViewById(R.id.search_movielist);
        search_movielist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleDateFormat dateFormat=new SimpleDateFormat("MM-dd");
                current_t=dateFormat.format(new Date());
                temp_selected_date=month_str+"-"+dayofMonth_str;
                try {
                    Date current_time=dateFormat.parse(current_t);
                    if(month_str==null) {
                        Toast.makeText(getApplicationContext(), "날짜를 먼저 선택해주세요", Toast.LENGTH_LONG).show();
                    }
                    else if(current_t.compareTo(temp_selected_date)==1){
                        Toast.makeText(getApplicationContext(), "예전 날짜의 영화정보는 불러올 수 없습니다.\n다른 날을 선택해주세요", Toast.LENGTH_LONG).show();

                    }
                    else{
                        Intent intent7 = new Intent(getTask.this, MovieList.class);
                        intent7.putExtra("Selected Month", month_str);
                        intent7.putExtra("Selected DayofMonth", dayofMonth_str);
                        intent7.putExtra("task", eventStrings);
                        intent7.putExtra("accountName", calendar_title);

                        startActivity(intent7);

                        Toast.makeText(getApplicationContext(), "2019-" + month_str + "-" + dayofMonth_str + "의 영화 정보를 불러옵니다.", Toast.LENGTH_SHORT).show();
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }



            }
        });

        // Google Calendar API의 호출 결과를 표시하는 TextView를 준비
        textView_DailyCalendar.setVerticalScrollBarEnabled(true);
        textView_DailyCalendar.setMovementMethod(new ScrollingMovementMethod());


        // Google Calendar API 호출중에 표시되는 ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Google Calendar API 호출 중입니다.");


        // Google Calendar API 사용하기 위해 필요한 인증 초기화( 자격 증명 credentials, 서비스 객체 )
        // OAuth 2.0를 사용하여 구글 계정 선택 및 인증하기 위한 준비
        googleAccountCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(),
                Arrays.asList(SCOPES)
        ).setBackOff(new ExponentialBackOff()); // I/O 예외 상황을 대비해서 백오프 정책 사용

    }




    /**
     * 다음 사전 조건을 모두 만족해야 Google Calendar API를 사용할 수 있다.
     *
     * 사전 조건
     *     - Google Play Services 설치
     *     - 유효한 구글 계정 선택
     *     - 안드로이드 디바이스에서 인터넷 사용 가능
     *
     * 하나라도 만족하지 않으면 해당 사항을 사용자에게 알림.
     */
    private String getResultsFromApi() {

        if (!isGooglePlayServicesAvailable()) { // Google Play Services를 사용할 수 없는 경우

            acquireGooglePlayServices();
        } else if (googleAccountCredential.getSelectedAccountName() == null) { // 유효한 Google 계정이 선택되어 있지 않은 경우

            chooseAccount();
        } else if (!isDeviceOnline()) {    // 인터넷을 사용할 수 없는 경우

            Toast.makeText(getApplicationContext(), "네트워크가 연결되어 있지 않습니다", Toast.LENGTH_LONG).show();
        } else {

            // Google Calendar API 호출
            new MakeRequestTask(this, googleAccountCredential).execute();
        }
        return null;
    }



    /**
     * 안드로이드 디바이스에 최신 버전의 Google Play Services가 설치되어 있는지 확인
     */
    private boolean isGooglePlayServicesAvailable() {

        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();

        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }



    /*
     * Google Play Services 업데이트로 해결가능하다면 사용자가 최신 버전으로 업데이트하도록 유도하기위해
     * 대화상자를 보여줌.
     */
    private void acquireGooglePlayServices() {

        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {

            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }



    /*
     * 안드로이드 디바이스에 Google Play Services가 설치 안되어 있거나 오래된 버전인 경우 보여주는 대화상자
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode
    ) {

        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();

        Dialog dialog = apiAvailability.getErrorDialog(
                getTask.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES
        );
        dialog.show();
    }



    /*
     * Google Calendar API의 자격 증명( credentials ) 에 사용할 구글 계정을 설정한다.
     *
     * 전에 사용자가 구글 계정을 선택한 적이 없다면 다이얼로그에서 사용자를 선택하도록 한다.
     * GET_ACCOUNTS 퍼미션이 필요하다.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {

        // GET_ACCOUNTS 권한을 가지고 있다면
        if (EasyPermissions.hasPermissions(this, Manifest.permission.GET_ACCOUNTS)) {


            // SharedPreferences에서 저장된 Google 계정 이름을 가져온다.
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {

                // 선택된 구글 계정 이름으로 설정한다.
                googleAccountCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {


                // 사용자가 구글 계정을 선택할 수 있는 다이얼로그를 보여준다.
                startActivityForResult(
                        googleAccountCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }



            // GET_ACCOUNTS 권한을 가지고 있지 않다면
        } else {


            // 사용자에게 GET_ACCOUNTS 권한을 요구하는 다이얼로그를 보여준다.(주소록 권한 요청함)
            EasyPermissions.requestPermissions(
                    (Activity)this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }



    /*
     * 구글 플레이 서비스 업데이트 다이얼로그, 구글 계정 선택 다이얼로그, 인증 다이얼로그에서 되돌아올때 호출된다.
     */

    @Override
    protected void onActivityResult(
            int requestCode,  // onActivityResult가 호출되었을 때 요청 코드로 요청을 구분
            int resultCode,   // 요청에 대한 결과 코드
            Intent data
    ) {
        super.onActivityResult(requestCode, resultCode, data);


        switch (requestCode) {

            case REQUEST_GOOGLE_PLAY_SERVICES:

                if (resultCode != RESULT_OK) {

                    Toast.makeText(getApplicationContext(), "구글 플레이 서비스를 먼저 설치해주세요", Toast.LENGTH_LONG).show();

                } else {

                    getResultsFromApi();
                }
                break;


            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    /*if (accountName != null) {
                        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        googleAccountCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                    */
                }
                break;


            case REQUEST_AUTHORIZATION:

                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }


    /*
     * Android 6.0 (API 23) 이상에서 런타임 권한 요청시 결과를 리턴받음
     */
    @Override
    public void onRequestPermissionsResult(
            int requestCode,  //requestPermissions(android.app.Activity, String, int, String[])에서 전달된 요청 코드
            @NonNull String[] permissions, // 요청한 퍼미션
            @NonNull int[] grantResults    // 퍼미션 처리 결과. PERMISSION_GRANTED 또는 PERMISSION_DENIED
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    /*
     * EasyPermissions 라이브러리를 사용하여 요청한 권한을 사용자가 승인한 경우 호출된다.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> requestPermissionList) {

        // 아무일도 하지 않음
    }


    /*
     * EasyPermissions 라이브러리를 사용하여 요청한 권한을 사용자가 거부한 경우 호출된다.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> requestPermissionList) {

        // 아무일도 하지 않음
    }


    /*
     * 안드로이드 디바이스가 인터넷 연결되어 있는지 확인한다. 연결되어 있다면 True 리턴, 아니면 False 리턴
     */
    private boolean isDeviceOnline() {

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return (networkInfo != null && networkInfo.isConnected());
    }


    /*
     * 캘린더 이름에 대응하는 캘린더 ID를 리턴
     */
    private String getCalendarID(String calendarTitle){

        String id = null;

        // Iterate through entries in calendar list
        String pageToken = null;
        do {
            CalendarList calendarList = null;
            try {
                calendarList = API_Service.calendarList().list().setPageToken(pageToken).execute();
            } catch (UserRecoverableAuthIOException e) {
                startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
            }catch (IOException e) {
                e.printStackTrace();
            }
            List<CalendarListEntry> items = calendarList.getItems();


            for (CalendarListEntry calendarListEntry : items) {

                if ( calendarListEntry.getSummary().toString().equals(calendarTitle)) {

                    id = calendarListEntry.getId().toString();
                }
            }
            pageToken = calendarList.getNextPageToken();
        } while (pageToken != null);

        return id;
    }


    /*
     * 비동기적으로 Google Calendar API 호출
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, String> {

        private Exception exception = null;
        private getTask mActivity;


        public MakeRequestTask(getTask activity, GoogleAccountCredential credential) {

            mActivity = activity;

            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();

            API_Service = new com.google.api.services.calendar.Calendar
                    .Builder(transport, jsonFactory, credential)
                    .setApplicationName("Google Calendar API Android Quickstart")
                    .build();
        }


        @Override
        protected void onPreExecute() {

            progressDialog.show();

            textView_DailyCalendar.setText("");
        }


        /*
         * 백그라운드에서 Google Calendar API 호출 처리
         */
        @Override
        protected String doInBackground(Void... params) {
            try {

                if (mID == 3) {

                    return getEvent();
                }



            } catch (Exception e) {
                exception = e;
                cancel(true);
                return null;
            }

            return null;
        }


        /*
         * calendar_title 이름의 캘린더에서 10개의 이벤트를 가져와 리턴
         */
        private String getEvent() throws IOException, ParseException {
            eventStrings.clear();


            if(dayofMonth_str.charAt(0)=='0'){
                dayofMonth_str=dayofMonth_str.substring(1);
            }
            Log.d(dayofMonth_str, "받아온 날짜 정보");

            DateTime now = new DateTime(System.currentTimeMillis());

            //SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+09:00", Locale.KOREA);
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+09:00", Locale.KOREA);
            Calendar calendar = java.util.Calendar.getInstance();
            calendar.set(calendar.DATE, Integer.parseInt(dayofMonth_str));
            calendar.set(calendar.HOUR_OF_DAY, 00);
            calendar.set(calendar.MINUTE, 00);
            calendar.set(calendar.SECOND, 0);
            String date_selected_start=sdf.format(calendar.getTime());
            Log.d(date_selected_start, "sdf, calendar");

            Calendar calendar1=java.util.Calendar.getInstance();
            calendar1.set(calendar.DATE, Integer.parseInt(dayofMonth_str));
            calendar1.set(calendar1.HOUR_OF_DAY,23);
            calendar1.set(calendar1.MINUTE, 59);
            calendar1.set(calendar1.SECOND, 59);
            String date_selected_end=sdf.format(calendar1.getTime());

            DateTime today_start=new DateTime(date_selected_start);
            DateTime today_end=new DateTime(date_selected_end);

            String calendarID = getCalendarID(calendar_title);
            if ( calendarID == null ){

                return "캘린더를 먼저 생성하세요.";
            }


            Events events = API_Service.events().list(calendarID)//"primary")
                    .setMaxResults(10)
                    .setTimeMin(today_start)
                    .setTimeMax(today_end)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();
            List<Event> items = events.getItems();


            for (Event event : items) {

                DateTime start = event.getStart().getDateTime();

                //일정이 끝나는 시간
                DateTime end=event.getEnd().getDateTime();

                if (start == null) {

                    // 모든 이벤트가 시작 시간을 갖고 있지는 않다. 그런 경우 시작 날짜만 사용
                    start = event.getStart().getDate();
                }
                if(end==null){
                    end=event.getEnd().getDate();
                }


                String start_Datetime=start.toString().replace("T", " ").substring(0, 16);
                String end_Datetime=end.toString().replace("T", " ").substring(0, 16);

                //eventStrings.add(String.format("%s \n (%s) \n (%s)", event.getSummary(), start, end));
                eventStrings.add(String.format("%s \n      (%s) ~ (%s)", event.getSummary(), start_Datetime, end_Datetime));
            }


            return eventStrings.size() + "개의 데이터를 가져왔습니다.";
        }

        @Override
        protected void onPostExecute(String output) {

            progressDialog.hide();

            if ( mID == 3 )   {
                if(eventStrings.size()>0) {
                    textView_DailyCalendar.setText(TextUtils.join("\n\n", eventStrings));
                    textView_DailyCalendar.setVisibility(View.VISIBLE);
                    //here
                }
                else{

                    textView_DailyCalendar.setVisibility(View.GONE);
                }
            }
        }


        @Override
        protected void onCancelled() {
            progressDialog.hide();
            if (exception != null) {
                if (exception instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) exception)
                                    .getConnectionStatusCode());
                } else if (exception instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) exception).getIntent(),
                            getTask.REQUEST_AUTHORIZATION);
                } else {

                    Toast.makeText(getApplicationContext(), "Error: "+ exception.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Request cancled", Toast.LENGTH_SHORT).show();
            }
        }

    }


}