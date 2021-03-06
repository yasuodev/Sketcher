package org.startup.sketcher;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.startup.sketcher.util.Constant;
import org.startup.sketcher.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class RegisterActivity extends BaseActivity {

    @InjectView(R.id.etFullname)
    EditText etFullname;

    @InjectView(R.id.etCountry)
    EditText etCountry;
    @InjectView(R.id.etUsername)
    EditText etUserName;
    @InjectView(R.id.etPassword)
    EditText etPassword;
    @InjectView(R.id.etRetypePassword)
    EditText etRetypePassword;
    @InjectView(R.id.etEmail)
    EditText etEmail;
    @InjectView(R.id.rlCountry)
    RelativeLayout rlCountry;
    @InjectView(R.id.lvCountry)
    ListView lvCountry;

    boolean isRegisteredUsername = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUserName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    isCheckUsername();
                }
            }
        });

        Locale[] locale = Locale.getAvailableLocales();
        ArrayList<String> countries = new ArrayList<String>();
        String country;
        for (Locale loc : locale) {
            country = loc.getDisplayCountry();
            if (country.length() > 0 && !countries.contains(country)) {
                countries.add(country);
            }
        }
        Collections.sort(countries, String.CASE_INSENSITIVE_ORDER);

        CountryAdapter adapter = new CountryAdapter(getApplicationContext(), countries);
        lvCountry.setAdapter(adapter);

        etCountry.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    hideKeyboard();
                    rlCountry.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @OnClick(R.id.btnCreate)
    public void create(View view) {
        hideKeyboard();
        if (isValidate()) {
            if (Util.isOnline(getApplicationContext())){
                new Register(getText(etFullname), getText(etCountry), getText(etUserName), getText(etPassword), getText(etEmail)).execute();
            } else {
                toast(Constant.network_error);
            }

        }
    }

    private void isCheckUsername() {
        (new CheckUserName(getText(etUserName))).execute();
    }

    private boolean isValidate() {

        if (isRegisteredUsername) {
            etUserName.setBackgroundResource(R.drawable.textviewredborder);
            toast("ERROR - The username already exists. Please try again with different username.");
            return false;
        }

        initEditViews();

        if (isEmpty(getText(etFullname))) {
            etFullname.setBackgroundResource(R.drawable.textviewredborder);
            toast("please enter fullname");
            return false;
        }

        if (isEmpty(getText(etUserName))) {
            etUserName.setBackgroundResource(R.drawable.textviewredborder);
            toast("please enter user name");
            return false;
        }

        if (isEmpty(getText(etPassword))) {
            etPassword.setBackgroundResource(R.drawable.textviewredborder);
            toast("please enter password");
            return false;
        }

        if (isEmpty(getText(etRetypePassword))) {
            etRetypePassword.setBackgroundResource(R.drawable.textviewredborder);
            toast("please enter confirm password");
            return false;
        }

        if (!getText(etPassword).equals(getText(etRetypePassword))) {
            etRetypePassword.setBackgroundResource(R.drawable.textviewredborder);
            toast("password and confirm password must be same");
            return false;
        }

        if (Patterns.EMAIL_ADDRESS.matcher(getText(etEmail)).matches() == false) {
            etEmail.setBackgroundResource(R.drawable.textviewredborder);
            toast("please enter valid email");
            return false;
        }

        return true;
    }

    private void initEditViews() {
        etFullname.setBackgroundResource(R.drawable.textviewborder);
        etUserName.setBackgroundResource(R.drawable.textviewborder);
        etPassword.setBackgroundResource(R.drawable.textviewborder);
        etRetypePassword.setBackgroundResource(R.drawable.textviewborder);
        etEmail.setBackgroundResource(R.drawable.textviewborder);
    }


    class CheckUserName extends AsyncTask<Void, String, String> {

        String username;
        String responseString = "";

        public CheckUserName(String username) {
            this.username = username;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {

            String envelope = "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:quote5\">"+
            "<soapenv:Header/>"+
            "<soapenv:Body>"+
            "<urn:checkuser soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"+
            "<username xsi:type=\"xsd:string\">%s</username>"+
            "</urn:checkuser>"+
            "</soapenv:Body>"+
            "</soapenv:Envelope>";

            //String soapmessage = String.format(envelope, username, password);
            String soapmessage = String.format(envelope, username);

            HttpPost httpPost = new HttpPost(Constant.URL);
            StringEntity entity;

            try {
                entity = new StringEntity(soapmessage, HTTP.UTF_8);
                httpPost.setHeader("Content-Type", "text/xml;charset=UTF-8");
                httpPost.setEntity(entity);
                HttpClient client = new DefaultHttpClient();
                HttpResponse response = client.execute(httpPost);
                responseString = EntityUtils.toString(response.getEntity());
                Log.d("request: ", responseString);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return responseString;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d("CheckUserName:", result);

            try {
                JSONObject jObj = new JSONObject(result);
                boolean status = jObj.optBoolean("Status");

                if (status) {
                    etUserName.setBackgroundResource(R.drawable.textviewborder);
                    isRegisteredUsername = false;
                } else {
                    etUserName.setBackgroundResource(R.drawable.textviewredborder);
                    toast("Error - Ther username alread exists. Please try again with different username");
                    isRegisteredUsername = true;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    class Register extends AsyncTask<Void, String, String> {

        String fullname, country, username, password, confirmpassword, email;
        ProgressDialog progressDialog;
        String responseString = "";

        public Register(String fullname, String country, String username, String password, String email) {
            this.fullname = fullname;
            this.country = country;
            this.username = username;
            this.password = password;
            this.confirmpassword = password;
            this.email = email;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (progressDialog == null)
                progressDialog = ProgressDialog.show(RegisterActivity.this, null, "Registering...", true, false);
        }

        @Override
        protected String doInBackground(Void... params) {

            String envelope = "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:quote5\">"+
            "<soapenv:Header/>"+
            "<soapenv:Body>"+
            "<urn:register soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"+
            "<fullname xsi:type=\"xsd:string\">%s</fullname>"+
            "<country xsi:type=\"xsd:string\">%s</country>"+
            "<username xsi:type=\"xsd:string\">%s</username>"+
            "<password xsi:type=\"xsd:string\">%s</password>"+
            "<retypepassword xsi:type=\"xsd:string\">%s</retypepassword>"+
            "<useremail xsi:type=\"xsd:string\">%s</useremail>"+
            "</urn:register>"+
            "</soapenv:Body>"+
            "</soapenv:Envelope>";

            String soapmessage = String.format(envelope, fullname, country, username, password, confirmpassword, email);

            HttpPost httpPost = new HttpPost(Constant.URL);
            StringEntity entity;

            try {
                entity = new StringEntity(soapmessage, HTTP.UTF_8);
                httpPost.setHeader("Content-Type", "text/xml;charset=UTF-8");
                httpPost.setEntity(entity);
                HttpClient client = new DefaultHttpClient();
                HttpResponse response = client.execute(httpPost);
                responseString = EntityUtils.toString(response.getEntity());
                Log.d("registered: ", responseString);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return responseString;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (progressDialog != null) {
                if (progressDialog.isShowing()) progressDialog.dismiss();
            }

            try {
                Object json = new JSONTokener(result).nextValue();
                if (json instanceof JSONObject) {
                    JSONObject jObj = new JSONObject(result);
                    boolean status = jObj.optBoolean("Status");
                    if (status) {
                        String message = jObj.optString("Message");
                        toast(message);
                        finish();
                    } else {
                        toast("Failed register");
                    }
                } else if (json instanceof JSONArray) {
                    JSONArray jsonArray = new JSONArray(result);

                    if (jsonArray.length() > 0) {
                        JSONObject jObj = jsonArray.getJSONObject(0);
                        String message = jObj.optString("Message");
                        toast(message);
                    }

                }


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

    class CountryAdapter extends BaseAdapter
    {
        private Context context;
        private LayoutInflater inflater = null;
        ArrayList<String> arrCountries;

        public CountryAdapter(Context context, ArrayList<String> arrCountries) {
            this.context = context;
            this.arrCountries = arrCountries;

            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount(){
            return arrCountries.size();
        }

        @Override
        public Object getItem(int i){
            return null;
        }

        @Override
        public long getItemId(int i){
            return 0;
        }

        @Override
        public View getView(final int position, View view, ViewGroup viewGroup) {
            RegisterActivity.ViewHolder holder;
            if (view == null) {
                view = inflater.inflate(R.layout.list_country, null);
                holder = new RegisterActivity.ViewHolder(view);
                view.setTag(holder);
            } else {
                holder = (RegisterActivity.ViewHolder) view.getTag();
            }

            final String country = arrCountries.get(position);

            holder.tvCountry.setText(country);

            holder.tvCountry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Log.d("selected ", String.valueOf(position));
                    etCountry.setText(country);
                    rlCountry.setVisibility(View.GONE);

                    etUserName.requestFocus();
                }
            });

            return view;
        }

    }

    static class ViewHolder {

        @InjectView(R.id.tvCountry)
        TextView tvCountry;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }

    }
}
