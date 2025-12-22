package com.dms.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * Database backup service: on startup ensure today's backup exists, and schedule daily backups
 * at 00:00 Asia/Shanghai (UTC+8).
 *
 * Behavior:
 * - Uses `mysqldump` command. Reads JDBC URL, username and password from application properties.
 * - Writes backups to backend/db/backup-YYYYMMDD.sql
 * - If mysqldump fails or not available, logs the error (throws RuntimeException during startup only if initial backup fails).
 */
@Service
@RequiredArgsConstructor
public class DatabaseBackupService {

    private final Environment env;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    private Path backupDir() {
        // workspace relative path: backend/db under project root
        return Path.of("./db");
    }

    private String extractDatabaseName(String jdbcUrl) {
        // Expect URL patterns like jdbc:mysql://host:port/dbname?params
        try {
            String afterPrefix = jdbcUrl.substring(jdbcUrl.indexOf("://") + 3);
            int slash = afterPrefix.indexOf('/');
            if (slash < 0) return null;
            String afterSlash = afterPrefix.substring(slash + 1);
            int q = afterSlash.indexOf('?');
            return q >= 0 ? afterSlash.substring(0, q) : afterSlash;
        } catch (Exception e) {
            return null;
        }
    }

    private Path todaysBackupPath() {
        String name = "backup-" + LocalDate.now(ZONE).format(DATE_FMT) + ".sql";
        return backupDir().resolve(name);
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(backupDir());
        } catch (IOException e) {
            throw new RuntimeException("无法创建备份目录: " + backupDir(), e);
        }

        Path today = todaysBackupPath();
        if (!Files.exists(today)) {
            // try create an initial backup; don't fail the whole app if it fails later, but warn
            try {
                runBackup(today);
                System.out.println("数据库初始备份已创建: " + today.toAbsolutePath());
            } catch (Exception e) {
                // Log but don't prevent startup
                System.err.println("初始数据库备份失败: " + e.getMessage());
            }
        } else {
            System.out.println("今日备份已存在: " + today.toAbsolutePath());
        }
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Shanghai")
    public void scheduledDailyBackup() {
        Path today = todaysBackupPath();
        try {
            if (!Files.exists(today)) {
                runBackup(today);
                System.out.println("定时数据库备份已创建: " + today.toAbsolutePath());
            } else {
                System.out.println("定时备份跳过，今日备份已存在: " + today.toAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("定时备份失败: " + e.getMessage());
        }
    }

    private void runBackup(Path outFile) throws IOException, InterruptedException {
        String dbName = extractDatabaseName(jdbcUrl);
        if (dbName == null || dbName.isEmpty()) {
            throw new IllegalArgumentException("无法从 JDBC URL 中解析数据库名: " + jdbcUrl);
        }

        // Attempt to find mysqldump executable in PATH or common MySQL installation locations on Windows
        String mysqldump = "mysqldump";
        List<String> tryPaths = Arrays.asList(
                mysqldump,
                "C:/Program Files/MySQL/MySQL Server 8.0/bin/mysqldump.exe",
                "C:/Program Files/MySQL/MySQL Server 8.1/bin/mysqldump.exe",
                "C:/Program Files/MySQL/MySQL Server 5.7/bin/mysqldump.exe"
        );

        IOException lastIo = null;
        for (String cmd : tryPaths) {
            try {
                ProcessBuilder pb = new ProcessBuilder(
                        cmd,
                        "-u", dbUser,
                        "-p" + dbPassword,
                        dbName
                );
                pb.redirectErrorStream(true);
                // create temp file then move atomically to avoid partial writes
                Path tmp = outFile.resolveSibling(outFile.getFileName().toString() + ".tmp");
                pb.redirectOutput(tmp.toFile());
                Process p = pb.start();
                int exit = p.waitFor();
                if (exit == 0) {
                    Files.move(tmp, outFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                    return;
                } else {
                    // read error from tmp if exists
                    String err = "mysqldump 退出码: " + exit;
                    if (Files.exists(tmp)) {
                        try {
                            String s = Files.readString(tmp);
                            err += "\n" + s;
                        } catch (IOException ignored) {}
                        Files.deleteIfExists(tmp);
                    }
                    lastIo = new IOException(err);
                }
            } catch (IOException e) {
                lastIo = e;
            }
        }

        throw new IOException("无法执行 mysqldump，尝试路径: " + tryPaths + ", 最后错误: " + (lastIo != null ? lastIo.getMessage() : "无"), lastIo);
    }
}

