package org.noear.solon.extend.socketd;

import org.noear.nami.channel.socketd.SocketD;
import org.noear.solon.SolonApp;
import org.noear.solon.core.Aop;
import org.noear.solon.core.Plugin;
import org.noear.solon.core.message.Listener;
import org.noear.solon.core.message.Session;
import org.noear.solon.extend.socketd.annotation.ClientListenEndpoint;

public class XPluginImp implements Plugin {
    @Override
    public void start(SolonApp app) {
        //注册 @ClientListenEndpoint 构建器
        Aop.context().beanBuilderAdd(ClientListenEndpoint.class, (clz, wrap, anno) -> {
            if (Listener.class.isAssignableFrom(clz)) {
                Listener l = wrap.raw();

                //创建会话
                Session session = SocketD.create(anno.host(), anno.port());

                //绑定监听
                session.listener(l);
            }
        });
    }
}
