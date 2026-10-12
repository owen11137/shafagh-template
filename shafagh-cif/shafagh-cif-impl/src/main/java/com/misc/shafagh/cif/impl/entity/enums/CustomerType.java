package com.misc.shafagh.cif.impl.entity.enums;

/**
 * @author m_kor
 * date 12/7/2024
 */
public enum CustomerType {
    REAL(0),
    LEGAL(1),
    FOREIGN_REAL(2),
    FOREIGN_LEGAL(3);

    private final int code;

    CustomerType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static String getTitle(CustomerType type) {
        String title = "";
        switch (type) {
            case REAL -> title = "حقیقی ایرانی";
            case LEGAL -> title = "حقوقی ایرانی";
            case FOREIGN_REAL -> title = "حقیقی خارجی";
            case FOREIGN_LEGAL -> title = "حقوقی خارجی";
            default -> throw new TypeNotPresentException(type.toString(), null);
        }
        return title;
    }

    public static CustomerType fromCode(int code) {
        for (CustomerType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid CustomerType code: " + code);
    }
}