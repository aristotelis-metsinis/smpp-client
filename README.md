## SMPP Client

Short Message Peer-to-Peer (SMPP) using "Spring Boot" and "CloudHopper". This is a demo application that sends "SMS" messages and listens for "delivery receipts" using the "SMPP" protocol, bootstraped with "Spring Boot" and using the "Cloudhopper SMPP" library for sending "SMS".

Have a look @ https://juliuskrah.com/blog/2018/12/28/building-an-smpp-application-using-spring-boot/ as well as @ https://github.com/juliuskrah/smpp

If you like to check a demo application that sends "SMS" using the "SMPP" protocol, bootstraped with "Spring Boot" and "Camel" have a look @ https://juliuskrah.com/blog/2020/03/27/building-an-smpp-application-using-spring-boot-and-camel/

The application supports the submission of the following message types :

* **"Text" SMS**, the well-known "Short Message Service".
* **"Flash" SMS**, a type of "SMS" that appears directly on the main screen without user interaction and is not automatically stored in the inbox.
* **"WAP Push SI"** (Service Indication) message. On receiving a "WAP Push", a "WAP 1.2" (or later) enabled handset will automatically give the user the option to access the "WAP" content.
* **"WAP Push SL"** (Service Loading) message, which directly opens the browser to display the "WAP" content, without user interaction. Since this behaviour raises security concerns, some handsets handle "WAP Push SL" messages in the same way as "SI", by providing user interaction.
* **"WAP Push MMS notification"** message with "SMS" as bearer. A Mobile Terminating "MMS" is triggered by a "Multimedia Message Notification", i.e. "m-notification.ind". The "MMS" notification is used to inform the end user mobile that an "MMS" is waiting to be fetched. Usually the "m-notification.ind" is sent to the mobile phone by means of an "SMS".

----

### Usage

```
usage: SmppClient -d <dst-addr> [-D] [-h <href>] [-m <text>] [--mm-subject
       <subject>] -s <scr-addr> [-S <size>] -t <type>
 -d,--destination-addr <dst-addr>   msg destination address, e.g.
                                    306944000000
 -D,--delivery-receipt              request for delivery-receipt if the
                                    option has been specified, otherwise
                                    not if missing (default)
 -h,--wappush-href <href>           wap push href, e.g.
                                    "http://aristotelis-metsinis.github.io/"
 -m,--message-text <text>           msg text, e.g. "hello world"
    --mm-subject <subject>          multimedia subject, e.g. 'hello world'
 -s,--source-addr <scr-addr>        msg source address, e.g. 1284
 -S,--mm-size <size>                multimedia msg size (approximate
                                    calculation in bytes), e.g. 29696
 -t,--message-type <type>           message type, e.g.
                                    sms|flash|mms|wapSI|wapSL
```

----

### Usage | Examples

* [ 1.1 ] **Text SMS** 

```
--source-addr "560" --destination-addr=230000000000  -t sms  --message-text="SMS world hello world hello world hello world hello world hello world hello world hello world hello world hello world hello world 133"
```

* [ 1.2 ] **Text SMS concatenated** 

``` 
--source-addr "560" --destination-addr=230000000000  -t sms  --message-text="SMS world hello world hello world hello world hello world hello world hello world hello world hello world hello world hello world hello world hello world hello world hello world hello world hell 198" 
```

* [ 2.1 ] **Flash SMS** 

```
--source-addr "560" --destination-addr=230000000000  -t flash  --message-text="FLASH world hello world hello world hello world hello world hello world hello world hello world hello world hello world hello world 135"
```

* [ 2.2 ] **Flash SMS concatenated**

```
--source-addr "560" --destination-addr=230000000000  -t flash  --message-text="FLASH world hello world hello world hello world hello world hello world hello world hello world hello world hello world hello world hello world hello world hello world hello world hello world hell 200"
```

* [ 3 ] **WAP Push SI message**

```
--source-addr "560" --destination-addr=230000000000  -t wapsi  --message-text="WAP_SI hello world" -h "http://aristotelis-metsinis.github.io/"  
```

* [ 4 ] **WAP Push SL message**

```
--source-addr "560" --destination-addr=230000000000  -t wapsl  -h "http://aristotelis-metsinis.github.io/" 
```

* [ 5.1 ] **WAP Push MMS Notification message**

```
--source-addr "made by Aristotelis" --destination-addr=230000000000  -t mms  --mm-subject "hello world" -h "http://127.0.0.1:8080/sample.mms?seed=77" -S 1038694 
```

* [ 5.2 ] **WAP Push MMS Notification message making use of the optional parameter "message payload"**

```
--source-addr "made by Aristotelis" --destination-addr=230000000000  -t mms  --mm-subject "world hello world hello world hello world hello world hello world hello world hello world hello world hello world hello world hello world hello world hello world hello world hello world" -h "http://127.0.0.1:8080/sample.mms?seed=78" -S 1038694 
```

----

### Log

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v2.4.3)

2022-01-25 18:24:43.875  INFO 14808 --- [           main] c.c.smpp.impl.DefaultSmppSession         : sync send PDU: (bind_transmitter: 0x00000027 0x00000002 0x00000000 0x00000001) (body: systemId [smpp_system_id] password [smpp_password] systemType [] interfaceVersion [0x34] addressRange (0x00 0x00 [])) (opts: )
2022-01-25 18:24:44.871  INFO 14808 --- [   smpp.session] c.c.smpp.impl.DefaultSmppSession         : received PDU: (bind_transmitter_resp: 0x00000019 0x80000002 0x00000000 0x00000001 result: "OK") (body: systemId [system_Id]) (opts: )
2022-01-25 18:24:44.927  INFO 14808 --- [           main] o.s.s.c.ThreadPoolTaskScheduler          : Initializing ExecutorService 'taskScheduler'
2022-01-25 18:24:44.940  INFO 14808 --- [           main] com.smpp.client.SmppClient               : Started SmppClient in 2.153 seconds (JVM running for 2.576)
2022-01-25 18:24:44.947  INFO 14808 --- [           main] c.c.smpp.impl.DefaultSmppSession         : sync send PDU: (submit_sm: 0x000000CC 0x00000004 0x00000000 0x00000002) (body: (serviceType [] sourceAddr [0x00 0x01 [560]] destAddr [0x01 0x01 [230000000000]] esmCls [0x40] regDlvry [0x00] dcs [0x00] message [050003DF0201534D5320776F726C642068656C6C6F20776F726C642068656C6C6F20776F726C642068656C6C6F20776F726C642068656C6C6F20776F726C642068656C6C6F20776F726C642068656C6C6F20776F726C642068656C6C6F20776F726C642068656C6C6F20776F726C642068656C6C6F20776F726C642068656C6C6F20776F726C642068656C6C])) (opts: )
2022-01-25 18:24:45.871  INFO 14808 --- [   smpp.session] c.c.smpp.impl.DefaultSmppSession         : received PDU: (submit_sm_resp: 0x00000035 0x80000004 0x00000000 0x00000002 result: "OK") (body: (messageId [89f14a62-a4b2-42ab-a7ff-1038e617965a])) (opts: )
2022-01-25 18:24:45.871  INFO 14808 --- [           main] com.smpp.client.SmppClient               : SMS submitted, message id 89f14a62-a4b2-42ab-a7ff-1038e617965a
2022-01-25 18:24:45.872  INFO 14808 --- [           main] c.c.smpp.impl.DefaultSmppSession         : sync send PDU: (submit_sm: 0x00000086 0x00000004 0x00000000 0x00000003) (body: (serviceType [] sourceAddr [0x00 0x01 [560]] destAddr [0x01 0x01 [230000000000]] esmCls [0x40] regDlvry [0x00] dcs [0x00] message [050003DF02026F20776F726C642068656C6C6F20776F726C642068656C6C6F20776F726C642068656C6C6F20776F726C642068656C6C6F20776F726C642068656C6C20313938])) (opts: )
2022-01-25 18:24:46.874  INFO 14808 --- [   smpp.session] c.c.smpp.impl.DefaultSmppSession         : received PDU: (submit_sm_resp: 0x00000035 0x80000004 0x00000000 0x00000003 result: "OK") (body: (messageId [e9e18bf6-6fe8-4cde-af4c-63b6b1a872ca])) (opts: )
2022-01-25 18:24:46.875  INFO 14808 --- [           main] com.smpp.client.SmppClient               : SMS submitted, message id e9e18bf6-6fe8-4cde-af4c-63b6b1a872ca
2022-01-25 18:25:14.942  INFO 14808 --- [   scheduling-1] com.smpp.client.SmppClient               : sending enquire_link
2022-01-25 18:25:14.943  INFO 14808 --- [   scheduling-1] c.c.smpp.impl.DefaultSmppSession         : sync send PDU: (enquire_link: 0x00000010 0x00000015 0x00000000 0x00000004) (body: ) (opts: )
2022-01-25 18:25:15.904  INFO 14808 --- [   smpp.session] c.c.smpp.impl.DefaultSmppSession         : received PDU: (enquire_link_resp: 0x00000010 0x80000015 0x00000000 0x00000004 result: "OK") (body: ) (opts: )
2022-01-25 18:25:15.904  INFO 14808 --- [   scheduling-1] com.smpp.client.SmppClient               : enquire_link_resp: (enquire_link_resp: 0x00000010 0x80000015 0x00000000 0x00000004 result: "OK") (body: ) (opts: )
2022-01-25 18:25:45.917  INFO 14808 --- [   scheduling-1] com.smpp.client.SmppClient               : sending enquire_link
2022-01-25 18:25:45.917  INFO 14808 --- [   scheduling-1] c.c.smpp.impl.DefaultSmppSession         : sync send PDU: (enquire_link: 0x00000010 0x00000015 0x00000000 0x00000005) (body: ) (opts: )
2022-01-25 18:25:45.934  INFO 14808 --- [   smpp.session] c.c.smpp.impl.DefaultSmppSession         : received PDU: (enquire_link_resp: 0x00000010 0x80000015 0x00000000 0x00000005 result: "OK") (body: ) (opts: )
2022-01-25 18:25:45.935  INFO 14808 --- [   scheduling-1] com.smpp.client.SmppClient               : enquire_link_resp: (enquire_link_resp: 0x00000010 0x80000015 0x00000000 0x00000005 result: "OK") (body: ) (opts: )
2022-01-25 18:26:03.314  INFO 14808 --- [extShutdownHook] o.s.s.c.ThreadPoolTaskScheduler          : Shutting down ExecutorService 'taskScheduler'
2022-01-25 18:26:03.315  INFO 14808 --- [extShutdownHook] c.c.smpp.impl.DefaultSmppSession         : sync send PDU: (unbind: 0x00000010 0x00000006 0x00000000 0x00000006) (body: ) (opts: )
2022-01-25 18:26:03.956  INFO 14808 --- [   smpp.session] c.c.smpp.impl.DefaultSmppSession         : received PDU: (unbind_resp: 0x00000010 0x80000006 0x00000000 0x00000006 result: "OK") (body: ) (opts: )
2022-01-25 18:26:03.963  INFO 14808 --- [extShutdownHook] c.c.smpp.impl.DefaultSmppSession         : Successfully closed
```
