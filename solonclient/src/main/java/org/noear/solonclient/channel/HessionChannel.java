package org.noear.solonclient.channel;

import okhttp3.MediaType;
import okhttp3.Response;
import org.noear.solonclient.*;

import java.util.Map;

public class HessionChannel implements IChannel {
    public static final HessionChannel instance = new HessionChannel();

    @Override
    public Result call(XProxyConfig cfg, String url, Map<String, String> headers, Map<String, Object> args) throws Exception {
        HttpUtils http = HttpUtils.http(url).headers(headers);

        http.header("Solon-Serialization","@hession");

        //1.执行并返回
        Response response;
        if (cfg.enctype == Enctype.form_data) {
            if (args != null && args.size() > 0) {
                response = http.data(args).exec("POST");
            } else {
                response = http.exec("GET");
            }
        }else{
            throw new RuntimeException("Only support form_data post");
        }

        //2.构建结果
        Result result = new Result(response.code(), response.body().bytes());

        //2.1.设置头
        for (int i = 0, len = response.headers().size(); i < len; i++) {
            result.headerAdd(response.headers().name(i), response.headers().value(i));
        }

        //2.2.设置字符码
        MediaType contentType = response.body().contentType();
        if (contentType != null) {
            result.charsetSet(contentType.charset());
        }

        //3.返回结果
        return result;
    }
}
