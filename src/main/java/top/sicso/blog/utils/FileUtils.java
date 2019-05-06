package top.sicso.blog.utils;

import lombok.extern.slf4j.Slf4j;
import top.sicso.blog.constant.FileHelper;
import top.sicso.blog.exception.CustomerException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import static org.springframework.util.StreamUtils.copy;

/**
 * 文件操作工具类
 */
@Slf4j
public class FileUtils {

    private FileUtils() {
    }

    /**
     * 获取文件后缀
     */
    static String getSuffix(String fileName) {
        String[] token = fileName.split("\\.");
        if (token.length > 0) {
            return token[token.length - 1];
        } else {
            return "";
        }
    }

    static boolean isLinuxPath(String path) {
        return path.contains("/");
    }

    /**
     * 读取文件为字节数组
     */
    public static byte[] readFileToByteArray(final File file) throws IOException {
        InputStream in = openInputStream(file);
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(in, output);
        return output.toByteArray();
    }

    private static FileInputStream openInputStream(final File file) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (!file.canRead()) {
                throw new IOException("File" + file + "' cannot be read");
            }
        } else {
            throw new CustomerException("File" + file + "' does not exist");
        }
        return new FileInputStream(file);
    }

    public static String updatePic(String restUrl, String picHome, HttpServletRequest request) throws IOException {

        MultipartFile multipartFile = getMultipartFile(request);

        //设置图片名称为currentTimeMillis+文件后缀
        String fileName = String.valueOf(System.currentTimeMillis()) + "." +
                FileUtils.getSuffix(multipartFile.getOriginalFilename());
        //获取当前年月
        String yearMonth = TimeTools.getYearMonthOfNow();

        //图片存储路径为根路径/年月。比如user/sicso/xmarket/201608
        File path = new File(picHome + File.separatorChar + yearMonth);

        //合成图片在服务器上的绝对路径
        File targetFile = new File(picHome + File.separatorChar + yearMonth + File.separatorChar + fileName);
        if (!path.exists()) {
            path.mkdirs();
        }
        //保存图片
        multipartFile.transferTo(targetFile);
        return PathUtils.getRootServerPath(request) + restUrl + yearMonth + "/" + fileName;
    }

    public static String updatePicture(String picturePath, MultipartFile multipartFile) throws IOException {

        if (multipartFile == null || multipartFile.isEmpty()) throw new RuntimeException("文件内容为空");

        //设置图片名称为currentTimeMillis+文件后缀
        String fileName = String.valueOf(System.currentTimeMillis()) + "." + FileUtils.getSuffix(multipartFile.getOriginalFilename());
        //获取当前年月
        String yearMonth = TimeTools.getYearMonthOfNow();

        //图片存储路径为base file path/picture/年月/filename(time millis).suffix
        File path = new File(FileHelper.BASE_FILEPATH + picturePath + File.separatorChar + yearMonth);
        if (!path.exists()) path.mkdirs();

        //合成图片在服务器上的绝对路径
        File targetFile = new File(path , fileName);

        //保存图片
        multipartFile.transferTo(targetFile);
        return picturePath + yearMonth + "/" + fileName;
    }

    /**
     * 从HttpServletRequest中获取MultipartFile
     */
    private static MultipartFile getMultipartFile(HttpServletRequest request) {
        MultipartHttpServletRequest multipartRequest =
                (MultipartHttpServletRequest) request;
        Iterator<String> fileNames = multipartRequest.getFileNames();
        return multipartRequest.getFile(fileNames.next());
    }


}
