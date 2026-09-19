package com.razorpay.rn.custom;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.razorpay.PaymentResultWithDataListener;
import com.razorpay.Razorpay;
import com.razorpay.PaymentData;
import org.json.JSONObject;
import android.content.Intent;
import java.util.Map;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.util.Log;

public class RazorpayPaymentActivity extends Activity implements PaymentResultWithDataListener {
    private Razorpay razorpay;
    private WebView webview;
    private static final String TAG = RazorpayPaymentActivity.class.getSimpleName();
    private JSONObject payload;
    private String apiKey = null;
    public static final int RZP_REQUEST_CODE = 62442;
    public static final int RZP_RESULT_CODE = 62443;
    public static final int RZP_USER_BACK_PRESSED_ERROR_CODE = 5;
    public static final int RZP_UNKNOWN_ERROR_CODE = 6;
    private boolean isPaymentInProgress = false;

    private static final String KEY_OPTIONS_STRING = "com.razorpay.rn.saved_options_string";
    private static final String KEY_API_KEY = "com.razorpay.rn.saved_api_key";
    private static final String KEY_IS_PAYMENT_IN_PROGRESS = "com.razorpay.rn.is_payment_in_progress";
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (payload != null) {
            outState.putString(KEY_OPTIONS_STRING, payload.toString());
        }
        if (apiKey != null) {
            outState.putString(KEY_API_KEY, apiKey);
        }
        if (isPaymentInProgress) {
            outState.putBoolean(KEY_IS_PAYMENT_IN_PROGRESS, true);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String optionsString = null;
        
        // First try to restore from saved state
        if (savedInstanceState != null) {
            optionsString = savedInstanceState.getString(KEY_OPTIONS_STRING);
            apiKey = savedInstanceState.getString(KEY_API_KEY);
            isPaymentInProgress = savedInstanceState.getBoolean(KEY_IS_PAYMENT_IN_PROGRESS, false);
        }
        // If not in saved state, try to get from Intent
        if (optionsString == null) {
            Intent intent = getIntent();
            if (intent != null) {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    optionsString = extras.getString(Constants.OPTIONS);
                }
            }
        }
        // Validate we have the options string
        if (optionsString == null || optionsString.trim().isEmpty()) {
            Log.e(TAG, "Options string not available. Cannot proceed with payment.");
            returnErrorCallback(RZP_UNKNOWN_ERROR_CODE, "Payment options not found", new PaymentData());
            return;
        }
        try {
            payload = new JSONObject(optionsString);
            if(payload.has(Constants.KEY_ID)){
                apiKey = payload.getString(Constants.KEY_ID);
                payload.remove(Constants.KEY_ID);
            }else if (apiKey == null){
                returnErrorCallback(RZP_UNKNOWN_ERROR_CODE, "API key not found", new PaymentData());
                return;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse options JSON: " + e.getMessage(), e);
            returnErrorCallback(RZP_UNKNOWN_ERROR_CODE, "Invalid payment options format", new PaymentData());
            return;
        }
        initRazorpay();
        createWebView();
        if (!isPaymentInProgress) {
            sendRequest();
        }
    }

    private void initRazorpay() {
        if(apiKey == null || apiKey.trim().isEmpty()){
            Log.e(TAG, "API key missing. Pass key_id in options.");
            returnErrorCallback(RZP_UNKNOWN_ERROR_CODE, "API key missing", new PaymentData());
            return;
        }
        razorpay = new Razorpay(this, apiKey);
    }

    private void createWebView() {

        /**
         * Creating webview and adding it to rootview
         */
        ViewGroup rootview = (ViewGroup) this.findViewById(android.R.id.content);
        webview = new WebView(this);
        webview.setScrollContainer(false);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
        webview.setLayoutParams(params);
        rootview.addView(webview);
        razorpay.setWebView(webview);
    }

    private void sendRequest() {
        try {
            isPaymentInProgress = true;
            razorpay.submit(payload, RazorpayPaymentActivity.this);
        } catch(Exception e) {
            Log.e(TAG, "Failed to submit.", e);
            returnErrorCallback(RZP_UNKNOWN_ERROR_CODE, "Failed to submit.", new PaymentData());
        }
    }

    @Override
    public void onBackPressed() {
            new AlertDialog.Builder(this)
            .setMessage(Constants.BACK_ALERT_MESSAGE)
            .setPositiveButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                }
            })
            .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    if(razorpay != null){
                        razorpay.onBackPressed();
                    }
                    returnErrorCallback(RZP_USER_BACK_PRESSED_ERROR_CODE, "User pressed back button", new PaymentData());
                }
            })
            .show();
    }

    /* callback for permission requested from android */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (razorpay != null) {
            razorpay.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        isPaymentInProgress = false;
        if(razorpay != null){
            razorpay.onActivityResult(requestCode,resultCode,data);
        }else{
            returnErrorCallback(RZP_UNKNOWN_ERROR_CODE, "Something went wrong. Please try again.", new PaymentData());
        }
    }

    @Override
    public void onPaymentSuccess(String razorpayPaymentId, PaymentData paymentData){
        returnSuccessCallback(razorpayPaymentId, paymentData);
    }

    @Override
    public void onPaymentError(int errorCode, String errorDescription, PaymentData paymentData){
        returnErrorCallback(errorCode, errorDescription, paymentData);
    }

    private void returnSuccessCallback(String razorpayPaymentId, PaymentData paymentData){
        Intent returnIntent = new Intent();
        returnIntent.putExtra(Constants.IS_SUCCESS, true);
        returnIntent.putExtra(Constants.PAYMENT_ID, razorpayPaymentId);
        returnIntent.putExtra(Constants.PAYMENT_DATA, paymentData.getData().toString());
        this.setResult(RZP_RESULT_CODE, returnIntent);
        isPaymentInProgress = false;
        this.finish();
    }

    private void returnErrorCallback(int errorCode, String errorDescription, PaymentData paymentData){
        Intent returnIntent = new Intent();
        returnIntent.putExtra(Constants.IS_SUCCESS, false);
        returnIntent.putExtra(Constants.ERROR_CODE, errorCode);
        returnIntent.putExtra(Constants.ERROR_MESSAGE, errorDescription);
        returnIntent.putExtra(Constants.PAYMENT_DATA, paymentData.getData().toString());
        this.setResult(RZP_RESULT_CODE, returnIntent);
        isPaymentInProgress = false;
        this.finish();
    }

}
