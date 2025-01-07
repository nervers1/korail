package or.kr.formulate.korail.util;

import com.jcraft.jsch.*;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Vector;

public class SFTPUtil {

    private static final Logger logger = LoggerFactory.getLogger(SFTPUtil.class);

    public Session session = null;
    public Channel channel = null;
    public ChannelSftp channelSftp = null;

    /**
     * SFTP 접속
     * @param ip IP
     * @param port Port
     * @param id Id
     * @param pw Password
     * @param privateKey 개인키
     * @throws Exception 예외 클래스
     */
    public void sftpInit(String ip, int port, String id, String pw, String privateKey) throws Exception {
        // 접속 PW
        int timeout = 10000; // 타임아웃 10초

        JSch jsch = new JSch();
        try {
            InetAddress local;
            local = InetAddress.getLocalHost();

            if (null != privateKey && !privateKey.isEmpty()) {
                jsch.addIdentity(privateKey);
            }

            // 세션객체 생성
            // 접속 ID, IP, PORT
            session = jsch.getSession(id, ip, port);

            if (null == privateKey || !privateKey.isEmpty()) {
                session.setPassword(pw); // password 설정
            }

            // 세션관련 설정정보 설정
            java.util.Properties config = new java.util.Properties();

            // 호스트 정보 검사하지 않는다.
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.setTimeout(timeout); // 타임아웃 설정

            if (logger.isInfoEnabled()) {
                logger.info("Connecting to " + ip + ":" + port);
            }

            session.connect(); // 접속

            channel = session.openChannel("sftp"); // sftp 채널 접속
            channel.connect();

        } catch (JSchException e) {
            if (logger.isErrorEnabled()) {
                logger.error(e.toString());
            }
            throw e;
        } catch (Exception e1) {
            if (logger.isErrorEnabled()) {
                logger.error(e1.toString());
            }
            throw e1;
        }
        channelSftp = (ChannelSftp) channel;
    }

    /**
     * SFTP 서버 접속 종료
     */
    public void disconnect() {
        if (channelSftp != null) {
            channelSftp.quit();
        }
        if (channel != null) {
            channel.disconnect();
        }
        if (session != null) {
            session.disconnect();
        }
    }

    /**
     * SFTP 서버 파일 업로드
     *
     * @param uploadPath 업로드 경로
     * @param localPath 로컬 경로
     * @param uploadFileNm 업로드 파일명
     * @throws Exception 예외
     */
    public void sftpFileUpload(String uploadPath, String localPath, String uploadFileNm) throws Exception {
        FileInputStream in = null;

        try {
            // 파일을 가져와서 inputStream에 넣고 저장경로를 찾아 업로드
            in = new FileInputStream(localPath + uploadFileNm);
            channelSftp.cd(uploadPath);
            channelSftp.put(in, uploadFileNm);

            if (logger.isDebugEnabled()) {
                logger.debug(uploadFileNm + " sent to " + uploadPath);
            }
        } catch (SftpException se) {
            if (logger.isErrorEnabled()) {
                logger.error(se.toString());
            }
            throw se;
        } catch (FileNotFoundException fe) {
            if (logger.isErrorEnabled()) {
                logger.error(fe.toString());
            }
            throw fe;
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error(e.toString());
            }
            throw e;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ioe) {
                if (logger.isErrorEnabled()) {
                    logger.error(ioe.toString());
                }
            }
        }
    }


    /**
     * SFTP 서버 파일 업로드
     * @param uploadPath 업로드 경로
     * @param localPath 로컬 경로
     * @param uploadFiles 파일명목록
     * @throws Exception 예외 클래스
     */
    public void sftpMultiFileUpload(String uploadPath, String localPath, ArrayList<String> uploadFiles)
            throws Exception {
        FileInputStream in = null;

        channelSftp.cd(uploadPath);

        for (String uploadFile : uploadFiles) {
            try {
                // 파일을 가져와서 inputStream에 넣고 저장경로를 찾아 업로드
                in = new FileInputStream(localPath + String.valueOf(uploadFile));
                channelSftp.put(in, String.valueOf(uploadFile));
                // success..");
                if (logger.isDebugEnabled()) {
                    logger.debug("sftpMultiFileUpload ------------[{}] success.. ", uploadFile);
                }
                in.close();
            } catch (SftpException se) {
                if (logger.isErrorEnabled()) {
                    logger.error(se.toString());
                }
                throw se;
            } catch (FileNotFoundException fe) {
                if (logger.isErrorEnabled()) {
                    logger.error(fe.toString());
                }
                throw fe;
            } catch (Exception e) {
                if (logger.isErrorEnabled()) {
                    logger.error(e.toString());
                }
                throw e;
            }
        }
    }

    /**
     * SFTP 서버 파일 다운로드
     * @param downloadPath  다운로드 경로
     * @param localFilePath 로컬 파일 경로
     * @throws Exception 예외 클래스
     */
    public void sftpFileDownload(String downloadPath, String localFilePath) throws Exception {
        byte[] buffer = new byte[1024];
        BufferedInputStream bis = null;
        OutputStream os = null;
        BufferedOutputStream bos = null;

        try {
            // SFTP 서버 파일 다운로드 경로
            String cdDir = downloadPath.substring(0, downloadPath.lastIndexOf("/") + 1);
            // 파일명
            String fileName = downloadPath.substring(downloadPath.lastIndexOf("/") + 1);

            channelSftp.cd(cdDir);

            File file = new File(downloadPath);
            bis = new BufferedInputStream(channelSftp.get(fileName));

            // 파일 다운로드 SFTP 서버 -> 다운로드 서버
            File newFile = new File(localFilePath + fileName);
            os = new FileOutputStream(newFile);
            bos = new BufferedOutputStream(os);

            int readCount;

            while ((readCount = bis.read(buffer)) > 0) {
                bos.write(buffer, 0, readCount);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("sftpFileDownload success.. ");
            }
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error(e.toString());
            }
            throw e;
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (bos != null) {
                    bos.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                if (logger.isErrorEnabled()) {
                    logger.error(e.toString());
                }
            }
        }
    }

    /**
     * SFTP 서버 파일 찾기
     * @param downloadPath 다운로드 경로
     * @return 파일명
     * @throws Exception 예외 클래스
     */
    public String sftpSearchFile(String downloadPath) throws Exception {
        try {
            // SFTP 서버 파일 다운로드 경로
            String cdDir = downloadPath.substring(0, downloadPath.lastIndexOf("/") + 1);
            // 파일명
            String fileName = downloadPath.substring(downloadPath.lastIndexOf("/") + 1);
            String findFileName = "";

            channelSftp.cd(cdDir);


            Vector fileList = channelSftp.ls(cdDir);

            for (Object o : fileList) {
                LsEntry files = (LsEntry) o;

                if (logger.isDebugEnabled()) {
                    logger.debug("file >>>>> " + files);
                }

                if (files.getFilename().matches(fileName + (".*"))) {
                    findFileName = files.getFilename();
                    if (logger.isDebugEnabled()) {
                        logger.debug("find file : " + fileName);
                    }
                    break;
                }
            }

            return findFileName;

        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error(e.toString());
            }
            throw e;
        }
    }
}