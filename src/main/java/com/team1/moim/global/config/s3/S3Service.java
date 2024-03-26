package com.team1.moim.global.config.s3;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

@Service
@Slf4j
public class S3Service {
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final AmazonS3Client amazonS3Client;

    @Autowired
    public S3Service(AmazonS3Client amazonS3Client) {
        this.amazonS3Client = amazonS3Client;
    }

    // S3에 파일 업로드
    public String uploadFile(String fileType, MultipartFile multipartFile){

        if (multipartFile.isEmpty()){
            return null;
        }
        String uploadFilePath = fileType + "/" + getFolderName();
        String originalFilename = multipartFile.getOriginalFilename();
        String uploadFileName = getUuidFileName(originalFilename);
        String uploadFileUrl = "";
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(multipartFile.getSize());
        objectMetadata.setContentType(multipartFile.getContentType());

        try (InputStream inputStream = multipartFile.getInputStream()){
            String keyName = uploadFilePath + "/" + uploadFileName;
            log.info("키 네임: " + keyName);

            // S3에 폴더 및 파일 업로드
            amazonS3Client.putObject(new PutObjectRequest(bucket, keyName, inputStream, objectMetadata));
            log.info("S3에 폴더 및 파일 업로드 완료");

            // S3에 업로드한 폴더 및 파일 URL
            uploadFileUrl = getUrl(keyName);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("파일 업로드 실패", e);
        }
        return uploadFileUrl;
    }

    public String getUrl(String keyName){
        return amazonS3Client.getUrl(bucket, keyName).toString();
    }

    // UUID 파일명 반환
    public String getUuidFileName(String fileName){
        String ext = fileName.substring(fileName.indexOf(".") + 1);

        return UUID.randomUUID() + "." + ext;
    }

    // 년/월/일 폴더명 반환
    private String getFolderName(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date = new Date();
        String str = sdf.format(date);

        return str.replace("-", "/");
    }

}
