package com.hk.walk.config;

import com.hk.walk.config.point.Backend;
import com.hk.walk.enumerate.HttpProtocolEnum;
import com.hk.walk.loadbalance.LoadBalancer;
import com.hk.walk.loadbalance.LoadBalancerFactory;
import com.hk.walk.wrapper.HttpClientWrapper;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.net.JksOptions;
import io.vertx.core.net.OpenSSLEngineOptions;
import io.vertx.core.net.SSLEngineOptions;
import io.vertx.core.net.TrustOptions;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.builder.EqualsExclude;

import java.net.URI;
import java.net.URL;
import java.util.*;

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
    private String location;

    /**
     * see also {@link HttpVersion}
     */
    private String protocol = HttpVersion.HTTP_1_1.alpnName();

    /**
     * 代理到的目标地址uri
     */
    @EqualsAndHashCode.Exclude
    private List<String> serverUriList;


    /**
     * 代理服务器地址
     */
    private List<Backend> servers;


    /**
     * 客户端地址列表
     */
    @EqualsAndHashCode.Exclude
    private List<HttpClient> httpClientList;


    /**
     * 负载均衡策略
     */
    private String loadBalance;


    /**
     * 自定义请求头
     * TODO 后续支持表达式方法，OGNL表达式
     */
    private Map<String, List<String>> headers = new LinkedHashMap<>();


    /**
     * 负载均衡器
     */
    @EqualsAndHashCode.Exclude
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
        for (Backend server : servers) {
            URL url = new URI(server.getServer()).toURL();
            // 获取端口和地址
            String host = url.getHost();
            int port = url.getPort();
            if (port == -1) {
                port = url.getDefaultPort();
            }

            // 目标URI
            this.serverUriList.add(url.toURI().getPath());

            // 创建客户端
            // 根据权重放入对应数量客户端
            for (Integer i = 0; i < server.getWeight(); i++) {

                // 构建options
                HttpClientOptions options = buildHttpClientOptions(server, host, port, url);
                // 创建客户端
                HttpClient client = vertx.createHttpClient(options);
                log.info("create http client:host={},port={},index={},weight={}", host, port, i, server.getWeight());
                this.httpClientList.add(client);
            }

            // 扰乱客户端顺序
            Collections.shuffle(this.httpClientList);
        }

        // 负载均衡处理
        this.loadBalancer = LoadBalancerFactory.getLoadBalancer(this.loadBalance);
    }


    /**
     * 构建HttpClientOptions
     * @param server
     * @param host
     * @param port
     * @param url
     * @return
     */
    private HttpClientOptions buildHttpClientOptions(Backend server, String host, int port, URL url) {

        HttpClientOptions options = new HttpClientOptions();
        options.setDefaultHost(host)
                .setDefaultPort(port)
                .setKeepAlive(true)
                // 设置WebSocket压缩策略：如果不设置则无法进行正常的frame收发流程：参考资料：https://golang.0voice.com/?id=1105,
                // request.Header.Set("Sec-WebSocket-Extensions", "permessage-deflate"), http://timd.cn/parsing-ws-permessage-extension-using-rust/
                .setTryUsePerMessageWebSocketCompression(true);

        // HTTP 版本设置
        HttpVersion httpVersion = HttpProtocolEnum.getHttpVersion(protocol);
        if (Objects.equals(httpVersion, HttpVersion.HTTP_2)) {
            // http2 版本
            options.setUseAlpn(true);
        }
        options.setProtocolVersion(httpVersion);

        // HTTPS 代理设置
        if (url.getProtocol().equalsIgnoreCase(WalkConfig.PROTOCOL_HTTPS)) {
            options.setSsl(true);
            options.setTrustAll(true);
            // 此处port 已经是443了
            options.setDefaultPort(port);
            //  options.setVerifyHost(false);
            // 使用 OpenSSL 引擎
            // options.setOpenSslEngineOptions(new OpenSSLEngineOptions());
        }
        return options;
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
