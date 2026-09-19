/** Matches Razorpay's `card_networks` keys (AMEX, MC, VISA, RUPAY, MAES, JCB, DICL, UNP). */
export type CardNetwork =
  | 'VISA'
  | 'MC'
  | 'AMEX'
  | 'RUPAY'
  | 'MAES'
  | 'JCB'
  | 'DICL'
  | 'UNP';

export const CARD_NETWORK_LENGTHS: Record<CardNetwork, number[]>;

/** Detects the card network from a (possibly partial) card number, by BIN prefix. */
export function detectCardNetwork(cardNumber: string): CardNetwork | null;

export function passesLuhnCheck(digits: string): boolean;

/** Full validation: known network, correct length for that network, and Luhn checksum. */
export function isValidCardNumber(cardNumber: string): boolean;

/** Longest valid digit length for a network, e.g. for input maxLength. Returns 0 if unknown. */
export function getCardNetworkLength(network: string): number;

/** Amex uses a 4-digit CVV (CID); every other network uses 3. */
export function isValidCardCvv(cvv: string, network: CardNetwork | string | null): boolean;
