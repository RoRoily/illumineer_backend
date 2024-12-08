package com.buaa01.illumineer_backend.tool;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.*;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.entity.ContentType;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class OssTool {
    @Value("${oss.bucket}")
    private String OSS_BUCKET;

    @Value("${oss.bucketUrl}")
    private String OSS_BUCKET_URL;

    @Autowired
    private OSS ossClient;

    /**
     * 获取真正的文件名，因为要存入文件夹中
     * @param saveFolder 存入的文件夹路径
     * @param fileName 文件名
     * @return 两者组合后的文件名
     */
    private static String getRealFileName(String saveFolder, String fileName) {
        return StringUtils.isNotEmpty(saveFolder) ? saveFolder + "/" + fileName : fileName;
    }

    /**
     * 往阿里云对象存储上传单张图片
     * @param file 图片文件
     * @param type 图片分类，如 cover、carousel、other等，不允许空字符串，这里没有做判断了，自己注意就好
     * @return  图片的URL地址
     * @throws IOException 抛出IO异常
     */
    public String uploadImage(@NonNull MultipartFile file, @NonNull String type) throws IOException {
        // 生成文件名
        String originalFilename = file.getOriginalFilename();   // 获取原文件名
        String ext = "." + FilenameUtils.getExtension(originalFilename);    // 获取文件后缀
        String uuid = System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "");
        String fileName = uuid + ext;
        // 完整路径名
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date()).replace("-", "");
        String filePathName = date + "/img/" + type + "/" + fileName;
        return getUrlString(file, filePathName);
    }

    /**
     * 获取URL链接
     * @param file 文件
     * @param filePathName 文件路径名
     * @return url
     * @throws IOException
     */
    @NotNull
    private String getUrlString(@NonNull MultipartFile file, String filePathName) throws IOException {
        try {
            ossClient.putObject(
                    OSS_BUCKET, // 仓库名
                    filePathName,   // 文件名（含路径）
                    file.getInputStream()   // 数据流
            );
        } catch (OSSException oe) {
            log.error("OSS出错了:" + oe.getErrorMessage());
            throw oe;
        } catch (ClientException ce) {
            log.error("OSS连接出错了:" + ce.getMessage());
            throw ce;
        }
        return OSS_BUCKET_URL + filePathName;
    }

    /**
     * 往阿里云对象存储上传单个专栏文档
     * @param file 专栏文档markdown文件
     * @return  图片的URL地址
     * @throws IOException
     */
    public String uploadDocument(@NonNull MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String ext = "." + FilenameUtils.getExtension(originalFilename);
        String uuid = System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "");
        String fileName = uuid + ext;
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date()).replace("-", "");
        String filePathName = date + "/document/" + fileName;
        return getUrlString(file, filePathName);
    }

    public String uploadDocument(@NonNull String content) throws IOException {
        String fileName = System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "");
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date()).replace("-", "");
        String filePathName = date + "/document/" + fileName;
        File file1 = new File(filePathName);
        try(FileWriter writer = new FileWriter(file1)){
            writer.write(content);
            writer.flush();
        }catch (IOException e){
            log.error(e.getMessage(), e);
        }
        MultipartFile file = null;
        try(FileInputStream fileInputStream = new FileInputStream(file1)){
            file = new MockMultipartFile(file1.getName(),file1.getName(), ContentType.APPLICATION_OCTET_STREAM.toString(), fileInputStream);
        }catch (IOException e){
            log.error(e.getMessage(), e);
        }
        try{
            if (file != null) {
                ossClient.putObject(
                        OSS_BUCKET,
                        filePathName,
                        file.getInputStream()
                );
            }
        }catch (OSSException oe) {
            log.error("OSS出错了:" + oe.getErrorMessage());
            throw oe;
        } catch (ClientException ce) {
            log.error("OSS连接出错了:" + ce.getMessage());
            throw ce;
        }
        return OSS_BUCKET_URL + filePathName;
    }

    /**
     * 查询指定目录下，指定前缀的文件数量，阿里云限制了单次查询最多100条
     * @param prefix  要筛选的文件名前缀，包括目录路径，如果为空字符串，则查询全部文件
     * @return  指定目录下，指定前缀的文件数量
     */
    public int countFiles(@NonNull String prefix) {
        int count;
        try {
            ListObjectsRequest listObjectsRequest = new ListObjectsRequest(OSS_BUCKET);
            listObjectsRequest.setPrefix(prefix);
            ObjectListing objectListing = ossClient.listObjects(listObjectsRequest);
            List<OSSObjectSummary> objectSummaries = objectListing.getObjectSummaries();
            count = objectSummaries.size();
        } catch (OSSException oe) {
            log.error("OSS出错了:" + oe.getErrorMessage());
            throw oe;
        } catch (ClientException ce) {
            log.error("OSS连接出错了:" + ce.getMessage());
            throw ce;
        }
        return count;
    }

    /**
     * 删除指定目录下指定前缀的所有文件，不允许目录和前缀都是空字符串
     * @param prefix    要筛选的文件名前缀，包括目录路径，不允许为空字符串
     */
    public void deleteFiles(@NonNull String prefix) {
        if (prefix.isEmpty()) {
            log.warn("你正试图删除整个bucket，已拒绝该危险操作");
            return;
        }
        try {
            // 列举所有包含指定前缀的文件并删除。
            String nextMarker = null;
            ObjectListing objectListing;
            do {
                ListObjectsRequest listObjectsRequest = new ListObjectsRequest(OSS_BUCKET).withPrefix(prefix).withMarker(nextMarker);
                objectListing = ossClient.listObjects(listObjectsRequest);
                if (!objectListing.getObjectSummaries().isEmpty()) {
                    List<String> keys = new ArrayList<>();
                    for (OSSObjectSummary s : objectListing.getObjectSummaries()) {
                        System.out.println("key name: " + s.getKey());
                        keys.add(s.getKey());
                    }
                    DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(OSS_BUCKET).withKeys(keys).withEncodingType("url");
                    DeleteObjectsResult deleteObjectsResult = ossClient.deleteObjects(deleteObjectsRequest);
                    List<String> deletedObjects = deleteObjectsResult.getDeletedObjects();
                    for(String obj : deletedObjects) {
                        String deleteObj =  URLDecoder.decode(obj, StandardCharsets.UTF_8);
                        log.info("删除文件：{}", deleteObj);
                    }
                }
                nextMarker = objectListing.getNextMarker();
            } while (objectListing.isTruncated());
        } catch (OSSException oe) {
            log.error("OSS出错了:" + oe.getErrorMessage());
        } catch (ClientException ce) {
            log.error("OSS连接出错了:" + ce.getMessage());
        }
    }

    /**
     * uploadFile:上传文件到Oss
     * @param accessKeyId accessKeyId
     * @param accessKeySecret accessKeySecret
     * @param endpoint endpoint
     * @param savePath 存放路径
     * @param bucketName bucket名字
     * @param imageName 图片名字
     * @param fileInputStream 图片流
     * @param fileSize fileSize 图片大小
     * @throws Exception
     */
    public static void uploadFile(String accessKeyId, String accessKeySecret, String endpoint, String savePath, String bucketName,
                                  String imageName, InputStream fileInputStream, Long fileSize) throws Exception {
        OSS client = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            ObjectMetadata objectMeta = new ObjectMetadata();
            objectMeta.setContentLength(fileSize);
            if (!savePath.endsWith("/")) {
                savePath = savePath + "/";
            }
            client.putObject(bucketName,savePath + imageName, fileInputStream, objectMeta);
        } catch (Exception e) {
            log.error("上传文件到oss出错", e);
            throw new Exception("上传文件到oss出错");
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                    client.shutdown();
                } catch (IOException e) {
                    log.error("上传文件到oss出错", e);
                }
            }
        }
    }
}