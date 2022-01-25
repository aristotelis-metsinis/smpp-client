package com.smpp.client;

// Annotation for externalized configuration.
import org.springframework.boot.context.properties.ConfigurationProperties;
// Indicates that a field in a "@ConfigurationProperties" object should be treated as if it were a nested type.
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Class, which contains all the external properties we need.
 */
// Annotation for externalized configuration.
@ConfigurationProperties( "sms" )
public class SmppClientProperties {
    // The "async" field in a "@ConfigurationProperties" object should be treated as if it were a nested type.
    @NestedConfigurationProperty
    private final Async async = new Async();
    // The "smpp" field in a "@ConfigurationProperties" object should be treated as if it were a nested type.
    @NestedConfigurationProperty
    private final SMPP smpp = new SMPP();

    /**
     * Get "async" object.
     *
     * @return The "async" object.
     */
    public Async getAsync() {
        return async;
    }

    /**
     * Get "smpp" object.
     *
     * @return The "smpp" object.
     */
    public SMPP getSmpp() {
        return smpp;
    }

    /**
     * Class to access and update the value of "async" variables.
     *
     * Add default values for some of the settings - override some of the defaults in "yaml" file.
     */
    public static class Async {
        // "smpp-session-size" should be lower than the value assigned to the "core-pool-size".
        private int smppSessionSize = 2;
        private int corePoolSize = 5;
        private int maxPoolSize = 50;
        private int queueCapacity = 10000;
        private int initialDelay = 1000;
        private int timeout = 10000;

        /**
         * Get the smpp session size.
         *
         * @return The smpp session size.
         */
        public int getSmppSessionSize() {
            return smppSessionSize;
        }

        /**
         * Set the smpp session size.
         *
         * @param smppSessionSize The smpp session size.
         */
        public void setSmppSessionSize( int smppSessionSize ) {
            this.smppSessionSize = smppSessionSize;
        }

        /**
         * Get the core pool size.
         *
         * @return The core pool size.
         */
        public int getCorePoolSize() {
            return corePoolSize;
        }

        /**
         * Set the core pool size.
         *
         * @param corePoolSize The core pool size.
         */
        public void setCorePoolSize( int corePoolSize ) {
            this.corePoolSize = corePoolSize;
        }

        /**
         * Get the max pool size.
         *
         * @return The max pool size.
         */
        public int getMaxPoolSize() {
            return maxPoolSize;
        }

        /**
         * Set the max pool size.
         *
         * @param maxPoolSize The max pool size.
         */
        public void setMaxPoolSize( int maxPoolSize ) {
            this.maxPoolSize = maxPoolSize;
        }

        /**
         * Get the queue capacity.
         *
         * @return The queue capacity.
         */
        public int getQueueCapacity() {
            return queueCapacity;
        }

        /**
         * Set the queue capacity.
         *
         * @param queueCapacity The queue capacity.
         */
        public void setQueueCapacity( int queueCapacity ) {
            this.queueCapacity = queueCapacity;
        }

        /**
         * Get the initial delay.
         *
         * @return The initial delay.
         */
        public int getInitialDelay() {
            return initialDelay;
        }

        /**
         * Set the initial delay.
         *
         * @param initialDelay The initial delay.
         */
        public void setInitialDelay( int initialDelay ) {
            this.initialDelay = initialDelay;
        }

        /**
         * Get the timeout.
         *
         * @return The timeout.
         */
        public int getTimeout() {
            return timeout;
        }

        /**
         * Set the timeout.
         *
         * @param timeout The timeout.
         */
        public void setTimeout( int timeout ) {
            this.timeout = timeout;
        }
    }

    /**
     * Class to access and update the value of "smpp" variables.
     *
     * Add default values for some of the settings - override some of the defaults in "yaml" file.
     */
    public static class SMPP {
        // The "SMSC" IP address.
        private String host;
        // The "SMSC" port number.
        private int port = 2775;
        // The SMPP "system_id".
        private String userId;
        // The SMPP "password".
        private String password;
        // "true" if we would like to detect "DLR" by the "Optional Parameters" of the "SMPP" PDU, else "false".
        private boolean detectDlrByOpts = false;

        /**
         * Get the "SMSC" IP address.
         *
         * @return The "SMSC" IP address.
         */
        public String getHost() {
            return host;
        }

        /**
         * Set the "SMSC" IP address.
         *
         * @param host The "SMSC" IP address.
         */
        public void setHost( String host ) {
            this.host = host;
        }

        /**
         * Get the "SMSC" port number.
         *
         * @return The "SMSC" port number.
         */
        public int getPort() {
            return port;
        }

        /**
         * Set the "SMSC" port number.
         *
         * @param port The "SMSC" port number.
         */
        public void setPort( int port ) {
            this.port = port;
        }

        /**
         * Get the SMPP "system_id".
         *
         * @return The SMPP "system_id".
         */
        public String getUserId() {
            return userId;
        }

        /**
         * Set the SMPP "system_id".
         *
         * @param userId The SMPP "system_id".
         */
        public void setUserId( String userId ) {
            this.userId = userId;
        }

        /**
         * Get the SMPP "password".
         *
         * @return The SMPP "password".
         */
        public String getPassword() {
            return password;
        }

        /**
         * Set the SMPP "password".
         *
         * @param password The SMPP "password".
         */
        public void setPassword( String password ) {
            this.password = password;
        }

        /**
         * "true" if we would like to detect "DLR" by the "Optional Parameters" of the "SMPP" PDU, else "false".
         *
         * @return "true" if we would like to detect "DLR" by the "Optional Parameters" of the "SMPP" PDU, else "false".
         */
        public boolean isDetectDlrByOpts() {
            return detectDlrByOpts;
        }

        /**
         * Set "true" if we would like to detect "DLR" by the "Optional Parameters" of the "SMPP" PDU, else "false".
         *
         * @param detectDlrByOpts "true" if we would like to detect "DLR" by the "Optional Parameters" of the "SMPP" PDU,
         *                        else "false".
         */
        public void setDetectDlrByOpts( boolean detectDlrByOpts ) {
            this.detectDlrByOpts = detectDlrByOpts;
        }
    }
}
