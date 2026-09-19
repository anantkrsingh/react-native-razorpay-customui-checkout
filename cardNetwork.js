'use strict';

/**
 * Card network detection, length and Luhn validation done entirely with
 * regex/BIN-prefix matching in JS — no native round trip needed. Network
 * codes match Razorpay's own `card_networks` keys (AMEX, MC, VISA, RUPAY,
 * MAES, JCB, DICL, UNP) so this is a drop-in replacement for the previous
 * native-backed getCardsNetwork/getCardNetworkLength/isValidCardNumber.
 */

// IIN/BIN prefix ranges per network, matched against digits typed so far
// (so the network can be detected as the user is still typing).
const CARD_NETWORK_PATTERNS = [
  ['AMEX', /^3[47]\d*$/],
  ['DICL', /^3(?:0[0-5]|[68]\d)\d*$/],
  ['JCB', /^(?:2131|1800|35\d{0,2})\d*$/],
  ['MAES', /^(?:5[0678]\d{0,2}|6304|6390|67\d{0,2})\d*$/],
  ['MC', /^(?:5[1-5]\d{0,2}|22[2-9]\d{0,2}|2[3-6]\d{0,3}|27[01]\d{0,2}|2720)\d*$/],
  ['UNP', /^62\d*$/],
  ['RUPAY', /^(?:60|65|81|82|508)\d*$/],
  ['VISA', /^4\d*$/],
];

const CARD_NETWORK_LENGTHS = {
  VISA: [13, 16, 19],
  MC: [16],
  AMEX: [15],
  RUPAY: [16],
  MAES: [12, 13, 14, 15, 16, 17, 18, 19],
  JCB: [16],
  DICL: [14],
  UNP: [16, 17, 18, 19],
};

function onlyDigits(value) {
  return String(value || '').replace(/\D/g, '');
}

/** Detects the card network from a (possibly partial) card number, by BIN prefix. */
function detectCardNetwork(cardNumber) {
  const digits = onlyDigits(cardNumber);
  if (!digits) return null;
  const match = CARD_NETWORK_PATTERNS.find(([, pattern]) => pattern.test(digits));
  return match ? match[0] : null;
}

function passesLuhnCheck(digits) {
  let sum = 0;
  let shouldDouble = false;
  for (let i = digits.length - 1; i >= 0; i--) {
    let digit = Number(digits[i]);
    if (shouldDouble) {
      digit *= 2;
      if (digit > 9) digit -= 9;
    }
    sum += digit;
    shouldDouble = !shouldDouble;
  }
  return sum % 10 === 0;
}

/** Full validation: known network, correct length for that network, and Luhn checksum. */
function isValidCardNumber(cardNumber) {
  const digits = onlyDigits(cardNumber);
  const network = detectCardNetwork(digits);
  if (!network || !CARD_NETWORK_LENGTHS[network].includes(digits.length)) {
    return false;
  }
  return passesLuhnCheck(digits);
}

/** Longest valid digit length for a network, e.g. for input maxLength. */
function getCardNetworkLength(network) {
  const lengths = CARD_NETWORK_LENGTHS[network];
  if (!lengths || lengths.length === 0) return 0;
  return Math.max(...lengths);
}

/** Amex uses a 4-digit CVV (CID); every other network uses 3. */
function isValidCardCvv(cvv, network) {
  const expectedLength = network === 'AMEX' ? 4 : 3;
  return new RegExp(`^\\d{${expectedLength}}$`).test(String(cvv || ''));
}

module.exports = {
  CARD_NETWORK_LENGTHS,
  detectCardNetwork,
  passesLuhnCheck,
  isValidCardNumber,
  getCardNetworkLength,
  isValidCardCvv,
};
