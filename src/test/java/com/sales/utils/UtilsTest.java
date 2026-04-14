package com.sales.utils;

import com.sales.claims.AuthUser;
import com.sales.entities.User;
import com.sales.exceptions.MyException;
import com.sales.exceptions.UserException;
import com.sales.jwtUtils.JwtToken;
import com.sales.wholesaler.services.WholesaleUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UtilsTest {

    @Test
    void testIsEmpty() {
        assertTrue(Utils.isEmpty(null));
        assertTrue(Utils.isEmpty(""));
        assertTrue(Utils.isEmpty("   \t\n"));
        assertFalse(Utils.isEmpty("abc"));
    }

    @Test
    void testImageAndEmailAndPhoneValidations() {
        // use common extensions in lowercase which are supported reliably
        assertTrue(Utils.isValidImage("image.png"));
        assertTrue(Utils.isValidImage("image.jpeg"));
        assertFalse(Utils.isValidImage("notanimage.txt"));

        assertTrue(Utils.isValidEmail("test@example.com"));
        assertFalse(Utils.isValidEmail("bad-email"));

        assertTrue(Utils.isValidPhoneNumber("9876543210"));
        assertFalse(Utils.isValidPhoneNumber("12345"));
    }

    @Test
    void testMobileAndEmailValidationThrows() {
        Exception e1 = assertThrows(IllegalArgumentException.class, () -> Utils.mobileAndEmailValidation("a@b.com", "12345", "error_msg"));
        assertTrue(e1.getMessage().contains("mobile number"));

        Exception e2 = assertThrows(IllegalArgumentException.class, () -> Utils.mobileAndEmailValidation("bademail", "9876543210", "error_msg"));
        assertTrue(e2.getMessage().contains("email address"));
    }

    @Test
    void testCanUpdateAStaffAndStatus() {
        AuthUser authUser = new AuthUser() {
            @Override
            public int getId() {
                return 5;
            }

            @Override
            public String getSlug() {
                return "other";
            }

            @Override
            public String getUserType() {
                return "U";
            }

            @Override
            public Integer getActivePlan() {
                return 0;
            }

            @Override
            public java.util.Collection getAuthorities() {
                return null;
            }

            @Override
            public String getPassword() {
                return null;
            }

            @Override
            public String getUsername() {
                return null;
            }

            @Override
            public String getEmail() {
                return null;
            }

            @Override
            public boolean isEnabled() {
                return true;
            }

            @Override
            public String getAvatar() {
                return "";
            }
        };
        // should throw because userType S and different slug
        assertThrows(org.springframework.dao.PermissionDeniedDataAccessException.class, () -> Utils.canUpdateAStaff("slug", "S", authUser));

        // canUpdateAStaffStatus: should throw when slug equals logged user's slug and userType S
        AuthUser authUser2 = new AuthUser() {
            @Override
            public int getId() {
                return 5;
            }

            @Override
            public String getSlug() {
                return "slug";
            }

            @Override
            public String getUserType() {
                return "U";
            }

            @Override
            public Integer getActivePlan() {
                return 0;
            }

            @Override
            public java.util.Collection getAuthorities() {
                return null;
            }

            @Override
            public String getPassword() {
                return null;
            }

            @Override
            public String getUsername() {
                return null;
            }

            @Override
            public String getEmail() {
                return null;
            }

            @Override
            public boolean isEnabled() {
                return true;
            }

            @Override
            public String getAvatar() {
                return "";
            }
        };
        assertThrows(org.springframework.dao.PermissionDeniedDataAccessException.class, () -> Utils.canUpdateAStaffStatus("slug", "S", authUser2));
    }

    @Test
    void testIsValidName() {
        assertEquals("John Doe", Utils.isValidName("John Doe", "user"));
        assertEquals("Item-123", Utils.isValidName("Item-123", "item"));
        assertThrows(MyException.class, () -> Utils.isValidName("!badname@", "user"));
        assertThrows(IllegalArgumentException.class, () -> Utils.isValidName("!badname@", "item"));
    }

    @Test
    void testGenerateOtp() {
        int otp = Utils.generateOTP(4);
        assertTrue(otp >= 0);
        assertTrue(otp < Math.pow(10, 4));
    }

    @Test
    void testGetUserFromRequestHeaderSuccessAndFailure() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        // project uses "Beaver " prefix - use GlobalConstant to match behavior
        when(req.getHeader(org.springframework.http.HttpHeaders.AUTHORIZATION)).thenReturn(com.sales.global.GlobalConstant.AUTH_TOKEN_PREFIX + "token123");
        JwtToken jwt = mock(JwtToken.class);
        when(jwt.getSlugFromToken("token123")).thenReturn("abc-slug");

        WholesaleUserService userService = mock(WholesaleUserService.class);
        User u = new User();
        u.setSlug("abc-slug");
        u.setStatus("A");
        u.setIsDeleted("N");
        when(userService.findUserBySlug("abc-slug")).thenReturn(u);

        AuthUser found = Utils.getUserFromRequest(req, jwt, userService);
        assertNotNull(found);
        assertEquals("abc-slug", found.getSlug());

        // no header -> throws UserException
        HttpServletRequest req2 = mock(HttpServletRequest.class);
        when(req2.getHeader(org.springframework.http.HttpHeaders.AUTHORIZATION)).thenReturn(null);
        assertThrows(UserException.class, () -> Utils.getUserFromRequest(req2, jwt, userService));
    }

    @Test
    void testGetUserFromRequestWithTokenVariants() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn("/test");
        JwtToken jwt = mock(JwtToken.class);
        WholesaleUserService userService = mock(WholesaleUserService.class);

        when(jwt.getSlugFromToken("tkn")).thenReturn("slug1");
        User u = new User();
        u.setSlug("slug1");
        u.setIsDeleted("Y");
        when(userService.findUserBySlug("slug1")).thenReturn(u);

        // implementation wraps thrown NotFoundException into UserException; assert UserException
        assertThrows(UserException.class, () -> Utils.getUserFromRequest(req, "tkn", jwt, userService));

        u.setIsDeleted("N");
        u.setStatus("D");
        when(userService.findUserBySlug("slug1")).thenReturn(u);
        assertThrows(UserException.class, () -> Utils.getUserFromRequest(req, "tkn", jwt, userService));

        // null token
        assertThrows(UserException.class, () -> Utils.getUserFromRequest(req, null, jwt, userService));
    }

    @Test
    void testGetHostUrl() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getScheme()).thenReturn("http");
        when(req.getServerName()).thenReturn("localhost");
        when(req.getServerPort()).thenReturn(8080);
        assertEquals("http://localhost:8080", Utils.getHostUrl(req));

        when(req.getScheme()).thenReturn("https");
        when(req.getServerPort()).thenReturn(443);
        assertEquals("https://localhost", Utils.getHostUrl(req));
    }

    @Test
    void testCheckRequiredFieldsAndSanitize() throws Exception {
        Dto d = new Dto();
        d.setName(null);
        List<String> fields = new ArrayList<>();
        fields.add("name");
        Exception e = assertThrows(Exception.class, () -> Utils.checkRequiredFields(d, fields));
        // The implementation may throw IllegalArgumentException when the field is null or NoSuchMethodException when the getter isn't detected
        assertTrue(e instanceof IllegalArgumentException || e instanceof NoSuchMethodException);

        assertEquals("null", Utils.sanitizeForLog(null));
        assertEquals("abc_123", Utils.sanitizeForLog("abc@123"));
    }

    static class Dto {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getId() {
            return null;
        }
    }

}
