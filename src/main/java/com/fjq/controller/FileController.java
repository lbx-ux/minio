package com.fjq.controller;

import com.fjq.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/oss")
@RequiredArgsConstructor
public class FileController {
    private final MinioService minioService;

    /**
     * 上传接口
     */
    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) {
        String objectName = minioService.uploadFile(file);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "上传成功");
        result.put("data", objectName);
        return result;
    }

    /**
     * 获取授权预览链接
     * 默认生成 10 分钟有效的临时链接
     */
    @GetMapping("/preview/{objectName}")
    public Map<String, Object> getPreviewUrl(@PathVariable String objectName) {
        String url = minioService.getPresignedUrl(objectName, 10, TimeUnit.MINUTES);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "获取成功");
        result.put("data", url);
        return result;
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/delete/{objectName}")
    public Map<String, Object> delete(@PathVariable String objectName) {
        minioService.deleteFile(objectName);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "删除成功");
        return result;
    }

    /**
     * 列出所有文件
     */
    @GetMapping("/list")
    public Map<String, Object> list() {
        List<Map<String, Object>> files = minioService.listFiles();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "获取成功");
        result.put("data", files);
        return result;
    }
}
