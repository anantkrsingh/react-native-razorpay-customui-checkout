
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

  private static final String TAG = "RazorpayCustomui";

  public static final int UNKNOWN_ERROR_CODE = 0;
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
    if (currentActivity == null) {
      onPaymentError(UNKNOWN_ERROR_CODE, "No active activity to start payment", new JSONObject());
      return;
    }
    try {
      JSONObject optionsJSON = Utils.readableMapToJson(options);
      Intent intent = new Intent(currentActivity, RazorpayPaymentActivity.class);
      intent.putExtra(Constants.OPTIONS, optionsJSON.toString());
      currentActivity.startActivityForResult(intent, RazorpayPaymentActivity.RZP_REQUEST_CODE);
    } catch (Exception e) {
      Log.e(TAG, "Failed to start payment activity", e);
      onPaymentError(UNKNOWN_ERROR_CODE, "Failed to start payment: " + e.getMessage(), new JSONObject());
    }
  }

  @ReactMethod
  public void initRazorpay(String key){
    final Activity currentActivity = getCurrentActivity();
    if (currentActivity == null) {
      Log.e(TAG, "initRazorpay called with no active activity; razorpay was not initialized");
      return;
    }
    currentActivity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        razorpay = new Razorpay(currentActivity, key);
      }
    });
  }

  @ReactMethod
  public void getCardsNetwork(String cardNumber){
    if (razorpay == null) {
      emitErrorPayload("Razorpay::CARD_NETWORK", "Razorpay is not initialized. Call initRazorpay first.");
      return;
    }
    try {
      String cardNetwork = razorpay.getCardNetwork(cardNumber);
      JSONObject payload = new JSONObject();
      payload.put("data", cardNetwork);
      sendEvent("Razorpay::CARD_NETWORK", Utils.jsonToWritableMap(payload));
    } catch (Exception e) {
      emitErrorPayload("Razorpay::CARD_NETWORK", e.getMessage());
    }
  }

  @ReactMethod
  public void getCardNetworkLength(String networkName){
    if (razorpay == null) {
      emitErrorPayload("Razorpay::CARD_NETWORK_LENGTH", "Razorpay is not initialized. Call initRazorpay first.");
      return;
    }
    try {
      int length = this.razorpay.getCardNetworkLength(networkName);
      JSONObject payload = new JSONObject();
      payload.put("data", length);
      sendEvent("Razorpay::CARD_NETWORK_LENGTH", Utils.jsonToWritableMap(payload));
    } catch (Exception e) {
      emitErrorPayload("Razorpay::CARD_NETWORK_LENGTH", e.getMessage());
    }
  }

  @ReactMethod
  public void getWalletLogoUrl(String walletName){
    if (razorpay == null) {
      emitErrorPayload("Razorpay::WALLET_LOGO_URL", "Razorpay is not initialized. Call initRazorpay first.");
      return;
    }
    try {
      String walletUrl = this.razorpay.getWalletLogoUrl(walletName);
      JSONObject payload = new JSONObject();
      payload.put("data", walletUrl);
      sendEvent("Razorpay::WALLET_LOGO_URL", Utils.jsonToWritableMap(payload));
    } catch (Exception e) {
      emitErrorPayload("Razorpay::WALLET_LOGO_URL", e.getMessage());
    }
  }

  @ReactMethod
  public void isCredAppAvailable(){
    try {
      final Activity currentActivity = getCurrentActivity();
      boolean available = Razorpay.isCredAppInstalled(currentActivity);
      JSONObject payload = new JSONObject();
      payload.put("data", available);
      sendEvent("Razorpay::CRED_APP_AVAILABLE", Utils.jsonToWritableMap(payload));
    } catch (Exception e) {
      emitErrorPayload("Razorpay::CRED_APP_AVAILABLE", e.getMessage());
    }
  }

  @ReactMethod
  public void getSubscriptionAmount(String subscriptionId){
    if (razorpay == null) {
      emitErrorPayload("Razorpay::SUBSCRIPTION_AMOUNT", "Razorpay is not initialized. Call initRazorpay first.");
      return;
    }
    this.razorpay.getSubscriptionAmount(subscriptionId, new SubscriptionAmountCallback() {
      @Override
      public void onSubscriptionAmountReceived(long l) {
        JSONObject payload = new JSONObject();
        try {
          payload.put("data",l);
          sendEvent("Razorpay::SUBSCRIPTION_AMOUNT", Utils.jsonToWritableMap(payload));
        } catch (JSONException e) {
          emitErrorPayload("Razorpay::SUBSCRIPTION_AMOUNT", e.getMessage());
        }

      }

      @Override
      public void onError(String s) {
        emitErrorPayload("Razorpay::SUBSCRIPTION_AMOUNT", s);
      }
    });
  }

  @ReactMethod
  public void isValidVpa(String vpaAddress){
    if (razorpay == null) {
      emitErrorPayload("Razorpay::VPA_VALIDITY", "Razorpay is not initialized. Call initRazorpay first.");
      return;
    }
    this.razorpay.isValidVpa(vpaAddress, new ValidateVpaCallback() {
      @Override
      public void onResponse(JSONObject jsonObject) {
        sendEvent("Razorpay::VPA_VALIDITY",Utils.jsonToWritableMap(jsonObject));
      }

      @Override
      public void onFailure() {
        emitErrorPayload("Razorpay::VPA_VALIDITY", "VPA Invalid");
      }
    });
  }

  @ReactMethod
  public void isValidCardNumber(String cardNumber){
    if (razorpay == null) {
      emitErrorPayload("Razorpay::CARD_NUMBER_VALIDITY", "Razorpay is not initialized. Call initRazorpay first.");
      return;
    }
    try {
      boolean validity = this.razorpay.isValidCardNumber(cardNumber);
      JSONObject payload = new JSONObject();
      payload.put("data",validity);
      sendEvent("Razorpay::CARD_NUMBER_VALIDITY", Utils.jsonToWritableMap(payload));
    } catch (Exception e) {
      emitErrorPayload("Razorpay::CARD_NUMBER_VALIDITY", e.getMessage());
    }
  }


  @ReactMethod
  public void getBankLogoUrl(String bankName){
    if (razorpay == null) {
      emitErrorPayload("Razorpay::BANK_LOGO_URL", "Razorpay is not initialized. Call initRazorpay first.");
      return;
    }
    try {
      String bankUrl = this.razorpay.getBankLogoUrl(bankName);
      JSONObject payload = new JSONObject();
      payload.put("data", bankUrl);
      sendEvent("Razorpay::BANK_LOGO_URL",Utils.jsonToWritableMap(payload));
    } catch (Exception e) {
      emitErrorPayload("Razorpay::BANK_LOGO_URL", e.getMessage());
    }
  }

  @ReactMethod
  public void getSqWalletLogoUrl(String walletName){
    if (razorpay == null) {
      emitErrorPayload("Razorpay::SQ_WALLET_LOGO_URL", "Razorpay is not initialized. Call initRazorpay first.");
      return;
    }
    try {
      String walletLogoSq = this.razorpay.getWalletSqLogoUrl(walletName);
      JSONObject payload = new JSONObject();
      payload.put("data", walletLogoSq);
      sendEvent("Razorpay::SQ_WALLET_LOGO_URL",Utils.jsonToWritableMap(payload));
    } catch (Exception e) {
      emitErrorPayload("Razorpay::SQ_WALLET_LOGO_URL", e.getMessage());
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
    if (razorpay == null) {
      emitErrorPayload("Razorpay::PAYMENT_METHODS_ERROR", "Razorpay is not initialized. Call initRazorpay first.");
      return;
    }
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
                 } catch (Exception e) {
                   emitErrorPayload("Razorpay::PAYMENT_METHODS_ERROR", "Failed to parse payment methods: " + e.getMessage());
                 }
             }

             @Override
             public void onError(String error) {
               try {
                 JSONObject jsonError = new JSONObject(error);
                 sendEvent("Razorpay::PAYMENT_METHODS_ERROR", Utils.jsonToWritableMap(jsonError));
               } catch (Exception e) {
                 emitErrorPayload("Razorpay::PAYMENT_METHODS_ERROR", error);
               }
             }
         });

  }

  @ReactMethod
  public void getRecommendedInstruments(ReadableMap options){
    if (razorpay == null) {
      emitErrorPayload("Razorpay::RECOMMENDED_INSTRUMENTS_ERROR", "Razorpay is not initialized. Call initRazorpay first.");
      return;
    }
    razorpay.getRecommendedInstruments(Utils.readableMapToJson(options), new RecommendedInstrumentsCallback() {
      @Override
      public void onRecommendedInstrumentsReceived(JSONObject recommendedInstruments) {
        sendEvent("Razorpay::RECOMMENDED_INSTRUMENTS", Utils.jsonToWritableMap(recommendedInstruments));
      }

      @Override
      public void onError(String error) {
        emitErrorPayload("Razorpay::RECOMMENDED_INSTRUMENTS_ERROR", error);
      }
    });
  }

  @ReactMethod
  public void validateOptions(ReadableMap payload){
    if (razorpay == null) {
      emitErrorPayload("Razorpay::VALIDATE_OPTIONS_ERROR", "Razorpay is not initialized. Call initRazorpay first.");
      return;
    }
    razorpay.validateFields(Utils.readableMapToJson(payload), new ValidationListener() {
      @Override
      public void onValidationSuccess() {
        try{
          JSONObject data = new JSONObject();
          data.put("data", true);
          sendEvent("Razorpay::VALIDATE_OPTIONS", Utils.jsonToWritableMap(data));
        }catch (JSONException e){
          emitErrorPayload("Razorpay::VALIDATE_OPTIONS_ERROR", e.getMessage());
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
          emitErrorPayload("Razorpay::VALIDATE_OPTIONS_ERROR", e.getMessage());
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

  /** Emits a {"error": message} payload on eventName, so a waiting JS promise
   *  settles instead of hanging forever when a native call can't proceed. */
  private void emitErrorPayload(String eventName, String message) {
    JSONObject payload = new JSONObject();
    try {
      payload.put("error", message != null ? message : "Unknown error");
    } catch (JSONException e) {
      // "error" is always a valid JSON key/string value; this can't happen.
    }
    sendEvent(eventName, Utils.jsonToWritableMap(payload));
  }


    public void onPaymentSuccess(String razorpayPaymentId, JSONObject paymentData) {
      sendEvent("Razorpay::PAYMENT_SUCCESS", Utils.jsonToWritableMap(paymentData));
    }


    public void onPaymentError(int code, String description, JSONObject paymentDataJson) {
      try{
        paymentDataJson.put(MAP_KEY_ERROR_CODE, code);
        paymentDataJson.put(MAP_KEY_ERROR_DESC, description);
      } catch(Exception e){
      }
      sendEvent("Razorpay::PAYMENT_ERROR", Utils.jsonToWritableMap(paymentDataJson));
    }
}
