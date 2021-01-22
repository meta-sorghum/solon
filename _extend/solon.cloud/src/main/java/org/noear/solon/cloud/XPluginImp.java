package org.noear.solon.cloud;

import org.noear.solon.Solon;
import org.noear.solon.SolonApp;
import org.noear.solon.Utils;
import org.noear.solon.cloud.impl.CloudLoadBalanceFactory;
import org.noear.solon.core.Aop;
import org.noear.solon.core.Bridge;
import org.noear.solon.core.Plugin;
import org.noear.solon.core.wrap.ClassWrap;
import org.noear.solon.cloud.annotation.CloudConfig;
import org.noear.solon.cloud.annotation.CloudEvent;
import org.noear.solon.cloud.impl.CloudBeanInjector;
import org.noear.solon.cloud.model.Config;
import org.noear.solon.cloud.model.Instance;

import java.util.Properties;

/**
 * @author noear
 * @since 1.2
 */
public class XPluginImp implements Plugin {
    @Override
    public void start(SolonApp app) {
        Aop.context().beanBuilderAdd(CloudConfig.class, (clz, bw, anno) -> {
            CloudConfigHandler handler;
            if (bw.raw() instanceof CloudConfigHandler) {
                handler = bw.raw();
            } else {
                handler = (Config cfg) -> {
                    Properties val0 = cfg.toProps();
                    ClassWrap.get(clz).fill(bw.raw(), val0::getProperty);
                };
            }

            CloudManager.register(anno, handler);

            if (CloudClient.config() != null) {
                Config config = CloudClient.config().get(anno.group(), anno.value());
                if (config != null) {
                    handler.handler(config);
                }

                //关注配置
                CloudClient.config().attention(anno.group(), anno.value(), handler);
            }
        });

        Aop.context().beanBuilderAdd(CloudEvent.class, (clz, bw, anno) -> {
            if (bw.raw() instanceof CloudEventHandler) {
                CloudManager.register(anno, bw.raw());

                if (CloudClient.event() != null) {
                    //关注事件
                    CloudClient.event().attention(anno.level(), anno.queue(), anno.value(), bw.raw());
                }
            }
        });

        Aop.context().beanInjectorAdd(CloudConfig.class, CloudBeanInjector.instance);

        if (CloudClient.discovery() != null) {
            //设置负载工厂
            Bridge.upstreamFactorySet(CloudLoadBalanceFactory.instance);
        }
    }

    @Override
    public void stop() throws Throwable {
        if (Solon.cfg().isDriftMode()) {
            if (CloudClient.discovery() != null) {
                if (Utils.isNotEmpty(Solon.cfg().appName())) {
                    CloudClient.discovery().deregister(Solon.cfg().appGroup(), Instance.local());
                }
            }
        }
    }
}
