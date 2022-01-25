package com.smpp.client;

// The main API for dates, times, instants, and durations.
import java.time.ZoneId;
// A date-time with a time-zone in the "ISO-8601" calendar system, such as "2007-12-03T10:15:30+01:00 Europe/Paris".
import java.time.ZonedDateTime;
// Formatter for printing and parsing date-time objects.
import java.time.format.DateTimeFormatter;
// An object that maps keys to values.
import java.util.Map;
// A red-black tree based implementation. It provides an efficient means of storing key-value pairs in sorted order.
import java.util.TreeMap;

// "DateTimeZone" represents a time zone.
import org.joda.time.DateTimeZone;

// A set of "String" utilities.
import com.cloudhopper.commons.util.StringUtil;
// All constants defined for the "SMPP" protocol.
import com.cloudhopper.smpp.SmppConstants;
// "Delivery Receipt Exception" with the specified detail message.
import com.cloudhopper.smpp.util.DeliveryReceiptException;

/**
 * Utility class to represent a "Delivery Receipt" that may be contained within a
 * "DataSm" or "DeliverSm" PDU. A "delivery receipt" has a specific message text and a
 * few specific optional parameters.
 * <p>
 * If the {@link #setRawErrorCode(String)} method takes in a "String" that is not
 * parseable to an "int" via {@link Integer#parseInt(String)} then the
 * {@link #errorCode} property will remain what it was originally set as,
 * default(int) or in the case of
 * {@link #parseShortMessage(String, DateTimeZone)} -1.
 *
 * @author Julius Krah (twitter: @juliuskrah or
 *         <a href="http://twitter.com/juliuskrah" target=
 *         window>http://twitter.com/juliuskrah</a>)
 */
public class DeliveryReceipt {
    // Template format of the dates included with "delivery receipts". Create a formatter using the specified pattern.
    private static final DateTimeFormatter dateFormatTemplate = DateTimeFormatter.ofPattern( "yyMMddHHmm" );
    // Template format of the dates included with "delivery receipts". Create a formatter using the specified pattern.
    private static final DateTimeFormatter dateFormatTemplateWithSeconds = DateTimeFormatter.ofPattern( "yyMMddHHmmss" );
    // Template format of the dates included with "delivery receipts". Create a formatter using the specified pattern.
    // An example of a 3rd format "20110303100008" (yyyyMMddHHmmss).
    private static final DateTimeFormatter dateFormatTemplateWithFullYearAndSeconds = DateTimeFormatter
            .ofPattern( "yyyyMMddHHmmss" );

    // The "err" field cannot be longer than 3 chars.
    public static final int FIELD_ERR_MAX_LEN = 3;

    // "SMPP" provides for return of an SMSC "delivery receipt" via the "deliver_sm" or "data_sm" PDU,
    // which indicates the delivery status of the message.
    // The informational content of an SMSC "delivery receipt" may be inserted into the
    // "short_message" parameter of the "deliver_sm" operation. The format for this "delivery receipt"
    // message is "SMSC" vendor specific but following is a typical example of "delivery receipt" report :
    // "id:IIIIIIIIII sub:SSS dlvrd:DDD submit date:YYMMDDhhmm done date:YYMMDDhhmm stat:DDDDDDD err:E Text: . . . . . "
    // Field "id": id of message originally submitted.
    public static final String FIELD_ID = "id:";
    // Field "sub": number of messages originally submitted.
    public static final String FIELD_SUB = "sub:";
    // Field "dlvrd": number of messages delivered.
    public static final String FIELD_DLVRD = "dlvrd:";
    // Field "submit date": date message was originally submitted at.
    public static final String FIELD_SUBMIT_DATE = "submit date:";
    // Field "done date": date message reached a final "done" state.
    public static final String FIELD_DONE_DATE = "done date:";
    // Field "stat": final state of message.
    public static final String FIELD_STAT = "stat:";
    // Field "err": network/SMSC specific error code. "SMPP 3.4" spec states that the "err" field is a length 3 c-octet string.
    public static final String FIELD_ERR = "err:";
    // Field "text": first 20 characters of original message.
    public static final String FIELD_TEXT = "text:";

    // ID of message originally submitted.
    private String messageId;
    // Number of messages originally submitted.
    private int submitCount;
    // Number of messages delivered.
    private int deliveredCount;
    // Date message was originally submitted at.
    private ZonedDateTime submitDate;
    // Date message reached a final "done" state.
    private ZonedDateTime doneDate;
    // Final state of message.
    private byte state;
    // Network/SMSC specific error code.
    private int errorCode;
    // "SMPP 3.4" spec states that the "err" field is a length 3 c-octet string.
    // In order to allow for reverse compatibility we will store both values.
    private String rawErrorCode;
    // First 20 characters of original message.
    private String text;

    /**
     * Constructor (Overloading).
     */
    public DeliveryReceipt() {
        setErrorCode( 0 );
    }

    /**
     * Constructor (Overloading).
     *
     * @param messageId ID of message originally submitted.
     * @param submitCount Number of messages originally submitted.
     * @param deliveredCount Number of messages delivered.
     * @param submitDate Date message was originally submitted at.
     * @param doneDate Date message reached a final "done" state.
     * @param state Final state of message.
     * @param errorCode Network/SMSC specific error code.
     * @param text First 20 characters of original message.
     */
    public DeliveryReceipt( String messageId, int submitCount, int deliveredCount, ZonedDateTime submitDate,
                           ZonedDateTime doneDate, byte state, int errorCode, String text ) {
        this.messageId = messageId;
        this.submitCount = submitCount;
        this.deliveredCount = deliveredCount;
        this.submitDate = submitDate;
        this.doneDate = doneDate;
        this.state = state;
        setErrorCode( errorCode );
        this.text = text;
    }

    /**
     * Constructor (Overloading).
     *
     * @param messageId ID of message originally submitted.
     * @param submitCount Number of messages originally submitted.
     * @param deliveredCount Number of messages delivered.
     * @param submitDate Date message was originally submitted at.
     * @param doneDate Date message reached a final "done" state.
     * @param state Final state of message.
     * @param errorCode Network/SMSC specific error code. "SMPP 3.4" spec states that the "err" field is a length 3 c-octet string.
     * @param text First 20 characters of original message.
     */
    public DeliveryReceipt( String messageId, int submitCount, int deliveredCount, ZonedDateTime submitDate,
                           ZonedDateTime doneDate, byte state, String errorCode, String text ) {
        this.messageId = messageId;
        this.submitCount = submitCount;
        this.deliveredCount = deliveredCount;
        this.submitDate = submitDate;
        this.doneDate = doneDate;
        this.state = state;
        setRawErrorCode( errorCode );
        this.text = text;
    }

    /**
     * Get number of messages delivered.
     *
     * @return The number of messages delivered.
     */
    public int getDeliveredCount() {
        return deliveredCount;
    }

    /**
     * Set number of messages delivered.
     *
     * @param deliveredCount The number of messages delivered.
     */
    public void setDeliveredCount( int deliveredCount ) {
        this.deliveredCount = deliveredCount;
    }

    /**
     * Get network/SMSC specific error code.
     *
     * @return The network/SMSC specific error code.
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * Set network/SMSC specific error code.
     *
     * @param errorCode The network/SMSC specific error code.
     */
    public void setErrorCode( int errorCode ) {
        // "SMPP 3.4" spec states that the "err" field is a length 3 c-octet string.
        // In order to allow for reverse compatibility we will store both values.
        this.errorCode = errorCode;
        this.rawErrorCode = String.format( "%03d", errorCode );
    }

    /**
     * Network/SMSC specific error code.
     * "SMPP 3.4" spec states that the "err" field is a length 3 c-octet string.
     *
     * @return The network/SMSC specific error code.
     */
    public String getRawErrorCode() {
        return rawErrorCode;
    }

    /**
     * "SMPP 3.4" spec states that the "err" field is a <= 3 c-octet string, this
     * field takes that into account and will be chained with the
     * {@link #setErrorCode(int)} field if the "err" field is valid.
     *
     * @param rawErrorCode The network/SMSC specific error code.
     */
    public void setRawErrorCode( String rawErrorCode ) {
        this.rawErrorCode = rawErrorCode;

        try {
            this.errorCode = Integer.parseInt( rawErrorCode );
        } catch ( Exception e ) {
        }
    }

    /**
     * Get date message reached a final "done" state.
     *
     * @return The date message reached a final "done" state.
     */
    public ZonedDateTime getDoneDate() {
        return doneDate;
    }

    /**
     * Set date message reached a final "done" state.
     *
     * @param finalDate The date message reached a final "done" state.
     */
    public void setDoneDate( ZonedDateTime finalDate ) {
        this.doneDate = finalDate;
    }

    /**
     * Get id of message originally submitted.
     *
     * @return The id of message originally submitted.
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Parse id of message as a signed decimal long.
     *
     * @return The id of message as a signed decimal long.
     * @throws NumberFormatException If the string does not contain a parsable long.
     */
    public long getMessageIdAsLong() throws NumberFormatException {
        return Long.parseLong( this.messageId );
    }

    /**
     * Set the "id" field to the exact "String" we'll use. Utility method provided
     * for setting this value as a long.
     *
     * @param messageId The "id" field to the exact "String" we'll use.
     */
    public void setMessageId( String messageId ) {
        this.messageId = messageId;
    }

    /**
     * Set the "id" field parameter as a long value that is zero padded to 10
     * digits.
     *
     * @param messageId The "id" field parameter as a long value that is zero padded to 10 digits.
     */
    public void setMessageId( long messageId ) {
        this.messageId = String.format( "%010d", messageId );
    }

    /**
     * Get final state of message.
     *
     * @return The final state of message.
     */
    public byte getState() {
        return state;
    }

    /**
     * Set final state of message.
     *
     * @param state The final state of message.
     */
    public void setState( byte state ) {
        this.state = state;
    }

    /**
     * Get number of messages originally submitted.
     *
     * @return The number of messages originally submitted.
     */
    public int getSubmitCount() {
        return submitCount;
    }

    /**
     * Set number of messages originally submitted.
     *
     * @param submitCount The number of messages originally submitted.
     */
    public void setSubmitCount( int submitCount ) {
        this.submitCount = submitCount;
    }

    /**
     * Get date message was originally submitted at.
     *
     * @return The date message was originally submitted at.
     */
    public ZonedDateTime getSubmitDate() {
        return submitDate;
    }

    /**
     * Set date message was originally submitted at.
     *
     * @param submitDate The date message was originally submitted at.
     */
    public void setSubmitDate( ZonedDateTime submitDate ) {
        this.submitDate = submitDate;
    }

    /**
     * Get first 20 characters of original message.
     *
     * @return The first 20 characters of original message.
     */
    public String getText() {
        return text;
    }

    /**
     * Set first 20 characters of original message.
     *
     * @param text The first 20 characters of original message.
     */
    public void setText( String text ) {
        this.text = text;
    }

    /**
     * Build the short message of a "delivery receipt".
     *
     * @return The short message of a "delivery receipt".
     */
    public String toShortMessage() {
        // Construct a string builder object; a mutable sequence of characters.
        StringBuilder buf = new StringBuilder(200 );
        // Append id of message originally submitted.
        buf.append( FIELD_ID );
        buf.append( this.messageId );
        buf.append(" ");
        // Append number of messages originally submitted.
        buf.append( FIELD_SUB );
        buf.append( String.format( "%03d", this.submitCount ) );
        buf.append(" ");
        // Append number of messages delivered.
        buf.append( FIELD_DLVRD );
        buf.append( String.format( "%03d", this.deliveredCount ) );
        buf.append(" ");
        // Append date message was originally submitted at.
        buf.append( FIELD_SUBMIT_DATE );
        if ( this.submitDate == null ) {
            buf.append( "0000000000" );
        } else {
            buf.append( dateFormatTemplate.format( this.submitDate ) );
        }
        buf.append(" ");
        // Append date message reached a final "done" state.
        buf.append( FIELD_DONE_DATE );
        if ( this.doneDate == null ) {
            buf.append( "0000000000" );
        } else {
            buf.append( dateFormatTemplate.format( this.doneDate ) );
        }
        buf.append(" ");
        // Append final state of message.
        buf.append( FIELD_STAT );
        buf.append( toStateText( this.state ) );
        buf.append(" ");
        // Append network/SMSC specific error code.
        buf.append( FIELD_ERR );
        buf.append( this.rawErrorCode );
        buf.append(" ");
        // Append first 20 characters of original message.
        buf.append( FIELD_TEXT );
        if ( this.text != null ) {
            if ( this.text.length() > 20 ) {
                buf.append( this.text.substring( 0, 20 ) );
            } else {
                buf.append( this.text );
            }
        }

        // Return a string representing the data in this sequence; the short message of a "delivery receipt".
        return buf.toString();
    }

    /**
     * Build a string representation of the short message of a "delivery receipt".
     *
     * @return The string representation of the short message of a "delivery receipt".
     */
    @Override
    public String toString() {
        // Construct a string builder object; a mutable sequence of characters.
        StringBuilder buf = new StringBuilder(160 );
        // Append id of message originally submitted.
        buf.append( "(id=" );
        buf.append( this.messageId );
        // Append number of messages originally submitted.
        buf.append( " sub=" );
        buf.append( this.submitCount );
        // Append number of messages delivered.
        buf.append( " dlvrd=" );
        buf.append( this.deliveredCount );
        // Append date message was originally submitted at.
        buf.append( " submitDate=" );
        buf.append( this.submitDate );
        // Append date message reached a final "done" state.
        buf.append( " doneDate=" );
        buf.append( this.doneDate );
        // Append final state of message.
        buf.append( " state=" );
        buf.append( toStateText( this.state ) );
        buf.append( "[" );
        buf.append( this.state );
        // Append network/SMSC specific error code.
        buf.append( "] err=" );
        buf.append( this.rawErrorCode );
        // Append first 20 characters of original message.
        buf.append( " text=[" );
        buf.append( this.text );
        buf.append( "])" );

        // Return a string representation of the short message of a "delivery receipt".
        return buf.toString();
    }

    /**
     * Validation method to guarantee that an "err" value passed in is valid by "SMPP 3.4" spec.
     *
     * @param errorCode The "err" value.
     * @return A Boolean value.
     */
    private static boolean isValidErrorCode( String errorCode ) {
        if ( StringUtil.isEmpty( errorCode )
                || ( !StringUtil.isEmpty( errorCode ) && errorCode.length() <= FIELD_ERR_MAX_LEN ) )
            return true;
        else
            return false;
    }

    /**
     * Parse date-time.
     *
     * @param value The date-time.
     * @param zone The time-zone "ID", such as "Europe/Paris".
     * @return The date-time with a time-zone in the "ISO-8601" calendar system, such as "2007-12-03T10:15:30+01:00 Europe/Paris".
     */
    static private ZonedDateTime parseDateTimeHelper( String value, ZoneId zone ) {
        if ( value == null ) {
            return null;
        }
        // Pick the correct template based on length, and obtain an instance of "ZonedDateTime" from the input date-time
        // string using a specific formatter. The input string is parsed using the formatter, returning a date-time.
        if ( value.length() == 14 ) {
            return ZonedDateTime.parse( value, dateFormatTemplateWithFullYearAndSeconds.withZone( zone ) );
        } else if ( value.length() == 12 ) {
            return ZonedDateTime.parse( value, dateFormatTemplateWithSeconds.withZone( zone ) );
        } else {
            return ZonedDateTime.parse( value, dateFormatTemplate.withZone( zone ) );
        }
    }

    /**
     * Find location of all possible fields in the text of message and add to a
     * "TreeMap" by their "startPos" so that we'll end up with an ordered list
     * of their occurrence. A field "value" only technically ends with the start of the next field "label"
     * since a field value could technically contain ":" or spaces. "SMPP" really has HORRIBLE specs for "delivery receipts".
     *
     * @param normalizedText The converted to lowercase (normalized) text, for case insensitivity.
     * @param field The field in the text of message.
     * @param fieldsByStartPos The "TreeMap" with keys the "startPos" of the fields and mapped values the fields.
     */
    static public void findFieldAndAddToTreeMap( String normalizedText, String field,
                                                TreeMap<Integer, String> fieldsByStartPos ) {
        int startPos = normalizedText.indexOf( field );
        // logger.debug( "Found field " + field + " at startPos " + startPos );
        if ( startPos >= 0 ) {
            fieldsByStartPos.put( startPos, field );
        }
    }

    /**
     * Parse the text of the short message and create a "DeliveryReceipt" from the
     * fields. This method is lenient as possible. The order of the fields does not
     * matter, as well as permitting some fields to be optional.
     * Method Overloading; "checkMissingFields" input parameter is assumed "true" by default.
     *
     * @param shortMessage The short message of the "DeliveryReceipt".
     * @param zone The time-zone "ID", such as "Europe/Paris".
     * @return a "Delivery Receipt" that may be contained within a "DataSm" or "DeliverSm" PDU.
     * @throws DeliveryReceiptException with the specified detail message.
     */
    static public DeliveryReceipt parseShortMessage( String shortMessage, ZoneId zone ) throws DeliveryReceiptException {
        return parseShortMessage( shortMessage, zone, true );
    }

    /**
     * Parse the text of the short message and create a "DeliveryReceipt" from the
     * fields. This method is lenient as possible. The order of the fields does not
     * matter, as well as permitting some fields to be optional.
     * Method Overloading.
     *
     * @param shortMessage The short message of the "DeliveryReceipt".
     * @param zone The time-zone "ID", such as "Europe/Paris".
     * @param checkMissingFields A Boolean value.
     * @return a "Delivery Receipt" that may be contained within a "DataSm" or "DeliverSm" PDU.
     * @throws DeliveryReceiptException with the specified detail message.
     */
    static public DeliveryReceipt parseShortMessage( String shortMessage, ZoneId zone, boolean checkMissingFields )
            throws DeliveryReceiptException {
        // For case insensitivity, convert to lowercase (normalized text).
        String normalizedText = shortMessage.toLowerCase();

        // Create a new "DLR" with fields set to "uninitialized" values.
        DeliveryReceipt dlr = new DeliveryReceipt(null, -1, -1, null, null, (byte) -1, -1, null );
        // Construct a new, empty tree map.
        TreeMap<Integer, String> fieldsByStartPos = new TreeMap<Integer, String>();

        // Find location of all possible fields in text of message and add to
        // "TreeMap" by their "startPos" so that we'll end up with an ordered list
        // of their occurrence. A field "value" only technically ends with the start of the next field "label"
        // since a field value could technically contain ":" or spaces.
        // "SMPP" really has HORRIBLE specs for delivery receipts.
        // Field "id": id of message originally submitted.
        findFieldAndAddToTreeMap( normalizedText, FIELD_ID, fieldsByStartPos );
        // Field "sub": number of messages originally submitted.
        findFieldAndAddToTreeMap( normalizedText, FIELD_SUB, fieldsByStartPos );
        // Field "dlvrd": number of messages delivered.
        findFieldAndAddToTreeMap( normalizedText, FIELD_DLVRD, fieldsByStartPos );
        // Field "submit date": date message was originally submitted at.
        findFieldAndAddToTreeMap( normalizedText, FIELD_SUBMIT_DATE, fieldsByStartPos );
        // Field "done date": date message reached a final "done" state.
        findFieldAndAddToTreeMap( normalizedText, FIELD_DONE_DATE, fieldsByStartPos );
        // Field "stat": final state of message.
        findFieldAndAddToTreeMap( normalizedText, FIELD_STAT, fieldsByStartPos );
        // Field "err": network/SMSC specific error code.
        findFieldAndAddToTreeMap( normalizedText, FIELD_ERR, fieldsByStartPos );
        // Field "text": first 20 characters of original message.
        findFieldAndAddToTreeMap( normalizedText, FIELD_TEXT, fieldsByStartPos );

        // Process all fields in the order they appear.
        Map.Entry<Integer, String> curFieldEntry = fieldsByStartPos.firstEntry();
        while ( curFieldEntry != null ) {
            Map.Entry<Integer, String> nextFieldEntry = fieldsByStartPos.higherEntry( curFieldEntry.getKey() );

            // Calculate the positions for the substring to extract the field value.
            int fieldLabelStartPos = curFieldEntry.getKey().intValue();
            int startPos = fieldLabelStartPos + curFieldEntry.getValue().length();
            int endPos = ( nextFieldEntry != null ? nextFieldEntry.getKey().intValue() : normalizedText.length() );

            // Get field label and value.
            String fieldLabel = curFieldEntry.getValue();
            String fieldValue = shortMessage.substring( startPos, endPos ).trim();

            // logger.debug( "startPos [" + curFieldEntry.getKey() + "] label ["
            // + curFieldEntry.getValue() + "] value [" + fieldValue + "]" );

            // If field value not empty.
            if ( !StringUtil.isEmpty( fieldValue ) ) {
                // Set id of message originally submitted.
                if ( fieldLabel.equalsIgnoreCase( FIELD_ID ) ) {
                    dlr.messageId = fieldValue;
                }
                // Set number of messages originally submitted or throw exception in case of error.
                else if ( fieldLabel.equalsIgnoreCase( FIELD_SUB ) ) {
                    try {
                        dlr.submitCount = Integer.parseInt( fieldValue );
                    } catch ( NumberFormatException e ) {
                        throw new DeliveryReceiptException(
                                "Unable to convert [sub] field with value [" + fieldValue + "] into an integer" );
                    }
                }
                // Set number of messages delivered or throw exception in case of error.
                else if ( fieldLabel.equalsIgnoreCase( FIELD_DLVRD ) ) {
                    try {
                        dlr.deliveredCount = Integer.parseInt( fieldValue );
                    } catch ( NumberFormatException e ) {
                        throw new DeliveryReceiptException(
                                "Unable to convert [dlvrd] field with value [" + fieldValue + "] into an integer" );
                    }
                }
                // Set date message was originally submitted at or throw exception in case of error.
                else if ( fieldLabel.equalsIgnoreCase( FIELD_SUBMIT_DATE ) ) {
                    try {
                        dlr.submitDate = parseDateTimeHelper( fieldValue, zone );
                    } catch ( IllegalArgumentException e ) {
                        throw new DeliveryReceiptException( "Unable to convert [submit date] field with value ["
                                + fieldValue + "] into a datetime object" );
                    }
                }
                // Set date message reached a final "done" state or throw exception in case of error.
                else if ( fieldLabel.equalsIgnoreCase( FIELD_DONE_DATE ) ) {
                    try {
                        dlr.doneDate = parseDateTimeHelper( fieldValue, zone );
                    } catch ( IllegalArgumentException e ) {
                        throw new DeliveryReceiptException( "Unable to convert [done date] field with value ["
                                + fieldValue + "] into a datetime object" );
                    }
                }
                // Set final state of message or throw exception in case of error.
                else if ( fieldLabel.equalsIgnoreCase( FIELD_STAT ) ) {
                    dlr.state = DeliveryReceipt.toState( fieldValue );
                    if ( dlr.state < 0 ) {
                        throw new DeliveryReceiptException(
                                "Unable to convert [stat] field with value [" + fieldValue + "] into a valid state" );
                    }
                }
                // Set network/SMSC specific error code or throw exception in case of error.
                else if ( fieldLabel.equalsIgnoreCase( FIELD_ERR ) ) {
                    if ( isValidErrorCode( fieldValue ) )
                        dlr.setRawErrorCode( fieldValue );
                    else
                        throw new DeliveryReceiptException(
                                "The [err] field was not of a valid lengh of <= " + FIELD_ERR_MAX_LEN );
                }
                // Set first 20 characters of original message.
                else if ( fieldLabel.equalsIgnoreCase( FIELD_TEXT ) ) {
                    dlr.text = fieldValue;
                }
                // Else unsupported field - throw exception.
                else {
                    throw new DeliveryReceiptException( "Unsupported field [" + fieldValue + "] found" );
                }
            }

            // Proceed with the next field in the short message of the "delivery receipt".
            curFieldEntry = nextFieldEntry;
        }

        // Check for missing fields in the short message of the "delivery receipt" if necessary.
        if ( checkMissingFields ) {
            // Check for id of message originally submitted and throw exception in case of error.
            if ( StringUtil.isEmpty( dlr.messageId ) ) {
                throw new DeliveryReceiptException(
                        "Unable to find [id] field or empty value in delivery receipt message" );
            }
            // Check for number of messages originally submitted and throw exception in case of error.
            if ( dlr.submitCount < 0 ) {
                throw new DeliveryReceiptException(
                        "Unable to find [sub] field or empty value in delivery receipt message" );
            }
            // Check for number of messages delivered and throw exception in case of error.
            if ( dlr.deliveredCount < 0 ) {
                throw new DeliveryReceiptException(
                        "Unable to find [dlvrd] field or empty value in delivery receipt message" );
            }
            // Check for date message was originally submitted at and throw exception in case of error.
            if ( dlr.submitDate == null ) {
                throw new DeliveryReceiptException(
                        "Unable to find [submit date] field or empty value in delivery receipt message" );
            }
            // check for date message reached a final "done" state and throw exception in case of error.
            if ( dlr.doneDate == null ) {
                throw new DeliveryReceiptException(
                        "Unable to find [done date] field or empty value in delivery receipt message" );
            }
            // Check for final state of message and throw exception in case of error.
            if ( dlr.state < 0 ) {
                throw new DeliveryReceiptException(
                        "Unable to find [stat] field or empty value in delivery receipt message" );
            }
            // Check for network/SMSC specific error code and throw exception in case of error.
            if ( StringUtil.isEmpty( dlr.rawErrorCode ) && dlr.errorCode < 0 ) {
                throw new DeliveryReceiptException(
                        "Unable to find [err] field or empty value in delivery receipt message" );
            }
        }

        // Return a "DeliveryReceipt" from the fields.
        return dlr;
    }

    /**
     * Check the final state of message and return the corresponding byte value.
     *
     * @param stateText The final state of message.
     * @return The corresponding byte value.
     */
    static public byte toState( String stateText ) {
        if ( stateText == null ) {
            return -1;
        }

        if ( stateText.equalsIgnoreCase("DELIVRD" ) ) {
            // Return "2".
            return SmppConstants.STATE_DELIVERED;
        } else if ( stateText.equalsIgnoreCase("EXPIRED" ) ) {
            // Return "3".
            return SmppConstants.STATE_EXPIRED;
        } else if ( stateText.equalsIgnoreCase("DELETED" ) ) {
            // Return "4".
            return SmppConstants.STATE_DELETED;
        } else if ( stateText.equalsIgnoreCase("UNDELIV" ) ) {
            // Return "5".
            return SmppConstants.STATE_UNDELIVERABLE;
        } else if ( stateText.equalsIgnoreCase("ACCEPTD" ) ) {
            // Return "6".
            return SmppConstants.STATE_ACCEPTED;
        } else if ( stateText.equalsIgnoreCase("UNKNOWN" ) ) {
            // Return "7".
            return SmppConstants.STATE_UNKNOWN;
        } else if ( stateText.equalsIgnoreCase("REJECTD" ) ) {
            // Return "8".
            return SmppConstants.STATE_REJECTED;
        } else if ( stateText.equalsIgnoreCase("ENROUTE" ) ) {
            // Return "1".
            return SmppConstants.STATE_ENROUTE;
        } else {
            return -1;
        }
    }

    /**
     * Check the byte value of the final state of message and return the corresponding string representation.
     *
     * @param state The byte value of the final state of message
     * @return The corresponding string representation.
     */
    static public String toStateText( byte state ) {
        switch (state) {
            case SmppConstants.STATE_DELIVERED:
                // "DELIVRD" with byte value = "2".
                return "DELIVERED";
            case SmppConstants.STATE_EXPIRED:
                // "EXPIRED" with byte value = "3".
                return "EXPIRED";
            case SmppConstants.STATE_DELETED:
                // "DELETED" with byte value = "4".
                return "DELETED";
            case SmppConstants.STATE_UNDELIVERABLE:
                // "UNDELIV" with byte value = "5".
                return "UNDELIVERABLE";
            case SmppConstants.STATE_ACCEPTED:
                // "ACCEPTD" with byte value = "6".
                return "ACCEPTED";
            case SmppConstants.STATE_UNKNOWN:
                // "UNKNOWN" with byte value = "7".
                return "UNKNOWN";
            case SmppConstants.STATE_REJECTED:
                // "REJECTD" with byte value = "8".
                return "REJECTED";
            case SmppConstants.STATE_ENROUTE:
                // "ENROUTE" with byte value = "1".
                return "ENROUTE";
            default:
                return "BADSTAT";
        }
    }

    /**
     * Convert a long value to a hex string. For example, 98765432101L to "16fee0e525".
     *
     * @param value The long value.
     * @return The hex string.
     */
    static public String toMessageIdAsHexString( long value ) {
        return String.format( "%x", value );
    }

    /**
     * Convert a hex string to a long value, e.g. "16fee0e525" to 98765432101L or throw exception in case of error.
     *
     * @param value The hex string.
     * @return The long value.
     * @throws NumberFormatException In case of error.
     */
    static public long toMessageIdAsLong( String value ) throws NumberFormatException {
        return Long.parseLong( value, 16 );
    }
}
