package com.sales.wholesaler.controller;

import com.sales.claims.AuthUser;
import com.sales.claims.SalesUser;
import com.sales.global.GlobalConstant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RequestMapping("removebg")
@RestController
@RequiredArgsConstructor
@Tag(name = "Background Removal", description = "APIs for uploading images and removing backgrounds")
public class RemoveBg {

  
  private static final Logger logger = LoggerFactory.getLogger(RemoveBg.class);

    @Value(value = "${removebg.absolute}")
    String outputPath;

    @Value(value = "${removebg.get}")
    String relativePath;

    @PostMapping("/")
    @PreAuthorize("hasAuthority('remove.bg.image.upload')")
    @Operation(summary = "Upload image for background removal", description = "Uploads an image and processes it to remove the background")
    public ResponseEntity<Map<String,String>> uploadImage(Authentication authentication,HttpServletRequest request, @RequestParam("image") MultipartFile file) throws IOException {
        logger.debug("Starting uploadImage method");
        AuthUser user = (SalesUser) authentication.getPrincipal();
        String baseUrl = GlobalConstant.removeBgUrl; // Replace with your Flask API URL
        Path baseDir = Paths.get(outputPath).toAbsolutePath().normalize();
        Path userFolder = baseDir.resolve(user.getSlug()).normalize();
        Path targetPath = userFolder.resolve(Objects.requireNonNull(FilenameUtils.getName(file.getOriginalFilename()))).normalize();
        if (!targetPath.startsWith(baseDir)) {
            throw new SecurityException("Invalid file path attempt detected!");
        }
        File filePath = targetPath.toFile();
        if (!filePath.exists()){
            boolean dirCreated = filePath.getParentFile().mkdirs();
            if(dirCreated) logger.debug("New dir created :{}",filePath.getName());

        }

        file.transferTo(filePath);

       /* if (UploadImageValidator.isValidImage(file, 200,
                200, GlobalConstant.maxWidth, GlobalConstant.maxHeight,
                GlobalConstant.allowedAspectRatios, GlobalConstant.allowedFormats)) {
            file.transferTo(filePath);
        }
        else {
            throw new MyException("Image is not fit in accept ratio. please resize you image before upload.");
        }
    */


        // Create a request body with the base64 image
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("input_path", filePath.getAbsolutePath());
        body.add("output_path", outputPath+user.getSlug()+GlobalConstant.PATH_SEPARATOR);
        body.add("output_filename","result_"+ file.getName());

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        // Send the request to the Flask API
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(baseUrl, requestEntity, String.class);

        // Extract the output path from the response
        String outputPathRes = responseEntity.getBody();
        Map<String,String> result = new HashMap<>();
        result.put("downloadPath","/removebg/"+outputPathRes);
        Files.delete(targetPath);
        logger.debug("File : {} successfully deleted",filePath.getAbsolutePath());
        logger.debug("Completed uploadImage method");
        return new ResponseEntity<>(result, responseEntity.getStatusCode());
    }


    @GetMapping("/{filename}")
    @PreAuthorize("hasAuthority('remove.bg.image.download')")
    @Operation(summary = "Download processed image", description = "Retrieves the processed image after background removal")
    public ResponseEntity<Resource> getFile(Authentication authentication,HttpServletRequest request, @PathVariable(required = true) String filename) throws MalformedURLException {
        logger.debug("Starting getFile method");
          AuthUser user = (SalesUser) authentication.getPrincipal();
        Path relative = Paths.get(relativePath);
        Path userSlug = relative.resolve(user.getSlug()).normalize();
        Path path = userSlug.resolve(filename).normalize();
        Resource resource = new UrlResource(path.toUri());
        logger.debug("Completed getFile method");
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(resource);
    }

}
