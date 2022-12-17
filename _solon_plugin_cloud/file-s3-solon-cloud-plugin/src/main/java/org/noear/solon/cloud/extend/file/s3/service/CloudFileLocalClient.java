package org.noear.solon.cloud.extend.file.s3.service;

import org.noear.solon.Utils;
import org.noear.solon.cloud.exception.CloudFileException;
import org.noear.solon.cloud.model.Media;
import org.noear.solon.cloud.service.CloudFileService;
import org.noear.solon.core.handle.Result;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * CloudFileService 的 本地 实现
 *
 * @author 等風來再離開
 * @since 1.11
 */
public class CloudFileLocalClient implements CloudFileService {
    private final File root;
    private final String endpoint;

    public CloudFileLocalClient(String bucketName, Properties properties) {
        this.endpoint = properties.getProperty("endpoint");

        this.root = new File(endpoint);

        if (root.exists() == false) {
            root.mkdirs();
        }
    }

    @Override
    public Media get(String bucket, String key) throws CloudFileException {
        try {
            File file = getFile(bucket, key);

            if (file.exists()) {
                String contentType = Utils.mime(file.getName());
                return new Media(new FileInputStream(file), contentType);
            } else {
                return null;
            }
        } catch (Throwable e) {
            throw new CloudFileException(e);
        }
    }

    @Override
    public Result put(String bucket, String key, Media media) throws CloudFileException {
        try {
            File file = getFile(bucket, key);
            if (file.exists() == false) {
                file.createNewFile();
            }

            try (OutputStream stream = new FileOutputStream(file, false)) {
                Utils.transferTo(media.body(), stream);
            }

            return Result.succeed(file.getAbsolutePath());
        } catch (Throwable e) {
            throw new CloudFileException(e);
        }
    }

    @Override
    public Result delete(String bucket, String key) throws CloudFileException {
        try {
            File file = getFile(bucket, key);
            if (file.exists()) {
                file.delete();
            }

            return Result.succeed(file.getAbsolutePath());
        } catch (Throwable e) {
            throw new CloudFileException(e);
        }
    }

    private File getFile(String bucket, String key) {
        if (Utils.isEmpty(bucket)) {
            bucket = "DEFAULT_BUCKET";
        }

        File dir = new File(root, bucket);
        if (dir.exists() == false) {
            synchronized (bucket.intern()) {
                if (dir.exists() == false) {
                    dir.mkdirs();
                }
            }
        }

        int last = key.lastIndexOf('/');
        if (last > 0) {
            String dir2Str = key.substring(0, last);
            File dir2Tmp = new File(dir, dir2Str);
            if (dir2Tmp.exists() == false) {
                synchronized (dir2Str.intern()) {
                    if (dir2Tmp.exists() == false) {
                        dir2Tmp.mkdirs();
                    }
                }
            }
        }

        File file = new File(dir, key);

        return file;
    }
}
