package com.smpp.client;

// The main API for dates, times, instants, and durations.
import java.time.ZoneOffset;

// Main user entry point of "SLF4J" API; logging takes place through concrete implementations of this interface.
import org.slf4j.Logger;
// Utility class producing "Loggers" for various logging APIs, most notably for "log4j", "logback" and "JDK 1.4" logging.
import org.slf4j.LoggerFactory;

// Utility class for encoding and decoding between Strings and byte arrays.
import com.cloudhopper.commons.charset.CharsetUtil;
// All constants defined for the "SMPP" protocol.
import com.cloudhopper.smpp.SmppConstants;
// Default implementation that provides empty implementations of all required methods.
import com.cloudhopper.smpp.impl.DefaultSmppSessionHandler;
// "deliver_sm" PDU request.
import com.cloudhopper.smpp.pdu.DeliverSm;
// Abstract class that extends "Pdu".
import com.cloudhopper.smpp.pdu.PduRequest;
// Abstract class that extends "Pdu".
import com.cloudhopper.smpp.pdu.PduResponse;
// Utility class for working with "SMPP" such as encoding/decoding a short message, "esm class", or "registered delivery" flags.
import com.cloudhopper.smpp.util.SmppUtil;

/**
 * The default implementation of "SmppSessionListener" is to discard received PDUs.
 * The default implementation class is extended handling "delivery receipts".
 */
public class ClientSmppSessionHandler extends DefaultSmppSessionHandler {
    // Return a logger named corresponding to the class passed as parameter, using the statically bound "ILoggerFactory" instance.
    private static final Logger log = LoggerFactory.getLogger( ClientSmppSessionHandler.class );
    // private ApplicationEventPublisher publisher;
    // Declare a variable that will hold all the external properties we need.
    private final SmppClientProperties properties;

    /**
     * Constructor.
     *
     * @param properties All the external properties we need.
     */
    public ClientSmppSessionHandler( SmppClientProperties properties ) {
        this.properties = properties;
    }

    /**
     * Map "data coding scheme" (DCS) to charset.
     *
     * @param dataCoding The "data coding scheme" (DCS) byte.
     * @return The corresponding charset (name).
     */
    private String mapDataCodingToCharset( byte dataCoding ) {
        switch ( dataCoding ) {
            // In case of "DCS" = 3 return "ISO-8859-1".
            case SmppConstants.DATA_CODING_LATIN1:
                return CharsetUtil.NAME_ISO_8859_1;
            // In case of "DCS" = 8 return "UCS-2".
            case SmppConstants.DATA_CODING_UCS2:
                return CharsetUtil.NAME_UCS_2;
            default:
                // Else return "GSM".
                return CharsetUtil.NAME_GSM;
        }
    }

    /**
     * To receive "delivery reports", an implementation of "SmppSessionListener".
     *
     * @param request The request "PDU".
     * @return The response "PDU".
     */
    @Override
    @SuppressWarnings("rawtypes")
    public PduResponse firePduRequestReceived( PduRequest request ) {
        // Declare a variable that will hold the response "PDU".
        PduResponse response = null;

        try {
            // In case of a "deliver_sm" PDU request.
            if ( request instanceof DeliverSm ) {
                // Get the source address of the "SMPP" PDU.
                String sourceAddress = ( ( DeliverSm ) request ).getSourceAddress().getAddress();
                // Retrieve the bytes array of the "short message" and map the byte of the "data coding scheme" (DCS)
                // of the "SMPP" PDU to the corresponding charset (name). Then get the message of the "SMPP" PDU by
                // decoding the "deliver_sm" PDU.
                String message = CharsetUtil.decode( ( ( DeliverSm ) request ).getShortMessage(),
                        mapDataCodingToCharset( ( ( DeliverSm ) request ).getDataCoding() ) );
                log.info( "SMS Message Received: {}, Source Address: {}", message.trim(), sourceAddress );

                // Declare a boolean variable - flag in case where the "deliver_sm" PDU is a "delivery receipt" message.
                boolean isDeliveryReceipt = false;
                // Check defined "properties" and detect "DLR" by the "Optional Parameters" of the "SMPP" PDU.
                if ( properties.getSmpp().isDetectDlrByOpts() ) {
                    // Get the current list of "Optional Parameters". If no parameters have been added, this will return
                    // null.
                    isDeliveryReceipt = request.getOptionalParameters() != null;
                }
                // If not defined in "properties", check if the "esm_class" (byte) value have a message type set at all.
                // This basically checks if the "esm_class" could either be SMSC "delivery receipt", ESME "delivery receipt",
                // manual user acknowledgement, conversation abort, or an intermediate "delivery receipt". True if the
                // option is set, otherwise false.
                else {
                    isDeliveryReceipt = SmppUtil.isMessageTypeAnyDeliveryReceipt( ( ( DeliverSm ) request ).getEsmClass() );
                }

                // "deliver_sm" PDU is a "delivery receipt" message.
                if ( isDeliveryReceipt ) {
                    // Parse the text of the short message and create a "DeliveryReceipt" from the fields.
                    DeliveryReceipt dlr = DeliveryReceipt.parseShortMessage( message, ZoneOffset.UTC );
                    log.info( "Received delivery from {} at {} with message-id {} and status {}", sourceAddress,
                            dlr.getDoneDate(), dlr.getMessageId(), DeliveryReceipt.toStateText( dlr.getState() ) );
                }
            }

            // Create a "deliver_sm_resp" PDU.
            response = request.createResponse();
        }
        // In case of error.
        catch ( Throwable error ) {
            log.warn( "Error while handling delivery", error );
            // Create a "deliver_sm_resp" PDU.
            response = request.createResponse();
            // Set result error message.
            response.setResultMessage( error.getMessage() );
            // Set "SMPP" command status to "Unknown Error" = "0x000000FF" = 255
            response.setCommandStatus( SmppConstants.STATUS_UNKNOWNERR );
        }

        // Return response "deliver_sm_resp" PDU.
        return response;
    }
}
