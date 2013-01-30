package util

import javax.crypto._
import javax.crypto.spec.SecretKeySpec

/**
 * Cryptographic utilities.
 */
case class Crypto(secret: String) {

  /**
   * Signs the given String with HMAC-SHA1 using the given key.
   */
  def sign(message: String, key: Array[Byte]): String = {
    val mac = Mac.getInstance("HmacSHA1")
    mac.init(new SecretKeySpec(key, "HmacSHA1"))
    toHexString(mac.doFinal(message.getBytes("utf-8")))
  }

  /**
   * Signs the given String with HMAC-SHA1 using the application’s secret key.
   */
  def sign(message: String): String = {
    sign(message, secret.getBytes("utf-8"))
  }

  /**
   * Encrypt a String with the AES encryption standard using the application secret
   * @param value The String to encrypt
   * @return An hexadecimal encrypted string
   */
  def encryptAES(value: String): String = {
    encryptAES(value, secret.substring(0, 16))
  }

  /**
   * Encrypt a String with the AES encryption standard. Private key must have a length of 16 bytes
   * @param value The String to encrypt
   * @param privateKey The key used to encrypt
   * @return An hexadecimal encrypted string
   */
  def encryptAES(value: String, privateKey: String): String = {
    val raw = privateKey.getBytes("utf-8")
    val skeySpec = new SecretKeySpec(raw, "AES")
    val cipher = Cipher.getInstance("AES")
    cipher.init(Cipher.ENCRYPT_MODE, skeySpec)
    toHexString(cipher.doFinal(value.getBytes("utf-8")))
  }

  /**
   * Decrypt a String with the AES encryption standard using the application secret
   * @param value An hexadecimal encrypted string
   * @return The decrypted String
   */
  def decryptAES(value: String): String = {
    decryptAES(value, secret.substring(0, 16))
  }

  /**
   * Decrypt a String with the AES encryption standard. Private key must have a length of 16 bytes
   * @param value An hexadecimal encrypted string
   * @param privateKey The key used to encrypt
   * @return The decrypted String
   */
  def decryptAES(value: String, privateKey: String): String = {
    val raw = privateKey.getBytes("utf-8")
    val skeySpec = new SecretKeySpec(raw, "AES")
    val cipher = Cipher.getInstance("AES")
    cipher.init(Cipher.DECRYPT_MODE, skeySpec)
    new String(cipher.doFinal(hexStringToByte(value)))
  }

  private val hexChars = Array('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')

  /**
   * Converts a byte array into an array of characters that denotes a hexadecimal representation.
   */
  def toHex(array: Array[Byte]): Array[Char] = {
    val result = new Array[Char](array.length * 2)
    for (i <- 0 until array.length) {
      val b = array(i) & 0xff
      result(2 * i) = hexChars(b >> 4)
      result(2 * i + 1) = hexChars(b & 0xf)
    }
    result
  }

  /**
   * Converts a byte array into a `String` that denotes a hexadecimal representation.
   */
  def toHexString(array: Array[Byte]): String = {
    new String(toHex(array))
  }

  /**
   * Transform an hexadecimal String to a byte array.
   */
  def hexStringToByte(hexString: String): Array[Byte] = {
    import org.apache.commons.codec.binary.Hex;
    Hex.decodeHex(hexString.toCharArray());
  }

}
