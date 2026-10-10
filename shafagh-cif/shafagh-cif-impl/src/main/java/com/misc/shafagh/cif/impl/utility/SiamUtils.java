package com.misc.shafagh.cif.impl.utility;/*
package com.misc.shafagh.cif.impl.utility;

import com.misc.shafagh.cif.impl.entity.enums.SiamInquiryPersonType;
import com.misc.shafagh.cif.impl.utility.time.PersianDate;

import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Pattern;

*/
/**
 * @author m_kor
 * date 9/23/2023
 *//*

public class SiamUtils {
    public static String getSiamTrackId(String userName) {
        PersianDate today = PersianDate.now();
        String[] dateArr = today.toString().split("-");
        String date = dateArr[0]+dateArr[1]+dateArr[2];
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String result = (userName.substring(0, 4) + date + uuid).toUpperCase();
        return result;
    }

    public static String getShahkarTrackId() {
        PersianDate today = PersianDate.now();
        String[] dateArr = today.toString().split("-");
        String date = dateArr[0]+dateArr[1]+dateArr[2];
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String result = ("SHFQ" + date + uuid).toUpperCase();
        return result;
    }

    private static final String SHAHAB_CODE_REGEX = "^\\d{16}$";

    public static boolean isValidShahabCode(String shahabCode) {
        return Pattern.compile(SHAHAB_CODE_REGEX).matcher(shahabCode).matches();
    }

    public static SiamInquiryPersonType getTypeByeShahabCode(String shahabCode) throws Exception {
        if(Verhoeff.isValidShahab(shahabCode)) {
            return SiamInquiryPersonType.getType(Integer.valueOf(shahabCode.substring(0,1)));
        }
        throw new Exception("Invalid Shahab Code");
    }

    public static String getShahabCode(String identifier, SiamInquiryPersonType type) {
        if(type.equals(SiamInquiryPersonType.REAL)) {
            String str = "100000"+identifier.substring(0, 9);
            return (str + Verhoeff.generateVerhoeff(str));
        }
        if(type.equals(SiamInquiryPersonType.CORPORATE)) {
            String str = "20000"+identifier.substring(0, 10);
            return (str + Verhoeff.generateVerhoeff(str));
        }
        if(type.equals(SiamInquiryPersonType.FOREIGNREAL)) {
            String str = "3" + String.format("%014d", Long.valueOf(identifier.substring(0, identifier.length() -1)));
            return str.concat(Verhoeff.generateVerhoeff(str));
        }
        if(type.equals(SiamInquiryPersonType.FOREIGNCORPORATE)) {
            String str = "4" + String.format("%014d", Long.valueOf(identifier.substring(0, identifier.length() -1)));
            return str.concat(Verhoeff.generateVerhoeff(str));
        }
        return "";
    }

    public static void setDefaultProperty() {

        System.setProperty("duration", "");
        System.setProperty("ipAddress", "");
        System.setProperty("result", "");
        System.setProperty("requestMethod", "");
        System.setProperty("http-host", "");
        System.setProperty("username", "");
        System.setProperty("userAgent", "");
        System.setProperty("host", "");
        System.setProperty("eventCategory", "");
        System.setProperty("eventType", "");
        System.setProperty("reason", "N/A");
        System.setProperty("contentLengthRequest", "");
        System.setProperty("contentLengthResponse", "");
        System.setProperty("statusCode", "");
        System.setProperty("contentType", "");
        System.setProperty("correlationID", "");
        System.setProperty("requestBody", "");
        System.setProperty("additionalInfo", "");
        System.setProperty("url", "");
        System.setProperty("module", "");
    }

}
*/
