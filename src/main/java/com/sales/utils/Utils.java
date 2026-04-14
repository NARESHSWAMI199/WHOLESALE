package com.sales.utils;

import com.sales.claims.AuthUser;
import com.sales.claims.SalesUser;
import com.sales.entities.User;
import com.sales.exceptions.MyException;
import com.sales.exceptions.NotFoundException;
import com.sales.exceptions.UserException;
import com.sales.global.GlobalConstant;
import com.sales.global.ResponseMessages;
import com.sales.jwtUtils.JwtToken;
import com.sales.wholesaler.services.WholesaleUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class Utils {

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    private static final Logger logger = LoggerFactory.getLogger(Utils.class);
    private static final SecureRandom random = new SecureRandom();
    public static String mobileRegex = "^[6789]\\d{9}$";
    private Utils() {
    }

    public static Long getCurrentMillis() {
        return new Date().getTime();
    }

    public static String getMillisToDate(Long millis) {
        DateFormat format = new SimpleDateFormat("dd MMM yyyy HH:mm:ss:SSS Z");
        Date date = new Date(millis);
        return format.format(date);
    }

    public static String getStringDateOnly(Long millis) {
        if (millis == null) return "null";
        DateFormat format = new SimpleDateFormat("dd MMM yyyy");
        Date date = new Date(millis);
        return format.format(date);
    }

    public static boolean isEmpty(String string) {
        return (string == null || string.trim().isEmpty());
    }

    public static boolean isValidImage(String image) {
        String IMAGE_PATTERN =
                "([^\\s]+(\\.(?i)(jpg|png|gif|bmp|jpeg|jfif|webp))$)";
        Pattern pattern = Pattern.compile(IMAGE_PATTERN);
        Matcher matcher = pattern.matcher(image);
        return matcher.matches();
    }

    public static boolean isValidEmail(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.matches();
    }

    public static boolean isValidPhoneNumber(String mobileNumber) {
        return mobileNumber != null && mobileNumber.matches(mobileRegex);
    }

    public static void mobileAndEmailValidation(String email, String contact, String errorMessage) {
        if (Utils.isEmpty(contact) || !contact.matches(Utils.mobileRegex))
            throw new IllegalArgumentException(errorMessage.replaceAll("_", "mobile number") + " [" + contact + "]");
        if (Utils.isEmpty(email) || !isValidEmail(email))
            throw new IllegalArgumentException(errorMessage.replaceAll("_", "email address") + " [" + email + "]");
    }

    public static void canUpdateAStaff(String slug, String userType, AuthUser loggedUser) throws PermissionDeniedDataAccessException {
        if ((!loggedUser.getUserType().equals("SA") && // if user is not a super admin
                loggedUser.getId() != GlobalConstant.suId) &&  // if user not owner
                userType.equals("S") && // but user is a staff
                !loggedUser.getSlug().equals(slug)) { // request slug equals self slug
            throw new PermissionDeniedDataAccessException(ResponseMessages.PERMISSION_DENIED_STAFF_CONTACT, new Exception());
        }
    }

    public static void canUpdateAStaffStatus(String slug, String userType, AuthUser loggedUser) throws PermissionDeniedDataAccessException {
        if ((!loggedUser.getUserType().equals("SA") && // if user is not a super admin
                loggedUser.getId() != GlobalConstant.suId) &&  // if user not owner
                userType.equals("S") && // but user is a staff
                loggedUser.getSlug().equals(slug)) { // request slug equals logged user's slug
            throw new PermissionDeniedDataAccessException(ResponseMessages.PERMISSION_DENIED_STAFF_CONTACT, new Exception());
        }
    }


    public static String isValidName(final String name, String flag) {
        if (name == null)
            throw new IllegalArgumentException(String.format(ResponseMessages.FIELD_CANNOT_BE_NULL, flag + "'s name"));
        String NAME_PATTERN =
                "^[a-zA-Z](?=.{1,100}$)[A-Za-z_& ]*(?:\\h+[A-Z][A-Za-z]*)*$";
        if (flag.equalsIgnoreCase("user")) {
            /* item can hold more then 0 char and less then 28 and don't  support &*/
            //NAME_PATTERN = "^[A-Z](?=.{1,28}$)[A-Za-z_ ]*(?:\\h+[A-Z][A-Za-z]*)*$";
        } else if (flag.equalsIgnoreCase("item")) {
            NAME_PATTERN = "^[a-zA-Z][a-zA-Z0-9\\s,().-]*$";
        }
        Pattern pattern = Pattern.compile(NAME_PATTERN);
        Matcher matcher = pattern.matcher(name);
        logger.debug("{}", matcher.matches());
        if (!matcher.matches()) {
            String neededSyntax = ResponseMessages.SPECIAL_SYMBOL_NOT_ALLOWED;
            if (flag.equals("user")) {
                throw new MyException(ResponseMessages.NOT_A_VALID_USERNAME + " " + neededSyntax);
            }
            String message = String.format(ResponseMessages.INVALID_NAME_FORMAT, flag);
            logger.debug("message : {}", message);
            throw new IllegalArgumentException(message + " " + neededSyntax);
        }
        return name;
    }


    public static int generateOTP(int length) {
        int otp = 0;
        for (int i = 0; i < length; i++) {
            otp = otp * 10 + random.nextInt(10);
        }
        return otp;
    }


    public static User getUserFromRequest(HttpServletRequest request, JwtToken jwtToken, WholesaleUserService userService) {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        // Token from swagger because swagger not sends Authorization header in request.
        token = token == null ? request.getHeader("authToken") : token;
        logger.debug("request url : {}", request.getRequestURI());
        try {
            if (token != null && token.startsWith(GlobalConstant.AUTH_TOKEN_PREFIX)) {
                token = token.substring(7);
                String slug = jwtToken.getSlugFromToken(token);
                /* get user by slug. */
                User user = userService.findUserBySlug(slug);
                if (!user.isEnabled()) {
                    throw new UserException(ResponseMessages.USER_NOT_ACTIVE);
                }
                return user;
            }
            throw new UserException(ResponseMessages.INVALID_AUTHORIZATION);
        } catch (Exception e) {
            throw new UserException(e.getMessage());
        }
    }


    public static User getUserFromRequest(HttpServletRequest request, String token, JwtToken jwtToken, WholesaleUserService userService) {
        logger.debug("[getUserFromRequest] request url : {}", request.getRequestURI());
        token = token != null ? URLDecoder.decode(token, StandardCharsets.UTF_8) : token;
        try {
            if (token != null) {
                String slug = jwtToken.getSlugFromToken(token);
                /* get user by slug. */
                User user = userService.findUserBySlug(slug);
                if (user.getIsDeleted().equals("Y")) {
                    throw new NotFoundException(ResponseMessages.USER_NOT_FOUND);
                } else if (user.getStatus().equals("D")) {
                    throw new UserException(ResponseMessages.USER_NOT_ACTIVE);
                }
                return user;
            }
            throw new UserException(ResponseMessages.INVALID_AUTHORIZATION);

        } catch (Exception e) {
            throw new UserException(e.getMessage());
        }
    }


    public static String getHostUrl(HttpServletRequest request) {
        String scheme = request.getScheme(); // http or https
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();

        String hostUrl = scheme + "://" + serverName;

        // Append port if it's not the default port for the scheme
        if (("http".equals(scheme) && serverPort != 80)
                || ("https".equals(scheme) && serverPort != 443)) {
            hostUrl += ":" + serverPort;
        }

        return hostUrl;
    }


    public static <T> void checkRequiredFields(T dto, List<String> requiredFields) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        for (String field : requiredFields) {
            if (PropertyUtils.getProperty(dto, field) == null) {
                throw new IllegalArgumentException(String.format(ResponseMessages.FIELD_CANNOT_BE_NULL, field));
            }
        }
    }

    public static String sanitizeForLog(String input) {
        if (input == null) return "null";
        return input.replaceAll("[^a-zA-Z0-9-]", "_");
    }

}
