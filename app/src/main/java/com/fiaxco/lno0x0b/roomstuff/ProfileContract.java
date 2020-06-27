package com.fiaxco.lno0x0b.roomstuff;

public final class ProfileContract {

    private ProfileContract() {
    }

    public static final class ProfileEntry  {

        // Database constants
        final static String TABLE_NAME = "profiles";

        final static String DATABASE_NAME = "profile_database";

        public final static String _ID = "_ID";
        public final static String NAME = "name";
        public final static String AGE = "age";
        public final static String GENDER = "gender";
        public final static String HEIGHT = "height";
        public final static String WEIGHT = "weight";

        // Gender codes
        public static final int GENDER_UNKNOWN = 0;
        public static final int GENDER_MALE = 1;
        public static final int GENDER_FEMALE = 2;


        // Gender code check
        static boolean idGenderValid(Integer gender) {
            return gender != GENDER_UNKNOWN && gender != GENDER_MALE && gender != GENDER_FEMALE;
        }

        // Gender code to string
        public static String genderType(Integer genderCode) {
            switch (genderCode) {
                case GENDER_UNKNOWN:
                    return "Unknown";
                case GENDER_MALE:
                    return "Male";
                case GENDER_FEMALE:
                    return "Female";
                default:
                    return "Invalid";
            }
        }



    }



}