package com.misc.shafagh.cif.impl.utility;

import java.time.LocalDateTime;

/**
 * @author m_kor
 * date 5/4/2025
 */
public class DateUtil {
    public static boolean isValidDateRange(LocalDateTime fromDate, LocalDateTime toDate) {
        if(fromDate == null) return false;
        if(toDate == null) return true;
        return !(toDate != null && fromDate.isAfter(toDate));
    }

    public static boolean isOverlappingNullableDateRange(LocalDateTime fromDate1, LocalDateTime toDate1, LocalDateTime fromDate2, LocalDateTime toDate2) {
        if (toDate1 != null && toDate2 == null && fromDate2.isAfter(toDate1)) return false;
        if (toDate2 != null && toDate1 == null && (fromDate1.isAfter(fromDate2))) return false;
        if(toDate1 != null && toDate2 != null && (toDate1.isBefore(fromDate2) || fromDate1.isAfter(toDate2))) return false;
        return true;
    }
}
