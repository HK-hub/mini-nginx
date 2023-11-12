package com.hk.walk.config;

import com.hk.walk.loadbalance.LoadBalancer;
import com.hk.walk.loadbalance.LoadBalancerFactory;
import com.hk.walk.wrapper.HttpClientWrapper;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : HK意境
 * @ClassName : Upstream
 * @date : 2023/11/12 20:44
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@Slf4j
@Data
public class Upstream {


    /**
     * 匹配路径
     */
    private String path;


    /**
     * 代理到的目标地址uri
     */
    private List<String> serverUriList;


    /**
     * 代理服务器地址
     */
    private List<String> servers;


    /**
     * 客户端地址列表
     */
    private List<HttpClient> httpClientList;


    /**
     * 负载均衡策略
     */
    private String loadBalance;


    /**
     * 负载均衡器
     */
    private LoadBalancer loadBalancer;

    /**
     * 初始化客户端
     */
    public void init(Vertx vertx) throws Exception {

        if (CollectionUtils.isEmpty(this.servers)) {
            this.servers = new ArrayList<>();
        }

        if (CollectionUtils.isEmpty(this.httpClientList)) {
            this.httpClientList = new ArrayList<>();
        }

        if (CollectionUtils.isEmpty(this.serverUriList)) {
            this.serverUriList = new ArrayList<>();
        }

        // 创建客户端
        for (String server : servers) {
            URL url = new URI(server).toURL();
            // 获取端口和地址
            String host = url.getHost();
            int port = url.getPort();

            // 目标URI
            this.serverUriList.add(url.toURI().getPath());

            // 创建客户端
            HttpClientOptions options = new HttpClientOptions();
            options.setDefaultHost(host).setDefaultPort(port)
                    .setKeepAlive(true);
            HttpClient client = vertx.createHttpClient(options);

            log.info("create http client:host={},port={}", host, port);
            this.httpClientList.add(client);
        }

        // 负载均衡处理
        this.loadBalancer = LoadBalancerFactory.getLoadBalancer(this.loadBalance);
    }


    /**
     * 负载均衡策略
     * @return
     */
    public HttpClientWrapper loadBalanceSelect(String ip) {

        HttpClientWrapper wrapper = this.loadBalancer.select(ip, this.httpClientList);
        return wrapper;
    }


}
