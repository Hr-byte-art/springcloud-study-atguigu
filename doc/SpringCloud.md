# SpringCloud

# SpringCloud

# 分布式基础：常用搭配

* SpringCloudAlibaba

  * Nacos
  * Sentinel
  * Seata
* Spring Cloud

  * OpenFeign
  * Gateway

分布式配套：日志系统，指标监控，链路追踪，消息追踪 ·····

‍

# 课程版本选择

框架版本：

* SpringBoot ----- 3.3.4
* SpringCloud ----- 2023.0.3
* SpringCloudAlibaba ---- 2023.0.3.2

组件版本：

* Nacos ---- 2.4.3
* Sentinel ---- 1.8.8
* Seata ---- 2.2.0

> nacos 单体启动 : startup.cmd -m standalone

# nacos

官网：[https://nacos.io/docs/latest/quickstart/quick-start/?spm=5238cd80.49fdea5.0.0.dbdf44411YDcNA](https://nacos.io/docs/latest/quickstart/quick-start/?spm=5238cd80.49fdea5.0.0.dbdf44411YDcNA)

## 配置nacos

```yaml
spring:
  application:
    name: service-order
  cloud:
    nacos:
      server-addr: localhost:8848
```

‍

## 服务发现

> 方法一：DiscoveryClient

```Java
@Autowired
DiscoveryClient discoveryClient;
```

> 方法二：NacosDiscoveryClient

```Java
@Autowired
NacosDiscoveryClient nacosDiscoveryClient;
```

区别：前者都可以使用，后者只用于nacos

‍

## 实现负载均衡

> 导入依赖

```xml
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-loadbalancer</artifactId>
</dependency>
```

> 一 ： 注入 - 使用

```java
@Autowired
LoadBalancerClient loadBalancerClient;
```

```java
ServiceInstance choose = loadBalancerClient.choose("service-product");
URI uri = choose.getUri();
```

‍

> 二：注解使用

在远程调用客户端上就可是使得restTemplate 有负载均衡得作用

```java
@Bean
@LoadBalanced
public RestTemplate restTemplate() {
    return new RestTemplate();
}
```

```java
return restTemplate.getForEntity
("http:/service-product/getProductById/" + id, Product.class).getBody();

```

‍

‍

> 注册中心宕机后，远程调用还会成功吗

![image](assets/image-20250412234621-b5kv6pq.png)

结论：

情况一：调用过：远程调用可以不再依赖注册中心 ， 可以通过

情况二：有调用过（第一次调用），必须依赖注册中心，不可以通过

‍

## 配置中心

> 引入依赖

```xml
<!--        nacos配置中心-->
<dependency>
	<groupId>com.alibaba.cloud</groupId>
	<artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
</dependency>
```

> 配置 - service-order-config 是在nacos 中创建的 数据集

```yaml
spring:
  config:
# 注意：这里 xx.yaml 配置后缀也要加上
    import: nacos:service-order-config.yaml
```

> 在nacos 中 创建数据集并编写配置后发布

![image](assets/image-20250413000707-sjz8xit.png)

> 修改配置实时刷新

在控制器加注解 `@RefreshScope`​

```yaml
@RestController
@RefreshScope
public class OrderController {
	······
}
```

‍

### 注意：禁用导入检查

如果在父项目导入的依赖，所有的子模块都会被引入依赖，也就自动检查，如果没有使用，就会报如下错

```markdown
00:14:57.849 [main] ERROR org.springframework.boot.diagnostics.LoggingFailureAnalysisReporter -- 

***************************
APPLICATION FAILED TO START
***************************

Description:

No spring.config.import property has been defined

Action:

Add a spring.config.import=nacos: property to your configuration.
	If configuration is not required add spring.config.import=optional:nacos: instead.
	To disable this check, set spring.cloud.nacos.config.import-check.enabled=false.

```

可以使用配置**禁止导入检查**

```yaml
spring:
  cloud:
    config:
      import-check:
        enabled: false
```

> 小技巧

```java
@RefreshScope
public class OrderController {

@Value("${order.timeout}")
private String timeout;
@Value("${order.auto-confirm}")
private String autoConfirm;
```

这样写对于少量配置合适，但是对于多的配置不方便

```java
@Component
@ConfigurationProperties(prefix = "order")
@Data
public class ApplicationConfig {
    private String timeout;
    // auto-confirm ---> autoConfirm
    private String autoConfirm;
}
```

可以做一个配置类 作为组件 注入配置信息，

​`@ConfigurationProperties(prefix = "order")`​ --- 配置前缀

这样写之后，在控制器也不需要打`@RefreshScope`​注解也会实现动态刷新

‍

### 配置修改监听--来源：刷题通项目

```java
@Slf4j
@Component
public class NacosListener implements InitializingBean {

    @NacosInjected
    private ConfigService configService;

    @Value("${nacos.config.data-id}")
    private String dataId;

    @Value("${nacos.config.group}")
    private String group;

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("nacos 监听器启动");

        String config = configService.getConfigAndSignListener(dataId, group, 3000L, new Listener() {
            final ThreadFactory threadFactory = new ThreadFactory() {
                private final AtomicInteger poolNumber = new AtomicInteger(1);

                @Override
                public Thread newThread(@NotNull Runnable r) {
                    Thread thread = new Thread(r);
                    thread.setName("refresh-ThreadPool" + poolNumber.getAndIncrement());
                    return thread;
                }
            };
            final ExecutorService executorService = Executors.newFixedThreadPool(1, threadFactory);

            // 通过线程池异步处理黑名单变化的逻辑
            @Override
            public Executor getExecutor() {
                return executorService;
```

---

### 配置的优先级

![image](assets/image-20250413004349-vtdhwr7.png)

所以 ， 一般情况下 ， 如果在配置中心里面配置的信息 ， 会优先被使用；

## 配置中心  -----   数据隔离

![image](assets/image-20250413135421-knvmogq.png)

![image](assets/image-20250413140427-r8scgp4.png)

> 创建命名空间

![image](assets/image-20250413150105-kjxsasi.png)

> 编辑配置，注意可以分组

![image](assets/image-20250413150226-jh87i3a.png)

> 在配置中导入配置

```yaml
spring:
  application:
    name: service-order
  cloud:
    nacos:
      server-addr: localhost:8848
      config:
        namespace: dev # 命名空间为dev
  config:
    import:
      - nacos:common.yaml?group=order # 组为order 的 common.yaml 配置
      - nacos:database.yaml?group=order # 组为order 的 database.yaml 配置
```

‍

> 如果在使用中，在不同的测试环境有不同数量的配置，可以使用spring配置的分段配置

```yaml
spring:
  profiles:
    active: dev
  application:
    name: service-order
  cloud:
    nacos:
      server-addr: localhost:8848
      config:
        namespace: ${spring.profiles.active:dev}  # 动态配置命名空间 - 如果没有配置spring:profiles:active就使用dev
server:
  port: 9000
  
---
spring:
  config:
    import:
      - nacos:common.yaml?group=order
      - nacos:database.yaml?group=order
    activate:
      on-profile: dev # spring:profiles:active:dev使用这个
---
spring:
  config:
    import:
      - nacos:common.yaml?group=order
      - nacos:database.yaml?group=order
      - nacos:hahahaha.yaml?group=order
    activate:
      on-profile: test # # spring:profiles:active:test使用这个
---
spring:
  config:
    import:
      - nacos:common.yaml?group=order
      - nacos:database.yaml?group=order
      - nacos:hehehehe.yaml?group=order
    activate:
      on-profile: prop # spring:profiles:active:prop使用这个
```

注意：如果没有`spring:profiles:active`​没有配置的话，虽然默认使用`spring:profiles:active:dev`​，但是要设置：

​`spring:cloud:nacos:config:import-check:enabled: false`​

‍

# OpenFeign

中文文档：[https://springdoc.cn/spring-cloud-openfeign/](https://springdoc.cn/spring-cloud-openfeign/)

声明式Rest客户端（OpenFeign）

编程式Rest客户端（RestTemplate）

## 注解驱动

* 指定**远程地址**：@FeignClient
* 指定**请求方式**：@GetMapping ，@PostMapping，@DeleteMapping
* 指定**携带数据**：@RequestHeader，@RequestParam，@RequestBody
* 指定**结果返回**：响应模型

‍

> 导入依赖

```xml
<!--        openFeign-->
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

> 启动类打上注解表示启动feign

```java
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class OrderApplicationMain {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplicationMain.class, args);
    }
}
```

> 编写feignClient

```java
@FeignClient("service-product") // 服务名
public interface ProductFeignClient {
    @GetMapping("/getProductById/{id}") // 请求的方法及路由
    Product getProductById(@PathVariable("id") Integer id); //指定返回的类型
}
```

> 在实现类注入并使用

```java
@Autowired
ProductFeignClient productFeignClient;

@Override
public Order createOrderByFeign(Integer productId) {
	Product product = productFeignClient.getProductById(productId);
	return getOrder(productId, product);
}
```

注意：使用feign后就会自动负载均衡

‍

## 调用第三方库 --- 参数见第三方库说明文档

```java
@FeignClient(@value = service-other" ， url="http://xxx.com") // 服务名（自定），url：请求地址
public interface OtherFeignClient {
    @PostMapping("/xxx/xxx/xxx") // 请求的方法及路由
    XXXX getProductById(@RequestHeader("Authoruzation") String auth,
						   @RequestParam("token") String Token,
						   @RequestPaeam("xxx")String XXX) //指定参数返回的类型
}
```

‍

## 客户端负载均衡和服务端负载均衡的区别

![image](assets/image-20250413172921-bvt26ny.png)

## OpenFeign使用

### 日志使用

> 配置日志级别

```yaml
logging:
  level:
    com.wjh.feign: debug # 设置feign目录下的日志为debug级别，也可以精切到某个类
```

> 配置类写入配置Bean

```java
@Bean
Logger.Level feignLoggerLevel() {
	return Logger.Level.FULL; // 全记录
}
```

---

### 超时控制

![image](assets/image-20250413174933-ywj1s9h.png)

![image](assets/image-20250413175410-leixdu1.png)

如果未设置的话，默认OpenFeign 的连接超时时间是：10S，读取超时的时间是：60S

```yaml
spring:
	cloud:
		openfeign:
			client:
				config:
					default:
						logger-level: full
						connect-timeout: 1000
						read-timeout: 2000
					service-product:
						Logger-level: full
						connect-timeout: 3000
						read-timeout: 5000
```

---

### 重试机制

注意：OpenFeign默认是不启动重试机制

![image](assets/image-20250413180040-incoax3.png)

> 方法一：启动默认配置

```yaml
spring:
	cloud:
		openfeign:
			client:
				config:
					service-product:
						retryer: feign.retryer.Default # 使用feign默认的cc重试机制
# 请求，超过100ms，重试，100X1.5，100X1.5X1.5...，最多X5次，最高到1S
```

![image](assets/image-20250413180358-b7akfzz.png)

> 方法二：自定义一个Retryer

```java
@Bean
Retryer retryer() {
	return new Retryer() {
		@Override
		public void continueOrPropagate(RetryableException e) {       
			// XXX自定义逻辑 
		}
		@Override
		public Retryer clone() {
			// XXX自定义逻辑
			return null;
		}
    };
}
```

除此之外

```java
@Bean
Retryer retryer() {
		return Retryer.NEVER_RETRY;
	}
// 设置不重试
```

或者

```java
@Bean
Retryer retryer() {
	return new Retryer.Default(
		100, 1000, 3 // 这里如果不写参数就是相当于启动默认配置
	);
}
```

---

### 拦截器

![image](assets/image-20250413181506-n4bjob2.png)

> 实践：向请求头里面加信息

1. 自定义请求拦截器

```java
@Component
public class XTokenRequestInterceptor implements RequestInterceptor {

    /**
     * 请求拦截器
     * @param requestTemplate 请求模板
     */
    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header("X-Token", UUID.randomUUID().toString());
    }
}
```

2. 在服务中获取到

```java
@GetMapping("/getProductById/{id}")
public Product getProductById(@PathVariable("id") Integer id,
                              HttpServletRequest request){
    log.info("拦截器自定义的X-Token:{}",request.getHeader("X-Token"));
    return service.getProductById(id);
}
```

3. 查看到日志里面有信息

![image](assets/image-20250413184102-58lw7gc.png)​

![image](assets/image-20250413184116-uajw3gn.png)

### Fallback 兜底返回

注意：这个需要配合sentinel使用

![image](assets/image-20250413210810-qov2355.png)

#### 实现步骤：

> 写一个类实现client

```java
@Component
public class ProductFeignFallback implements ProductFeignClient {
    @Override
    public Product getProductById(Integer id) {
        Product product = new Product();
        product.setId(new Random().nextInt());
        product.setName("未知商品");
        product.setPrice(new BigDecimal("999999...."));
        product.setCount(99999999);
        product.setVersion(1);
        return product;
    }
}
```

> 在原来的client里面加添加fallback

```java
@FeignClient(value = "service-product" , fallback = ProductFeignFallback.class)
public interface ProductFeignClient {

    @GetMapping("/getProductById/{id}")
    Product getProductById(@PathVariable("id") Integer id);
}
```

> 需要配合sentinel，所以导入sentinel依赖

```xml
<!--        sentinel-->
<dependency>
	<groupId>com.alibaba.cloud</groupId>
	<artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>
```

> 在配置中打开sentinel

```yaml
feign:
  sentinel:
    enabled: true
```

---

# sentinel

‍

官网：[https://sentinelguard.io/zh-cn/docs/quick-start.html](https://sentinelguard.io/zh-cn/docs/quick-start.html)

随着微服务的流行，服务和服务之间的<span data-type="text" style="color: var(--b3-font-color1);">稳定性</span>变得越来越重要。Spring cloudAlibaba <span data-type="text" style="color: var(--b3-font-color1);">Sentinel </span>以流量为切入点，从<span data-type="text" style="color: var(--b3-font-color1);">流量控制、流量路由、熔断降级、系统自适应过载保护、热点流量防护</span>等多个维度保护服务的稳定性。

‍

![image](assets/image-20250413213350-qwb4uds.png)​

架构原理：

![image](assets/image-20250413214117-4j8fcs4.png)

## 资源 & 规则

* 定义<span data-type="text" style="color: var(--b3-font-color1);">资源</span>

  * 主流框架<span data-type="text" style="color: var(--b3-font-color10);">自动适配</span>(Web Servlet、Dubbo、Spring Cloud、gRPC、Spring WebFlux、Reactor)所有Web接口均为资源
  * <span data-type="text" style="color: var(--b3-font-color10);">编程式</span>:SphU API
  * <span data-type="text" style="color: var(--b3-font-color10);">声明式</span>:(@SentinelResource
* 定义<span data-type="text" style="color: var(--b3-font-color1);">规则</span>:

  * 流量控制(FlowRule)
  * 熔断降级(DegradeRule)
  * 系统保护(SystemRule)
  * 来源访问控制(AuthorityRule)
  * 热点参数(ParamFlowRule)

## 工作原理

![image](assets/image-20250413215225-w9kwpax.png)

## 整合使用

![image](assets/image-20250413220148-it5xgac.png)

> 引入依赖

```xml
<!--        sentinel-->
<dependency>
	<groupId>com.alibaba.cloud</groupId>
	<artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>
```

> 下载dashboard

[https://github.com/alibaba/Sentinel/releases/tag/1.8.8](https://github.com/alibaba/Sentinel/releases/tag/1.8.8)

> 配置dashboard

```yaml
spring:
  cloud:
    sentinel:
      transport:
        dashboard: localhost:8080
	  eager: true #因为sentinel是懒加载，默认是启动后，访问了再加载，打开这个设置为启动就自动加载
```

可以看见两个服务已经被注册到控制台

![image](assets/image-20250413221242-kyc4djv.png)

## 异常处理

[https://sentinelguard.io/zh-cn/docs/basic-api-resource-rule.html](https://sentinelguard.io/zh-cn/docs/basic-api-resource-rule.html)

![image](assets/image-20250414162224-765p1zn.png)​

### 异常处理：web接口

> 自定义实现 `BlockExceptionHandler`​
>
> 注意：不能忘了打上@Component注入容器

```java
@Component
public class MyBlockExceptionHandler implements BlockExceptionHandler {
    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, String s, BlockException e) throws Exception {
        httpServletResponse.setContentType("application/json;Charset=Utf-8");

        PrintWriter writer = httpServletResponse.getWriter();
        R error = R.error(e + "被Sentinel限制了，原因：" + e.getMessage());
        String jsonString = JSONObject.toJSONString(error);
        writer.write(jsonString);
        writer.flush();
        writer.close();
    }
}
```

> 创建流控规则

![image](assets/image-20250414165323-vb728cl.png)

> 验证

![image](assets/image-20250414165745-y9u3p23.png)​

‍

### 异常处理：@SentinelResource

当一个非接口的方法打上注解`@SentinelResource`​ ， 就会对这个方法进行监控设置规则，如果触发了为他设置的规则，会优先使用自定义的`blockHandler`​，如果没有自定义`blockHandler`​，就再找自定义的`fallback`​，如果还是没有，就会抛出到springboot的默认全局处理方法（一般项目都会自定义GlobalException）

> 对`createOrder`​ 方法做限制

```java
@SentinelResource(value = "createOrder", blockHandler = "getProductByIdBlockHandler")
@Override
public Order createOrder(Integer productId) {
	Product product = getProductByIdWithLoadBalance(productId);	
	return getOrder(productId, product);
}
```

> 对`createOrder`​自定义BlockHandlr

注意：`public Order getProductByIdBlockHandler(Integer productId)`​，方法名和注解中自定义的方法一样，但是参数和打了注解的那个方法一样，可以加

```java
/**
* 熔断处理
* @param productId
* @return
*/
public Order getProductByIdBlockHandler(Integer productId) {
	Order order = new Order();
	order.setId(new Random().nextInt());
	order.setProductName("未知商品");
	order.setProductId(productId);
	order.setProductNum(99999999);
	order.setTotalAccount(new BigDecimal("999999"));
	order.setProducts(Arrays.asList(new Product()));
	return order;
}
```

> 给目标方法添加限制规则

![image](assets/image-20250414173313-i1rdf8p.png)

> 测试

![image](assets/image-20250414173352-p1rogfu.png)​

‍

### 异常处理：针对OpenFeign

针对OpenFeign和@SentinelResource类似，只是不用BlockHandler，设置Fallback就好

![image](assets/image-20250414174309-wmx98n0.png)

> 在Feign接口里面设置fallback类名

```java
@FeignClient(value = "service-product" , fallback = ProductFeignFallback.class)
public interface ProductFeignClient {

    @GetMapping("/getProductById/{id}")
    Product getProductById(@PathVariable("id") Integer id);
}
```

> 编写ProductFeignFallback，注意要实现接口（ProductFeignClient）并注入容器

```java
@Component
public class ProductFeignFallback implements ProductFeignClient {
    @Override
    public Product getProductById(Integer id) {
        Product product = new Product();
        product.setId(new Random().nextInt());
        product.setName("未知商品");
        product.setPrice(new BigDecimal("999999"));
        product.setCount(99999999);
        product.setVersion(1);
        return product;
    }
}
```

> 设置规则

![image](assets/image-20250414174612-m37xuqr.png)

> 测试

![image](assets/image-20250414174633-55v6swg.png)​

### 异常处理：硬编码Sphu.entry

常使用try-catch

[https://sentinelguard.io/zh-cn/docs/basic-api-resource-rule.html](https://sentinelguard.io/zh-cn/docs/basic-api-resource-rule.html)

```java
try {
	SphU.entry("getProductById"); //这里可以是方法名，或者具体代码
} catch (BlockException e) {
	throw new RuntimeException(e); //在这里是处理方法
}
```

‍

## 流量控制

### 流控规则（流量控制）

![image](assets/image-20250415093640-2t6wei0.png)​

#### 流控规则：阈值类型

![image](assets/image-20250415095005-sedn3pn.png)​

QPS：每秒请求数

并发线程数 ：都是每秒请求数，但是这个要使用线程池，存在线程切换选择，效率较低，一般情况用得少点

集群-单机均摊：假设10个机器，每个机器可执行单阈值数

集群-总体阈值：假设10个机器，10个机器一共可执行的阈值数

![image](assets/image-20250415095527-e6xwc9k.png)​

流控模式

调用关系包括调用方，被调用方，一个方法又有可能会调用其他方法，形成一个调用链路的层级关系，有了调用链路的统计信息，就有了多种流量控制手段

![image](assets/image-20250415101442-q25ktmq.png)​

#### 链路策略实践：

> 新建一个秒杀接口

```java
@GetMapping(value = "/createOrder/{productId}")
public Order createOrder(@PathVariable("productId") Integer productId) {
	return orderService.createOrder(productId);
}
@GetMapping(value = "/createKillOrder/{productId}")
public Order createKillOrder(@PathVariable("productId") Integer productId) {
	return orderService.createOrder(productId);
}
```

> 添加配置信息

```yaml
spring:
  cloud:
    sentinel:
      web-context-unify: false # 关闭统一web上下文
```

> 配置

![image](assets/image-20250415102407-648mbbi.png)​

入口资源是秒杀接口，所以普通创建订单接口不受影响，但是秒杀接口会被限流

#### 关联策略

当某一个类型请求大之后，在请求关联的另一个，才会触发限流

![image](assets/image-20250415110937-byy4o8g.png)

### 流控效果

#### 直接失败

超出设置的阈值就爆出BrockHandler（可以自定义）

#### warmUp

将一个请求在预热时间内达到设置的QPS值

#### 排队等待

将设置的值平摊到每秒内，超出设置的timeout时间后，直接删除后续的排队内容 

---

## 熔断降级

作为保护自身的手段，通常在客户端（调用端）进行配置

* 切断不稳定调用
* 快速返回不积压
* 避免雪崩效应

![image](assets/image-20250415141321-tp3sohq.png)​

当 Service-H出现问题，D，E，A，B都会出现问题

**断路器**的工作原理

![image](assets/image-20250415142810-tutztvu.png)

### 熔断规则

#### 慢调用服务

![image](assets/image-20250415144648-k8e5oni.png)​

最大RT：最大响应时间

#### 异常比例

![image](assets/image-20250415150906-pqcvybn.png)

无熔断：发现有错误，但是还是会先发送，然后调用fallback

![image](assets/image-20250415150920-kc3io3x.png)

有熔断：直接调用fallback

#### 异常数

![image](assets/image-20250415151712-tui59pa.png)​

## 热点规则

注意：热点规则执Dubbo方法埋点带了热点参数，对web不支持热点规则，需要通过自定义埋点方式指定新的资源名并传入新的参数，但是要和web接口的名字不一样

> 使用

```java
@GetMapping(value = "/createKillOrder/{productId}")
@SentinelResource(value = "KillOrder" , fallback = "createKillOrderBlockHandler") // 自定义埋点
public Order createKillOrder(@PathVariable("productId") Integer productId,
											HttpServletRequest request) {
	return orderService.createOrder(productId , request);
}
/**
* 回调方法
*/
public Order createKillOrderBlockHandler(Integer productId , BlockException e) {
	log.info("createKillOrderBlockHandler");
	return new Order(
				new Random().nextInt(),
                "未知商品",
                productId,
                99999999,
                new BigDecimal("999999"),
                Collections.singletonList((Product) null)
	);
}
```

> 配置

![image](assets/image-20250415154622-90i5xmc.png)对第一个参数（即：`Integer productId`​），单机阈值为1

![image](assets/image-20250415155009-asidu9x.png)对第一个参数（即：`Integer productId`​），单机阈值为1，当值为11时，阈值为：333

---

# Gateway

[https://springdoc.cn/spring-cloud-gateway/](https://springdoc.cn/spring-cloud-gateway/)

![image](assets/image-20250415172009-reyuoj6.png)

作用：

* 统一入口
* 请求路由
* 负载均衡
* 流量控制
* 身份认证
* 协议转换
* 系统监控
* 安全防护

## 工作原理

![image](assets/image-20250416093633-063zo4p.png)​

## 断言

### 断言规则

|序号|谓词名称|参数个数|参数类型|作用|
| ------| ----------------------| ----------| -----------------------------------| ---------------------------------------------------------|
|5.1|After|1|ZonedDateTime|匹配请求时间在指定时间之后|
|5.2|Before|1|ZonedDateTime|匹配请求时间在指定时间之前|
|5.3|Between|2|ZonedDateTime, ZonedDateTime|匹配请求时间在两个指定时间之间|
|5.4|Cookie|2|String（名称）, String（正则）|检查请求是否包含指定名称的 Cookie 且值匹配正则表达式|
|5.5|Header|2|String（名称）, String（正则）|检查请求头是否包含指定名称的 Header 且值匹配正则表达式|
|5.6|Host|1+|String...（主机模式）|匹配请求的 Host 头是否符合 Ant 风格模式（如 `**.example.com`​）|
|5.7|Method|1+|HttpMethod...|匹配请求的 HTTP 方法是否在指定列表中（如 GET/POST）|
|5.8|Path|1|String（路径模式）|匹配请求路径是否符合 Ant 风格或正则表达式路径（如 `/api/**`​）|
|5.9|Query|1-2|String（参数名）, String?（正则）|检查请求是否包含指定查询参数且值匹配正则表达式|
|5.10|RemoteAddr|1+|String...（IP/CIDR）|匹配请求客户端的 IP 地址是否在指定列表（如 `192.168.1.1/24`​）|
|5.11|Weight|2|String（组名）, int（权重）|按权重分组路由，相同组名的路由按权重比例分配流量|
|5.12|XForwardedRemoteAddr|1+|String...（IP/CIDR）|匹配 `X-Forwarded-For`​ 头中的客户端 IP（用于反向代理后的真实客户端IP）|

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id : bing-route
          uri : lb://www.bing.com
          predicates:
            - Path=/search/** # 短写法
 			# 长写法
            - name: Query # 断言方法
              args: # 断言参数
                param: q
                regex: haha
```

> 作用：只有路由为search?q=haha,才会调用这个

### 自定义断言工厂

> 编写方法继承`AbstractRoutePredicateFactory`​，<span data-type="text" style="color: var(--b3-font-color9);">类名不可以随便取，必须是想要的断言的 名字+</span>`RoutePredicateFactory`​​<span data-type="text" style="color: var(--b3-font-color9);">，不然会报错，比如我想指定断言的名字是Role，类名就必须是</span>`RoleRoutePredicateFactory`​​

```java
public class RoleRoutePredicateFactory extends AbstractRoutePredicateFactory<RoleRoutePredicateFactory.Config> 
```

> 编写config类,写自定义断言需要的参数(内部类)

```java
@Validated
public static class Config {
	@NotEmpty
	private String param;
	@NotEmpty
	private String value;
	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
```

> 创建构造函数的实现超类

```java
public RoleRoutePredicateFactory() {
    super(Config.class);
}
```

> 设置短格式撰写的参数编写的顺序

```java
@Override
public List<String> shortcutFieldOrder() {
    return Arrays.asList("param", "value");
}
```

> 重写apply方法，用于 写逻辑和处理方法

```java
@Override
public Predicate<ServerWebExchange> apply(Config config) {
    return new GatewayPredicate() {
        @Override
        public boolean test(ServerWebExchange serverWebExchange) {
            //localhost/search?q=haha&vip=true
            ServerHttpRequest request = serverWebExchange.getRequest();
            String first = request.getQueryParams().getFirst(config.param);
            return StringUtils.hasText(first) && first.equals(config.value);
        }
    };
}
```

> 别忘了注入容器

```java
@Component
public class RoleRoutePredicateFactoryextends AbstractRoutePredicateFactory<RoleRoutePredicate.Config> {
```

> 使用 localhost/search?q=haha&vip=true 才可以到达

```yaml
spring:
#  profiles:
#    active: route
  cloud:
    gateway:
      routes:
        - id: bing-route
          uri: https://cn.bing.com
          predicates:
            - Path=/search
            - name: Query
              args:
                param: q
                regex: haha
            - name: Role
              args:
                param: vip
                value: true
```

> 测试

![image](assets/image-20250416113105-rey7b3e.png)

![image](assets/image-20250416113118-30sd4us.png)

![image](assets/image-20250416113131-dwdgemr.png)

---

## 过滤器

![image](assets/image-20250416113715-cbdwuh7.png)

|序号|过滤器名称|参数个数|参数类型|是否常用|作用|
| ------| ----------------------| ----------| -----------------------------------------------------------| -----------| -------------------------------------------|
|6.1|AddRequestHeader|2|String（键）, String（值）|✔️|添加请求头|
|6.2|AddRequestParameter|2|String（键）, String（值）|✔️|添加请求查询参数|
|6.3|AddResponseHeader|2|String（键）, String（值）|✔️|添加响应头|
|6.4|PrefixPath|1|String（前缀路径）|✔️|给请求路径添加前缀（如 `/api`​）|
|6.5|RemoveRequestHeader|1|String（头名称）|✔️|移除指定请求头|
|6.6|RemoveResponseHeader|1|String（头名称）|✔️|移除指定响应头|
|6.7|RewritePath|2|正则表达式, 替换字符串|✔️|使用正则表达式重写请求路径（如 `/v1/old/(?<segment>.*)`​ → `/v2/new/$\\{segment}`​）|
|6.8|SetPath|1|String（模板路径）|✔️|通过占位符设置新路径（如 `/new/{segment}`​）|
|6.9|SetRequestHeader|2|String（键）, String（值）|✔️|覆盖指定请求头的值|
|6.10|SetResponseHeader|2|String（键）, String（值）|✔️|覆盖指定响应头的值|
|6.11|SetStatus|1|HttpStatus|✔️|强制设置响应状态码（如 `401`​）|
|6.12|StripPrefix|1|int（剥离层数）|✔️|剥离请求路径的前缀层级（如 `/api/user → /user`​）|
|6.13|Retry|4|retries(int), statuses(List), methods(List), series(List)|⚠️ 按需|配置请求重试策略|
|6.14|RequestRateLimiter|2|限流器Bean, KeyResolver|⚠️ 按需|基于令牌桶算法实现限流|
|6.15|SaveSession|0|-|❌|保存WebSession（用于Spring Session项目）|
|6.16|RequestSize|1|DataSize|⚠️ 按需|限制请求体大小|
|6.17|ModifyRequestBody|1|函数式转换逻辑|❌|通过代码修改请求体（需自定义实现）|
|6.18|ModifyResponseBody|1|函数式转换逻辑|❌|通过代码修改响应体（需自定义实现）|

### 全局过滤器（GlobalFilter）

|类型|是否常用|作用|
| --------------------------| ----------| ----------------------------------------------|
|LoadBalancerClientFilter|✔️|集成Ribbon实现负载均衡（需配合服务发现使用）|
|NettyRoutingFilter|✔️|基于Netty处理HTTP请求转发|
|NettyWriteResponseFilter|✔️|将响应写回客户端|
|RouteToRequestUrlFilter|✔️|将路由地址转换为实际请求URL|

* ✔️：80%以上场景高频使用
* ⚠️ 按需：特定需求使用（如限流、重试）
* ❌：极少使用或需要自定义开发

> 实例-路径重写

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: order-route
          uri: lb://service-order # lb = load balance
          predicates:
            - Path=/api/order/**
          filters:
            - RewritePath=/api/order/?(?<segment>.*), /$\{segment}
        - id: user-route
          uri: lb://service-product
          predicates:
            - Path=/api/product/**
# 实现路径重写功能：将请求路径中/api/product/前缀移除，保留后续路径参数。
# 正则表达式/api/product/?(?<segment>.*)匹配带或不带斜杠的前缀，捕获组segment获取后续路径，最终重写为/${segment}
          filters:
            - RewritePath=/api/product/?(?<segment>.*), /$\{segment}

```

这样就可以将控制器中的 @RequestMapping("/api/XXX")去掉，网关服务会自动的添加然后发送请求

‍

### Default Filters

```yaml
spring:
#  profiles:
#    active: route
  application:
    name: gateway
  cloud:
    gateway:
      default-filters: 
        - name: AddResponseHeader
          args:
            name: X-Response-Header
            value: "Hello World"
# 将所有的请求头上都加上 hello world
```

![image](assets/image-20250416140054-iqbintd.png)​

‍

### Global Filters

> 实例

```java
@Component
@Slf4j
public class MyGlobalFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        log.info("请求路径：{}", request.getURI());
        ServerHttpResponse response = exchange.getResponse();
        log.info("响应状态码：{}", response.getStatusCode());
        long start = System.currentTimeMillis();
        log.info("请求开始时间：{}", start);
        // =============================上面是前置逻辑==============================================
        return chain.filter(exchange)
                .doFinally(
                        result -> {
                            // =============================下面是后置逻辑==============================================
                            long end = System.currentTimeMillis();
                            log.info("请求结束，耗时：{}", end - start);
                        }
                );
    }

    /**
     * 优先级 也可以使用 @Order(0)代替下面内容
     * @return 
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
```

---

#### **默认过滤器（GatewayFilter）** 与**全局过滤器（GlobalFilter）** 的核心区别

|维度|默认过滤器（GatewayFilter）|全局过滤器（GlobalFilter）|
| ------| ------------------------------------------------| --------------------------------------------------|
|**作用范围**|仅对特定路由生效（需在路由配置中显式声明）|对所有路由全局生效（无需路由配置）|
|**配置方式**|通过 `filters`​ 属性在路由规则中定义（YAML/代码）|实现 `GlobalFilter`​ 接口并注册为 Spring Bean|
|**执行阶段**|在路由匹配后执行（作用于单个路由的请求/响应）|在路由匹配前/后均可执行（贯穿整个请求生命周期）|
|**典型用途**|业务相关的定制化处理（如修改请求头、路径重写）|基础设施级处理（如负载均衡、协议转发、全局鉴权）|
|**常见示例**|​`AddRequestHeader`​, `RewritePath`​, `Retry`​|​`LoadBalancerClientFilter`​, `NettyRoutingFilter`​|
|**优先级控制**|通过配置顺序决定（配置中靠前的先执行）|通过 `@Order`​ 注解或 `Ordered`​ 接口定义优先级|
|**是否依赖路由**|必须关联到具体路由规则|完全独立于路由配置|

##### 1. 默认过滤器（GatewayFilter）

* **本质**：通过**过滤器工厂（GatewayFilterFactory）** 生成的实例
* **配置示例**（YAML）：

```yaml
routes:
  - id: user_route
    uri: lb://user-service
    predicates: [...]
    filters:
      - AddRequestHeader=X-User, admin      # 添加请求头
      - RewritePath=/api/(?<segment>.*), /$\{segment}  # 路径重写
```

* **特点**：

  * 需要针对每个路由**显式配置**
  * 常用于**具体业务需求**（如接口版本兼容、参数注入）
  * 执行顺序由配置中的**声明顺序决定**（从上到下）

‍

##### 2. 全局过滤器（GlobalFilter）

* **本质**：实现 `GlobalFilter`​ 接口的 Spring Bean
* **自定义示例**：

```java
@Component
@Order(0)  // 优先级最高
public class AuthFilter implements GlobalFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 全局鉴权逻辑
        if (!checkToken(exchange.getRequest())) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }
}
```

* **特点**：

  * **自动应用到所有路由**（无需在 `routes`​ 中配置）
  * 通常处理**跨路由的通用逻辑**（如全局限流、日志记录、IP黑名单）
  * 通过 `Order`​ 值控制与其他过滤器的**执行顺序**（值越小优先级越高）

##### 执行顺序

```rust
请求进入 Gateway
    │
    ↓
GlobalFilter 1 (如鉴权) → 可中断请求
    │
    ↓
GlobalFilter 2 (如限流) 
    │
    ↓
路由匹配成功 → 执行该路由的 GatewayFilter 链（如 AddRequestHeader）
    │
    ↓
GlobalFilter 3 (如转发请求到后端服务)
    │
    ↓
后端服务响应 → 执行响应相关的 GlobalFilter (如处理响应头)
```

##### 选择

* **用默认过滤器（GatewayFilter）当**：

  * 需要针对**特定接口**修改请求/响应
  * 例如：为 `/order/**`​ 路径添加 `X-Service-Type`​ 头
* **用全局过滤器（GlobalFilter）当**：

  * 需要**所有请求**都遵守的规则
  * 例如：全局限流、统一身份认证、全局日志记录

‍

### 自定义过滤器工厂

```java
@Component
@Slf4j
public class OnceTokenGatewayFilterFactory extends AbstractNameValueGatewayFilterFactory {
    @Override
    public GatewayFilter apply(NameValueConfig config) {
        return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain){
                return chain.filter(exchange).then(
                        Mono.fromRunnable(() -> {
                            ServerHttpResponse response = exchange.getResponse();
                            HttpHeaders headers = response.getHeaders();
                            String value = config.getValue();
                            if ("uuid".equalsIgnoreCase(value)){
                                value = UUID.randomUUID().toString();
                                log.info("生成uuid：{}", value);
                            }
                            if ("jwt".equalsIgnoreCase(value)){
							    // 固定的，就只是用来模拟
                                value = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyMTIzIiwiaWF0IjoxNzEzMjg5ODQwLCJleHAiOjE3MTMyOTM0NDB9.6Yt7kZ4n7XqY2wTJ1f7d9y7zv8Vn7Wk9l7Yr6m6s3Z8";
                            }
                            headers.add(config.getName(), value);
                     })
                );
            }
        };
    }
}
```

> 配置

```yaml
spring:
#  profiles:
#    active: route
  application:
    name: gateway
  cloud:
    nacos:
      server-addr: localhost:8848
    gateway:
      routes:
        - id: order-route
          uri: lb://service-order # lb = load balance
          predicates:
            - Path=/api/order/**
          filters:
            - RewritePath=/api/order/?(?<segment>.*), /$\{segment}
            - OnceToken=X-Response-Token,uuid # 设置value-uuid  
        - id: user-route
          uri: lb://service-product
          predicates:
            - Path=/api/product/**
          filters:
            - RewritePath=/api/product/?(?<segment>.*), /$\{segment}
```

---

### Gateway 全局跨域

单体项目是可以在控制器打注解`@CrossOrigin`​或者做`CrossConfig`​

```yaml
spring:
  cloud:
    nacos:
      server-addr: localhost:8848
    gateway:
# 设置全局cors跨域 允许所有请求的来源，方法，请求头
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins: "*"
            allowedMethods: "*"
            allowed-headers: "*"
```

路由的 CORS 配置

```yaml
spring:
  cloud:
    gateway:
		routes:
        - id: order-route
          uri: lb://service-order # lb = load balance
          predicates:
            - Path=/api/order/**
          filters:
            - RewritePath=/api/order/?(?<segment>.*), /$\{segment}
            - OnceToken=X-Response-Token,jwt
# 配置单个路由的cors配置
        metadata:
          cors
            allowedOrigins: '*'
            allowedMethods: '*'
            allowedHeaders: '*'
            maxAge: 30
```

> 查看到元素内多了跨域请求的允许

![image](assets/image-20250416155557-xbiclns.png)​

‍

## 微服务之间的调用经过网关吗

可以但不需要

> 不经过网关

![image](assets/image-20250416160404-dxma0no.png)​

> 经过网关

![image](assets/image-20250416160548-ep6wwfj.png)

> 怎么才能经过网关：
>
> 将远程调用的url修改成getaway就好，将相关的路由修改成和网关配置的一致就好了

```java
@FeignClient(value = "gateway" , fallback = ProductFeignFallback.class)
public interface ProductFeignClient {

    @GetMapping("/api/product/getProductById/{id}")
    Product getProductById(@PathVariable("id") Integer id);
}
```

---

‍

# Seata

官网：[https://seata.apache.org/zh-cn/](https://seata.apache.org/zh-cn/)

‍
