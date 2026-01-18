package com.sales.admin.controllers;


import com.sales.admin.services.PaginationService;
import com.sales.admin.services.UserService;
import com.sales.claims.AuthUser;
import com.sales.claims.SalesUser;
import com.sales.dto.*;
import com.sales.entities.User;
import com.sales.global.ConstantResponseKeys;
import com.sales.global.GlobalConstant;
import com.sales.jwtUtils.JwtToken;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for user authentication, management, and related operations")
public class UserController  {

    private final AuthenticationManager authenticationManager;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final PaginationService paginationService;
    private final JwtToken jwtToken;

    @PreAuthorize("hasAuthority('user.all')")
    @PostMapping("/{userType}/all")
    @Operation(summary = "Get all users by type", description = "Retrieves a paginated list of users based on user type with optional search filters")
    public ResponseEntity<Page<User>> getAllUsers(Authentication authentication,HttpServletRequest request,@RequestBody UserSearchFilters searchFilters, @PathVariable(required = true) String userType) {
        logger.info("authentication  authorities : {}",authentication.getAuthorities());
        logger.debug("Fetching all users of type: {}", userType);
        searchFilters.setUserType(userType);
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        Page<User> userPage = userService.getAllUser(searchFilters,loggedUser);
        return new ResponseEntity<>(userPage, HttpStatus.OK);
    }


    // Required params for login in swagger ui
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        content = @Content(schema = @Schema(example = """
                    {
                       "email" : "string",
                       "password" : "string"
                    }
                    """)
    ))
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates a user and returns a JWT token along with user details")
    public ResponseEntity<Map<String, Object>> login(@RequestBody UserDto userDetails) {
        logger.debug("Admin login attempt with email: {}", userDetails.getEmail());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userDetails.getEmail(),userDetails.getPassword()
        ));
        SalesUser user = (SalesUser) authentication.getPrincipal();
        Map<String, Object> responseObj = new HashMap<>();
        String message;
        if (user.isEnabled()) {
            message = ConstantResponseKeys.SUCCESS;
            Map<String, Object> paginations = paginationService.findUserPaginationsByUserId(user);
            responseObj.put(ConstantResponseKeys.TOKEN, GlobalConstant.AUTH_TOKEN_PREFIX + jwtToken.generateToken(user.getSlug()));
            responseObj.put("user", user);
            responseObj.put(ConstantResponseKeys.PAGINATIONS,paginations);
            responseObj.put(ConstantResponseKeys.STATUS, 200);
        } else {
            message =  "You are blocked by admin";
            responseObj.put(ConstantResponseKeys.STATUS, 401);
        }
        responseObj.put(ConstantResponseKeys.MESSAGE,message);
        return new ResponseEntity<>(responseObj, HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));
    }


    // Required params for otp login in swagger ui
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        content = @Content(schema = @Schema(example = """
                {
                   "email" : "string",
                   "password" : "(otp) string"
                }
                """)
    ))
    @PostMapping("/login/otp")
    public ResponseEntity<Map<String, Object>> findUserByOtp(@RequestBody UserDto userDetails) {
        logger.debug("Admin OTP login attempt with email: {}", userDetails.getEmail());
        Map<String, Object> responseObj = new HashMap<>();
        AuthUser user = userService.findUserByOtpAndEmail(userDetails);
        if (user == null) {
            responseObj.put(ConstantResponseKeys.MESSAGE, "Wrong otp password.");
            responseObj.put(ConstantResponseKeys.STATUS, 401);
        } else if (user.isEnabled()) {
            Map<String, Object> paginations = paginationService.findUserPaginationsByUserId(user);
            responseObj.put(ConstantResponseKeys.TOKEN, GlobalConstant.AUTH_TOKEN_PREFIX + jwtToken.generateToken(user.getSlug()));
            responseObj.put(ConstantResponseKeys.MESSAGE, "Successfully logged in.");
            responseObj.put("user", user);
            responseObj.put(ConstantResponseKeys.PAGINATIONS,paginations);
            responseObj.put(ConstantResponseKeys.STATUS, 200);
            userService.resetOtp(user.getUsername());
        } else {
            responseObj.put(ConstantResponseKeys.MESSAGE, "You are blocked by admin.");
            responseObj.put(ConstantResponseKeys.STATUS, 401);
        }
        return new ResponseEntity<>(responseObj, HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));
    }



    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        content = @Content(schema = @Schema(example = """
            {
               "email" : "string",
            }
            """)
    ))
    @PostMapping("sendOtp")
    public ResponseEntity<Map<String,Object>> sendOtp(@RequestBody UserDto userDto) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Sending OTP to email: {}", userDto.getEmail());
        Map<String,Object> responseObj = new HashMap<>();
        boolean sendOtp = userService.sendOtp(userDto);
        if(sendOtp)  {
            responseObj.put(ConstantResponseKeys.STATUS,200);
            responseObj.put(ConstantResponseKeys.MESSAGE, "Otp sent successfully");
        }else {
            responseObj.put(ConstantResponseKeys.STATUS,400);
            responseObj.put(ConstantResponseKeys.MESSAGE, "We facing some issue to send otp to this mail ->"+userDto.getEmail());
        }
        return  new ResponseEntity<>(responseObj,HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));
    }




    // For add and update user
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(schema = @Schema( description = "If you going to update must add slug",
            example = """
            {
                    "slug" : "(only during update) string",
                    "email" : "string",
                    "username" : "string",
                    "userType"  : "W|R|S|SA",
                    "contact" : "string",
                    "city" : "cityId",
                    "state" : "stateId",
                    "street" : "string",
                    "storeName" : "string",
                    "storeEmail" : "string",
                    "description" : "string",
                    "categoryId" : 0,
                    "subCategoryId"  : 0,
                    "zipCode" : "string",
                    "storePhone" : "string"
                }
            """)
    ))
    @PreAuthorize("hasAnyAuthority('user.add','user.edit','user.update')")
    @Transactional
    @PostMapping(value = {"/add", "/update"})
    public ResponseEntity<Map<String, Object>> register(Authentication authentication,HttpServletRequest request, @RequestBody UserDto userDto) throws Exception {
        logger.debug("Registering or updating user with email: {}", userDto.getEmail());
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        String path = request.getRequestURI();
        Map<String,Object> responseObj = userService.createOrUpdateUser(userDto, loggedUser,path);
        return new ResponseEntity<>(responseObj, HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));

    }

    @PreAuthorize("hasAuthority('user.detail')")
    @GetMapping("/detail/{slug}")
    public ResponseEntity<Map<String, Object>> getDetailUser(Authentication authentication,HttpServletRequest request,@PathVariable String slug) {
        logger.debug("Fetching details for user with slug: {}", slug);
        Map<String,Object> responseObj = new HashMap<>();
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        User user = userService.getUserDetail(slug,loggedUser);
        if (user != null) {
            responseObj.put(ConstantResponseKeys.RES, user);
            responseObj.put(ConstantResponseKeys.STATUS, 200);
        } else {
            responseObj.put(ConstantResponseKeys.MESSAGE, "User not found.");
            responseObj.put(ConstantResponseKeys.STATUS, 404);
        }
        return new ResponseEntity<>(responseObj, HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));
    }

    @Transactional
    @PreAuthorize("hasAuthority('user.delete')")
    @PostMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteUserBySlug(Authentication authentication,HttpServletRequest request, @RequestBody DeleteDto deleteDto) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Deleting user with slug: {}", deleteDto.getSlug());
        Map<String,Object> responseObj = new HashMap<>();
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        int isUpdated = userService.deleteUserBySlug(deleteDto,loggedUser);
        if (isUpdated > 0) {
            responseObj.put(ConstantResponseKeys.MESSAGE, "User has been successfully deleted.");
            responseObj.put(ConstantResponseKeys.STATUS, 200);
        } else {
            responseObj.put(ConstantResponseKeys.MESSAGE, "No user found to delete");
            responseObj.put(ConstantResponseKeys.STATUS, 404);
        }
        return new ResponseEntity<>(responseObj, HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));
    }

    @PreAuthorize("hasAuthority('user.reset.password')")
    @Transactional
    @PostMapping("/password")
    public ResponseEntity<Map<String, Object>> resetUserPasswordBySlug(Authentication authentication,HttpServletRequest request ,@RequestBody PasswordDto passwordDto) {
        logger.debug("Resetting password for user with slug: {}", passwordDto.getSlug());
        Map<String,Object> responseObj = new HashMap<>();
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        int isUpdated = userService.resetPasswordByUserSlug(passwordDto,loggedUser);
        if (isUpdated > 0 || loggedUser.getId() == GlobalConstant.suId) {
            responseObj.put(ConstantResponseKeys.MESSAGE, "User password has been successfully updated.");
            responseObj.put(ConstantResponseKeys.STATUS, 200);
        } else {
            responseObj.put(ConstantResponseKeys.MESSAGE, "There is nothing to update.recheck you parameters");
            responseObj.put(ConstantResponseKeys.STATUS, 400);
        }
        return new ResponseEntity<>(responseObj, HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));
    }


    @PreAuthorize("hasAuthority('user.status')")
    @PostMapping("/status")
    public ResponseEntity<Map<String, Object>> stockSlug(Authentication authentication,HttpServletRequest request,@RequestBody StatusDto statusDto) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Updating status for user with slug: {}", statusDto.getSlug());
        Map<String,Object> responseObj = new HashMap<>();
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        int isUpdated = userService.updateStatusBySlug(statusDto,loggedUser);
        if (isUpdated > 0) {
            responseObj.put(ConstantResponseKeys.MESSAGE, "User's status updated successfully.");
            responseObj.put(ConstantResponseKeys.STATUS, 200);
        } else {
            responseObj.put(ConstantResponseKeys.MESSAGE, "No user found to update.");
            responseObj.put(ConstantResponseKeys.STATUS, 404);
        }
        return new ResponseEntity<>(responseObj, HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));
    }





    @Transactional
    @PreAuthorize("hasAuthority('user.profile.edit')")
    @PostMapping("/update_profile/{slug}")
    public ResponseEntity<Map<String, Object>> updateProfileImage(Authentication authentication,HttpServletRequest request, @RequestPart MultipartFile profileImage, @PathVariable String slug ) {
        logger.debug("Updating profile image for user with slug: {}", slug);
        Map<String,Object> responseObj = new HashMap<>();
        try {
            AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
            String  imageName = userService.updateProfileImage(profileImage,slug,loggedUser);
            if(imageName!=null) {
                responseObj.put(ConstantResponseKeys.STATUS , 200);
                responseObj.put("imageName",imageName);
                responseObj.put(ConstantResponseKeys.MESSAGE , "Profile image successfully updated");
            }else {
                responseObj.put(ConstantResponseKeys.STATUS  , 406);
                responseObj.put(ConstantResponseKeys.MESSAGE , "Not a valid profile image");
            }
        } catch (Exception e) {
            responseObj.put(ConstantResponseKeys.MESSAGE, e.getMessage());
            responseObj.put(ConstantResponseKeys.STATUS, 500);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return new ResponseEntity<>(responseObj, HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));

    }

    @Value("${profile.get}")
    String filePath;

    @GetMapping("/profile/{slug}/{filename}")
    public ResponseEntity<Resource> getFile(@PathVariable(required = true) String filename ,@PathVariable String slug) throws Exception {
        logger.debug("Fetching profile image: {} for user with slug: {}", filename, slug);
        Path filePathObj = Paths.get(filePath);
        Path filePathDynamic = filePathObj.resolve(slug).normalize();
        Path path = filePathDynamic.resolve(filename).normalize();
        Resource resource = new UrlResource(path.toUri());
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(resource);
    }

    @PreAuthorize("hasAuthority('user.groups')")
    @GetMapping("/groups/{slug}")
    public ResponseEntity<Map<String,Object>> getUserGroupsIdsBySlug(HttpServletRequest request,@PathVariable String slug){
        logger.debug("Fetching group IDs for user with slug: {}", slug);
        Map<String,Object> responseObj = new HashMap<>();
        List<Integer> groupsIds = userService.getUserGroupsIdBySlug(slug);
        if (!groupsIds.isEmpty()) {
            responseObj.put("content", groupsIds);
            responseObj.put(ConstantResponseKeys.STATUS, 200);
        } else {
            responseObj.put(ConstantResponseKeys.MESSAGE, "There is no groups.");
            responseObj.put(ConstantResponseKeys.STATUS, 400);
        }
        return new ResponseEntity<>(responseObj, HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));
    }



    @PreAuthorize("hasAuthority('wholesaler.permission')")
    @GetMapping("wholesale/permissions/{slug}")
    public ResponseEntity<Map<String,Object>> getAllAssignedPermissionsForWholesaler(HttpServletRequest request,@PathVariable String slug){
        logger.debug("Fetching all assigned permissions for wholesaler with slug: {}", slug);
        Map<String, Object> wholesalerAllPermissions = userService.getWholesalerAllPermissions();
        List<Integer> permissions =  userService.getWholesalerAllAssignedPermissions(slug);
        Map<String,Object> responseObj = new HashMap<>();
        if (permissions != null ) {
            responseObj.put("assigned", permissions);
            responseObj.put("allPermissions", wholesalerAllPermissions);
            responseObj.put(ConstantResponseKeys.STATUS, 200);
        } else {
            responseObj.put(ConstantResponseKeys.MESSAGE, "There is no permission for this user.");
            responseObj.put(ConstantResponseKeys.STATUS, 400);
        }
        return new ResponseEntity<>(responseObj,  HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));
    }


    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(schema = @Schema(example = """
            {
               "slug" : "string",
               "userType" : "string",
               "storePermissions" : "[permissionIds list]"
            }
            """)
    ))
    @Transactional
    @PreAuthorize("hasAuthority('wholesaler.permission.update')")
    @PostMapping("wholesaler/permissions/update")
    public ResponseEntity<Map<String,Object>> updateWholesalerPermissions(Authentication authentication,HttpServletRequest request, @RequestBody UserDto userDto) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Updating permissions for wholesaler with slug: {}", userDto.getSlug());
        Map<String,Object> response= userService.updateWholesalerPermissions(userDto);
        return new ResponseEntity<>(response, HttpStatus.valueOf((Integer) response.get(ConstantResponseKeys.STATUS )));
    }


}




