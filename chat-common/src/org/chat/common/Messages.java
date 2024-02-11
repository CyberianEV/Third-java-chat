package org.chat.common;

public class Messages {
    public static final String DELIMITER = "§";
    public static final String AUTH_REQUEST = "/auth_request";
    public static final String AUTH_ACCEPT = "/auth_accept";
    public static final String AUTH_DENY = "/auth_deny";
    public static final String MSG_BROADCAST = "/bcast";
    public static final String MSG_FORMAT_ERROR = "/msg_error";
    public static final String USER_LIST = "/user_list";
    public static final String USER_BROADCAST = "/user_bcast";
    public static final String SIGNUP_REQUEST = "/signup_request";
    public static final String SIGNUP_SUCCEED = "/signup_succeed";
    public static final String FAILED = "/failed";
    public static final String PASSCHANGE_REQUEST = "/passchange_request";
    public static final String PASSCHANGE_SUCCEED = "/passchange_succeed";
    public static final String NAMECHANGE_REQUEST = "/namechange_request";
    public static final String NAMECHANGE_SUCCEED = "/namechange_succeed";

    public static final String MSG_SIGNUP_SUCCEED = "User %s successfully created";
    public static final String MSG_PASSCHANGE_SUCCEED = "Password for user %s has been successfully changed";
    public static final String MSG_USER_UNUNIQUE = "A user with login %s already exists";
    public static final String MSG_PASS_LENGTH = "Password length can not be less than 3 symbols";
    public static final String MSG_SIGNUP_FAILED = "Failed to create a new user";
    public static final String MSG_PASSCHANGE_FAILED = "Failed to change the password";
    public static final String MSG_NAMECHANGE_FAILED = "Failed to change the name";
    public static final String MSG_MISMATCHED_LOGIN_PASS = "Invalid login or password";
    public static final String MSG_EMPTY_FIELDS = "Fields can not be empty";

    public static String getTypeBcastFromClient(String msg) {
        return USER_BROADCAST + DELIMITER + msg;
    }

    // /userlist±user1±user2±.....
    public static String getUserList(String users) {
        return USER_LIST + DELIMITER + users;
    }

    public static String getAuthRequest(String login, String password) {
        return AUTH_REQUEST + DELIMITER + login + DELIMITER + password;
    }

    public static String getAuthAccept(String nickname) {
        return AUTH_ACCEPT + DELIMITER + nickname;
    }

    public static String getAuthDenied() {
        return AUTH_DENY;
    }
    public static String getMsgFormatError(String message) {
        return MSG_FORMAT_ERROR + DELIMITER + message;
    }

    public static String getTypeBroadcast(String src, String message) {
        return MSG_BROADCAST + DELIMITER + System.currentTimeMillis() +
                DELIMITER + src + DELIMITER + message;
    }

    public static String getSignupRequest(String login, String password, String nickname) {
        return SIGNUP_REQUEST + DELIMITER + login + DELIMITER + password +
                DELIMITER + nickname;
    }

    public static String getSignupSucceed(String login) {
        return SIGNUP_SUCCEED + DELIMITER + MSG_SIGNUP_SUCCEED.formatted(login);
    }

    public static String getPassChangeRequest(String newPass, String oldPass, String login) {
        return PASSCHANGE_REQUEST + DELIMITER + newPass + DELIMITER + oldPass + DELIMITER + login;
    }

    public static String getPasschangeSucceed(String login) {
        return PASSCHANGE_SUCCEED + DELIMITER + MSG_PASSCHANGE_SUCCEED.formatted(login);
    }

    public static String getFailedUserUnunique(String login) {
        return FAILED + DELIMITER + MSG_USER_UNUNIQUE.formatted(login);
    }

    public static String getMismathedLoginPass() {
        return FAILED + DELIMITER + MSG_MISMATCHED_LOGIN_PASS;
    }

    public static String getNameChangeRequest(String name, String login) {
        return NAMECHANGE_REQUEST + DELIMITER + name + DELIMITER + login;
    }

    public static String getNameChangeFailed() {
        return FAILED + DELIMITER + MSG_NAMECHANGE_FAILED;
    }

    public static String getFailedPassLength() {
        return FAILED + DELIMITER + MSG_PASS_LENGTH;
    }

    public static String getFailedEmptyFields() {
        return FAILED + DELIMITER + MSG_EMPTY_FIELDS;
    }

    public static String getSignupFailed() {
        return FAILED + DELIMITER + MSG_SIGNUP_FAILED;
    }

    public static String getPasschangeFailed() {
        return FAILED + DELIMITER + MSG_PASSCHANGE_FAILED;
    }
}
