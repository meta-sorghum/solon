package org.noear.solon.cloud.extend.xxljob;

import com.xxl.job.core.executor.XxlJobExecutor;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.noear.solon.Solon;
import org.noear.solon.Utils;
import org.noear.solon.cloud.CloudManager;
import org.noear.solon.cloud.extend.xxljob.service.CloudJobServiceImpl;
import org.noear.solon.core.AopContext;
import org.noear.solon.core.Plugin;
import org.noear.solon.core.event.AppLoadEndEvent;

/**
 * @author noear
 * @since 1.4
 */
public class XPluginImp implements Plugin {
    @Override
    public void start(AopContext context) {
        if (Utils.isEmpty(XxlJobProps.instance.getServer())) {
            return;
        }

        if (XxlJobProps.instance.getJobEnable() == false) {
            return;
        }

        ///////////////////////////////////////////////////

        //注册Job服务
        CloudManager.register(CloudJobServiceImpl.instance);

        //注册构建器和提取器
        context.beanExtractorAdd(XxlJob.class, new XxlJobExtractor());

        //构建自动配置
        context.beanMake(XxlJobAutoConfig.class);

        Solon.app().onEvent(AppLoadEndEvent.class, e -> {
            XxlJobExecutor executor = context.getBean(XxlJobExecutor.class);
            executor.start();
        });
    }
}
