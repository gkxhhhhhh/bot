# BTC Bot Spring Boot

基于 **Spring Boot 2.7.18 + Java 8 + MyBatis-Plus + MySQL** 的 BTC 现货策略项目。

## 1. 你现在拿到的内容

- 完整 Maven 项目骨架
- 所有配置项入库方案
- 每次执行入库日志
- 下单 / 撤单 / OCO / 平仓流水入库
- 持仓表、事件表、运行态表
- `controller + service + mapper + entity`
- 手动触发一轮策略接口

## 2. 数据库初始化

执行：

```sql
source sql/init.sql;
```

然后把表 `t_bot_config` 里的这两个值改成你自己的：

- `BINANCE_API_KEY`
- `BINANCE_API_SECRET`

## 3. 启动前修改 application.yml

把数据库连接改成你的：

- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

## 4. 启动方式

```bash
mvn clean package -DskipTests
java -jar target/btc-bot-springboot-1.0.0.jar
```

## 5. 核心接口

### 手动执行一轮

```http
POST /api/bot/run-once
Content-Type: application/json

{
  "symbol": "BTCUSDT"
}
```

`symbol` 可不传；不传时会自动取配置表里的 `BINANCE_SYMBOL`。

### 配置分页

```http
GET /api/config/page?current=1&size=20
```

### 配置保存

```http
POST /api/config/save
Content-Type: application/json
```

### 执行日志分页

```http
GET /api/record/executions?current=1&size=20
```

### 订单流水分页

```http
GET /api/record/orders?current=1&size=20
```

### 事件提醒分页

```http
GET /api/record/events?current=1&size=20
```

### 持仓分页

```http
GET /api/record/positions?current=1&size=20
```

## 6. 代码职责划分

- `BotExecutionServiceImpl`：策略主流程
- `BinanceClientServiceImpl`：真实 Binance REST 下单与查询
- `BotConfigServiceImpl`：每次从配置表取配置
- `RuntimeStateServiceImpl`：运行状态落库
- `BotRecordServiceImpl`：日志、订单、持仓、事件入库
- `BotMessageServiceImpl`：统一消息发送入口，当前只 `System.out.println`

## 7. 当前策略

### 规则3

- 当前价在 `[L, L + 0.25R]`
- 现价买入
- 成交后挂 OCO：
  - 止盈 = 成交价 * 1.02
  - 止损 = 成交价 * (1 - 0.012)

### 规则4

- 当前价 `<= L + 0.50R`
- 且 `((H - L) / 当前价) * 100 > 1.5`
- 在 `当前价 * (1 - 0.015)` 挂限价买单
- 若未成交且价格涨到 `L0 + 0.75R0`，撤单失效
- 若成交，则按实际成交价挂 OCO
- 若持仓 24h 内止盈止损都未触发，撤 OCO 并现价平仓

## 8. 提醒规则

当前只在以下情况调用 `BotMessageService.send(String message)`：

- 买入成交
- 卖出成交
- 挂单失效
- 超时强平

持仓继续有效 / 挂单继续有效时，不提醒。

## 9. 你后续最适合继续改的地方

### 9.1 接钉钉机器人

直接改这里：

- `com.example.btcbot.service.impl.BotMessageServiceImpl`

当前只保留了一个入参：

```java
void send(String message)
```

### 9.2 做自动轮询

现在项目默认提供的是“手动触发一轮”。

如果你要改成自动轮询，建议新建一个定时任务类，内部直接调用：

```java
botExecutionService.runOnce(null);
```

### 9.3 更细的风控

你可以继续把这些参数也入表：

- 最小账户余额限制
- 最大连续亏损限制
- 单日最大交易次数
- 单日最大亏损阈值

