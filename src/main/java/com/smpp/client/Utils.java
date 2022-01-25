package com.smpp.client;

/**
 * A class that consists of a set of utility "static" methods, which perform "byte-to-hex" and "hex-to-byte"
 * transformations as well as encoding and decoding of a "hex" string.
 *
 * Tested with Java version "1.8.0_281"
 *
 * @author  Aristotelis Metsinis ( aristotelis.metsinis@gmail.com )
 * @version 1.0
 * @since   January 2022
 */
public class Utils {
    /**
     * "Byte to Hexadecimal" helper method.
     * The bytes are 8-bit signed integers in Java. Therefore, we need to convert each 4-bit segment to
     * "hex" separately and concatenate them. Consequently, we'll get two hexadecimal characters after conversion.
     * For instance, we can write "45" as "0010 1101" in binary, and the hexadecimal equivalent will be "2d".
     *
     * @param num This "byte" will be converted to "hex".
     * @return The "byte" as a "hex" string.
     */
    public static String byteToHex( byte num ) {
        // Create a char array of length 2 to store the output.
        char[] hexDigits = new char[ 2 ];
        // Isolate higher order bits by right shifting 4 bits. And then, apply a mask to isolate lower
        // order 4 bits. Masking is required because negative numbers are internally represented as two's
        // complement of the positive number.
        hexDigits[ 0 ] = Character.forDigit( ( num >> 4 ) & 0xF, 16 );
        // Convert the remaining 4 bits to hexadecimal.
        hexDigits[ 1 ] = Character.forDigit( ( num & 0xF ), 16 );
        // Create a "String" object from the char array. And then, return this object as converted hexadecimal array.
        return new String( hexDigits );
    }

    /**
     * "Hexadecimal to Byte" helper method.
     * A byte contains 8 bits. Therefore, we need two hexadecimal digits to create one byte. We'll convert each
     * hexadecimal digit into binary equivalent separately. And then, we need to concatenate the two four bit-segments
     * to get the byte equivalent. For example, "2d" = "0010 1101" (base 2) = "45".
     *
     * @param hexString This "hex" string will be converted to "byte".
     * @return The "hex" string as a "byte".
     */
    public static byte hexToByte( String hexString ) {
        // Convert hexadecimal characters into integers.
        int firstDigit = toDigit( hexString.charAt( 0 ) );
        int secondDigit = toDigit( hexString.charAt( 1 ) );
        // Left shift most significant digit by 4 bits. Consequently, the binary representation has zeros at four
        // least significant bits. Add the least significant digit to it.
        return (byte) ( ( firstDigit << 4 ) + secondDigit );
    }

    /**
     * "Hexadecimal Char to Integer" helper method.
     *
     * @param hexChar This "hex" character will be converted to integer.
     * @return The "hex" character as an integer.
     * @throws IllegalArgumentException On invalid "hex" character.
     */
    private static int toDigit( char hexChar ) throws IllegalArgumentException {
        // Use the "Character.digit()" method for conversion. If the character value passed to this method is not a
        // valid digit in the specified radix, -1 is returned.
        int digit = Character.digit( hexChar, 16 );
        // Validate the return value and throw an exception if an invalid value was passed.
        if( digit == -1 ) {
            throw new IllegalArgumentException( "Invalid Hexadecimal Character : " + hexChar );
        }
        return digit;
    }

    /**
     * "Byte Array to Hexadecimal String" helper method.
     * We loop through the array and generate hexadecimal pair for each byte.
     * The output will always be in lowercase.
     *
     * @param byteArray This "byte" array will be encoded to a "hex" string.
     * @return The "byte" array as a "hex" string.
     */
    public static String encodeHexString( byte[] byteArray ) {
        StringBuffer hexStringBuffer = new StringBuffer();
        for ( int i = 0; i < byteArray.length; i++ ) {
            hexStringBuffer.append( byteToHex( byteArray[ i ] ) + "" );
        }
        return hexStringBuffer.toString();
    }

    /**
     * "Hexadecimal String to Byte Array" helper method.
     * We need to check if the length of the hexadecimal "String" is an even number. This is because a
     * hexadecimal "String" with odd length will result in incorrect byte representation. Then we'll iterate
     * through the array and convert each hexadecimal pair to a byte.
     *
     * @param hexString This "hex" string will be decoded to a byte array.
     * @return The "hex" string as a "byte" array.
     * @throws IllegalArgumentException On invalid "hex" string.
     */
    public static byte[] decodeHexString( String hexString ) throws IllegalArgumentException {
        if ( hexString.length() % 2 == 1 ) {
            throw new IllegalArgumentException( "Invalid hexadecimal String supplied" );
        }

        byte[] bytes = new byte[ hexString.length() / 2 ];
        for ( int i = 0; i < hexString.length(); i += 2 ) {
            bytes[ i / 2 ] = hexToByte( hexString.substring( i, i + 2 ) );
        }
        return bytes;
    }
}
