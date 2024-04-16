package com.jantabank.config;

public class Enums {
    public static class UserRole {
        public static final int USER = 1;
        public static final int ADMIN = 2;
    }

    public static class AccountStatus {
        public static final int REQUESTED = 0;
        public static final int ACTIVE = 1;
        public static final int INACTIVE = 2;
        public static final int COMPLETED = 3;
        public static final int REJECTED = 3;
    }

    public static class TransactionStatus {
        public static final int INITIATED = 0;
        public static final int COMPLETED = 1;
        public static final int FAILED = 2;
    }
    public static class AccountType {
        public static final int SAVINGS = 1;
        public static final int CURRENT = 2;
    }

}
