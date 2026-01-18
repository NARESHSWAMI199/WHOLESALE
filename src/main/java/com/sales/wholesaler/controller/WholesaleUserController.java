package com.sales.wholesaler.controller;


import com.sales.cachemanager.services.UserCacheService;
import com.sales.claims.AuthUser;
import com.sales.claims.SalesUser;
import com.sales.dto.PasswordDto;
import com.sales.dto.UserDto;
import com.sales.dto.UserSearchFilters;
import com.sales.entities.Store;
import com.sales.entities.User;
import com.sales.global.ConstantResponseKeys;
import com.sales.global.GlobalConstant;
import com.sales.jwtUtils.JwtToken;
import com.sales.utils.Utils;
import com.sales.wholesaler.services.WholesalePaginationService;
import com.sales.wholesaler.services.WholesaleStoreService;
import com.sales.wholesaler.services.WholesaleUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("wholesale/auth")
@RequiredArgsConstructor
@Tag(name = "Wholesale User Authentication and Management", description = "APIs for wholesaler user authentication, profile management, and user operations")
public class WholesaleUserController  {

    private final WholesaleUserService wholesaleUserService;
    private final WholesaleStoreService wholesaleStoreService;
    private final WholesalePaginationService wholesalePaginationService;
    private final JwtToken jwtToken;
    private static final Logger logger = LoggerFactory.getLogger(WholesaleUserController.class);
    private final UserCacheService userCacheService;

    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(
            example = """
                    {
                        "email" : "string",
                        "password" : "string"
                    }
                    """
    )))
    @PostMapping("/login")
    @Operation(summary = "Login wholesaler", description = "Authenticates a wholesaler user with email and password credentials")
    public ResponseEntity<Map<String, Object>> loginWholesaler(@RequestBody Map<String,String> param) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Starting loginWholesaler method");
        Map<String, Object> responseObj = new HashMap<>();
        User user = wholesaleUserService.findByEmailAndPassword(param);
        String message;
        if (user == null) {
            message = "invalid credentials.";
            responseObj.put(ConstantResponseKeys.STATUS, 401);
        }else if(!Utils.isEmpty(user.getOtp())) {
            message = "User exist but not verified. You can login via otp.";
            responseObj.put(ConstantResponseKeys.STATUS, 401);
        }else if (user.getStatus().equalsIgnoreCase("A")) {
            message = ConstantResponseKeys.SUCCESS;
            responseObj.put(ConstantResponseKeys.TOKEN, GlobalConstant.AUTH_TOKEN_PREFIX + jwtToken.generateToken(user.getSlug()));
            Store storeDetails = wholesaleStoreService.getStoreByUserId(user.getId());
            Map<String,Object> paginationsObj = wholesalePaginationService.findUserPaginationsByUserId(new SalesUser(user));
            responseObj.put("user", user);
            responseObj.put(ConstantResponseKeys.STORE, storeDetails);
            responseObj.put(ConstantResponseKeys.PAGINATIONS,paginationsObj);
            responseObj.put(ConstantResponseKeys.STATUS, 200);
        }else {
            message = "You are blocked by admin.";
            responseObj.put(ConstantResponseKeys.STATUS, 401);
        }
        responseObj.put(ConstantResponseKeys.MESSAGE, message);
        logger.debug("Completed loginWholesaler method.");
        return new ResponseEntity<>(responseObj, HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));
    }



    @PostMapping("/login/otp")
    @Operation(summary = "Login via OTP", description = "Authenticates a wholesaler user using OTP verification")
    public ResponseEntity<Map<String, Object>> loginUserViaOtp (@RequestBody UserDto userDetails) {
        logger.debug("Starting loginUserViaOtp method");
        Map<String, Object> responseObj = new HashMap<>();
        User user = wholesaleUserService.findUserByOtpAndEmail(userDetails);
        if (user == null) {
            responseObj.put(ConstantResponseKeys.MESSAGE, "Wrong otp password.");
            responseObj.put(ConstantResponseKeys.STATUS, 401);
        } else if (user.getStatus().equalsIgnoreCase("A")) {
            responseObj.put(ConstantResponseKeys.TOKEN, GlobalConstant.AUTH_TOKEN_PREFIX + jwtToken.generateToken(user.getSlug()));
            Store store = wholesaleStoreService.getStoreByUserId(user.getId());
            Map<String,Object> paginations = wholesalePaginationService.findUserPaginationsByUserId(new SalesUser(user));
            responseObj.put(ConstantResponseKeys.MESSAGE, ConstantResponseKeys.SUCCESS);
            responseObj.put("user", user);
            responseObj.put(ConstantResponseKeys.STORE, store);
            responseObj.put(ConstantResponseKeys.PAGINATIONS,paginations);
            responseObj.put(ConstantResponseKeys.STATUS, 200);
            wholesaleUserService.resetOtp(user.getEmail());
        } else {
            responseObj.put(ConstantResponseKeys.MESSAGE, "You are blocked by admin");
            responseObj.put(ConstantResponseKeys.STATUS, 401);
        }
        logger.debug("Completed loginUserViaOtp method");
        return new ResponseEntity<>(responseObj, HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));
    }


    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(
            example = """
                    {
                        "slug" : "string",
                        "password" : "otp-password"
                    }
                    """
    )))

    @PostMapping("validate-otp")
    @Operation(summary = "Validate OTP", description = "Validates the OTP for user authentication")
    public ResponseEntity<Map<String, Object>> validateUserOtp(@RequestBody UserDto userDetails) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Starting validateUserOtp method");
        Map<String, Object> responseObj = new HashMap<>();
        User user = wholesaleUserService.findUserByOtpAndSlug(userDetails);
        if (user == null) {
            responseObj.put(ConstantResponseKeys.MESSAGE, "Wrong otp password.");
            responseObj.put(ConstantResponseKeys.STATUS, 401);
        } else if (user.getStatus().equalsIgnoreCase("A")) {
            Store store = wholesaleStoreService.getStoreByUserId(user.getId());
            Map<String,Object> paginations = wholesalePaginationService.findUserPaginationsByUserId(new SalesUser(user));
            responseObj.put(ConstantResponseKeys.TOKEN, GlobalConstant.AUTH_TOKEN_PREFIX + jwtToken.generateToken(user.getSlug()));
            responseObj.put(ConstantResponseKeys.MESSAGE, ConstantResponseKeys.SUCCESS);
            responseObj.put("user", user);
            responseObj.put(ConstantResponseKeys.STORE, store);
            responseObj.put(ConstantResponseKeys.PAGINATIONS,paginations);
            responseObj.put(ConstantResponseKeys.STATUS, 200);
            // setting blank otp
            wholesaleUserService.resetOtp(user.getEmail());
        } else {
            responseObj.put(ConstantResponseKeys.MESSAGE, "You are blocked by admin");
            responseObj.put(ConstantResponseKeys.STATUS, 401);
        }
        logger.debug("Completed validateUserOtp method");
        return new ResponseEntity<>(responseObj, HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));
    }


    @PostMapping("sendOtp")
    @Operation(summary = "Send OTP", description = "Sends an OTP to the user's email for verification")
    public ResponseEntity<Map<String,Object>> sendOtp(HttpServletRequest request, @RequestBody UserDto userDto){
        logger.debug("Starting sendOtp method");
        Map<String,Object> responseObj = new HashMap<>();
        boolean sendOtp = wholesaleUserService.sendOtp(userDto);
        if(sendOtp)  {
            responseObj.put(ConstantResponseKeys.STATUS,200);
            responseObj.put(ConstantResponseKeys.MESSAGE, "Otp sent successfully");
        }else {
            responseObj.put(ConstantResponseKeys.STATUS,400);
            responseObj.put(ConstantResponseKeys.MESSAGE, "We facing some issue to send otp to this mail ->"+userDto.getEmail());
        }
        logger.debug("Completed sendOtp method");
        return  new ResponseEntity<>(responseObj,HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));
    }

    // For add and update user
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(schema = @Schema( description = "If you going to update must add slug",
                    example = """
                {
                    "slug" : "string",
                    "email" : "string",
                    "username" : "string",
                    "contact" : "string"
                }
            """)
            ))
    @PostMapping(value = {"/update"})
    @Operation(summary = "Update user profile", description = "Updates the profile information of the authenticated wholesaler user")
    public ResponseEntity<Map<String, Object>> updateAuth(Authentication authentication,HttpServletRequest request, @RequestBody UserDto userDto) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Starting updateAuth method");
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        Map<String,Object> responseObj = wholesaleUserService.updateUserProfile(userDto, loggedUser);
        logger.debug("Completed updateAuth method");
        return new ResponseEntity<>(responseObj, HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));

    }

    @GetMapping(value = {"/detail","/detail/{slug}"})
    @Operation(summary = "Get user details", description = "Retrieves detailed information for the authenticated user or a specific user by slug")
    public ResponseEntity<Map<String, Object>> getDetailUser(@PathVariable(required = false) String slug, HttpServletRequest request) {
        logger.debug("Starting getDetailUser method");
        Map<String,Object> responseObj = new HashMap<>();
        AuthUser user = null;
        if(slug == null){
            user = Utils.getUserFromRequest(request,jwtToken,wholesaleUserService);
        }else {
            user = new SalesUser(wholesaleUserService.findUserBySlug(slug));
        }
        if(slug == null){
            Store store = wholesaleStoreService.getStoreByUserSlug(user.getId());
            responseObj.put(ConstantResponseKeys.STORE, store);
        }
        responseObj.put("user", user);
        responseObj.put(ConstantResponseKeys.STATUS, 200);
        logger.debug("Completed getDetailUser method");
        return new ResponseEntity<>(responseObj, HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));
    }



    @Transactional
    @PostMapping("/password")
    @PreAuthorize("hasAuthority('wholesale.password.reset')")
    @Operation(summary = "Reset user password", description = "Resets the password for the authenticated wholesaler user")
    public ResponseEntity<Map<String, Object>> resetUserPasswordBySlug(Authentication authentication,HttpServletRequest request ,@RequestBody PasswordDto passwordDto) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Starting resetUserPasswordBySlug method");
        Map<String,Object> responseObj = new HashMap<>();
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        User updatedUser = wholesaleUserService.resetPasswordByUserSlug(passwordDto,loggedUser);
        responseObj.put(ConstantResponseKeys.RES,updatedUser);
        responseObj.put(ConstantResponseKeys.MESSAGE, "User password has been successfully updated.");
        responseObj.put(ConstantResponseKeys.STATUS, 200);
        logger.debug("Completed resetUserPasswordBySlug method");
        return new ResponseEntity<>(responseObj, HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));
    }



    @PostMapping("/update_profile")
    @PreAuthorize("hasAnyAuthority('wholesale.profile.update','wholesale.profile.edit')")
    @Operation(summary = "Update profile image", description = "Updates the profile image of the authenticated wholesaler user")
    public ResponseEntity<Map<String, Object>> updateProfileImage(Authentication authentication,HttpServletRequest request, @RequestPart MultipartFile profileImage) throws IOException {
        logger.debug("Starting updateProfileImage method");
        Map<String,Object> responseObj = new HashMap<>();
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        String  imageName = wholesaleUserService.updateProfileImage(profileImage,loggedUser);
        if(imageName!=null) {
            responseObj.put("imageName",imageName);
            responseObj.put(ConstantResponseKeys.MESSAGE , "Profile image successfully updated");
            responseObj.put(ConstantResponseKeys.STATUS , 200);
        }else {
            responseObj.put(ConstantResponseKeys.STATUS , 406);
            responseObj.put(ConstantResponseKeys.MESSAGE , "Not a valid profile image");
        }
        logger.debug("Completed updateProfileImage method");
        return new ResponseEntity<>(responseObj, HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));

    }

    @Value("${profile.get}")
    String filePath;

    @GetMapping("/profile/{slug}/{filename}")
    @Operation(summary = "Get profile image", description = "Retrieves the profile image file for a specific user")
    public ResponseEntity<Resource> getFile(@PathVariable(required = true) String filename , @PathVariable String slug) throws MalformedURLException {
        Path filePathFolder = Paths.get(filePath);
        Path userSlug = filePathFolder.resolve(slug).normalize();
        Path path = userSlug.resolve(filename).normalize();
        Resource resource = new UrlResource(path.toUri());
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(resource);
    }


    // For add and update user
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(schema = @Schema( description = "If you going to update must add slug",
                example = """
                {
                    "email" : "string",
                    "username" : "string",
                    "password' : "string",
                    "contact" : "string"
                }
            """)
    ))
    @PostMapping(value = {"add","register"})
    @Operation(summary = "Add new user", description = "Creates a new wholesaler user account")
    public ResponseEntity<Map<String,Object>> addNewUser(@RequestBody UserDto userDto) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Starting addNewUser method");
        Map<String,Object> result = new HashMap<>();
        User insertedUser = wholesaleUserService.addNewUser(userDto);
        result.put("user",insertedUser);
        result.put(ConstantResponseKeys.MESSAGE, "User created successfully");
        result.put(ConstantResponseKeys.STATUS, 201);
        logger.debug("Completed addNewUser method");
        return new ResponseEntity<>(result,HttpStatus.valueOf((Integer) result.get(ConstantResponseKeys.STATUS)));
    }


    @GetMapping("last-seen")
    @Operation(summary = "Update last seen", description = "Updates the last seen timestamp for the authenticated user")
    public ResponseEntity<Map<String,Object>> updateUserLastSeen(Authentication authentication,HttpServletRequest request){
        logger.debug("Starting updateUserLastSeen method");
        Map<String,Object> result = new HashMap<>();
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        int isUpdated = wholesaleUserService.updateLastSeen(loggedUser);

        User user = userCacheService.getCacheUser(loggedUser.getSlug());
        user.setOnline(false);
        wholesaleUserService.updateLastSeen(loggedUser);
        GlobalConstant.onlineUsers.put(loggedUser.getSlug(), user);
        if(isUpdated > 0){
            result.put(ConstantResponseKeys.MESSAGE, "User's last seen successfully updated.");
            result.put(ConstantResponseKeys.STATUS, 200);
        }else{
            result.put(ConstantResponseKeys.MESSAGE,"Something went wrong during updating last seen of user");
            result.put(ConstantResponseKeys.STATUS,500);
        }
        logger.debug("Completed updateUserLastSeen method");
        return new ResponseEntity<>(result,HttpStatus.valueOf((Integer) result.get(ConstantResponseKeys.STATUS)));

    }



    /** Returning a list of users where users are retailer and wholesaler only for chat purpose.*/

    @PostMapping("chat/users")
    @Operation(summary = "Get chat users", description = "Retrieves a list of users (retailers and wholesalers) for chat functionality")
    public ResponseEntity<Page<User>> getAllChatUser(Authentication authentication,HttpServletRequest request, @RequestBody UserSearchFilters userSearchFilters){
        logger.debug("Starting getAllChatUser method");
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        Page<User> allUsers = wholesaleUserService.getAllUsers(userSearchFilters, loggedUser);
        logger.debug("Completed getAllChatUser method");
        return new ResponseEntity<>(allUsers,HttpStatus.OK);
    }



}




