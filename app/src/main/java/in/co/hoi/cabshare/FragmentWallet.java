package in.co.hoi.cabshare;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.codec.DecoderException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

/**
 * Created by Ujjwal on 27-06-2015.
 */
public class FragmentWallet extends Fragment{
    String authenticationHeader;
    Double balance;

    public static final String AUTH_HEAD = "authenticationheader";
    private final static String BASE_SERVER_URL = "http://www.hoi.co.in/api/";
    //public static final String IT = "itemName";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_wallet, container, false);
        authenticationHeader = getArguments().getString(AUTH_HEAD);
        balance = getArguments().getDouble("balance");
        TabHost tabs = (TabHost)view.findViewById(R.id.tabHost);
        tabs.setup();

        // Wallet
        TabHost.TabSpec walletTab = tabs.newTabSpec("wallet");
        walletTab.setContent(R.id.wallet);
        walletTab.setIndicator("Wallet");
        tabs.addTab(walletTab);

        TextView remBalance = (TextView) view.findViewById(R.id.remaining_balance);
        remBalance.setText("\u20B9 " + ((double) Math.round(balance) * 100) / 100);

        // Credit
        TabHost.TabSpec creditTab = tabs.newTabSpec("credit");
        creditTab.setContent(R.id.credits);
        creditTab.setIndicator("Credits");
        tabs.addTab(creditTab);

        // Debit
        TabHost.TabSpec debitTab = tabs.newTabSpec("debit");
        debitTab.setContent(R.id.debits);
        debitTab.setIndicator("Debits");
        tabs.addTab(debitTab);

        //Voucher Code
        TextView voucher = (TextView) view.findViewById(R.id.voucher);
        voucher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).createVoucherDialog();
            }
        });
        return view;
    }

    public void setBalance(Double balance){
        TextView remBalance = (TextView) getView().findViewById(R.id.remaining_balance);
        remBalance.setText("\u20B9 " + ((double) Math.round(balance) * 100) / 100);
    }

    public void setCredits(List<TransactionItem> transactionList){
        WalletListAdapter creditsAdapter = new WalletListAdapter(getActivity(), transactionList, getResources());
        ListView creditlv = (ListView) getView().findViewById(R.id.credits_list);
        creditlv.setAdapter(creditsAdapter);
    }

    public void setDebits(List<TransactionItem> transactionList){
        WalletListAdapter debitsAdapter = new WalletListAdapter(getActivity(), transactionList, getResources());
        ListView debitlv = (ListView) getView().findViewById(R.id.debits_list);
        debitlv.setAdapter(debitsAdapter);
    }

    private class HttpPostTask extends AsyncTask<String, Void, String> {

        private HttpResponse response;

        @Override
        protected String doInBackground(String... url) {
            // TODO: attempt authentication against a network service.
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm");
            String currentDateandTime = sdf.format(new Date());
            Encryptor encryptor = new Encryptor();
            String encrptedkey = "";

            try {
                encrptedkey = encryptor.getEncryptedKeyValue(currentDateandTime);
            } catch (InvalidKeyException e1) {
                e1.printStackTrace();
            } catch (IllegalBlockSizeException e1) {
                e1.printStackTrace();
            } catch (BadPaddingException e1) {
                e1.printStackTrace();
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            } catch (DecoderException e1) {
                e1.printStackTrace();
            }

            try {

                HttpPost request = new HttpPost(url[0]);
                request.addHeader("Authorization", "Basic " + authenticationHeader);
                request.addHeader("androidkey",encrptedkey);

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                nameValuePairs.add(new BasicNameValuePair("counter", currentDateandTime));

                request.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpClient httpclient = new DefaultHttpClient();

                try {
                    response = httpclient.execute(request);
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            InputStream inputStream = null;
            String result = null;
            try {
                HttpEntity entity = response.getEntity();
                inputStream = entity.getContent();

                // json is UTF-8 by default
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null)
                {
                    sb.append(line + "\n");
                }
                result = sb.toString();
            } catch (Exception e) {
                // Oops
            }
            finally {
                try{if(inputStream != null)inputStream.close();}catch(Exception squish){}
            }
            return result;
        }

        @Override
        protected void onPostExecute(final String data) {
        }

        @Override
        protected void onCancelled() {

        }
    }
}
