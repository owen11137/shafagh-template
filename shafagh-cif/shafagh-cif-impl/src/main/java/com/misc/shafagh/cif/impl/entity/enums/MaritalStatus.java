package com.misc.shafagh.cif.impl.entity.enums;

/**
 * @author m_kor
 * date 11/23/2024
 */
public enum MaritalStatus {
    SINGLE, MARRIED, DIVORCED;
    public static String getTitle(MaritalStatus maritalStatus) {
        if (maritalStatus == null) {
            throw new TypeNotPresentException(null, null);
        }
        String title = "";
        switch (maritalStatus) {
            case SINGLE -> title = "مجرد";
            case MARRIED -> title = "متاهل";
            case DIVORCED -> title = "جدا شده";
            default -> throw new TypeNotPresentException(maritalStatus.toString(), null);
        }
        return title;
    }
}
