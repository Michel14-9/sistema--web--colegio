package com.universidad.sistema_academico.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class VoucherService {

    @Value("${voucher.upload.directory:uploads/vouchers}")
    private String uploadDirectory;

    public String guardarVoucher(MultipartFile voucher, String dni) throws IOException {
        // Crear directorio si no existe
        Path uploadPath = Paths.get(uploadDirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generar nombre único para el archivo
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String extension = obtenerExtension(voucher.getOriginalFilename());
        String nombreArchivo = dni + "_" + timestamp + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
        Path filePath = uploadPath.resolve(nombreArchivo);

        // Guardar archivo
        Files.write(filePath, voucher.getBytes());

        return filePath.toString();
    }

    public boolean validarVoucher(String voucherPath) {
        // Validación básica: que el archivo exista
        if (!Files.exists(Paths.get(voucherPath))) {
            return false;
        }


        return true;
    }

    private String obtenerExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}