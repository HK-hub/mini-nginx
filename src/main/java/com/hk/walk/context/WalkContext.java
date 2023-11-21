package com.hk.walk.context;


import com.hk.walk.config.WalkConfig;
import com.hk.walk.constant.WalkConstants;
import com.hk.walk.proxy.ProxyVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author : HK意境
 * @ClassName : WalkContext
 * @date : 2023/11/21 14:02
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@Slf4j
public class WalkContext {

    /**
     * 配置文件所在位置: 默认当前程序所在文件夹位置的 walkConfig 文件夹
     */
    public String confPath = null;


    /**
     * 应用运行端口
     */
    public Integer port = null;


    /**
     * 配置文件映射
     */
    public Map<Integer, WalkConfig> walkConfigMap = new ConcurrentHashMap<>();

    /**
     * 配置Json对象映射
     */
    public Map<Integer, JsonObject> walkConfigJsonMap = new ConcurrentHashMap<>();


    /**
     * vertx
     */
    public Vertx vertx = Vertx.vertx();


    /**
     * 代理服务器Map
     */
    public Map<Integer, List<ProxyVerticle>> proxyVerticleMap = new ConcurrentHashMap<>();

    /**
     * 单例上下文对象
     */
    private static final WalkContext instance = new WalkContext();

    private WalkContext() {

    }



    /**
     * 解析应用启动参数
     * @param args
     * @return
     */
    public boolean ensureArguments(String[] args) throws Exception{

        // 配置文件位置
        for (String arg : args) {

            // 配置文件位置
            if (arg.startsWith(WalkConstants.CONFIG_PATH_ARGUMENT)) {
                confPath = arg.split("=")[1];
                continue;
            }

            // 应用端口
            if (arg.startsWith(WalkConstants.CONFIG_PORT_ARGUMENT)) {
                port = Integer.parseInt(arg.split("=")[1]);
            }
        }


        // 如果没有指定配置文件位置
        if (Objects.isNull(confPath)) {
            // 使用当前工程所在目录下的 config 文件夹
            String projectPath = System.getProperty("user.dir");
            String parentPath = new File(projectPath).getParent();

            // 在父目录下创建 walkConfig 目录
            File configPath = new File(parentPath, WalkConstants.DEFAULT_CONFIG_DIRECTORY);
            confPath = configPath.getAbsolutePath();
        }

        // 创建配置文件夹
        File configPath = new File(confPath);
        if (BooleanUtils.isFalse(configPath.exists())) {
            boolean mkdirs = configPath.mkdirs();
            if (BooleanUtils.isFalse(mkdirs)) {
                // 创建配置文件夹失败
                throw new RuntimeException("创建配置文件夹:" + configPath.getAbsolutePath() + ", 失败!");
            }
        }

        return true;
    }


    /**
     * 读取配置文件
     * @return
     */
    public JsonObject readConfig(String[] args) throws Exception {

        // 解析参数
        this.ensureArguments(args);

        // 获取配置文件夹路径下的所有配置文件
        File configPath = new File(this.confPath);
        Collection<File> configFiles = FileUtils.listFiles(configPath, new String[]{"json"}, true);
        List<WalkConfig> tempConfigList = new ArrayList<>();
        parseConfigFile(configFiles, tempConfigList);

        // 合并配置数据
        Map<Integer, JsonObject> jsonObjectMap = this.buildAndMergeConfig(tempConfigList);

        // 解析成功，进行部署或者重加载配置
        for (WalkConfig walkConfig : tempConfigList) {
            try{
                this.deployOrReloadProxyVerticle(walkConfig);
            }catch(Exception e){
                log.error("reload proxy verticle:{}, use config:{}, failed the cause is:", walkConfig.getPort(), walkConfig, e);
                throw e;
            }
        }

        this.walkConfigJsonMap = jsonObjectMap;
        return null;
    }


    /**
     * 构造合并配置
     *
     * @param tempConfigList
     *
     * @return
     */
    public Map<Integer, JsonObject> buildAndMergeConfig(List<WalkConfig> tempConfigList) {

        Map<Integer, JsonObject> jsonObjectMap = new HashMap<>();

        // 按照监听端口分组
        Map<Integer, List<WalkConfig>> configListMap = tempConfigList.stream().collect(Collectors.groupingBy(WalkConfig::getPort));

        // 对分组后的配置表进行合并
        for (Map.Entry<Integer, List<WalkConfig>> configEntry : configListMap.entrySet()) {

            // 获取端口
            Integer port = configEntry.getKey();

            // 合并该
            for (WalkConfig walkConfig : configEntry.getValue()) {
                // 获取或者创建jsonObject
                JsonObject jsonObject = null;
                if (jsonObjectMap.containsKey(walkConfig.getPort())) {
                    jsonObject = jsonObjectMap.get(walkConfig.getPort());
                } else {
                    jsonObject = new JsonObject();
                }

                // 放入合并之后的配置
                this.mergeConfig(jsonObject, WalkConfig.toJsonObject(walkConfig));
                jsonObjectMap.put(port, jsonObject);
            }
        }

        return jsonObjectMap;
    }


    /**
     * 深度合并配置,如果重复用target 覆盖
     * @param jsonObject
     * @param walkConfig
     */
    private JsonObject mergeConfig(JsonObject jsonObject, JsonObject walkConfig) {

        jsonObject = jsonObject.mergeIn(walkConfig, true);
        return jsonObject;
    }


    /**
     * 解析配置文件
     * @param configFiles
     * @param configList
     * @return
     * @throws IOException
     */
    public boolean parseConfigFile(Collection<File> configFiles, List<WalkConfig> configList) throws Exception {

        for (File configFile : configFiles) {
            String jsonString = FileUtils.readFileToString(configFile, StandardCharsets.UTF_8);
            // 创建JSONObject
            try{
                WalkConfig walkConfig = WalkConstants.gson.fromJson(jsonString, WalkConfig.class);
                configList.add(walkConfig);
            }catch(Exception e){
                log.error("parse config file:" + configFile.getName()
                        + ",to json config failed. please check your config file is a correct JSON Object: "
                        + e.getMessage() + ",content=" + jsonString);
                throw e;
            }
        }

        return true;
    }


    /**
     * 部署或者重新加载代理服务器
     * @param newConfig
     * @return
     */
    public boolean deployOrReloadProxyVerticle(WalkConfig newConfig) {

        // 如果配置没有更新
        WalkConfig oldConfig = this.walkConfigMap.get(newConfig.getPort());
        if (Objects.equals(newConfig, oldConfig)) {
            log.info("port:{}, nwe config:{} is equals to oldConfig:{}.There is no need to reload the configuration", oldConfig.getPort(), newConfig, oldConfig);
            return true;
        }

        // 获取Verticle 进行优雅热重载
        List<ProxyVerticle> proxyVerticleList = this.proxyVerticleMap.get(newConfig.getPort());
        try{

            // 优雅停机
            if (CollectionUtils.isNotEmpty(proxyVerticleList)) {
                for (ProxyVerticle proxyVerticle : proxyVerticleList) {
                    this.vertx.undeploy(proxyVerticle.deploymentID());
                }
                proxyVerticleList.clear();
            }
            this.proxyVerticleMap.remove(newConfig.getPort());

            // 部署选项
            DeploymentOptions options = new DeploymentOptions();
            options.setConfig(JsonObject.mapFrom(newConfig)).setInstances(newConfig.getWorker());

            Future<String> resultFuture = this.vertx.deployVerticle(ProxyVerticle.class, options);
            resultFuture.onSuccess(res -> {
                log.info("deploy proxy verticle success:{}", res);
            }).onFailure(err -> {
                log.error("deploy proxy verticle failed:", err);
            });

            // 部署成功
            return resultFuture.succeeded();
        }catch(Exception e){
            log.error("deloy proxy verticle:{}, use config:{}, failed:", newConfig.getPort(), newConfig, e);
            return false;
        }
    }


    /**
     * 单例
     * @return
     */
    public static WalkContext getInstance() {
        return instance;
    }


    /**
     * 添加ProxyVerticle 进入映射
     *
     * @param port
     * @param verticle
     */
    public void addProxyVerticle(Integer port, ProxyVerticle verticle) {

        List<ProxyVerticle> proxyVerticleList = this.proxyVerticleMap.get(port);
        if (Objects.isNull(proxyVerticleList)) {
            proxyVerticleList = new ArrayList<>();
        }

        proxyVerticleList.add(verticle);
        this.proxyVerticleMap.put(port, proxyVerticleList);
        log.info("proxy verticle map add a verticle: port={}, list={}", port, proxyVerticleList.size());
    }

}
