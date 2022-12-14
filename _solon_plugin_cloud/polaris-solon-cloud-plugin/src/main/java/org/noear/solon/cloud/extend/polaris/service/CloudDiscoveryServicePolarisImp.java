package org.noear.solon.cloud.extend.polaris.service;

import com.tencent.polaris.api.core.ConsumerAPI;
import com.tencent.polaris.api.core.ProviderAPI;
import com.tencent.polaris.api.rpc.*;
import com.tencent.polaris.factory.ConfigAPIFactory;
import com.tencent.polaris.factory.api.DiscoveryAPIFactory;
import com.tencent.polaris.factory.config.ConfigurationImpl;
import com.tencent.polaris.factory.config.consumer.ConsumerConfigImpl;
import com.tencent.polaris.factory.config.global.ClusterConfigImpl;
import com.tencent.polaris.factory.config.global.ServerConnectorConfigImpl;
import com.tencent.polaris.factory.config.provider.ProviderConfigImpl;
import org.noear.solon.Solon;
import org.noear.solon.Utils;
import org.noear.solon.cloud.CloudDiscoveryHandler;
import org.noear.solon.cloud.CloudProps;
import org.noear.solon.cloud.model.Discovery;
import org.noear.solon.cloud.model.Instance;
import org.noear.solon.cloud.service.CloudDiscoveryObserverEntity;
import org.noear.solon.cloud.service.CloudDiscoveryService;
import org.noear.solon.core.Props;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

/**
 * @author 何荣振
 * @since 1.11
 * */
public class CloudDiscoveryServicePolarisImp implements CloudDiscoveryService , Closeable {
    static final String PROP_serverConnector = "discovery.serverConnector";
    static final String PROP_provider = "discovery.provider";
    static final String PROP_consumer = "discovery.consumer";

    private ProviderAPI providerAPI;
    private ConsumerAPI consumerAPI;

    public CloudDiscoveryServicePolarisImp(CloudProps cloudProps) {
        String server = cloudProps.getDiscoveryServer();
        String namespace = Solon.cfg().appNamespace();

        ConfigurationImpl configuration = (ConfigurationImpl) ConfigAPIFactory.defaultConfig();

        //发现集群设置
        ClusterConfigImpl clusterConfig = configuration.getGlobal().getSystem().getDiscoverCluster();
        clusterConfig.setNamespace(namespace);


        //发现提供方设置
        ProviderConfigImpl providerConfig = configuration.getProvider();
        Props providerConfigProps = cloudProps.getProp(PROP_provider);
        if (providerConfigProps.size() > 0) {
            Utils.injectProperties(providerConfig, providerConfigProps);
        }


        //发现消费方设置
        ConsumerConfigImpl consumerConfig = configuration.getConsumer();
        Props consumerConfigProps = cloudProps.getProp(PROP_consumer);
        if (consumerConfigProps.size() > 0) {
            Utils.injectProperties(consumerConfig, consumerConfigProps);
        } else {
            consumerConfig.getLocalCache().setPersistEnable(false);
        }


        //发现连接设置(8091)
        ServerConnectorConfigImpl connectorConfig = configuration.getGlobal().getServerConnector();
        connectorConfig.setAddresses(Arrays.asList(server));
        //注入本置
        Props connectorProps = cloudProps.getProp(PROP_serverConnector); //支持配置注入
        Utils.injectProperties(connectorConfig, connectorProps);

        providerAPI = DiscoveryAPIFactory.createProviderAPIByConfig(configuration);
        consumerAPI = DiscoveryAPIFactory.createConsumerAPIByConfig(configuration);
    }

    /**
     * 注册服务实例
     *
     * @param group    分组
     * @param instance 服务实例
     */
    @Override
    public void register(String group, Instance instance) {
        registerState(group, instance, true);
    }

    /**
     * 注册服务实例健康状态
     *
     * @param group    分组
     * @param instance 服务实例
     */
    @Override
    public void registerState(String group, Instance instance, boolean health) {
        String[] ss = instance.address().split(":");

        if (ss.length != 2) {
            throw new IllegalArgumentException("Instance.address error");
        }

        InstanceRegisterRequest request = new InstanceRegisterRequest();
        request.setNamespace(Solon.cfg().appNamespace());
        request.setWeight((int) instance.weight());
        request.setMetadata(instance.meta());
        request.setService(instance.service());
        request.setHost(ss[0]);
        request.setPort(Integer.parseInt(ss[1]));
        request.setProtocol(instance.protocol());

        providerAPI.registerInstance(request);
    }

    /**
     * 注销服务实例
     *
     * @param group    分组
     * @param instance 服务实例
     */
    @Override
    public void deregister(String group, Instance instance) {
        String[] ss = instance.address().split(":");

        if (ss.length != 2) {
            throw new IllegalArgumentException("Instance.address error");
        }
        InstanceDeregisterRequest deregisterRequest = new InstanceDeregisterRequest();

        deregisterRequest.setNamespace(Solon.cfg().appNamespace());
        deregisterRequest.setService(instance.service());
        deregisterRequest.setHost(ss[0]);
        deregisterRequest.setPort(Integer.parseInt(ss[1]));

        providerAPI.deRegister(deregisterRequest);
    }

    /**
     * @param group   分组
     * @param service 服各名
     * @return
     */
    @Override
    public Discovery find(String group, String service) {
        Discovery discovery = new Discovery(service);

        GetHealthyInstancesRequest request = new GetHealthyInstancesRequest();

        request.setNamespace(Solon.cfg().appNamespace());
        request.setService(service);

        InstancesResponse instancesResponse = consumerAPI.getHealthyInstances(request);

        if (Objects.isNull(instancesResponse) || instancesResponse.getInstances().length > 0) {
            return discovery;
        }

        for (com.tencent.polaris.api.pojo.Instance instance : instancesResponse.getInstances()) {
            //只关注健康的
            if (instance.isHealthy()) {
                discovery.instanceAdd(new Instance(service,
                        instance.getHost() + ":" + instance.getPort())
                        .weight(instance.getWeight())
                        .protocol(instance.getProtocol())
                        .metaPutAll(instance.getMetadata()));
            }
        }

        return discovery;
    }

    /**
     * 关注服务实例列表
     *
     * @param group    分组
     * @param service  服各名
     * @param observer 观察者
     */
    @Override
    public void attention(String group, String service, CloudDiscoveryHandler observer) {
        if (Utils.isEmpty(group)) {
            group = Solon.cfg().appGroup();
        }

        CloudDiscoveryObserverEntity entity = new CloudDiscoveryObserverEntity(group, service, observer);

        WatchServiceRequest request = WatchServiceRequest.builder()
                .namespace(Solon.cfg().appNamespace())
                .service(service)
                .listeners(Collections.singletonList(event -> {
                    Discovery discovery = new Discovery(service);

                    for (com.tencent.polaris.api.pojo.Instance instance : event.getAllInstances()) {
                        if (instance.isHealthy()) {
                            discovery.instanceAdd(new Instance(service,
                                    instance.getHost() + ":" + instance.getPort())
                                    .weight(instance.getWeight())
                                    .protocol(instance.getProtocol())
                                    .metaPutAll(instance.getMetadata()));
                        }
                    }

                    entity.handle(discovery);
                }))
                .build();

        consumerAPI.watchService(request);
    }

    @Override
    public void close() throws IOException {
        if (consumerAPI != null) {
            consumerAPI.close();
        }

        if (providerAPI != null) {
            providerAPI.close();
        }
    }
}