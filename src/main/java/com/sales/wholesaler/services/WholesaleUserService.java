package com.sales.wholesaler.services;


import com.sales.cachemanager.services.UserCacheService;
import com.sales.claims.AuthUser;
import com.sales.request.*;
import com.sales.entities.ServicePlan;
import com.sales.entities.SupportEmail;
import com.sales.entities.User;
import com.sales.exceptions.MyException;
import com.sales.global.ConstantResponseKeys;
import com.sales.global.GlobalConstant;
import com.sales.utils.SecureAesUtil;
import com.sales.utils.Utils;
import com.sales.wholesaler.dto.WholesaleUserDto;
import com.sales.wholesaler.mapper.WholesaleUserMapper;
import com.sales.wholesaler.repository.WholesaleServicePlanRepository;
import com.sales.wholesaler.repository.WholesaleSupportEmailsRepository;
import com.sales.wholesaler.repository.WholesaleUserHbRepository;
import com.sales.wholesaler.repository.WholesaleUserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static com.sales.helpers.PaginationHelper.getPageable;
import static com.sales.specifications.UserSpecifications.*;


@Service
@RequiredArgsConstructor
public class WholesaleUserService  {

    private static final Logger logger = LoggerFactory.getLogger(WholesaleUserService.class);
    private final WholesaleServicePlanService wholesaleServicePlanService;
    private final WholesalePaginationService wholesalePaginationService;
    private final UserCacheService userCacheService;
    private final WholesaleUserRepository wholesaleUserRepository;
    private final WholesaleUserHbRepository wholesaleUserHbRepository;
    private final WholesaleSupportEmailsRepository wholesaleSupportEmailsRepository;
    private final WholesaleServicePlanRepository wholesaleServicePlanRepository;
    private final WholesaleUserMapper wholesaleUserMapper;

    @Value("${profile.absolute}")
    String profilePath;

    @Value("${aes.key}")
    String key;


    @Value("${default.password}")
    String password;


    @Transactional
    public User findByEmailAndPassword(Map<String,String> param) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Starting findByEmailAndPassword method with param: {}", param);
        // Validating required fields. If their we found any required field is null, this will throw an Exception
        Utils.checkRequiredFields(param,List.of("email","password"));

        String email = param.get("email");
        String password = param.get("password");
        User user = wholesaleUserRepository.findByEmailAndPassword(email,password);
        logger.debug("Completed findByEmailAndPassword method");
        return user;
    }

    public User findUserByOtpAndSlug(UserRequest userRequest) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Starting findUserByOtpAndSlug method with userRequest: {}", userRequest);
        // Validating required fields. If their we found any required field is null, this will throw an Exception
        Utils.checkRequiredFields(userRequest,List.of("slug","password"));
        User user = wholesaleUserRepository.findUserByOtpAndSlug(userRequest.getSlug(),userRequest.getPassword());
        logger.debug("Completed findUserByOtpAndSlug method");
        return user;
    }

    public User findUserByOtpAndEmail(UserRequest userRequest) {
        logger.debug("Starting findUserByOtpAndEmail method with userRequest: {}", userRequest);
        User user = wholesaleUserRepository.findUserByOtpAndEmail(userRequest.getEmail(),userRequest.getPassword());
        logger.debug("Completed findUserByOtpAndEmail method");
        return user;
    }

    public void resetOtp(String email){
        logger.debug("Starting resetOtp method with email: {}", email);
        wholesaleUserHbRepository.updateOtp(email,"");
        logger.debug("Completed resetOtp method");
    }

    public boolean sendOtp(UserRequest userRequest){
        logger.debug("Starting sendOtp method with userRequest: {}", userRequest);
        boolean sent = false;
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com"); // Replace it with your mail server
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        User user = null;
        if(userRequest.getEmail() == null){
            user = wholesaleUserRepository.findUserBySlug(userRequest.getSlug());
        }else{
            user = wholesaleUserRepository.findUserByEmail(userRequest.getEmail());
        }
        if (user == null) return  false;

        String recipient = user.getEmail();

        SupportEmail supportEmail =  wholesaleSupportEmailsRepository.findSupportEmailBySupportType("SUPPORT");
        if(Objects.isNull(supportEmail)) {
            throw new InternalError("Support email is not found. please contact administrator.");
        }
        String sender = supportEmail.getEmail();
        String pKey = SecureAesUtil.decrypt(new String(Base64.getDecoder().decode(supportEmail.getPasswordKey())), key);
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication()
            {
                return new PasswordAuthentication(sender, pKey);
            }
        });
        try
        {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            String otp = String.valueOf(Utils.generateOTP(6));
            String subject = "Subject: Otp-in to Receive Updates from Swami Sales";
            message.setSubject(subject);
            String body = "Dear "+user.getUsername()+",<br/>" +
                    "<br/>" +
                    "You recently requested a login otp for your Swami Sales account. <br/>" +
                    "<br/>" +
                    "Your one-time password (OTP) is: <b>"+otp+"</b><br/>" +
                    "<br/>" +
                    "Please use this OTP to verify your identity and complete the password reset process. <br/>" +
                    "<br/>" +
                    "<b>Important:</b><br/>" +
                    "<br/>" +
                    "* Do not share this OTP with anyone.<br/>" +
                    "* If you did not request this OTP, please ignore this email.<br/>" +
                    "<br/>" +
                    "If you have any issues or require further assistance, please contact our customer support team at support@swamisales.com.\n" +
                    "<br/>" +
                    "Thank you,<br/>" +
                    "The Swami Sales Team<br/>";
            message.setContent(body, "text/html; charset=utf-8");
            Transport.send(message);
            wholesaleUserHbRepository.updateOtp(user.getEmail(),otp);
            sent = true;
        }
        catch (MessagingException mex)
        {
            logger.error("Fetching Exception : {} ",mex.getMessage());
        }
        logger.debug("Completed sendOtp method");
        return  sent;
    }

    public User findUserBySlug(String slug){
        logger.debug("Starting findUserBySlug method with slug: {}", slug);
        User user = wholesaleUserRepository.findUserBySlug(slug);
        logger.debug("Completed findUserBySlug method");
        return user;
    }

    public StoreCreationRequest userDtoToStoreDto(UserRequest userRequest) {
        logger.debug("Starting userDtoToStoreDto method with userRequest: {}", userRequest);
        StoreCreationRequest storeCreationRequest = new StoreCreationRequest();
        storeCreationRequest.setStoreName(userRequest.getStoreName());
        storeCreationRequest.setStoreEmail(userRequest.getStoreEmail());
        storeCreationRequest.setDescription(userRequest.getDescription());
        storeCreationRequest.setCity(userRequest.getCity());
        storeCreationRequest.setState(userRequest.getState());
        storeCreationRequest.setStorePhone(userRequest.getStorePhone());
        logger.debug("Completed userDtoToStoreDto method");
        return storeCreationRequest;
    }

    public Map<String, Object> updateUserProfile(UserRequest userRequest, AuthUser loggedUser) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Starting updateUserProfile method with userRequest: {}, loggedUser: {}", userRequest, loggedUser);
        // Validating required fields. If there we found any required field is null, this will throw an Exception
        Utils.checkRequiredFields(userRequest,List.of("slug","username","email","contact"));

        Map<String, Object> responseObj = new HashMap<>();

        Utils.mobileAndEmailValidation(
                userRequest.getEmail(),
                userRequest.getContact(),
                "Not a valid user's _ recheck your and user's _."
        );

        String username = Utils.isValidName( userRequest.getUsername(),"user");
        userRequest.setUsername(username);
        int isUpdated = updateUser(userRequest, loggedUser); // Update operation
        if (isUpdated > 0) {
            //Evict wholesaler from redis
            deleteCacheUser(loggedUser.getSlug());
            responseObj.put(ConstantResponseKeys.MESSAGE, "Successfully updated.");
            responseObj.put(ConstantResponseKeys.STATUS, 200);
        } else {
            responseObj.put(ConstantResponseKeys.MESSAGE, "No user found to update.");
            responseObj.put(ConstantResponseKeys.STATUS, 404);
        }
        logger.debug("Completed updateUserProfile method");
        return responseObj;
    }

    @Transactional
    public int updateUser(UserRequest userRequest, AuthUser loggedUser) {
        logger.debug("Starting updateUser method with userRequest: {}, loggedUser: {}", userRequest, loggedUser);
        int updateCount = wholesaleUserHbRepository.updateUser(userRequest, loggedUser); // Update operation
        logger.debug("Completed updateUser method");
        return updateCount;
    }

    public User getUserDetail(String slug) {
        logger.debug("Starting getUserDetail method with slug: {}", slug);
        User user = wholesaleUserRepository.findUserBySlug(slug);
        logger.debug("Completed getUserDetail method");
        return user;
    }

    @Transactional
    public User resetPasswordByUserSlug(PasswordDto passwordDto, AuthUser loggedUser) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Starting resetPasswordByUserSlug method with passwordDto: {}, loggedUser: {}", passwordDto, loggedUser);
        // Validating required fields. If their we found any required field is null, this will throw an Exception
        Utils.checkRequiredFields(passwordDto,List.of("password"));
        if(passwordDto.getPassword().isEmpty()) throw new IllegalArgumentException("password can't by empty or blank");
        User user = userCacheService.getCacheUser(loggedUser.getSlug());
        user.setPassword(passwordDto.getPassword());
        User updatedUser = wholesaleUserRepository.save(user); // Update operation
        logger.debug("Completed resetPasswordByUserSlug method");
        return updatedUser;
    }

    public String updateProfileImage(MultipartFile profileImage,AuthUser loggedUser) throws IOException {
        logger.debug("Starting updateProfileImage method with profileImage: {}, loggedUser: {}", profileImage, loggedUser);
        String slug = loggedUser.getSlug();
        String imageName = UUID.randomUUID().toString().substring(0,5)+"_"+ Objects.requireNonNull(profileImage.getOriginalFilename()).replaceAll(" ","_");
        if (!Utils.isValidImage(imageName)) throw new IllegalArgumentException("Not a valid Image.");
        String dirPath = profilePath+slug+ GlobalConstant.PATH_SEPARATOR;
        File dir = new File(dirPath);
        if(!dir.exists()) dir.mkdirs();
        profileImage.transferTo(new File(dirPath+imageName));
        int isUpdated =  wholesaleUserHbRepository.updateProfileImage(slug,imageName); // Update operation
        if(isUpdated > 0) {
            //Evict wholesaler from redis
            deleteCacheUser(loggedUser.getSlug());
            logger.debug("Completed updateProfileImage method");
            return imageName;
        }
        logger.debug("Completed updateProfileImage method");
        return null;
    }

    public User addNewUser(UserRequest userRequest) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Starting addNewUser method with userRequest: {}", userRequest);
        // Validating required fields. If their we found any required field is null, this will throw an Exception
        Utils.checkRequiredFields(userRequest,List.of("username","email","password","contact"));

        // '_' replaced by actual error message in mobileAndEmailValidation
        Utils.mobileAndEmailValidation(userRequest.getEmail(), userRequest.getContact(),"Not a valid _");
        String username = Utils.isValidName(userRequest.getUsername(),"user");
        User user = User.builder()
            .username(username)
            .email(userRequest.getEmail())
            .password(userRequest.getPassword())
            .contact(userRequest.getContact())
            .slug(UUID.randomUUID().toString())
            .status("A")
            .isDeleted("N")
            .userType("W")
            .createdAt(Utils.getCurrentMillis())
            .updatedAt(Utils.getCurrentMillis())
            .build();
        User insertedUser =  wholesaleUserRepository.save(user); // Create operation
        // assigning a free plan to user
        ServicePlan defaultServicePlan = wholesaleServicePlanRepository.getDefaultServicePlan();
        if(defaultServicePlan != null) {
            wholesaleServicePlanService.assignUserPlan(insertedUser.getId(), defaultServicePlan.getId());
        }
        // Sending mail to user for email validation.
        if (!sendOtp(userRequest)){
            throw new MyException("User was created successfully. but we facing issue some issue during sending otp. Make sure your email address was correct.");
        }
        logger.debug("Completed addNewUser method");

        // updating default pagination settings also for both kind of user "W" and "R"
        wholesalePaginationService.setUserDefaultPaginationForSettings(insertedUser);
        return insertedUser;
    }

    public int updateLastSeen(AuthUser loggedUser) {
        logger.debug("Starting updateLastSeen method with loggedUser: {}", loggedUser);
        int updateCount = wholesaleUserHbRepository.updatedUserLastSeen(loggedUser.getSlug()); // Update operation
        logger.debug("Completed updateLastSeen method");
        return updateCount;
    }

    public boolean updateSeenMessages(MessageDto message){
        logger.debug("Starting updateSeenMessages method with message: {}", message);
        boolean isUpdated = wholesaleUserHbRepository.updateSeenMessage(message); // Update operation
        logger.debug("Completed updateSeenMessages method");
        return isUpdated;
    }


    /** Getting all retailers and wholesalers for chat purpose */
    public Page<WholesaleUserDto> getAllUsers(UserSearchFilters filters, AuthUser loggedUser) {
        logger.debug("Starting getAllUsers method with filters: {}, loggedUser: {}", filters, loggedUser);
        Specification<User> specification = Specification.allOf(
                (containsName(filters.getSearchKey()).or(containsEmail(filters.getSearchKey())))
                    .and(isStatus("A"))
                    .and(hasUserType("W").or(hasUserType("R")))
                    .and(notHasSlug(loggedUser.getSlug()))
        );

        Pageable pageable = getPageable(logger,filters);
                Page<User> usersPage = wholesaleUserRepository.findAll(specification, pageable);
                return usersPage.map(wholesaleUserMapper::toDto);
    }

    /**
     * DTO returning methods - These wrap entity methods and apply mapper in service layer only
     * Following the pattern: mappers should only be used in service layer
     */

    @Transactional
    public WholesaleUserDto convertUserToDto(User user) {
        logger.debug("Starting convertUserToDto method with user: {}", user);
        WholesaleUserDto result = user != null ? wholesaleUserMapper.toDto(user) : null;
        logger.debug("Completed convertUserToDto method");
        return result;
    }

    @Transactional
    public WholesaleUserDto findByEmailAndPasswordDto(Map<String,String> param) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Starting findByEmailAndPasswordDto method with param: {}", param);
        User user = findByEmailAndPassword(param);
        WholesaleUserDto result = user != null ? wholesaleUserMapper.toDto(user) : null;
        logger.debug("Completed findByEmailAndPasswordDto method");
        return result;
    }

    @Transactional
    public WholesaleUserDto findUserByOtpAndSlugDto(UserRequest userRequest) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Starting findUserByOtpAndSlugDto method with userRequest: {}", userRequest);
        User user = findUserByOtpAndSlug(userRequest);
        WholesaleUserDto result = user != null ? wholesaleUserMapper.toDto(user) : null;
        logger.debug("Completed findUserByOtpAndSlugDto method");
        return result;
    }

    @Transactional
    public WholesaleUserDto findUserByOtpAndEmailDto(UserRequest userRequest) {
        logger.debug("Starting findUserByOtpAndEmailDto method with userRequest: {}", userRequest);
        User user = findUserByOtpAndEmail(userRequest);
        WholesaleUserDto result = user != null ? wholesaleUserMapper.toDto(user) : null;
        logger.debug("Completed findUserByOtpAndEmailDto method");
        return result;
    }

    @Transactional
    public WholesaleUserDto resetPasswordByUserSlugDto(PasswordDto passwordDto, AuthUser loggedUser) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Starting resetPasswordByUserSlugDto method with passwordDto: {}, loggedUser: {}", passwordDto, loggedUser);
        User updatedUser = resetPasswordByUserSlug(passwordDto, loggedUser);
        WholesaleUserDto result = wholesaleUserMapper.toDto(updatedUser);
        logger.debug("Completed resetPasswordByUserSlugDto method");
        return result;
    }

    @Transactional
    public WholesaleUserDto addNewUserDto(UserRequest userRequest) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Starting addNewUserDto method with userRequest: {}", userRequest);
        User insertedUser = addNewUser(userRequest);
        WholesaleUserDto result = wholesaleUserMapper.toDto(insertedUser);
        logger.debug("Completed addNewUserDto method");
        return result;
    }


    private void deleteCacheUser(String slug){
        try {
            userCacheService.evictCacheUser(slug);
        }catch (Exception e){
            logger.warn("Facing issue when going to delete user from redis : {}",slug,e);
        }
    }

}
