'use strict';

import {NativeModules, NativeEventEmitter, Platform} from 'react-native';

const razorpayEvents = new NativeEventEmitter(NativeModules.RazorpayEventEmitterCustom);


const removeSubscriptions = () => {
  razorpayEvents.removeAllListeners('Razorpay::PAYMENT_SUCCESS');
  razorpayEvents.removeAllListeners('Razorpay::PAYMENT_ERROR');
  razorpayEvents.removeAllListeners('Razorpay::UPI_APPS');
  razorpayEvents.removeAllListeners('Razorpay::PAYMENT_METHODS');
  razorpayEvents.removeAllListeners('Razorpay::PAYMENT_METHODS_ERROR');
  razorpayEvents.removeAllListeners('Razorpay::CARD_NETWORK');
  razorpayEvents.removeAllListeners('Razorpay::CRED_APP_AVAILABLE');
  razorpayEvents.removeAllListeners('Razorpay::WALLET_LOGO_URL');
  razorpayEvents.removeAllListeners('Razorpay::SUBSCRIPTION_AMOUNT');
  razorpayEvents.removeAllListeners('Razorpay::CARD_NETWORK_LENGTH');
  razorpayEvents.removeAllListeners('Razorpay::CARD_NUMBER_VALIDITY');
  razorpayEvents.removeAllListeners('Razorpay::VPA_VALIDITY');
  razorpayEvents.removeAllListeners('Razorpay::BANK_LOGO_URL');
  razorpayEvents.removeAllListeners('Razorpay::SQ_WALLET_LOGO_URL');
  razorpayEvents.removeAllListeners('Razorpay::VALIDATE_OPTIONS');
  razorpayEvents.removeAllListeners('Razorpay::VALIDATE_OPTIONS_ERROR');
  razorpayEvents.removeAllListeners('Razorpay::RECOMMENDED_INSTRUMENTS');
  razorpayEvents.removeAllListeners('Razorpay::RECOMMENDED_INSTRUMENTS_ERROR');
};

let isRzpInitialized = false;

class Razorpay {



  static open(options, successCallback, errorCallback) {
    return new Promise(function(resolve, reject) {
      razorpayEvents.addListener('Razorpay::PAYMENT_SUCCESS', (data) => {
        let resolveFn = successCallback || resolve;
        resolveFn(data);
        removeSubscriptions();
      });
      razorpayEvents.addListener('Razorpay::PAYMENT_ERROR', (data) => {
        let rejectFn = errorCallback || reject;
        rejectFn(data);
        removeSubscriptions();
      });
      const payload = options;
      payload['FRAMEWORK'] = "react_native"
      console.log("data");
      console.log(payload);
      NativeModules.RazorpayCustomui.open(payload);
    });
  }
  static getAppsWhichSupportUPI(upiAppCallback){
    return new Promise(function(resolve,reject){
      razorpayEvents.addListener('Razorpay::UPI_APPS',(data)=>{
        let resolveFn = upiAppCallback || resolve;
        resolveFn(data);
        removeSubscriptions();
      });
      NativeModules.RazorpayCustomui.getAppsWhichSupportUpi();
    });
  }

  static getPaymentMethods(paymentMethodsCallback){
    return new Promise(function(resolve, reject){
      if(isRzpInitialized) {
        razorpayEvents.addListener('Razorpay::PAYMENT_METHODS', (data) => {
          let resolveFn = paymentMethodsCallback || resolve;
          resolveFn(data);
          removeSubscriptions();
        });
        razorpayEvents.addListener('Razorpay::PAYMENT_METHODS_ERROR', (error) => {
          let rejectFn = paymentMethodsCallback || reject;
          rejectFn(error);
          removeSubscriptions();
        });
        NativeModules.RazorpayCustomui.getPaymentMethods();
      }else{
        reject({'error':'Please initialize razorpay first by calling Razorpay.init(key)'});
      }
    });
  }

  static getRecommendedInstruments(options, successCallback, errorCallback){
    return new Promise(function(resolve, reject){
      if(isRzpInitialized) {
        razorpayEvents.addListener('Razorpay::RECOMMENDED_INSTRUMENTS', (data) => {
          let resolveFn = successCallback || resolve;
          resolveFn(data);
          removeSubscriptions();
        });
        razorpayEvents.addListener('Razorpay::RECOMMENDED_INSTRUMENTS_ERROR', (error) => {
          let rejectFn = errorCallback || reject;
          rejectFn(error);
          removeSubscriptions();
        });
        NativeModules.RazorpayCustomui.getRecommendedInstruments(options || {});
      }else{
        reject({'error':'Please initialize razorpay first by calling Razorpay.init(key)'});
      }
    });
  }

  static initRazorpay(key){
    // for initializing razorpay object with key
    return new Promise(function (resolve,reject){
      NativeModules.RazorpayCustomui.initRazorpay(key);
      isRzpInitialized = true;
    });
  }

  static getCardsNetwork(cardNumber){
    return new Promise(function(resolve, reject){
      if(isRzpInitialized){
        razorpayEvents.addListener('Razorpay::CARD_NETWORK', (data)=>{
          resolve(data);
          removeSubscriptions();
        });
        NativeModules.RazorpayCustomui.getCardsNetwork(cardNumber);
      }else{
        reject({'error':'Please initialize razorpay first by calling Razorpay.init(key)'});
      }

    });
  }

  static isCredAppAvailable(){
    return new Promise(function(resolve, reject){
      if (isRzpInitialized){
        razorpayEvents.addListener('Razorpay::CRED_APP_AVAILABLE',(data)=>{
          resolve(data);
          removeSubscriptions();
        });
        NativeModules.RazorpayCustomui.isCredAppAvailable();
      }else{
        reject({'error':'Please initialize razorpay first by calling Razorpay.init(key)'});

      }

    });
  }

  static getWalletLogoUrl(walletName){
    return new Promise(function(resolve, reject){
      if(isRzpInitialized){
        razorpayEvents.addListener('Razorpay::WALLET_LOGO_URL',(data)=>{
          resolve(data);
          // removeSubscriptions();
        });
        NativeModules.RazorpayCustomui.getWalletLogoUrl(walletName);
      }else{
        reject({'error':'Please initialize razorpay first by calling Razorpay.init(key)'});
      }

    });
  }

  static getSubscriptionAmount(subscriptionId){
    return new Promise(function(resolve, reject){
      if(isRzpInitialized){
        razorpayEvents.addListener('Razorpay::SUBSCRIPTION_AMOUNT',(data)=>{
          if(data['error']){
            reject(data);
          }else{
            resolve(data);
          }
          removeSubscriptions();
        });
        NativeModules.RazorpayCustomui.getSubscriptionAmount(subscriptionId);
      }else {
        reject({'error':'Please initialize razorpay first by calling Razorpay.init(key)'});
      }

    });
  }

  static calculateEmi(principleAmount, durationInMonths, annualInterestRate){
    return new Promise(function(resolve, reject){
          const rate = annualInterestRate / 1200;
          const multiplier= Math.pow(1 + rate, durationInMonths);
          let data = {
            data: Math.round(principleAmount * rate * multiplier / (multiplier - 1))
          };
          resolve(data);
    });
  }

  static getCardNetworkLength(networkName){
    return new Promise(function(resolve, reject){
      if(isRzpInitialized){
        razorpayEvents.addListener('Razorpay::CARD_NETWORK_LENGTH', (data)=>{
          resolve(data);
          removeSubscriptions();
        });
        NativeModules.RazorpayCustomui.getCardNetworkLength(networkName);
      }else{
        reject({'error':'Please initialize razorpay first by calling Razorpay.init(key)'});
      }

    });
  }

  static isValidCardNumber(cardNumber){
    return new Promise(function(resolve, reject){
      if(isRzpInitialized){
        razorpayEvents.addListener('Razorpay::CARD_NUMBER_VALIDITY', (data)=>{
          resolve(data);
          removeSubscriptions();
        })
        NativeModules.RazorpayCustomui.isValidCardNumber(cardNumber);
      }else{
        reject({'error':'Please initialize razorpay first by calling Razorpay.init(key)'});
      }

    });
  }

  static isValidVpa(vpaAddress){
    return new Promise(function(resolve, reject){
      if(isRzpInitialized) {
        razorpayEvents.addListener('Razorpay::VPA_VALIDITY', (data) => {
          if (data.hasOwnProperty('error')) {
            reject(data);
          } else {
            resolve(data);
          }
          removeSubscriptions();
        })
        NativeModules.RazorpayCustomui.isValidVpa(vpaAddress);
      }else{
        reject({'error':'Please initialize razorpay first by calling Razorpay.init(key)'});
      }
    });
  }

  static getBankLogoUrl(bankName){
    return new Promise(function(resolve, reject){
      if(isRzpInitialized) {
        razorpayEvents.addListener('Razorpay::BANK_LOGO_URL', (data) => {
          resolve(data);
        })
        NativeModules.RazorpayCustomui.getBankLogoUrl(bankName);
      }else{
        reject({'error':'Please initialize razorpay first by calling Razorpay.init(key)'});
      }
    });
  }

  static getSqWalletLogoUrl(walletName){
    return new Promise(function(resolve, reject){
      if(isRzpInitialized) {
        razorpayEvents.addListener('Razorpay::SQ_WALLET_LOGO_URL', (data) => {
          resolve(data);
          // removeSubscriptions();
        });
        NativeModules.RazorpayCustomui.getSqWalletLogoUrl(walletName);
      }else{
        reject({'error':'Please initialize razorpay first by calling Razorpay.init(key)'});
      }
    });
  }

  static validateOptions(payload){
    return new Promise(function(resolve, reject){
      if (Platform.OS === 'ios'){
        resolve({'data':true});
      }else{
        if(isRzpInitialized){
          razorpayEvents.addListener('Razorpay::VALIDATE_OPTIONS', (data)=>{
            resolve(data);
          });
          razorpayEvents.addListener('Razorpay::VALIDATE_OPTIONS_ERROR', (error)=>{
            reject(error);
          });
          NativeModules.RazorpayCustomui.validateOptions(payload);
        }else{
          reject({'error':'Please initialize razorpay first by calling Razorpay.init(key)'});
        }
      }

    });
  }

}

export default Razorpay;
