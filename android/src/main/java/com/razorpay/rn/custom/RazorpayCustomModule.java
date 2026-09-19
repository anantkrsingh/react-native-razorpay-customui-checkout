
package com.razorpay.rn.custom;

import static com.facebook.react.bridge.UiThreadUtil.runOnUiThread;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.razorpay.PaymentData;
import com.razorpay.PaymentResultWithDataListener;
import org.json.JSONArray;
import org.json.JSONException;
import com.razorpay.Razorpay;
import com.razorpay.RzpUpiSupportedAppsCallback;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import java.util.List;
import java.util.Map;

import com.razorpay.ApplicationDetails;
import com.razorpay.PaymentMethodsCallback;
import com.razorpay.RecommendedInstrumentsCallback;
import com.razorpay.SubscriptionAmountCallback;
import com.razorpay.ValidateVpaCallback;
import com.razorpay.ValidationListener;


public class RazorpayCustomModule extends ReactContextBaseJavaModule implements ActivityEventListener, RzpUpiSupportedAppsCallback  {


  public static final int RZP_REQUEST_CODE = 72967729;
  public static final String MAP_KEY_RZP_PAYMENT_ID = "razorpay_payment_id";
  public static final String MAP_KEY_PAYMENT_ID = "payment_id";
  public static final String MAP_KEY_ERROR_CODE = "code";
  public static final String MAP_KEY_ERROR_DESC = "description";
  public static final String MAP_KEY_PAYMENT_DETAILS = "details";
  public static final String MAP_KEY_WALLET_NAME="name";

  private Razorpay razorpay;
  ReactApplicationContext reactContext;
  public RazorpayCustomModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
    reactContext.addActivityEventListener(this);
  }

  @Override
  public String getName() {
    return "RazorpayCustomui";
  }

  @ReactMethod
  public void open(ReadableMap options) {
    final Activity currentActivity = getCurrentActivity();
    try {
      JSONObject optionsJSON = Utils.readableMapToJson(options);
      Intent intent = new Intent(currentActivity, RazorpayPaymentActivity.class);
      intent.putExtra(Constants.OPTIONS, optionsJSON.toString());
      currentActivity.startActivityForResult(intent, RazorpayPaymentActivity.RZP_REQUEST_CODE);
    } catch (Exception e) {}
  }
//
//  private Razorpay initializeAndReturn(String key){
//    final Activity currentActivity = getCurrentActivity();
//  }

  @ReactMethod
  public void initRazorpay(String key){
    final Activity currentActivity = getCurrentActivity();
    if(currentActivity != null){
      currentActivity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          razorpay = new Razorpay(currentActivity, key);
        }
      });
    }
  }

  @ReactMethod
  public void getCardsNetwork(String cardNumber){
    String cardNetwork = razorpay.getCardNetwork(cardNumber);
      try{
        JSONObject payload = new JSONObject();
        payload.put("data",cardNetwork);
        sendEvent("Razorpay::CARD_NETWORK", Utils.jsonToWritableMap(payload));
      }catch(JSONException e) {
      }
  }

  @ReactMethod
  public void getCardNetworkLength(String networkName){
   int length = this.razorpay.getCardNetworkLength(networkName);
       try{
         JSONObject payload = new JSONObject();
         payload.put("data",length);
         sendEvent("Razorpay::CARD_NETWORK_LENGTH", Utils.jsonToWritableMap(payload));
       }catch(JSONException e){}


  }

  @ReactMethod
  public void getWalletLogoUrl(String walletName){
    String walletUrl = this.razorpay.getWalletLogoUrl(walletName);
    try{
      JSONObject payload = new JSONObject();
      payload.put("data",walletUrl);
      sendEvent("Razorpay::WALLET_LOGO_URL", Utils.jsonToWritableMap(payload));
    }catch (JSONException e){

    }
  }

  @ReactMethod
  public void isCredAppAvailable(){
    final Activity currentActivity = getCurrentActivity();
    boolean available = Razorpay.isCredAppInstalled(currentActivity);
    try{
      JSONObject payload = new JSONObject();
      payload.put("data",available);
      Log.d("REACT_NATIVE",payload.toString());
      sendEvent("Razorpay::CRED_APP_AVAILABLE", Utils.jsonToWritableMap(payload));
    }catch (JSONException e){}
  }

  @ReactMethod
  public void getSubscriptionAmount(String subscriptionId){
    this.razorpay.getSubscriptionAmount(subscriptionId, new SubscriptionAmountCallback() {
      @Override
      public void onSubscriptionAmountReceived(long l) {
        JSONObject payload = new JSONObject();
        try {
          payload.put("data",l);
          sendEvent("Razorpay::SUBSCRIPTION_AMOUNT", Utils.jsonToWritableMap(payload));
        } catch (JSONException e) {
          e.printStackTrace();
        }

      }

      @Override
      public void onError(String s) {
        JSONObject payload = new JSONObject();
        try {
          payload.put("error",s);
          sendEvent("Razorpay::SUBSCRIPTION_AMOUNT", Utils.jsonToWritableMap(payload));
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }
    });
  }

  @ReactMethod
  public void isValidVpa(String vpaAddress){
    this.razorpay.isValidVpa(vpaAddress, new ValidateVpaCallback() {
      @Override
      public void onResponse(JSONObject jsonObject) {
        sendEvent("Razorpay::VPA_VALIDITY",Utils.jsonToWritableMap(jsonObject));
      }

      @Override
      public void onFailure() {
        JSONObject payload = new JSONObject();
        try {
          payload.put("error", "VPA Invalid");
          sendEvent("Razorpay::VPA_VALIDITY",Utils.jsonToWritableMap(payload));
        } catch (JSONException e) {
          e.printStackTrace();
        }

      }
    });
  }

  @ReactMethod
  public void isValidCardNumber(String cardNumber){
    boolean validity = this.razorpay.isValidCardNumber(cardNumber);
    JSONObject payload = new JSONObject();
    try {
      payload.put("data",validity);
      sendEvent("Razorpay::CARD_NUMBER_VALIDITY", Utils.jsonToWritableMap(payload));
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }


  @ReactMethod
  public void getBankLogoUrl(String bankName){
    String bankUrl = this.razorpay.getBankLogoUrl(bankName);
    JSONObject payload = new JSONObject();
    try {
      payload.put("data", bankUrl);
      sendEvent("Razorpay::BANK_LOGO_URL",Utils.jsonToWritableMap(payload));
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  @ReactMethod
  public void getSqWalletLogoUrl(String walletName){
    String walletLogoSq = this.razorpay.getWalletSqLogoUrl(walletName);
    JSONObject payload = new JSONObject();
    try {
      payload.put("data", walletLogoSq);
      sendEvent("Razorpay::SQ_WALLET_LOGO_URL",Utils.jsonToWritableMap(payload));
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  @ReactMethod
  public void getAppsWhichSupportUpi(){
    final Activity currentActivity = getCurrentActivity();
    if (currentActivity != null){
      Razorpay.getAppsWhichSupportUpi(currentActivity, this);
    }else{
      Razorpay.getAppsWhichSupportUpi(this.reactContext.getBaseContext(), this);
    }
  }

  @ReactMethod
  public void getPaymentMethods(){
   razorpay.getPaymentMethods(new PaymentMethodsCallback() {
             @Override
             public void onPaymentMethodsReceived(String result) {

                 /**
                  * This returns JSON data
                  * The structure of this data can be seen at the following link:
                  * https://api.razorpay.com/v1/methods?key_id=rzp_test_1DP5mmOlF5G5ag
                  *
                  */
                 try {
                   JSONObject paymentMethods = new JSONObject(result);
                   sendEvent("Razorpay::PAYMENT_METHODS", Utils.jsonToWritableMap(paymentMethods));
                 } catch (Exception e) {}
             }

             @Override
             public void onError(String error) {
               try {
                 JSONObject jsonError = new JSONObject(error);
                 sendEvent("Razorpay::PAYMENT_METHODS_ERROR", Utils.jsonToWritableMap(jsonError));
               } catch (Exception e) {}
             }
         });

  }

  @ReactMethod
  public void getRecommendedInstruments(ReadableMap options){
    razorpay.getRecommendedInstruments(Utils.readableMapToJson(options), new RecommendedInstrumentsCallback() {
      @Override
      public void onRecommendedInstrumentsReceived(JSONObject recommendedInstruments) {
        sendEvent("Razorpay::RECOMMENDED_INSTRUMENTS", Utils.jsonToWritableMap(recommendedInstruments));
      }

      @Override
      public void onError(String error) {
        JSONObject payload = new JSONObject();
        try {
          payload.put("error", error);
        } catch (JSONException e) {
          e.printStackTrace();
        }
        sendEvent("Razorpay::RECOMMENDED_INSTRUMENTS_ERROR", Utils.jsonToWritableMap(payload));
      }
    });
  }

  @ReactMethod
  public void validateOptions(ReadableMap payload){
    razorpay.validateFields(Utils.readableMapToJson(payload), new ValidationListener() {
      @Override
      public void onValidationSuccess() {
        try{
          JSONObject data = new JSONObject();
          data.put("data", true);
          sendEvent("Razorpay::VALIDATE_OPTIONS", Utils.jsonToWritableMap(data));
        }catch (JSONException e){

        }
      }

      @Override
      public void onValidationError(Map<String, String> map) {
        try{
          JSONObject error = new JSONObject();
          error.put("field", map.get("field"));
          error.put("description", map.get("description"));
          sendEvent("Razorpay::VALIDATE_OPTIONS_ERROR", Utils.jsonToWritableMap(error));
        }catch (JSONException e){

        }
      }
    });
  }


  @Override
  public void onReceiveUpiSupportedApps(List applicationDetailsList){
    List<ApplicationDetails> data = applicationDetailsList;
    JSONObject returnData = new JSONObject();
    JSONArray jsonArray = new JSONArray();

    try{

    for(int i=0;i<applicationDetailsList.size();i++){
        JSONObject app = new JSONObject();
        app.put("appName",data.get(i).getAppName());
        app.put("packageName",data.get(i).getPackageName());
        app.put("iconBase64",data.get(i).getIconBase64());
        app.put("appLogo",data.get(i).getAppLogoUrl());
        jsonArray.put(app);


    }
      returnData.put("data",jsonArray);
    }catch(JSONException e){
      e.printStackTrace();
    }
    sendEvent("Razorpay::UPI_APPS",Utils.jsonToWritableMap(returnData));

  }

  public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
    if(requestCode == RazorpayPaymentActivity.RZP_REQUEST_CODE && resultCode == RazorpayPaymentActivity.RZP_RESULT_CODE){
      onActivityResult(requestCode, resultCode, data);
    }
  }

  public void onNewIntent(Intent intent) {}


  public void onActivityResult(int requestCode, int resultCode, Intent data){
    String paymentDataString = data.getStringExtra(Constants.PAYMENT_DATA);
    JSONObject paymentData = new JSONObject();
    try{
          paymentData = new JSONObject(paymentDataString);
    } catch(Exception e){
    }
     if(data.getBooleanExtra(Constants.IS_SUCCESS, false)){
      String payment_id = data.getStringExtra(Constants.PAYMENT_ID);
      onPaymentSuccess(payment_id, paymentData);
     } else {
      int errorCode = data.getIntExtra(Constants.ERROR_CODE, 0);
      String errorMessage = data.getStringExtra(Constants.ERROR_MESSAGE);
      onPaymentError(errorCode, errorMessage, paymentData);
     }
  }

  private void sendEvent(String eventName, WritableMap params) {
  reactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
      .emit(eventName, params);
  }


    public void onPaymentSuccess(String razorpayPaymentId, JSONObject paymentData) {
      sendEvent("Razorpay::PAYMENT_SUCCESS", Utils.jsonToWritableMap(paymentData));
    }


    public void onPaymentError(int code, String description, JSONObject paymentDataJson) {
      WritableMap errorParams = Arguments.createMap();
      try{
        paymentDataJson.put(MAP_KEY_ERROR_CODE, code);
        paymentDataJson.put(MAP_KEY_ERROR_DESC, description);
      } catch(Exception e){
      }
      sendEvent("Razorpay::PAYMENT_ERROR", Utils.jsonToWritableMap(paymentDataJson));
    }
}
