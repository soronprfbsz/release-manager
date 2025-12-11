package com.ts.rm.global.ssh.executor;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * SFTP 파일 전송 실행기
 * <p>
 * JSch ChannelSftp를 사용하여 파일을 원격 호스트로 전송합니다.
 * 비즈니스 로직과 독립적으로 재사용 가능합니다.
 * </p>
 */
@Slf4j
@Component
public class SftpExecutor {

    /**
     * 파일 업로드
     *
     * @param session    SSH 세션
     * @param inputStream 파일 입력 스트림
     * @param fileName   파일명
     * @param remotePath 원격 경로 (디렉토리)
     * @throws BusinessException 파일 업로드 실패
     */
    public void uploadFile(Session session, InputStream inputStream, String fileName, String remotePath) {
        validateSession(session);

        ChannelSftp sftpChannel = openSftpChannel(session);

        try {
            // 원격 디렉토리 생성 (없으면)
            createRemoteDirectory(sftpChannel, remotePath);

            // 원격 경로로 이동
            changeDirectory(sftpChannel, remotePath);

            // 파일 업로드
            String fullRemotePath = remotePath + "/" + fileName;
            log.info("파일 업로드 시작: {} → {}", fileName, fullRemotePath);

            putFile(sftpChannel, inputStream, fileName);

            log.info("파일 업로드 완료: {}", fullRemotePath);

        } finally {
            closeSftpChannel(sftpChannel);
        }
    }

    /**
     * 로컬 파일 또는 디렉토리 업로드
     * <p>
     * 파일인 경우 단일 파일 업로드, 디렉토리인 경우 재귀적으로 모든 파일 업로드
     * </p>
     *
     * @param session    SSH 세션
     * @param localPath  로컬 파일 또는 디렉토리 경로
     * @param remotePath 원격 경로 (디렉토리)
     * @throws BusinessException 파일 업로드 실패
     */
    public void uploadLocalFile(Session session, Path localPath, String remotePath) {
        validateSession(session);

        File localFile = localPath.toFile();

        // 파일 존재 확인
        if (!localFile.exists()) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND,
                    "로컬 파일이 존재하지 않습니다: " + localPath);
        }

        ChannelSftp sftpChannel = openSftpChannel(session);

        try {
            // 원격 디렉토리 생성 (없으면)
            createRemoteDirectory(sftpChannel, remotePath);

            if (localFile.isDirectory()) {
                // 디렉토리인 경우 재귀적으로 업로드
                log.info("디렉토리 업로드 시작: {} → {}", localPath, remotePath);
                uploadDirectory(sftpChannel, localFile, remotePath);
                log.info("디렉토리 업로드 완료: {}", remotePath);
            } else {
                // 파일인 경우 단일 파일 업로드
                uploadSingleFile(sftpChannel, localFile, remotePath);
            }

        } finally {
            closeSftpChannel(sftpChannel);
        }
    }

    /**
     * 단일 파일 업로드
     *
     * @param sftpChannel SFTP 채널
     * @param localFile   로컬 파일
     * @param remotePath  원격 디렉토리 경로
     */
    private void uploadSingleFile(ChannelSftp sftpChannel, File localFile, String remotePath) {
        String fileName = localFile.getName();
        String fullRemotePath = remotePath + "/" + fileName;

        log.info("파일 업로드 시작: {} → {}", localFile.getAbsolutePath(), fullRemotePath);

        // 파일 입력 스트림 열기 및 업로드
        try (FileInputStream fis = new FileInputStream(localFile)) {
            changeDirectory(sftpChannel, remotePath);
            putFile(sftpChannel, fis, fileName);
        } catch (IOException e) {
            String errorMessage = String.format("파일 입력 스트림 읽기 실패: %s (%s)",
                    localFile.getAbsolutePath(), e.getMessage());
            log.error(errorMessage);
            throw new BusinessException(ErrorCode.SSH_IO_ERROR, errorMessage);
        }

        // 파일 권한 설정 (업로드 후)
        setFilePermissions(sftpChannel, fullRemotePath, fileName);

        log.info("파일 업로드 완료: {}", fullRemotePath);
    }

    /**
     * 파일 권한 설정
     * <p>
     * .sh 파일: 755 (rwxr-xr-x) - 실행 가능<br>
     * 기타 파일: 644 (rw-r--r--) - 읽기 전용
     * </p>
     *
     * @param sftpChannel    SFTP 채널
     * @param fullRemotePath 원격 파일 전체 경로
     * @param fileName       파일명
     */
    private void setFilePermissions(ChannelSftp sftpChannel, String fullRemotePath, String fileName) {
        int permissions;

        if (fileName.endsWith(".sh")) {
            // .sh 파일: 755 (소유자 rwx, 그룹 rx, 기타 rx)
            permissions = 0755;
            log.debug("실행 권한 부여: {} (755)", fullRemotePath);
        } else {
            // 기타 파일: 644 (소유자 rw, 그룹 r, 기타 r)
            permissions = 0644;
            log.debug("읽기 권한 부여: {} (644)", fullRemotePath);
        }

        changePermissions(sftpChannel, permissions, fullRemotePath);
    }

    /**
     * 디렉토리 재귀 업로드
     *
     * @param sftpChannel    SFTP 채널
     * @param localDirectory 로컬 디렉토리
     * @param remotePath     원격 디렉토리 경로
     */
    private void uploadDirectory(ChannelSftp sftpChannel, File localDirectory, String remotePath) {
        File[] files = localDirectory.listFiles();

        if (files == null) {
            log.warn("디렉토리가 비어있거나 읽을 수 없습니다: {}", localDirectory.getAbsolutePath());
            return;
        }

        for (File file : files) {
            String remoteFilePath = remotePath + "/" + file.getName();

            if (file.isDirectory()) {
                // 하위 디렉토리: 원격에 디렉토리 생성 후 재귀 업로드
                createRemoteDirectory(sftpChannel, remoteFilePath);
                uploadDirectory(sftpChannel, file, remoteFilePath);
            } else {
                // 파일: 업로드
                uploadSingleFile(sftpChannel, file, remotePath);
            }
        }
    }

    /**
     * SFTP 채널 열기
     *
     * @param session SSH 세션
     * @return SFTP 채널
     * @throws BusinessException 채널 열기 실패
     */
    private ChannelSftp openSftpChannel(Session session) {
        try {
            log.debug("SFTP 채널 열기");
            Channel channel = session.openChannel("sftp");
            channel.connect();
            return (ChannelSftp) channel;
        } catch (JSchException e) {
            String errorMessage = "SFTP 채널 열기 실패: " + e.getMessage();
            log.error(errorMessage);
            throw new BusinessException(ErrorCode.SSH_CHANNEL_OPEN_FAILED, errorMessage);
        }
    }

    /**
     * 원격 디렉토리 생성 (재귀적으로)
     * <p>
     * 상위 디렉토리가 없으면 차례대로 생성합니다.
     * </p>
     *
     * @param sftpChannel SFTP 채널
     * @param remotePath  원격 경로
     */
    private void createRemoteDirectory(ChannelSftp sftpChannel, String remotePath) {
        String[] directories = remotePath.split("/");
        StringBuilder currentPath = new StringBuilder();

        for (String directory : directories) {
            if (directory.isEmpty()) {
                currentPath.append("/");
                continue;
            }

            currentPath.append("/").append(directory);
            String path = currentPath.toString();

            // 디렉토리 존재 여부 확인 후 없으면 생성
            if (!remoteDirectoryExists(sftpChannel, path)) {
                makeDirectory(sftpChannel, path);
                log.info("디렉토리 생성: {}", path);
            } else {
                log.debug("디렉토리 존재: {}", path);
            }
        }
    }

    /**
     * SFTP 채널 닫기
     *
     * @param sftpChannel SFTP 채널
     */
    private void closeSftpChannel(ChannelSftp sftpChannel) {
        if (sftpChannel != null && sftpChannel.isConnected()) {
            sftpChannel.disconnect();
            log.debug("SFTP 채널 닫기");
        }
    }

    /**
     * 원격 디렉토리로 이동
     *
     * @param sftpChannel SFTP 채널
     * @param remotePath  원격 경로
     */
    private void changeDirectory(ChannelSftp sftpChannel, String remotePath) {
        try {
            sftpChannel.cd(remotePath);
        } catch (SftpException e) {
            String errorMessage = String.format("원격 디렉토리 이동 실패: %s (%s)",
                    remotePath, e.getMessage());
            log.error(errorMessage);
            throw new BusinessException(ErrorCode.SSH_IO_ERROR, errorMessage);
        }
    }

    /**
     * 파일 업로드 (SFTP put)
     *
     * @param sftpChannel SFTP 채널
     * @param inputStream 파일 입력 스트림
     * @param fileName    파일명
     */
    private void putFile(ChannelSftp sftpChannel, InputStream inputStream, String fileName) {
        try {
            sftpChannel.put(inputStream, fileName);
        } catch (SftpException e) {
            String errorMessage = String.format("SFTP 파일 전송 실패: %s (%s)",
                    fileName, e.getMessage());
            log.error(errorMessage);
            throw new BusinessException(ErrorCode.SSH_IO_ERROR, errorMessage);
        }
    }

    /**
     * 파일 권한 변경 (chmod)
     *
     * @param sftpChannel SFTP 채널
     * @param permissions 권한 (8진수)
     * @param remotePath  원격 파일 경로
     */
    private void changePermissions(ChannelSftp sftpChannel, int permissions, String remotePath) {
        try {
            sftpChannel.chmod(permissions, remotePath);
        } catch (SftpException e) {
            // 권한 설정 실패는 경고만 출력 (업로드는 성공)
            log.warn("파일 권한 설정 실패 (무시): {} - {}", remotePath, e.getMessage());
        }
    }

    /**
     * 원격 디렉토리 존재 여부 확인
     *
     * @param sftpChannel SFTP 채널
     * @param remotePath  원격 경로
     * @return 존재 여부
     */
    private boolean remoteDirectoryExists(ChannelSftp sftpChannel, String remotePath) {
        try {
            sftpChannel.stat(remotePath);
            return true;
        } catch (SftpException e) {
            return false;
        }
    }

    /**
     * 원격 디렉토리 생성
     *
     * @param sftpChannel SFTP 채널
     * @param remotePath  원격 경로
     */
    private void makeDirectory(ChannelSftp sftpChannel, String remotePath) {
        try {
            sftpChannel.mkdir(remotePath);
        } catch (SftpException e) {
            String errorMessage = String.format("디렉토리 생성 실패: %s (%s)",
                    remotePath, e.getMessage());
            log.error(errorMessage);
            throw new BusinessException(ErrorCode.SSH_IO_ERROR, errorMessage);
        }
    }

    /**
     * SSH 세션 유효성 검증
     *
     * @param session SSH 세션
     */
    private void validateSession(Session session) {
        if (session == null) {
            throw new IllegalArgumentException("SSH 세션은 필수입니다");
        }
        if (!session.isConnected()) {
            throw new IllegalArgumentException("SSH 세션이 연결되어 있지 않습니다");
        }
    }
}
