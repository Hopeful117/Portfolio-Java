package com.hopefull117.portfolio.java.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {
    private final Path root = Paths.get("uploads/projects");

    public String save(MultipartFile file) throws IOException {

        Files.createDirectories(root);

        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();

        Files.copy(file.getInputStream(),
                root.resolve(filename));

        return "/uploads/projects/" + filename;
    }
}
