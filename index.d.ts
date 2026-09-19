export type UpiApp = {
  appName: string;
  packageName: string;
  iconBase64?: string;
  appLogo?: string;
};

export type PaymentMethods = {
  entity?: string;
  card?: boolean;
  credit_card?: boolean;
  debit_card?: boolean;
  prepaid_card?: boolean;
  upi?: boolean;
  upi_intent?: boolean;
  upi_type?: {
    collect?: number;
    intent?: number;
  };
  upi_config?: Record<string, unknown>;
  netbanking?: Record<string, string>;
  wallet?: Record<string, boolean | string>;
  paylater?: Record<string, boolean> | boolean;
  emi?: boolean | Record<string, unknown>;
  cardless_emi?: Record<string, unknown> | boolean;
  card_networks?: Record<string, number>;
  app?: Record<string, number>;
  gpay?: boolean;
  google_pay_cards?: boolean;
  cod?: boolean;
  [key: string]: unknown;
};

export type RazorpaySuccess = {
  razorpay_payment_id: string;
  razorpay_order_id: string;
  razorpay_signature: string;
};

export type RazorpayError = {
  code?: number | string;
  description?: string;
  error?: {
    code?: string;
    description?: string;
    source?: string;
    step?: string;
    reason?: string;
  };
};

export type RazorpayOpenOptions = {
  key_id: string;
  amount: string | number;
  currency?: string;
  order_id: string;
  email?: string;
  contact?: string;
  description?: string;
  method: string;
  image?: string;
  notes?: Record<string, string>;
  upi_app_package_name?: string;
  '_[flow]'?: string;
  vpa?: string;
  bank?: string;
  wallet?: string;
  provider?: string;
  'card[name]'?: string;
  'card[number]'?: string;
  'card[cvv]'?: string;
  'card[expiry_month]'?: string;
  'card[expiry_year]'?: string;
  [key: string]: unknown;
};

declare class Razorpay {
  static initRazorpay(key: string): Promise<void>;
  static open(
    options: RazorpayOpenOptions,
    successCallback?: (data: RazorpaySuccess) => void,
    errorCallback?: (error: RazorpayError) => void,
  ): Promise<RazorpaySuccess>;
  static getAppsWhichSupportUPI(
    callback?: (data: { data: UpiApp[] }) => void,
  ): Promise<{ data: UpiApp[] }>;
  static getPaymentMethods(
    callback?: (data: PaymentMethods) => void,
  ): Promise<PaymentMethods>;
  static isValidVpa(vpaAddress: string): Promise<{ success?: boolean }>;
  static getBankLogoUrl(bankName: string): Promise<{ data: string }>;
  static getWalletLogoUrl(walletName: string): Promise<{ data: string }>;
  static isValidCardNumber(cardNumber: string): Promise<{ data: boolean }>;
  static getCardsNetwork(cardNumber: string): Promise<{ data: string }>;
}

export default Razorpay;
