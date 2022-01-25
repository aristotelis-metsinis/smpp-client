package com.smpp.client;

// Utility class for encoding and decoding between "Strings" and byte arrays.
import com.cloudhopper.commons.charset.CharsetUtil;
// Utility class for encoding or decoding objects to a hexadecimal format.
import com.cloudhopper.commons.util.HexUtil;
// Utility methods for working with "GSM" mobile technologies.
import com.cloudhopper.commons.gsm.GsmUtil;
// Enumeration of all "SMPP" session types.
import com.cloudhopper.smpp.SmppBindType;
// All constants defined for the "SMPP" protocol.
import com.cloudhopper.smpp.SmppConstants;
// Defines a common interface for either a Client (ESME) or Server (SMSC) "SMPP" session.
import com.cloudhopper.smpp.SmppSession;
// Configuration to bind an "SmppSession" as an "ESME" to an "SMSC".
import com.cloudhopper.smpp.SmppSessionConfiguration;
// Default implementation to "bootstrap" client "SMPP" sessions (create & bind).
import com.cloudhopper.smpp.impl.DefaultSmppClient;
// "enquire_link" SMPP PDU.
import com.cloudhopper.smpp.pdu.EnquireLink;
// "enquire_link_resp" SMPP PDU.
import com.cloudhopper.smpp.pdu.EnquireLinkResp;
// "submit_sm" SMPP PDU.
import com.cloudhopper.smpp.pdu.SubmitSm;
// "submit_sm_resp" SMPP PDU.
import com.cloudhopper.smpp.pdu.SubmitSmResp;
// "Tag-Length-Value" (TLV) optional parameter in "SMPP".
import com.cloudhopper.smpp.tlv.Tlv;
// Import class responsible for a simple representation of an "Address" in "SMPP", a class for the configuration to create
// a "TCP/IP" connection (Channel) for an "SmppSession", several "SMPP PDU" related "exception" classes, etc.
import com.cloudhopper.smpp.type.*;

// Copyright (c) 2000-2002 Empower Interactive Group Ltd.
// Gateway client functionality - represents a message, and all associated attributes.
import eigroup.sms.gateway.Message;
// Represents a "WapPush" message of content type "Service Indication".
import eigroup.sms.wappush.ServiceIndication;
// Represents a "WapPush" message of content type "Service Loading".
import eigroup.sms.wappush.ServiceLoading;
// A generic exception for this package. Objects of this class can also encapsulate another "Throwable" object.
import eigroup.sms.wappush.WapPushException;

// "SMS" User Data.
import org.marre.sms.SmsUserData;
// "MMS" Constants.
import org.marre.wap.mms.MmsConstants;
// Create an "MMS" notification "WAP push" message with "SMS" as bearer.
import org.marre.wap.push.SmsMmsNotificationMessage;

// Provides a simple "API" for presenting, processing and validating a "Command Line Interface".
import org.apache.commons.cli.*;

// Main user entry point of "SLF4J" API; logging takes place through concrete implementations of this interface.
import org.slf4j.Logger;
// Utility class producing "Loggers" for various logging APIs, most notably for "log4j", "logback" and "JDK 1.4" logging.
import org.slf4j.LoggerFactory;

// Marks a constructor, field, setter method or config method as to be autowired by Spring's dependency injection facilities.
import org.springframework.beans.factory.annotation.Autowired;
// Class that can be used to bootstrap and launch a "Spring" application from a Java main method.
import org.springframework.boot.SpringApplication;
// Indicates a configuration class that declares one or more "@Bean" methods and also triggers autoconfiguration and
// component scanning.
import org.springframework.boot.autoconfigure.SpringBootApplication;
// Enable support for "@ConfigurationProperties" annotated beans.
import org.springframework.boot.context.properties.EnableConfigurationProperties;
// "SPI" interface to be implemented by most if not all application contexts.
import org.springframework.context.ConfigurableApplicationContext;
// Indicates that a method produces a bean to be managed by the "Spring" container.
import org.springframework.context.annotation.Bean;
// Enables Spring's scheduled task execution capability, similar to functionality found in Spring's "<task:*>" XML namespace.
import org.springframework.scheduling.annotation.EnableScheduling;
// Annotation that marks a method to be scheduled.
import org.springframework.scheduling.annotation.Scheduled;

// The "PreDestroy" annotation is used on methods as a callback notification to signal that the instance is in the process
// of being removed by the container.
import javax.annotation.PreDestroy;

// Thrown to indicate that a malformed "URL" has occurred.
import java.net.MalformedURLException;
// Checked exception thrown to indicate that a string could not be parsed as a "URI" reference.
import java.net.URISyntaxException;
// Class "URL" represents a "Uniform Resource Locator", a pointer to a "resource" on the "World Wide Web".
import java.net.URL;

// Hash table based implementation of the "Map" interface.
import java.util.HashMap;
// An object that maps keys to values.
import java.util.Map;
// This class consists of static utility methods for operating on objects.
import java.util.Objects;
// An instance of this class is used to generate a stream of pseudorandom numbers.
import java.util.Random;
// Factory and utility methods for "Executor", "ExecutorService", "ScheduledExecutorService", "ThreadFactory", and "Callable"
// classes defined in this package.
import java.util.concurrent.Executors;

/**
 * Short Message Peer-to-Peer (SMPP) using "Spring Boot" and "CloudHopper". This is a demo application that sends
 * "SMS" messages and listens for "delivery receipts" using the "SMPP" protocol, bootstraped with "Spring Boot" and
 * using the "Cloudhopper SMPP" library for sending "SMS".
 *
 * Have a look @ https://juliuskrah.com/blog/2018/12/28/building-an-smpp-application-using-spring-boot/
 * as well as @ https://github.com/juliuskrah/smpp
 *
 * If you like to check a demo application that sends "SMS" using the "SMPP" protocol, bootstraped with "Spring Boot"
 * and "Camel" have a look @ https://juliuskrah.com/blog/2020/03/27/building-an-smpp-application-using-spring-boot-and-camel/
 *
 * The application supports the submission of "Text" and "Flash" SMS, "WAP Push SI" (Service Indication) and
 * "WAP Push SL" (Service Loading) messages as well as "MMS WAP Push" notifications.
 *
 * Tested with Java version "1.8.0_281"
 *
 * @author  Aristotelis Metsinis ( aristotelis.metsinis@gmail.com )
 * @version 1.0
 * @since   January 2022
 */
// Enables Spring's scheduled task execution capability.
@EnableScheduling
// Indicates a configuration class that declares one or more "@Bean" methods and also triggers autoconfiguration and
// component scanning.
@SpringBootApplication
// Enable support for "@ConfigurationProperties" annotated beans.
@EnableConfigurationProperties( SmppClientProperties.class )
public class SmppClient {
    // Return a logger named corresponding to the class passed as parameter, using the statically bound "ILoggerFactory" instance.
    private static final Logger log = (Logger) LoggerFactory.getLogger( SmppClient.class );

    // The well-known "Short Message Service" (SMS) - text messaging service.
    private static final String SMS = "sms";
    // A "Flash" SMS is a type of "SMS" that appears directly on the main screen without user interaction and is not
    // automatically stored in the inbox.
    private static final String FLASH = "flash";
    // On receiving a "WAP Push", a "WAP 1.2" (or later) enabled handset will automatically give the user the option to
    // access the "WAP" content.
    private static final String WAP_SI = "wapsi";
    // Directly opens the browser to display the "WAP" content, without user interaction. Since this behaviour raises
    // security concerns, some handsets handle "WAP Push SL" messages in the same way as "SI", by providing user interaction.
    private static final String WAP_SL = "wapsl";
    // A Mobile Terminating "MMS" is triggered by a "Multimedia Message Notification", i.e. "m-notification.ind". The "MMS"
    // notification is used to inform the end user mobile that an "MMS" is waiting to be fetched. Usually the "m-notification.ind"
    // is sent to the mobile phone by means of an "SMS".
    private static final String WAP_M_NOTIFICATION_IND = "mms";

    // Mark a constructor, field, setter method or config method as to be autowired by Spring's dependency injection facilities.
    @Autowired
    // Define a common interface for either a Client (ESME) or Server (SMSC) "SMPP" session.
    private SmppSession session;
    // Mark a constructor, field, setter method or config method as to be autowired by Spring's dependency injection facilities.
    @Autowired
    // Αll the external properties we need.
    private SmppClientProperties properties;

    /**
     * The main method within the Java application.
     *
     * @param args The array of Java "command line arguments".
     */
    public static void main( String[] args ) {
        // Parse, retrieve and return the "command line arguments" in a "Map" object.
        Map<String,String> arguments = getCLI( args );

        // Static helper that can be used to run a "SpringApplication" from the specified sources using default settings
        // and user supplied arguments. Return the running "ApplicationContext".
        ConfigurableApplicationContext ctx = SpringApplication.run( SmppClient.class, args );

        // Return the "bean" instance that uniquely matches the given object type.
        SmppSession session = ctx.getBean( SmppSession.class );

        // Check the type of the message we like to send.
        switch ( arguments.get( "messageType" ).toLowerCase() ) {
            case FLASH:
                // "Flash" SMS.
                // "switch" block without "break" statement. So, the execution will continue into the next "case" until a
                // "break" statement is reached. In practice, a "Flash" SMS will "execute" the statements of a "text" SMS.
            case SMS :
                // "Text" SMS.
                // Encodings : SMPP ENCODING DEFAULT | SMPP ENCODING ISO88591 | SMPP ENCODING ISO10646
                //             7bit length GSM       | 8bit length iso-8859-1 | UCS2 length utf-16-be
                //             SMSC Default          | Latin 1                | UCS2 ( ISO/IEC-10646 )
                // Encode the input text message to the "Latin-1/ISO-8859-1" alphabet.
                byte[] text_message = CharsetUtil.encode( arguments.get( "message" ), CharsetUtil.CHARSET_ISO_8859_1 );

                // Generate new "CSMS" reference number - it should be used in case of a "concatenated" message.
                // Each "SMS" part shall have the same reference number.
                byte[] referenceNumber = new byte[ 1 ];
                // Create a new random number generator and generate random bytes and place them into the supplied byte array.
                new Random().nextBytes( referenceNumber );
                // Create multiple short messages (that include a "User Data Header") by splitting the "text_message" data
                // into 134 byte parts. If the "text_message" does not need to be concatenated (less than or equal to 140 bytes),
                // this method will return NULL.
                // WARNING: This method only works on binary short messages that use 8-bit bytes. Short messages using 7-bit data
                // or packed 7-bit data will not be correctly handled by this method.
                // Ref : http://en.wikipedia.org/wiki/Concatenated_SMS
                byte[][] concatenated_sms = GsmUtil.createConcatenatedBinaryShortMessages( text_message, ( byte ) referenceNumber[ 0 ] );

                // The "text_message" is a "concatenated" message.
                if ( !Objects.isNull( concatenated_sms ) ) {
                    // Call the method to send a "text" or "Flash" SMS for all elements of the array of byte arrays representing
                    // each chunk (including "UDH").
                    for ( byte[] sms : concatenated_sms ) {
                        new SmppClient().sendTextMessage( session,
                                arguments.get( "sourceAddress" ),
                                arguments.get( "destinationAddress" ),
                                sms,
                                true,
                                Boolean.parseBoolean( arguments.get( "deliveryReceipt" ) ),
                                arguments.get( "messageType" ).toLowerCase().equals( FLASH ) );
                    }
                }
                // The "text_message" is not a "concatenated" message.
                else {
                    // Call the method to send a "text" or "Flash" SMS.
                    new SmppClient().sendTextMessage( session,
                            arguments.get( "sourceAddress" ),
                            arguments.get( "destinationAddress" ),
                            text_message,
                            false,
                            Boolean.parseBoolean( arguments.get( "deliveryReceipt" ) ),
                            arguments.get( "messageType" ).toLowerCase().equals( FLASH ) );
                }

                // Break out of the "switch" block.
                break;
            case WAP_SI :
                // Call the method to send a WAP Push "Service Indication" message or throw exception in case of error.
                try {
                    // Generate the "WAP Push SI" (Service Indication) message.
                    Message[] messages = generateWapPushSI( arguments.get( "destinationAddress" ),
                            arguments.get( "wapPushHref" ),
                            arguments.get( "message" ) );

                    // "Concatenated messages" are also supported.
                    for ( Message msg : messages ) {
                        // Send the "WAP Push SI" (Service Indication) message (part).
                        new SmppClient().sendWapPushMessage( session,
                                arguments.get( "sourceAddress" ),
                                arguments.get( "destinationAddress" ),
                                // The complete (binary) "SMS" in "<UDH> + <BODY>" hex format.
                                Utils.encodeHexString( msg.getUDH() ).concat( Utils.encodeHexString( msg.getBinaryMessageBody() ) ),
                                Boolean.parseBoolean( arguments.get( "deliveryReceipt" ) ) );
                    }
                } catch ( MalformedURLException | WapPushException e ) {
                    throw new IllegalStateException( e );
                }
                // Break out of the "switch" block.
                break;
            case WAP_SL :
                // Call the method to send a WAP Push "Service Loading" message or throw exception in case of error.
                try {
                    // Generate the "WAP Push SL" (Service Loading) message.
                    Message[] messages = generateWapPushSL( arguments.get( "destinationAddress" ),
                            arguments.get( "wapPushHref" ) );

                    // Concatenated messages are also supported.
                    for ( Message msg : messages ) {
                        // Send the "WAP Push SL" (Service Loading) message (part).
                        new SmppClient().sendWapPushMessage( session,
                                arguments.get( "sourceAddress" ),
                                arguments.get( "destinationAddress" ),
                                // The complete (binary) "SMS" in "<UDH> + <BODY>" hex format.
                                Utils.encodeHexString( msg.getUDH() ).concat( Utils.encodeHexString( msg.getBinaryMessageBody() ) ),
                                Boolean.parseBoolean( arguments.get( "deliveryReceipt" ) ) );
                    }
                } catch ( MalformedURLException | WapPushException e ) {
                    throw new IllegalStateException( e );
                }
                // Break out of the "switch" block.
                break;
            case WAP_M_NOTIFICATION_IND :
                // Call the method to send an "MMS" notification "WAP Push" message with "SMS" as bearer.
                // Generate the "Multimedia Message Notification", i.e. "m-notification.ind".
                Map<String,Object> msg = generateWapPush_M_NOTIFICATION_IND( arguments.get( "sourceAddress" ),
                        arguments.get( "wapPushHref" ), arguments.get( "mmSubject" ), Long.parseLong( arguments.get( "mmSize" ) ) );

                // "WAP_M_NOTIFICATION_IND" messages with length more than 254 octets are also supported by making use of
                // the "message_payload" (0x0424) optional parameter. Specifically, according to the "SMPP v3.4" protocol,
                // the maximum message length which can be specified in "sm_length" field (section 5.2.21) is 254 octets.
                // If an "ESME" wishes to submit a message of length greater than 254 octets, the "sm_length" field, which
                // specifies the length of the "short_message" parameter in octets, must be set to NULL (set to zero) and
                // the "message_payload" (0x0424) optional parameter must be populated with the message length value and
                // user data. Recall that the short message data must be inserted in either the "short_message" or
                // "message_payload" fields. Both fields must not be used simultaneously. At the same time, the application
                // sets the "UDHI" flag in the "esm_class" field since short message contains GSM User Data Header information
                // encoded in the "message_payload" parameter.
                // Send the "WAP Push" message.
                new SmppClient().sendWapPushMessage( session,
                        arguments.get( "sourceAddress" ),
                        arguments.get( "destinationAddress" ),
                        ( ( String ) msg.get( "UDH" ) ).concat( Utils.encodeHexString( ( ( SmsUserData ) msg.get( "BinaryMessageBody" ) ).getData() ) ),
                        Boolean.parseBoolean( arguments.get( "deliveryReceipt" ) ) );
        }
    }

    /**
     * To start using "SMPP" to send messages, we need to establish a session.
     * To bind a session, we need a "SmppSessionConfiguration" and "SmppClient".
     * The "SmppSessionConfiguration" class contains the configurable aspects of the "SmppSession".
     * The following method uses the externalized configuration.
     *
     * @param properties All the external properties we need.
     * @return The configuration to bind an "SmppSession" as an "ESME" to an "SMSC".
     */
    public SmppSessionConfiguration sessionConfiguration( SmppClientProperties properties ) {
        // Instantiate a session configuration object.
        SmppSessionConfiguration sessionConfig = new SmppSessionConfiguration();
        // Set the session name.
        sessionConfig.setName( "smpp.session" );
        // Set the version of the "SMPP" protocol.
        sessionConfig.setInterfaceVersion( SmppConstants.VERSION_3_4 );
        // Set the bind type.
        sessionConfig.setType( SmppBindType.TRANSMITTER );
        // Set the "SMSC" IP address.
        sessionConfig.setHost( properties.getSmpp().getHost() );
        // Set the "SMSC" port number.
        sessionConfig.setPort( properties.getSmpp().getPort() );
        // Identify the "ESME" to the "SMSC" at bind time.
        sessionConfig.setSystemId( properties.getSmpp().getUserId() );
        // "Password" parameter is used by the "SMSC" to authenticate the identity of the binding "ESME".
        sessionConfig.setPassword( properties.getSmpp().getPassword() );
        // Categorize the type of "ESME" that is binding to the "SMSC" (optional).
        sessionConfig.setSystemType( null );
        sessionConfig.getLoggingOptions().setLogBytes( false );
        sessionConfig.getLoggingOptions().setLogPdu( true );

        // Return session configuration object.
        return sessionConfig;
    }

    /**
     * Establish an "SMPP" session.
     *
     * @param properties All the external properties we need.
     * @return The common interface for either a Client (ESME) or Server (SMSC) "SMPP" session.
     * @throws SmppBindException Thrown only in the case where the "bind" request was successfully sent to the remote
     * system, and we actually got back a "bind" response that rejected the bind attempt.
     * @throws SmppTimeoutException Thrown if either the underlying "TCP/IP" connection cannot connect within the
     * "connectTimeout" or we can connect but don't receive a response back to the bind request within the "bindTimeout".
     * @throws SmppChannelException Thrown if there is an error with the underlying "TCP/IP" connection such as a bad
     * host name or the remote server's port is not accepting connections.
     * @throws UnrecoverablePduException Thrown in the case where we were able to connect and send our "bind" request,
     * but we got back data that was not failed parsing into a "PDU".
     * @throws InterruptedException Thrown if the calling thread is interrupted while we are attempting the bind.
     */
    // Set the "destroyMethod" attribute of the "@Bean" annotation. Note that if we have a public method named "close()"
    // or "shutdown()" in our bean, then it is automatically triggered with a destruction callback by default. However,
    // if we do not wish this behavior, we can disable it by setting destroyMethod="".
    @Bean( destroyMethod = "" )
    public SmppSession session( SmppClientProperties properties ) throws SmppBindException, SmppTimeoutException,
            SmppChannelException, UnrecoverablePduException, InterruptedException {
        // To bind a session, we need a "SmppSessionConfiguration" and "SmppClient". The "SmppSessionConfiguration"
        // class contains the configurable aspects of the "SmppSession". The following method uses the externalized
        // configuration.
        SmppSessionConfiguration config = sessionConfiguration( properties );
        // Create the "SmppClient" and bind the client to a remote "SMPP" endpoint by opening the socket, sending
        // a bind request, and waiting for a bind response. Provide the client session configuration and the session
        // handler as input parameters. If the bind is successful get the new session.
        // Register handler to take advantage of the "delivery receipt" handling.
        // "delivery receipts" printed in the console.
        SmppSession session = clientBootstrap( properties ).bind( config, new ClientSmppSessionHandler( properties ) );

        // Return the common interface for either a Client (ESME) or Server (SMSC) "SMPP" session.
        return session;
    }

    /**
     * Shutdown callback using "@PreDestroy" annotation.
     * Method will be executed before destroying - catch the "SIGTERM" signal and invoke the "PreDestroy" method.
     * Then, "unbind" the established session, close the underlying socket/channel, and finally
     * clean up all resources, while the application shuts down "graceful".
     *
     * @throws Exception In case of error.
     */
    @PreDestroy
    public void tearDown() throws Exception {
        // Check if the session is currently in the "BOUND" state, i.e. the session is bound and ready to process requests.
        if ( session.isBound() ) {
            // Attempt to "unbind" the session, waiting up to the specified period of
            // milliseconds for an "unbind" response from the remote endpoint. Regardless of whether
            // a proper unbind response was received, the socket/channel is closed.
            session.unbind( 10000 );
        }
        // Immediately close the session by closing the underlying socket/channel.
        // This method will not attempt to "unbind" first, rather just immediately
        // close the channel. Once closed, this session is not usable. It is
        // always recommended a proper "unbind" is attempted first, rather than just
        // closing the socket.
        session.close();
        // Destroy a session by ensuring the socket is closed and all
        // resources are cleaned up. This method should the last method called
        // before discarding or losing a reference to a session. Since this method
        // cleans up all resources, make sure that any data you need to access is
        // accessed before calling this method. After calling this method
        // it is not guaranteed that any other method will correctly work.
        session.destroy();
    }

    /**
     * Create the "SmppClient".
     * The "DefaultSmppClient" constructor takes an "ExecutorService" and expected number of sessions.
     * In this case, we are creating a "CachedThreadPoolExecutor" and assigning the desired number of concurrent sessions.
     *
     * @param properties All the external properties we need.
     * @return The "SmppClient".
     */
    public com.cloudhopper.smpp.SmppClient clientBootstrap( SmppClientProperties properties ) {
        // Create a new default "SmppClient" by providing the "executor" that IO workers will be executed with - a thread
        // pool that creates new threads as needed, but will reuse previously constructed threads when they are available,
        // and the max number of concurrent sessions expected to be active at any time - the max number of worker threads
        // that the underlying "Netty" library will use.
        return new DefaultSmppClient( Executors.newCachedThreadPool(), properties.getAsync().getSmppSessionSize() );
    }

    /**
     * Send a "Text" or "Flash" MT SMS on application startup.
     *
     * @param session The established "SMPP" session.
     * @param sourceAddress The source address (short code) of the "MT SMS".
     * @param destinationAddress The destination address (MSISDN) of the "MT SMS".
     * @param text The text message of the "MT SMS".
     * @param concatenated "true" in case of a concatenated "MT SMS", else "false".
     * @param delivery_receipt "true" in case where we request a "delivery receipt" (DLR), else "false".
     * @param isFlash "true" in case of a "Flash MT SMS", else "false".
     */
    private void sendTextMessage( SmppSession session, String sourceAddress, String destinationAddress, byte[] text,
                                  boolean concatenated, boolean delivery_receipt, boolean isFlash ) {
        // Check if the session is currently in the "BOUND" state, i.e. the session is bound and ready to process requests.
        if ( session.isBound() ) {
            try {
                // Construct a "submit_sm" SMPP PDU.
                SubmitSm submit = new SubmitSm();

                // DCS : SMPP ENCODING DEFAULT = 0x00 - SMSC Default.
                //       SMPP ENCODING IA5 = 0x01 - IA5 ( CCITT T.50 ) / ASCII ( ANSI X3.4 ).
                //       SMPP ENCODING BINARY = 0x02 - Octet unspecified ( 8-bit binary ).
                //       SMPP ENCODING ISO88591 = 0x03 - Latin 1 ( ISO-8859-1 ).
                //       SMPP ENCODING BINARY2 = 0x04 - Octet unspecified ( 8-bit binary ).
                //       SMPP ENCODING JIS = 0x05 - JIS ( X 0208-1990 ).
                //       SMPP ENCODING ISO88595 = 0x06 - Cyrillic ( ISO-8859-5 ).
                //       SMPP ENCODING ISO88598 = 0x07 - Latin/Hebrew ( ISO-8859-8 ).
                //       SMPP ENCODING ISO10646 = 0x08 - UCS2 ( ISO/IEC-10646 ).
                //       SMPP ENCODING PICTOGRAM = 0x09 - Pictogram Encoding.
                //       SMPP ENCODING ISO2022JP = 0x0A - ISO-2022-JP ( Music Codes ).
                //       SMPP ENCODING EXTJIS = 0x0D - Extended Kanji JIS ( X 0212-1990 ).
                //       SMPP ENCODING KSC5601 = 0x0E - KS C 5601
                // Set "data coding scheme" (DCS) for sending "Flash" SMS - SMS DATA CODING = 240 (0XF0).
                if ( isFlash ) {
                    submit.setDataCoding( (byte) 0XF0 );
                }
                // Set "data coding scheme" (DCS) for sending "Text" SMS - SMS DATA CODING = 0 (0X00).
                else{
                    submit.setDataCoding( SmppConstants.DATA_CODING_DEFAULT );
                }

                // ESM : SMPP MSGMODE DEFAULT = 0x00 - Default SMSC mode ( e.g. Store and Forward ).
                //       SMPP MSGMODE DATAGRAM = 0x01 - Datagram mode.
                //       SMPP MSGMODE FORWARD = 0x02 - Forward ( i.e. Transaction ) mode.
                //       SMPP MSGMODE STOREFORWARD = 0x03 - Explicit Store and Forward mode.
                //       SMPP MSGTYPE DEFAULT = 0x00 - Default message type ( i.e. normal message ).
                //       SMPP MSGTYPE DELIVERYACK = 0x08 - Message contains ESME Delivery acknowledgement.
                //       SMPP MSGTYPE USERACK = 0x10 - Message contains ESME Manual / User acknowledgement.
                //       SMPP GSMFEAT NONE = 0x00 - No specific features selected.
                //       SMPP GSMFEAT UDHI = 0x40 - UDHI Indicator ( only relevant for MT msgs ).
                //       SMPP GSMFEAT REPLYPATH = 0x80 - Set Reply Path ( only relevant for GSM net ).
                //       SMPP GSMFEAT UDHIREPLYPATH = 0xC0 - Set UDHI and Reply Path ( for GSM net ).
                // In case of a "concatenated" SMS set ESM CLASS = 64 (0x40), i.e. set "UDHI" Indicator.
                if ( concatenated ) {
                    submit.setEsmClass( (byte) 0x40 );
                }

                // In case we also request a "delivery receipt" (DLR).
                if ( delivery_receipt ) {
                    submit.setRegisteredDelivery( SmppConstants.REGISTERED_DELIVERY_SMSC_RECEIPT_REQUESTED );
                }

                // According to paragraph "5.2.22 short_message" of the protocol specification document, the "short_message" parameter
                // contains the user data. A maximum of 254 octets can be sent. ESME’s should use the optional "message_payload"
                // parameter in "submit_sm" to send larger user data sizes. That is, the "message_payload" parameter contains
                // the user data. Its function is to provide an alternative means of carrying text lengths above the 255
                // octet limit of the "short_message" field. Applications, which need to send messages longer than 255 octets,
                // should use the "message_payload" TLV populated with the message length value and user data. When used
                // in the context of a "submit_sm" PDU, the "sm_length" field should be set to zero (NULL). On the other hand,
                // if we try to push a longer message, which is more than 255 characters length, we will get the below
                // error message from "cloudhopper" :
                //       com.cloudhopper.smpp.type.SmppInvalidArgumentException: A short message in a PDU can only be a max of
                //       255 bytes [actual=" + value.length + "]; use optional parameter message_payload as an alternative
                if ( text != null && text.length > 255 ) {
                    // Add an optional parameter to this "PDU". Does not check if the "TLV" has already been added (allows duplicates).
                    submit.addOptionalParameter( new Tlv( SmppConstants.TAG_MESSAGE_PAYLOAD, text, "message_payload" ) );
                } else {
                    // Else set the "short_message" SMPP parameter with the user data.
                    submit.setShortMessage( text );
                }

                // TON : SMPP TON UNK = 0x00    | SMPP TON INTL = 0x01  | SMPP TON NATNL = 0x02
                //       SMPP TON NWSPEC = 0x03 | SMPP TON SBSCR = 0x04 | SMPP TON ALNUM = 0x05
                //       SMPP TON ABBREV = 0x06
                // NPI : SMPP NPI UNK = 0x00 - Unknown                  | SMPP NPI ISDN = 0x01 - ISDN ( E163/E164 )
                //       SMPP NPI DATA = 0x03 - Data ( X.121 )          | SMPP NPI TELEX = 0x04 - Telex ( F.69 )
                //       SMPP NPI LNDMBL = 0x06 - Land Mobile ( E.212 ) | SMPP NPI NATNL = 0x08 - National
                //       SMPP NPI PRVT = 0x09 - Private                 | SMPP NPI ERMES = 0x0A - ERMES
                //       SMPP NPI IP = 0x0E - IPv4                      | SMPP NPI WAP = 0x12 - WAP
                // Set source address ton:npi = 0:1
                submit.setSourceAddress( new Address( SmppConstants.TON_UNKNOWN, SmppConstants.NPI_E164, sourceAddress ) );
                // Set destination address ton:npi = 1:1
                submit.setDestAddress( new Address( SmppConstants.TON_INTERNATIONAL, SmppConstants.NPI_E164, destinationAddress ) );

                // Set a relative "validity period" of 8 hours. According to paragraph "5.2.16 validity_period" of the
                // protocol specification document,	the "validity_period" SMPP parameter indicates the "SMSC" expiration
                // time, after which the message should be discarded if not delivered to the destination. It can be
                // defined in absolute time format or relative time format (Section 7.1.1.). A "Relative Time Format" example :
                // "020610233429000R" would be interpreted as a relative period of 2 years, 6 months, 10 days, 23 hours, 34
                // minutes and 29 seconds from the current "SMSC" time.
                submit.setValidityPeriod( "000000080000000R" );

                // Submit message to "SMSC" for delivery with a timeout of 10000ms. Synchronously send a "submit" request
                // to the remote endpoint and wait for up to a specified number of milliseconds for a response. The
                // timeout value includes both waiting for a "window" slot, the time it takes to transmit the actual bytes
                // on the socket, and for the remote endpoint to send a response back. Get a valid response to the request.
                SubmitSmResp submitResponse = session.submit( submit, 10000 );
                // Check if the status of the submitted "SMPP" command is success, else throw exception.
                if ( submitResponse.getCommandStatus() == SmppConstants.STATUS_OK ) {
                    log.info( "SMS submitted, message id {}", submitResponse.getMessageId() );
                } else {
                    throw new IllegalStateException( submitResponse.getResultMessage() );
                }
            }
            // In case of error throw exception, i.e.
            // * when a recoverable "PDU" error occurs. A recoverable PDU error includes the partially decoded PDU in
            // order to generate a negative acknowledgement (NACK) response.
            // * when an unrecoverable "PDU" error occurs. This indicates a serious error occurred and usually indicates
            // the session should be immediately terminated.
            // * when a timeout occurs while waiting for a response from the remote endpoint.  A timeout can either occur
            // with an unresponsive remote endpoint or the bytes were not written in time.
            // * when the underlying socket/channel was unable to write the request.
            // * when the calling thread was interrupted while waiting to acquire a lock or write/read the bytes from the
            // socket/channel.
            catch ( RecoverablePduException | UnrecoverablePduException | SmppTimeoutException | SmppChannelException
                    | InterruptedException e ) {
                throw new IllegalStateException( e );
            }
            return;
        }
        // Else throw exception in case where the session is not currently in the "BOUND" state.
        throw new IllegalStateException( "SMPP session is not connected" );
    }

    /**
     * Send a "WAP Push" message on application startup.
     *
     * @param session The established "SMPP" session.
     * @param sourceAddress The source address (short code) of the "WAP Push" message.
     * @param destinationAddress The destination address (MSISDN) of the "WAP Push" message.
     * @param message The message of the "WAP Push".
     * @param delivery_receipt "true" in case where we request a "delivery receipt" (DLR), else "false".
     */
    private void sendWapPushMessage( SmppSession session, String sourceAddress, String destinationAddress, String message, boolean delivery_receipt ) {
        // Check if the session is currently in the "BOUND" state, i.e. the session is bound and ready to process requests.
        if ( session.isBound() ) {
            try {
                // Construct a "submit_sm" SMPP PDU.
                SubmitSm submit = new SubmitSm();

                // Create a byte array from the input message "CharSequence" containing only valid hexadecimal formatted characters.
                byte[] msgBytes = HexUtil.toByteArray( message );

                // DCS : SMPP ENCODING DEFAULT = 0x00 - SMSC Default.
                //       SMPP ENCODING IA5 = 0x01 - IA5 ( CCITT T.50 ) / ASCII ( ANSI X3.4 ).
                //       SMPP ENCODING BINARY = 0x02 - Octet unspecified ( 8-bit binary ).
                //       SMPP ENCODING ISO88591 = 0x03 - Latin 1 ( ISO-8859-1 ).
                //       SMPP ENCODING BINARY2 = 0x04 - Octet unspecified ( 8-bit binary ).
                //       SMPP ENCODING JIS = 0x05 - JIS ( X 0208-1990 ).
                //       SMPP ENCODING ISO88595 = 0x06 - Cyrillic ( ISO-8859-5 ).
                //       SMPP ENCODING ISO88598 = 0x07 - Latin/Hebrew ( ISO-8859-8 ).
                //       SMPP ENCODING ISO10646 = 0x08 - UCS2 ( ISO/IEC-10646 ).
                //       SMPP ENCODING PICTOGRAM = 0x09 - Pictogram Encoding.
                //       SMPP ENCODING ISO2022JP = 0x0A - ISO-2022-JP ( Music Codes ).
                //       SMPP ENCODING EXTJIS = 0x0D - Extended Kanji JIS ( X 0212-1990 ).
                //       SMPP ENCODING KSC5601 = 0x0E - KS C 5601
                // Set "data coding scheme" (DCS) for sending "WAP Push" message - SMS DATA CODING = 245 (0XF5).
                submit.setDataCoding( (byte) 0XF5 );

                // In case we also request a "delivery receipt" (DLR).
                if ( delivery_receipt ) {
                    submit.setRegisteredDelivery( SmppConstants.REGISTERED_DELIVERY_SMSC_RECEIPT_REQUESTED );
                }

                // According to paragraph "5.2.22 short_message" of the protocol specification document, the "short_message" parameter
                // contains the user data. A maximum of 254 octets can be sent. ESME’s should use the optional "message_payload"
                // parameter in "submit_sm" to send larger user data sizes. That is, the "message_payload" parameter contains
                // the user data. Its function is to provide an alternative means of carrying text lengths above the 255
                // octet limit of the "short_message" field. Applications, which need to send messages longer than 255 octets,
                // should use the "message_payload" TLV populated with the message length value and user data. When used
                // in the context of a "submit_sm" PDU, the "sm_length" field should be set to zero (NULL). On the other hand,
                // if we try to push a longer message, which is more than 255 characters length, we will get the below
                // error message from "cloudhopper" :
                //       com.cloudhopper.smpp.type.SmppInvalidArgumentException: A short message in a PDU can only be a max of
                //       255 bytes [actual=" + value.length + "]; use optional parameter message_payload as an alternative
                if ( msgBytes != null && msgBytes.length > 255 ) {
                    // Add an optional parameter to this "PDU". Does not check if the "TLV" has already been added (allows duplicates).
                    submit.addOptionalParameter(
                            new Tlv( SmppConstants.TAG_MESSAGE_PAYLOAD, msgBytes, "message_payload" ) );
                } else {
                    // Else set the "short_message" SMPP parameter with the user data.
                    submit.setShortMessage( msgBytes );
                }

                // TON : SMPP TON UNK = 0x00    | SMPP_TON INTL = 0x01  | SMPP TON NATNL = 0x02
                //       SMPP TON NWSPEC = 0x03 | SMPP_TON SBSCR = 0x04 | SMPP TON ALNUM = 0x05
                //       SMPP TON ABBREV = 0x06
                // NPI : SMPP NPI UNK = 0x00 - Unknown                  | SMPP NPI ISDN = 0x01 - ISDN ( E163/E164 )
                //       SMPP NPI DATA = 0x03 - Data ( X.121 )          | SMPP NPI TELEX = 0x04 - Telex ( F.69 )
                //       SMPP NPI LNDMBL = 0x06 - Land Mobile ( E.212 ) | SMPP NPI NATNL = 0x08 - National
                //       SMPP NPI PRVT = 0x09 - Private                 | SMPP NPI ERMES = 0x0A - ERMES
                //       SMPP NPI IP = 0x0E - IPv4                      | SMPP NPI WAP = 0x12 - WAP
                // Set source address ton:npi = 0:1
                submit.setSourceAddress( new Address( SmppConstants.TON_UNKNOWN, SmppConstants.NPI_E164, sourceAddress ) );
                // Set destination address ton:npi = 1:1
                submit.setDestAddress( new Address( SmppConstants.TON_INTERNATIONAL, SmppConstants.NPI_E164, destinationAddress ) );

                // ESM : SMPP MSGMODE DEFAULT = 0x00 - Default SMSC mode ( e.g. Store and Forward ).
                //       SMPP MSGMODE DATAGRAM = 0x01 - Datagram mode.
                //       SMPP MSGMODE FORWARD = 0x02 - Forward ( i.e. Transaction ) mode.
                //       SMPP MSGMODE STOREFORWARD = 0x03 - Explicit Store and Forward mode.
                //       SMPP MSGTYPE DEFAULT = 0x00 - Default message type ( i.e. normal message ).
                //       SMPP MSGTYPE DELIVERYACK = 0x08 - Message contains ESME Delivery acknowledgement.
                //       SMPP MSGTYPE USERACK = 0x10 - Message contains ESME Manual / User acknowledgement.
                //       SMPP GSMFEAT NONE = 0x00 - No specific features selected.
                //       SMPP GSMFEAT UDHI = 0x40 - UDHI Indicator ( only relevant for MT msgs ).
                //       SMPP GSMFEAT REPLYPATH = 0x80 - Set Reply Path ( only relevant for GSM net ).
                //       SMPP GSMFEAT UDHIREPLYPATH = 0xC0 - Set UDHI and Reply Path ( for GSM net ).
                // Set "ESM Class" for sending "WAP Push" message - ESM CLASS = 64 (0x40), i.e. set "UDHI" Indicator.
                submit.setEsmClass( (byte) 0x40 );

                // Set a relative "validity period" of 8 hours. According to paragraph "5.2.16 validity_period" of the
                // protocol specification document,	the "validity_period" SMPP parameter indicates the "SMSC" expiration
                // time, after which the message should be discarded if not delivered to the destination. It can be
                // defined in absolute time format or relative time format (Section 7.1.1.). A "Relative Time Format" example :
                // "020610233429000R" would be interpreted as a relative period of 2 years, 6 months, 10 days, 23 hours, 34
                // minutes and 29 seconds from the current "SMSC" time.
                submit.setValidityPeriod( "000000080000000R" );

                // According to paragraph "5.2.11 service_type" of the protocol specification document,	the "service_type"
                // SMPP parameter can be used to indicate the "SMS" Application service associated with the message.
                // Set the generic "service_type" - "WAP" Wireless Application Protocol.
                submit.setServiceType( "WAP" );

                // Submit message to "SMSC" for delivery with a timeout of 10000ms. Synchronously send a "submit" request
                // to the remote endpoint and wait for up to a specified number of milliseconds for a response. The
                // timeout value includes both waiting for a "window" slot, the time it takes to transmit the actual bytes
                // on the socket, and for the remote endpoint to send a response back. Get a valid response to the request.
                SubmitSmResp submitResponse = session.submit( submit, 10000 );
                // Check if the status of the submitted "SMPP" command is success, else throw exception.
                if ( submitResponse.getCommandStatus() == SmppConstants.STATUS_OK ) {
                    log.info( "SMS submitted, message id {}", submitResponse.getMessageId() );
                } else {
                    throw new IllegalStateException( submitResponse.getResultMessage() );
                }
            }
            // In case of error throw exception, i.e.
            // * when a recoverable "PDU" error occurs. A recoverable PDU error includes the partially decoded PDU in
            // order to generate a negative acknowledgement (NACK) response.
            // * when an unrecoverable "PDU" error occurs. This indicates a serious error occurred and usually indicates
            // the session should be immediately terminated.
            // * when a timeout occurs while waiting for a response from the remote endpoint.  A timeout can either occur
            // with an unresponsive remote endpoint or the bytes were not written in time.
            // * when the underlying socket/channel was unable to write the request.
            // * when the calling thread was interrupted while waiting to acquire a lock or write/read the bytes from the
            // socket/channel.
            catch ( RecoverablePduException | UnrecoverablePduException | SmppTimeoutException | SmppChannelException
                    | InterruptedException e ) {
                throw new IllegalStateException( e );
            }
            return;
        }
        // Else throw exception in case where the session is not currently in the "BOUND" state.
        throw new IllegalStateException( "SMPP session is not connected" );
    }

    /**
     * "SMPP" Sessions are short-lived; given a scenario where a mobile device is switched off, the "SMS" will be delivered only
     * when the device is switched back on. In this same scenario, the "SMPP" Session may be closed when the delivery is
     * received, leading to lost "delivery receipts". To overcome this limitation, the application periodically refreshes
     * the session before it un-binds. The following method periodically extends the "SMPP" session.
     *
     * The "fixedDelayString" value ${sms.async.initial-delay} is set in the "application.yaml" file. This will cause the
     * application to enquire link every x seconds.
     *
     * Note : when internet connectivity is lost on the server running the application, this will not work.
     * Also, when the "SMPP" Session unexpectedly unbinds this solution will not work.
     */
    // Mark the method to be scheduled with a specific number of units of time to delay before the first execution and
    // a fixed period between the end of the last invocation and the start of the next.
    @Scheduled( initialDelayString = "${sms.async.initial-delay}", fixedDelayString = "${sms.async.initial-delay}" )
    void enquireLinkJob() {
        // Check if the session is currently in the "BOUND" state, i.e. the session is bound and ready to process requests.
        if ( session.isBound() ) {
            try {
                log.info( "sending enquire_link" );
                // Synchronously send an "enquire_link" request to the remote endpoint and
                // wait for up to a specified number of milliseconds for a response. The
                // timeout value includes both waiting for a "window" slot, the time it
                // takes to transmit the actual bytes on the socket, and for the remote
                // endpoint to send a response back. Get a valid response to the request.
                EnquireLinkResp enquireLinkResp = session.enquireLink( new EnquireLink(),
                        properties.getAsync().getTimeout() );
                log.info( "enquire_link_resp: {}", enquireLinkResp );
            }
            // Throw exception in case where a timeout occurred while waiting for a response from the remote endpoint.
            // A timeout can either occur with an unresponsive remote endpoint or the bytes were not written in time.
            catch ( SmppTimeoutException e ) {
                log.info( "Enquire link failed, executing reconnect; " + e );
                log.error( "", e );
            }
            // Throw exception in case where the underlying socket/channel was unable to write the request.
            catch ( SmppChannelException e ) {
                log.info( "Enquire link failed, executing reconnect; " + e );
                log.warn( "", e );
            }
            // Throw exception in case where the calling thread was interrupted while waiting to acquire a lock or
            // write/read the bytes from the socket/channel.
            catch ( InterruptedException e ) {
                log.info( "Enquire link interrupted, probably killed by reconnecting" );
            }
            // Throw exception in any other case.
            catch ( Exception e ) {
                log.error( "Enquire link failed, executing reconnect", e );
            }
        } else {
            log.error( "enquire link running while session is not connected" );
        }
    }

    /**
     * Create a WAP Push "Service Indication" message to present the user with a "URL" and accompanying text.
     *
     * WAP Push message - "SI" (Service Indication)
     * --------------------------------------------
     *
     * A "Service Indication" is a binary "SMS" sent to a dedicated port on a device, which informs the device that there
     * is a "URL" waiting to be visited. The "URL" can be the address of a ringtone in a Web or "WAP" server, a "JPG" or simply
     * a "WAP" page.
     *
     * Note : there are special "Service Indication" messages, which are normally called "SL" ("Service Load" - have a look
     * at the following method), which are similar to "SI". An "SI" asks the permission of the user before fetching the
     * content over the network connection, while an "SL" downloads contents automatically without asking the permission
     * of the user. "SL" are very similar to "MMS" messages : the content is on a Web server and the "SMS" tells the phone
     * to download the message.
     *
     * "UDH" to send a "WAP Push" message
     *
     * The standard destination port for "WAP" pushes is "2948".
     * The "UDH" in hex format will be : "\x06 \x05 \x04 \x0B \x84 \x23 \xF0", where :
     * * "\x06" means "read the following 6 bytes".
     * * "\x05" is the format of numbers, in this case hexadecimal numbers.
     * * "\x04" will tell the "UDH" that each port is represented using 4 characters.
     * * "\x0B \x84" is the destination port, "2948" (decimal representation) or "0B84" (hexadecimal representation).
     * * "\x23 \xF0" is the source port, "9200" (decimal representation) or "23F0" (hexadecimal representation).
     *
     * Binary "SMS"
     *
     * Is an XML-formatted textual "SMS", which has been transformed with "WBXML".
     * "WBXML" is a "tag transformer". This means that for each "XML" tag, a binary byte is associated.
     * The result of a "WBXML" transformation is smaller in the number of generated bytes than the verbose textual
     * "XML" file itself. For example, the tag "<SI>" is converted as the binary character "&#x0005;".
     * Many tags are converted to bytes, but sometimes also contents (such as "URL" addresses).
     * For example, the "URL" : "http://www.google.com" can be written in "WBXML" as "0Dgoogle.com" , where "0D" stands for "http://www".
     * "0C" is more generic and stands for "http://" , so you can write the "URL" also as : "0Cwww.google.com"
     * Have a look at http://www.openmobilealliance.org/release/Push/V2_1-20051122-C/WAP-167-ServiceInd-20010731-a.pdf [ Service Indication ]
     * For example, read paragraph "8.3.2. Attribute Start Tokens" and chapter 9.
     * So, assuming that we like to send the "XML" at the right side of the following table, the compact binary representation
     * of the "Service Indication" message can be found at the left side below :
     *-----------------------------------------------------------
     * WBXML Token Description         | XML Rendering
     *-----------------------------------------------------------
     *   Known Tag 0x05           (.C) |  <si>
     *   Known Tag 0x06           (AC) |    <indication
     *   Known attrStart 0x0D          |      href='http://www.'
     * STR_I (Inline string)           |        'google'
     *   Known attrValue 0x05          |          '.com/'
     * STR_I (Inline string)           |        ''
     *   Known attrStart 0x08          |      action='signal-high'
     * END (attribute list)            |    >
     * STR_I (Inline string)           |    'Click Me'
     * END (Known Tag 0x06)            |    </indication>
     * END (Known Tag 0x05)            |  </si>
     *-----------------------------------------------------------
     * Finally, the "WAP Push SI" message is converted to the following "WBXML" hex format.
     * These are strings used to pass contents to the "SI" body, each character in the string is converted to its
     * hexadecimal representation.
     *-------------------------------------------------------------------------------
     * Hex code		 			                | Meaning
     *-------------------------------------------------------------------------------
     * \x00                                      | Transaction ID
     * \x06                                      | Push
     * \x01                                      | Headers Length
     * \xAE                                      | Content-Type
     * \x02                                      | WBXML Version 1.2
     * \x05                                      | SI 1.0 Public Identifier
     * \x6A                                      | Charset UTF-8
     * \x00                                      | String table length = 0
     * \x45                                      | <SI>
     * \xC6                                      | <indication>
     * \x0D                                      | href="http://www.
     * \x03                                      | String starts
     * \x67 \x6f \x6f \x67 \x6c \x65             | google
     * \x00                                      | String ends
     * \x85                                      | .com/
     * \x03                                      | String starts
     * \x00                                      | String ends
     * \x08                                      | Action attribute (signal-high)
     * \x01                                      | Ends of attributes, now the content
     * \x03                                      | String starts
     * \x43 \x6c \x69 \x63 \x6b \x20 \x4d \x65   | Click Me
     * \x00                                      | String ends
     * \x01                                      | </indication>
     * \x01                                      | </SI>
     *-------------------------------------------------------------------------------
     *
     * @param destinationAddress The destination address (MSISDN) of the "WAP Push" message.
     * @param href The "WAP Push SI" href "URL".
     * @param indicationText The text for "WAP Push SI" element.
     * @return The set of messages representing this "WAP Push WDP" datagram.
     * @throws MalformedURLException Indicate that a malformed "URL" has occurred. Either no legal protocol could be found
     * in a specification string or the string could not be parsed.
     * @throws WapPushException A generic exception for "WAP Push".
     */
    public static Message[] generateWapPushSI( String destinationAddress, String href, String indicationText ) throws MalformedURLException, WapPushException {
        // Create a "WAP Push" message of content type "Service Indication".
        // It seems that the "destinationAddress" is a mandatory parameter - in theory is the destination address
        // for the generated messages. However, in practice, the submission of the generated "SMS" messages will be
        // handled by a different method of this application.
        ServiceIndication serviceIndication = new ServiceIndication( destinationAddress );
        // Set "SI" action with action type "SIGNAL_HIGH".
        serviceIndication.setAction( ServiceIndication.ACTION_SIGNAL_HIGH );
        // Set "SI" href "URL".
        serviceIndication.setHref( href );
        // Set "SI" id - generate a random integer value within a "static" pre-defined range.
        serviceIndication.setId( String.valueOf( (int)( Math.random() * ( ( 9999 - 1000 ) + 1 ) ) + 1000 ) );
        // Set text for "SI indication" element.
        serviceIndication.setIndicationText( indicationText );

        // Generate a set of messages representing this "WAP Push WDP" datagram.
        Message[] msgs = serviceIndication.generateMessages();

        // Loop through the array of messages - just for DEBUGing purposes.
        for ( Message msg : msgs ) {
            // Get the byte array of user data headers and print it as hexadecimal string.
            log.info( "Wap Push SI UDH  : " + Utils.encodeHexString( msg.getUDH() ) );
            // Get the byte array of message payload and print it as hexadecimal string.
            log.info( "Wap Push SI Body : " + Utils.encodeHexString( msg.getBinaryMessageBody() ) );
        }

        // Return the set of messages representing this "WAP Push WDP" datagram.
        return msgs;
    }

    /**
     * Create a WAP Push "Service Loading" message.
     * Have a look also at the "generateWapPushSI" method description comments.
     *
     * @param destinationAddress The destination address (MSISDN) of the "WAP Push" message.
     * @param href The "WAP Push SL" href "URL".
     * @return The set of messages representing this "WAP Push WDP" datagram.
     * @throws MalformedURLException Indicate that a malformed "URL" has occurred. Either no legal protocol could be found
     * in a specification string or the string could not be parsed.
     * @throws WapPushException A generic exception for "WAP Push".
     */
    public static Message[] generateWapPushSL( String destinationAddress, String href ) throws MalformedURLException, WapPushException {
        // Create a "WAP Push" message of content type "Service Loading".
        // It seems that the "destinationAddress" is a mandatory parameter - in theory is the destination address
        // for the generated messages. However, in practice, the submission of the generated "SMS" messages will be
        // handled by a different method of this application.
        ServiceLoading serviceLoading = new ServiceLoading( destinationAddress, href );
        // Set "SL" action with action type "EXECUTE_HIGH".
        serviceLoading.setAction( ServiceLoading.ACTION_EXECUTE_HIGH );

        // Generate a set of messages representing this "WAP Push WDP" datagram.
        Message[] msgs = serviceLoading.generateMessages();

        // Loop through the array of messages - just for DEBUGing purposes.
        for ( Message msg : msgs ) {
            // Get the byte array of user data headers and print it as hexadecimal string.
            log.info( "Wap Push SL UDH  : " + Utils.encodeHexString( msg.getUDH() ) );
            // Get the byte array of message payload and print it as hexadecimal string.
            log.info( "Wap Push SL Body : " + Utils.encodeHexString( msg.getBinaryMessageBody() ) );
        }

        // Return the set of messages representing this "WAP Push WDP" datagram.
        return msgs;
    }

    /**
     * Create an "MMS" notification "WAP push" message with "SMS" as bearer.
     *
     * @param sourceAddress The source address (short code) of the notification message.
     * @param theContentLocation The "URL" of the "MMS" content location.
     * @param subject The subject of the notification message.
     * @param size The size of the "MMS" content - approximate calculation in bytes.
     * @return A "HashMap" consisting of the "User Data Header" and the "User Data" of the "MMS" notification "WAP push" message.
     */
    public static Map<String,Object> generateWapPush_M_NOTIFICATION_IND( String sourceAddress, String theContentLocation, String subject, long size ) {
        // WAP Push UDH
        // 06   : read the following 6 bytes.
        // 05   : the format for numbers, in this case hexadecimal numbers.
        // 04   : tell the "UDH" that each port is represented using 4 character.
        // 0B84 : the destination port, "2948" (decimal representation) or "0B84" (hexadecimal representation).
        // 23F0 : the source port, "9200" (decimal representation) or "23F0" (hexadecimal representation).
        final String UDH = "0605040b8423f0";

        // Create an "MMS" notification "WAP push" message with "SMS" as bearer (X-Mms-Message-Type: m-notification-ind).
        SmsMmsNotificationMessage smsMmsNotificationMessage = new SmsMmsNotificationMessage( theContentLocation, size );
        // Set "MMS" expiration in 3 days (X-Mms-Expiry).
        smsMmsNotificationMessage.setExpiry( 3 * 24 * 60 * 60 );
        // Set message originator (From).
        smsMmsNotificationMessage.setFrom( sourceAddress );
        // Set message class to "Personal" (X-Mms-Message-Class).
        smsMmsNotificationMessage.setMessageClass( MmsConstants.X_MMS_MESSAGE_CLASS_ID_PERSONAL );
        // Set message subject.
        smsMmsNotificationMessage.setSubject( subject );
        // Set message transaction-Id - generate a random integer value between a specified range (X-Mms-Transaction-ID).
        smsMmsNotificationMessage.setTransactionId( String.valueOf( (int)( Math.random()*( ( 99999 - 10000 ) + 1 ) ) + 10000) );

        // Return the whole "User Data".
        SmsUserData smsUserData = smsMmsNotificationMessage.getUserData();

        // Just for DEBUGing purposes.
        // Get the user data header and print it as hexadecimal string.
        log.info( "Wap Push M-NOTIFICATION-IND UDH  : " + UDH );
        // Get the byte array of message payload and print it as hexadecimal string.
        log.info( "Wap Push M-NOTIFICATION-IND Body : " + Utils.encodeHexString( smsUserData.getData() ) );

        // Construct an empty "HashMap" for the message.
        Map<String,Object> msg = new HashMap<>();
        // Associate the "User Data Header" value with the "UDH" key in this map.
        msg.put( "UDH", UDH );
        // Associate the "User Data" with the "message body" key in this map.
        msg.put( "BinaryMessageBody", smsUserData );

        // Return the "HashMap" consisting of the "User Data Header" and the "User Data" of the "MMS" notification "WAP push" message.
        return msg;
    }

    /**
     * Create the "Options" for this application.
     * Parse and retrieve the "command line arguments".
     *
     * @param arguments The "command line arguments".
     * @return A "HashMap" object consisting of the "command line arguments".
     */
    private static Map<String,String> getCLI( String[] arguments ) {
        // Create "Options" object. "Options" represents a collection of "Option" objects, which describe the possible
        // options for a command-line.
        Options options = new Options();

        // Return an "Option.Builder" to create an "Option" using descriptive methods. Then set the long name of the "Option",
        // the description for this "Option", indicate that the "Option" will require an argument, and set the display name
        // for the argument value, mark this "Option" as required and construct the "Option" with the values declared
        // by this "Option.Builder". Finally, add the "Option" instance.
        options.addOption( Option.builder("s" )
                .longOpt( "source-addr" )
                .desc( "msg source address, e.g. 1284" )
                .hasArg()
                .argName( "scr-addr" )
                .required( true )
                .build() );
        // Return an "Option.Builder" to create an "Option" using descriptive methods. Then set the long name of the "Option",
        // the description for this "Option", indicate that the "Option" will require an argument, and set the display name
        // for the argument value, mark this "Option" as required and construct the "Option" with the values declared
        // by this "Option.Builder". Finally, add the "Option" instance.
        options.addOption( Option.builder("d" )
                .longOpt( "destination-addr" )
                .desc( "msg destination address, e.g. 306944000000" )
                .hasArg()
                .argName( "dst-addr" )
                .required( true )
                .build() );
        // Return an "Option.Builder" to create an "Option" using descriptive methods. Then set the long name of the "Option",
        // the description for this "Option", indicate that the "Option" will require an argument, and set the display name
        // for the argument value, mark this "Option" as optional and construct the "Option" with the values declared
        // by this "Option.Builder". Finally, add the "Option" instance.
        options.addOption( Option.builder("m" )
                .longOpt( "message-text" )
                .desc( "msg text, e.g. \"hello world\"" )
                .hasArg()
                .argName( "text" )
                .required( false )
                .build() );
        // Return an "Option.Builder" to create an "Option" using descriptive methods. Then set the long name of the "Option",
        // the description for this "Option", indicate that the "Option" will require an argument, and set the display name
        // for the argument value, mark this "Option" as optional and construct the "Option" with the values declared
        // by this "Option.Builder". Finally, add the "Option" instance.
        options.addOption( Option.builder("h" )
                .longOpt( "wappush-href" )
                .desc( "wap push href, e.g. \"http://aristotelis-metsinis.github.io/\"" )
                .hasArg()
                .argName( "href" )
                .required( false )
                .build() );
        // Return an "Option.Builder" to create an "Option" using descriptive methods. Then set the long name of the "Option",
        // the description for this "Option", indicate that the "Option" will require an argument, and set the display name
        // for the argument value, mark this "Option" as optional and construct the "Option" with the values declared
        // by this "Option.Builder". Finally, add the "Option" instance.
        options.addOption( Option.builder("S" )
                .longOpt( "mm-size" )
                .desc( "multimedia msg size (approximate calculation in bytes), e.g. 29696" )
                .hasArg()
                .argName( "size" )
                .required( false )
                .build() );
        // Return an "Option.Builder" to create an "Option" using descriptive methods. Then set the long name of the "Option",
        // the description for this "Option", indicate that the "Option" will require an argument, and set the display name
        // for the argument value, mark this "Option" as required and construct the "Option" with the values declared
        // by this "Option.Builder". Finally, add the "Option" instance.
        options.addOption( Option.builder("t" )
                .longOpt( "message-type" )
                .desc( "message type, e.g. sms|flash|mms|wapSI|wapSL" )
                .hasArg()
                .argName( "type" )
                .required( true )
                .build() );
        // Return an "Option.Builder" to create an "Option" using descriptive methods. Then set the long name of the "Option",
        // the description for this "Option", set the display name
        // for the argument value, mark this "Option" as optional and construct the "Option" with the values declared
        // by this "Option.Builder". Finally, add the "Option" instance.
        options.addOption( Option.builder("D" )
                .longOpt( "delivery-receipt" )
                .desc( "request for delivery-receipt if the option has been specified, otherwise not if missing (default)" )
                .argName( "dlr" )
                .required( false )
                .build() );
        // Return an "Option.Builder" to create an "Option" using descriptive methods without a short name. Then set the long name of the "Option",
        // the description for this "Option", indicate that the "Option" will require an argument, and set the display name
        // for the argument value, mark this "Option" as optional and construct the "Option" with the values declared
        // by this "Option.Builder". Finally, add the "Option" instance.
        options.addOption( Option.builder()
                .longOpt( "mm-subject" )
                .desc( "multimedia subject, e.g. 'hello world'" )
                .hasArg()
                .argName( "subject" )
                .required( false )
                .build() );

        try {
            // Create the command line parser, i.e. a new "DefaultParser" instance with partial matching enabled.
            CommandLineParser parser = new DefaultParser();
            // Parse the "command line arguments" - this will parse the "command line arguments", using the rules specified
            // by the "Options" and shall return an instance of "CommandLine".
            CommandLine line = parser.parse( options, arguments );

            // Retrieve the "message-type" option value.
            switch ( line.getOptionValue( "t" ).toLowerCase() ) {
                case SMS :
                    // If "message-type" is "sms" but "message-text" option is missing then throw exception.
                    if ( line.getOptionValue( "m" ) == null ) {
                        throw new ParseException( "Missing message text for message type : \"" + line.getOptionValue( "t" ) + "\"" );
                    }
                    // Break out of the "switch" block.
                    break;
                case FLASH :
                    // If "message-type" is "flash" but "message-text" option is missing then throw exception.
                    if ( line.getOptionValue( "m" ) == null ) {
                        throw new ParseException( "Missing message text for message type : \"" + line.getOptionValue( "t" ) + "\"" );
                    }
                    // Break out of the "switch" block.
                    break;
                case WAP_SI :
                    // If "message-type" is "wapSI" but "message-text" option is missing then throw exception.
                    if ( line.getOptionValue( "m" ) == null ) {
                        throw new ParseException( "Missing message text for message type : \"" + line.getOptionValue( "t" ) + "\"" );
                    }
                    // If "message-type" is "wapSI" but "wappush-href" option is missing then throw exception.
                    if ( line.getOptionValue( "h" ) == null ) {
                        throw new ParseException( "Missing wap push href for message type : \"" + line.getOptionValue( "t" ) + "\"" );
                    }
                    // Break out of the "switch" block.
                    break;
                case WAP_SL :
                    // If "message-type" is "wapSL" but "wappush-href" option is missing then throw exception.
                    if ( line.getOptionValue( "h" ) == null ) {
                        throw new ParseException( "Missing wap push href for message type : \"" + line.getOptionValue( "t" ) + "\"" );
                    }
                    // Break out of the "switch" block.
                    break;
                case WAP_M_NOTIFICATION_IND :
                    // If "message-type" is "mms" but "mm-subject" option is missing then throw exception.
                    if ( line.getOptionValue( "mm-subject" ) == null ) {
                        throw new ParseException( "Missing message subject for message type : \"" + line.getOptionValue( "t" ) + "\"" );
                    }
                    // If "message-type" is "mms" but "wappush-href" option is missing then throw exception.
                    if ( line.getOptionValue( "h" ) == null ) {
                        throw new ParseException( "Missing wap push href for message type : \"" + line.getOptionValue( "t" ) + "\"");
                    }
                    // If "message-type" is "mms" but "mm-size" option is missing then throw exception.
                    if ( line.getOptionValue( "S" ) == null ) {
                        throw new ParseException( "Missing multimedia message size for message type : \"" + line.getOptionValue( "t" ) + "\"" );
                    }
                    // Break out of the "switch" block.
                    break;
                default:
                    // In case of any other "message-type" option value throw exception.
                    throw new ParseException( "Invalid value \"" + line.getOptionValue( "t" ) + "\" for option \"t\". Expected values : sms|flash|mms|wapSI|wapSL" );
            }

            // Validate "wappush-href" option value if any.
            if ( line.hasOption( "h" ) ) {
                // Initially construct an object of the "URL" class passing "URL" in "String" format.
                // In case of an unknown protocol or in case of a not specified protocol throw a "MalformedURLException".
                // Then return a "URI" object of the current "URL". If the current "URL" is not properly formatted or,
                // syntactically incorrect according to "RFC 2396" throw a "URISyntaxException".
                new URL( line.getOptionValue( "h" ) ).toURI();
            }

            // Construct an empty "HashMap" for the "command line arguments".
            Map<String,String> args = new HashMap<>();
            // Associate the "source-addr" option value with the "source address" key in this map.
            args.put( "sourceAddress", line.getOptionValue( "s" ) );
            // Associate the "destination-addr" option value with the "destination address" key in this map.
            args.put( "destinationAddress",  line.getOptionValue( "d" ) );
            // Associate the "message-text" option value with the "message" key in this map.
            args.put( "message", line.getOptionValue( "m" ) );
            // Associate the "wappush-href" option value with the "WAP PUSH URL" key in this map.
            args.put( "wapPushHref", line.getOptionValue( "h" ) );
            // Associate the "mm-size" option value with the "MMS size" key in this map.
            args.put( "mmSize", line.getOptionValue( "S" ) );
            // Associate the "message-type" option value with the "message type" key in this map.
            args.put( "messageType", line.getOptionValue( "t" ) );
            // Associate the boolean transformation of the "delivery-receipt" option value with the "delivery receipt"
            // key in this map.
            args.put( "deliveryReceipt", line.hasOption( "D" ) ? "true" : "false" );
            // Associate the "mm-subject" option value with the "MMS subject" key in this map.
            args.put( "mmSubject", line.getOptionValue( "mm-subject" ) );

            // Return the "HashMap" object consisting of the "command line arguments".
            return args;
        }
        // If an exception is thrown.
        catch( ParseException | MalformedURLException | URISyntaxException exp ) {
            log.error( exp.getMessage() );
            // Automatically generate a usage statement as well as the "help" information.
            // Construct a formatter of help messages for command line options.
            HelpFormatter formatter = new HelpFormatter();
            // Print the help for options with the specified command line syntax.
            formatter.printHelp( "SmppClient", options, true );

            // Terminate application - exit immediately with error.
            System.exit(1 );
        }
        return null;
    }
}
