package gov.nist.healthcare.ttt.tempxdrcommunication.artifact;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.UUID;

/**
 *
 * @author mccaffrey
 */
public class ArtifactManagement {

    public enum Type {

        XDR_FULL_METADATA,
        XDR_MINIMAL_METADATA,
        NEGATIVE_BAD_SOAP_HEADER,
        NEGATIVE_BAD_SOAP_BODY,
        NEGATIVE_MISSING_DIRECT_BLOCK,
        NEGATIVE_MISSING_METADATA_ELEMENTS1,
        NEGATIVE_MISSING_METADATA_ELEMENTS2,
        NEGATIVE_MISSING_METADATA_ELEMENTS3,
        NEGATIVE_MISSING_METADATA_ELEMENTS4,
        NEGATIVE_MISSING_METADATA_ELEMENTS5,
        NEGATIVE_MISSING_ASSOCIATION,
        DELIVERY_STATUS_NOTIFICATION_SUCCESS,
        DELIVERY_STATUS_NOTIFICATION_FAILURE
    };

    public static final String NIST_OID_PREFIX = "2.16.840.1.113883.3.72.5";

    private static final String FILENAME_XDR_FULL_METADATA = "Xdr_full_metadata.xml";
    private static final String FILENAME_XDR_FULL_METADATA_ONLY = "Xdr_full_metadata_only.xml";
    private static final String FILENAME_XDR_FULL_METADATA_NO_SOAP_NO_XOP = "Xdr_full_metadata_no_soap_no_xop.xml";
    private static final String FILENAME_BAD_SOAP = "negative_bad_soap.xml";
    private static final String FILENAME_BAD_SOAP_BODY = "negative_bad_soap_body.xml";
    private static final String FILENAME_MISSING_DIRECT_BLOCK = "negative_missing_direct_block.xml";
    private static final String FILENAME_MISSING_METADATA_ELEMENTS1 = "negative_missing_metadata_elements1.xml";
    private static final String FILENAME_MISSING_METADATA_ELEMENTS1_NO_SOAP_NO_XOP = "negative_missing_metadata_elements1_no_soap_no_xop.xml";                                
    private static final String FILENAME_MISSING_METADATA_ELEMENTS2 = "negative_missing_metadata_elements2.xml";
    private static final String FILENAME_MISSING_METADATA_ELEMENTS2_NO_SOAP_NO_XOP = "negative_missing_metadata_elements2_no_soap_no_xop.xml";
    private static final String FILENAME_MISSING_METADATA_ELEMENTS3 = "negative_missing_metadata_elements3.xml";
    private static final String FILENAME_MISSING_METADATA_ELEMENTS3_NO_SOAP_NO_XOP = "negative_missing_metadata_elements3_no_soap_no_xop.xml";
    private static final String FILENAME_MISSING_METADATA_ELEMENTS4 = "negative_missing_metadata_elements4.xml";
    private static final String FILENAME_MISSING_METADATA_ELEMENTS4_NO_SOAP_NO_XOP = "negative_missing_metadata_elements4_no_soap_no_xop.xml";
    private static final String FILENAME_MISSING_METADATA_ELEMENTS5 = "negative_missing_metadata_elements5.xml";
    private static final String FILENAME_MISSING_METADATA_ELEMENTS5_NO_SOAP_NO_XOP = "negative_missing_metadata_elements5_no_soap_no_xop.xml";
    private static final String FILENAME_MISSING_ASSOCIATION = "negative_missing_association.xml";
    private static final String FILENAME_MISSING_ASSOCIATION_NO_SOAP_NO_XOP = "negative_missing_association_no_soap_no_xop.xml";    
    private static final String FILENAME_XDR_MINIMAL_METADATA = "Xdr_minimal_metadata.xml";
    private static final String FILENAME_XDR_MINIMAL_METADATA_ONLY = "Xdr_minimal_metadata_only.xml";
    private static final String FILENAME_XDR_MINIMAL_METADATA_NO_SOAP_NO_XOP = "Xdr_minimal_metadata_no_soap_no_xop.xml";
    private static final String FILENAME_ENCODED_CCDA = "encodedCCDA.txt";
    private static final String FILENAME_DELIVERY_STATUS_NOTIFICATION_SUCCESS = "DeliveryStatusNotification_success.xml";
    private static final String FILENAME_DELIVERY_STATUS_NOTIFICATION_FAILURE = "DeliveryStatusNotification_failure.xml";

    // TODO: If a schema for the Direct Headers becomes available, replace this
    // string manipulation with XML objects.
    public static String getAdditionalSOAPHeaders(Type type, Settings settings) {
        String metadata = null;
        if (type.equals(Type.XDR_FULL_METADATA)) {
            metadata = "XDS";
        } else {
            metadata = "minimal";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<direct:metadata-level xmlns:direct=\"urn:direct:addressing\">" + metadata + "</direct:metadata-level>");
        if (!type.equals(Type.NEGATIVE_MISSING_DIRECT_BLOCK)) {
            sb.append("<direct:addressBlock xmlns:direct=\"urn:direct:addressing\" xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\" ");
            sb.append("soapenv:role=\"urn:direct:addressing:destination\" soapenv:relay=\"true\">");
            sb.append("<direct:from>" + settings.getDirectFrom() + "</direct:from>");
            sb.append("<direct:to>" + settings.getDirectTo() + "</direct:to>");
            sb.append("</direct:addressBlock>");
        }
        return sb.toString();
    }

    public static String getMetadata(Type type, Settings settings) {

        String metadata = null;
        switch (type) {
            case XDR_FULL_METADATA:
                metadata = getTemplate(FILENAME_XDR_FULL_METADATA_NO_SOAP_NO_XOP);
                break;
            case XDR_MINIMAL_METADATA:
                metadata = getTemplate(FILENAME_XDR_FULL_METADATA_NO_SOAP_NO_XOP);
                break;

            case NEGATIVE_MISSING_METADATA_ELEMENTS1:
                metadata = getTemplate(FILENAME_MISSING_METADATA_ELEMENTS1_NO_SOAP_NO_XOP);
                break;
            case NEGATIVE_MISSING_METADATA_ELEMENTS2:
                metadata = getTemplate(FILENAME_MISSING_METADATA_ELEMENTS2_NO_SOAP_NO_XOP);
                break;
            case NEGATIVE_MISSING_METADATA_ELEMENTS3:
                metadata = getTemplate(FILENAME_MISSING_METADATA_ELEMENTS3_NO_SOAP_NO_XOP);
                break;
            case NEGATIVE_MISSING_METADATA_ELEMENTS4:
                metadata = getTemplate(FILENAME_MISSING_METADATA_ELEMENTS4_NO_SOAP_NO_XOP);
                break;
            case NEGATIVE_MISSING_METADATA_ELEMENTS5:
                metadata = getTemplate(FILENAME_MISSING_METADATA_ELEMENTS5_NO_SOAP_NO_XOP);
                break;
            case NEGATIVE_MISSING_ASSOCIATION:
                metadata = getTemplate(FILENAME_MISSING_ASSOCIATION_NO_SOAP_NO_XOP);
                break;

            default:
                break;
        }

        // metadata = setDirectAddressBlock(metadata, settings.getDirectTo(), settings.getDirectFrom());
        //metadata = setSOAPHeaders(metadata, settings.getWsaTo());
        metadata = setIds(metadata, settings.getMessageId());

        return metadata;

    }

    public static String getPayload(Type type, Settings settings) throws IOException {
        makeSettingsSafe(settings);
        String payload = null;
        switch (type) {
            case XDR_FULL_METADATA:
                payload = getXdrFullMetadata(settings);
                break;
            case XDR_MINIMAL_METADATA:
                payload = getXdrMinimalMetadata(settings);
                break;
            case DELIVERY_STATUS_NOTIFICATION_SUCCESS:
                payload = getDeliveryStatusNotificationSuccess(settings);
                break;
            case DELIVERY_STATUS_NOTIFICATION_FAILURE:
                break;

        }
        return payload;
    }

    public static String getMtomSoap(Type type, Settings settings) {
        makeSettingsSafe(settings);
        String payload = null;
        switch (type) {
            case XDR_FULL_METADATA:
                payload = getXdrFullMetadataOnly(settings);
                break;
            case XDR_MINIMAL_METADATA:
                payload = getXdrMinimalMetadataOnly(settings);
                break;
            case NEGATIVE_BAD_SOAP_HEADER:
                payload = getXdrBadSoap(settings);
                break;
            case NEGATIVE_MISSING_DIRECT_BLOCK:
                payload = getXdrMissingDirectBlock(settings);
                break;
            case NEGATIVE_BAD_SOAP_BODY:
                payload = getXdrBadSoapBody(settings);
                break;
            case NEGATIVE_MISSING_METADATA_ELEMENTS1:
                payload = getXdrMissingMetadataElements1(settings);
                break;
            case NEGATIVE_MISSING_METADATA_ELEMENTS2:
                payload = getXdrMissingMetadataElements2(settings);
                break;
            case NEGATIVE_MISSING_METADATA_ELEMENTS3:
                payload = getXdrMissingMetadataElements3(settings);
                break;
            case NEGATIVE_MISSING_METADATA_ELEMENTS4:
                payload = getXdrMissingMetadataElements4(settings);
                break;

            case NEGATIVE_MISSING_METADATA_ELEMENTS5:
                payload = getXdrMissingMetadataElements5(settings);
                break;
            case NEGATIVE_MISSING_ASSOCIATION:
                payload = getXdrMissingAssociation(settings);
                break;
        }

        return payload;

    }

    // if messageId null or empty, creates one
    private static String getDeliveryStatusNotificationSuccess(Settings settings) {
        makeSettingsSafe(settings);
        String message = getTemplate(FILENAME_DELIVERY_STATUS_NOTIFICATION_SUCCESS);
        message = setDirectAddressBlock(message, settings.getDirectTo(), settings.getDirectFrom());
        message = message.replaceAll("#DIRECT_RELATESTO#", settings.getDirectRelatesTo());
        message = message.replaceAll("#DIRECT_RECIPIENT#", settings.getDirectRecipient());
        message = setSOAPHeaders(message, settings.getWsaTo());
        message = setIds(message, settings.getMessageId());

        return message;
    }

    public static String getXdrFullMetadata(Settings settings) {
        makeSettingsSafe(settings);
        String message = getTemplate(FILENAME_XDR_FULL_METADATA);
        message = setDirectAddressBlock(message, settings.getDirectTo(), settings.getDirectFrom());
        message = setSOAPHeaders(message, settings.getWsaTo());
        message = setIds(message, settings.getMessageId());

        return message;
    }

    public static String getXdrFullMetadataOnly(Settings settings) {
        makeSettingsSafe(settings);
        String message = getTemplate(FILENAME_XDR_FULL_METADATA_ONLY);
        message = setDirectAddressBlock(message, settings.getDirectTo(), settings.getDirectFrom());
        message = setSOAPHeaders(message, settings.getWsaTo());
        message = setIds(message, settings.getMessageId());

        return message;
    }

    public static String getXdrMinimalMetadata(Settings settings) {
        makeSettingsSafe(settings);
        String message = getTemplate(FILENAME_XDR_MINIMAL_METADATA);
        message = setDirectAddressBlock(message, settings.getDirectTo(), settings.getDirectFrom());
        message = setSOAPHeaders(message, settings.getWsaTo());
        message = setIds(message, settings.getMessageId());

        return message;
    }

    public static String getXdrMinimalMetadataOnly(Settings settings) {
        makeSettingsSafe(settings);
        String message = getTemplate(FILENAME_XDR_MINIMAL_METADATA_ONLY);
        message = setDirectAddressBlock(message, settings.getDirectTo(), settings.getDirectFrom());
        message = setSOAPHeaders(message, settings.getWsaTo());
        message = setIds(message, settings.getMessageId());

        return message;
    }

    public static String getXdrBadSoap(Settings settings) {
        makeSettingsSafe(settings);
        String message = getTemplate(FILENAME_BAD_SOAP);
        message = setDirectAddressBlock(message, settings.getDirectTo(), settings.getDirectFrom());
        message = setSOAPHeaders(message, settings.getWsaTo());
        message = setIds(message, settings.getMessageId());

        return message;
    }

    public static String getXdrBadSoapBody(Settings settings) {
        makeSettingsSafe(settings);
        String message = getTemplate(FILENAME_BAD_SOAP_BODY);
        message = setDirectAddressBlock(message, settings.getDirectTo(), settings.getDirectFrom());
        message = setSOAPHeaders(message, settings.getWsaTo());
        message = setIds(message, settings.getMessageId());

        return message;
    }

    public static String getXdrMissingDirectBlock(Settings settings) {
        makeSettingsSafe(settings);
        String message = getTemplate(FILENAME_MISSING_DIRECT_BLOCK);
        message = setDirectAddressBlock(message, settings.getDirectTo(), settings.getDirectFrom());
        message = setSOAPHeaders(message, settings.getWsaTo());
        message = setIds(message, settings.getMessageId());

        return message;
    }

    public static String getXdrMissingMetadataElements1(Settings settings) {
        makeSettingsSafe(settings);
        String message = getTemplate(FILENAME_MISSING_METADATA_ELEMENTS1);
        message = setDirectAddressBlock(message, settings.getDirectTo(), settings.getDirectFrom());
        message = setSOAPHeaders(message, settings.getWsaTo());
        message = setIds(message, settings.getMessageId());

        return message;
    }

    public static String getXdrMissingMetadataElements2(Settings settings) {
        makeSettingsSafe(settings);
        String message = getTemplate(FILENAME_MISSING_METADATA_ELEMENTS2);
        message = setDirectAddressBlock(message, settings.getDirectTo(), settings.getDirectFrom());
        message = setSOAPHeaders(message, settings.getWsaTo());
        message = setIds(message, settings.getMessageId());

        return message;
    }

    public static String getXdrMissingMetadataElements3(Settings settings) {
        makeSettingsSafe(settings);
        String message = getTemplate(FILENAME_MISSING_METADATA_ELEMENTS3);
        message = setDirectAddressBlock(message, settings.getDirectTo(), settings.getDirectFrom());
        message = setSOAPHeaders(message, settings.getWsaTo());
        message = setIds(message, settings.getMessageId());

        return message;
    }

    public static String getXdrMissingMetadataElements4(Settings settings) {
        makeSettingsSafe(settings);
        String message = getTemplate(FILENAME_MISSING_METADATA_ELEMENTS4);
        message = setDirectAddressBlock(message, settings.getDirectTo(), settings.getDirectFrom());
        message = setSOAPHeaders(message, settings.getWsaTo());
        message = setIds(message, settings.getMessageId());

        return message;
    }

    public static String getXdrMissingMetadataElements5(Settings settings) {
        makeSettingsSafe(settings);
        String message = getTemplate(FILENAME_MISSING_METADATA_ELEMENTS5);
        message = setDirectAddressBlock(message, settings.getDirectTo(), settings.getDirectFrom());
        message = setSOAPHeaders(message, settings.getWsaTo());
        message = setIds(message, settings.getMessageId());

        return message;
    }

    public static String getXdrMissingAssociation(Settings settings) {
        makeSettingsSafe(settings);
        String message = getTemplate(FILENAME_MISSING_ASSOCIATION);
        message = setDirectAddressBlock(message, settings.getDirectTo(), settings.getDirectFrom());
        message = setSOAPHeaders(message, settings.getWsaTo());
        message = setIds(message, settings.getMessageId());

        return message;
    }

    private static String getTemplate(String resourceName) {

        InputStream is = ArtifactManagement.class.getClassLoader().getResourceAsStream(resourceName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder out = new StringBuilder();
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line + "\r\n");
            }
            reader.close();
        } catch (IOException ioe) {
            //TODO
            System.err.println("RESOURCE NOT FOUND: " + resourceName);
            ioe.printStackTrace();
        }
        return out.toString();
    }

    public static String getBaseEncodedCCDA() {
        return getTemplate(FILENAME_ENCODED_CCDA);
    }

    private static String setIds(
            String message,
            String messageId) {

        if (messageId == null || "".equals(messageId)) {
            messageId = UUID.randomUUID().toString();
        }
        message = message.replaceAll("#MESSAGE_ID#", messageId);
        String entryUuid = UUID.randomUUID().toString();
        message = message.replaceAll("#ENTRY_UUID#", entryUuid);
        String documentUuid = UUID.randomUUID().toString();
        message = message.replaceAll("#DOCUMENT_ID#", documentUuid);
        long timestamp = Calendar.getInstance().getTimeInMillis();
        String uniqueId = NIST_OID_PREFIX + "." + timestamp;
        message = message.replaceAll("#UNIQUE_ID_SS#", uniqueId);

        return message;
    }

    private static String setDirectAddressBlock(String message, String directTo, String directFrom) {
        message = message.replaceAll("#DIRECT_TO#", directTo);
        message = message.replaceAll("#DIRECT_FROM#", directFrom);
        return message;
    }

    private static String setSOAPHeaders(String message, String wsaTo) {
        return message.replaceAll("#WSA_TO#", wsaTo);
    }

    private static void makeSettingsSafe(Settings settings) {
        if (settings == null) {
            settings = new Settings();
        }
        if (settings.getDirectFrom() == null) {
            settings.setDirectFrom("");
        }
        if (settings.getDirectRecipient() == null) {
            settings.setDirectRecipient("");
        }
        if (settings.getDirectRelatesTo() == null) {
            settings.setDirectRelatesTo("");
        }
        if (settings.getDirectTo() == null) {
            settings.setDirectTo("");
        }
        if (settings.getWsaTo() == null) {
            settings.setWsaTo("");
        }

    }

    public final static void main(String args[]) {

        try {

            Settings settings = new Settings();
            settings.setDirectFrom("from@direct.com");
            settings.setDirectTo("to@direct.com");
            settings.setWsaTo("fakeWsaHere");

            //  String payload = getPayload(Type.XDR_FULL_METADATA, settings);
            String payload = ArtifactManagement.getMtomSoap(Type.XDR_FULL_METADATA, settings);
            //      System.out.println("here!\n" + payload);

//            System.out.println(ArtifactManagement.getAdditionalSOAPHeaders(Type.XDR_FULL_METADATA, settings));

            System.out.println(ArtifactManagement.getMetadata(Type.NEGATIVE_MISSING_ASSOCIATION, settings));

            //    URL url = ClassLoader.getSystemResource("DeliveryStatusNotification_success.xml");
            //  System.out.println(url.getPath());
        /*    
             System.out.println(getDeliveryStatusNotificationSuccess("/home/mccaffrey/xdr/DeliveryStatusNotification_success.xml",
             "directTo",
             "directFrom",
             "relatesTo",
             "recipient",
             "wsaTo",
             null));
             */
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
