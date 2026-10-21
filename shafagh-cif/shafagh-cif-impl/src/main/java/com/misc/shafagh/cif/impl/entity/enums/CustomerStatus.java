package com.misc.shafagh.cif.impl.entity.enums;


public enum CustomerStatus {
    INACTIVE(0),
    ACTIVE(1);

    private final int value;

    CustomerStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static String getTitle(CustomerStatus status) {
        String title = "";
        switch (status) {
            case INACTIVE -> title = "غیر فعال";
            case ACTIVE -> title = "فعال";
            default -> throw new TypeNotPresentException(status.toString(), null);
        }
        return title;
    }

    public static CustomerStatus fromValue(int value) {
        for (CustomerStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid CustomerStatus value: " + value);
    }
}
