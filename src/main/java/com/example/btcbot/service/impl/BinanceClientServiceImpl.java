package com.example.btcbot.service.impl;

import com.example.btcbot.common.BizException;
import com.example.btcbot.service.BinanceClientService;
import com.example.btcbot.service.BotConfigService;
import com.example.btcbot.util.HmacHelper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import com.example.btcbot.dto.binance.Recent24hRangeDTO;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BinanceClientServiceImpl implements BinanceClientService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final BotConfigService botConfigService;

    public BinanceClientServiceImpl(BotConfigService botConfigService) {
        this.botConfigService = botConfigService;
    }

//    @Override
//    public Map<String, Object> getTicker24h(String symbol) {
//        Map<String, String> params = new LinkedHashMap<String, String>();
//        params.put("symbol", symbol);
//        return publicGet("/v3/ticker/24hr", params);
//    }

    @Override
    public Map<String, Object> getExchangeInfo(String symbol) {
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("symbol", symbol);
        return publicGet("/v3/exchangeInfo", params);
    }

    @Override
    public Map<String, Object> getAccount() {
        return signedRequest(HttpMethod.GET, "/v3/account", new LinkedHashMap<String, String>());
    }

    @Override
    public Map<String, Object> placeMarketBuyByQuote(String symbol, BigDecimal quoteOrderQty, String clientOrderId) {
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("symbol", symbol);
        params.put("side", "BUY");
        params.put("type", "MARKET");
        params.put("quoteOrderQty", quoteOrderQty.stripTrailingZeros().toPlainString());
        params.put("newClientOrderId", clientOrderId);
        params.put("newOrderRespType", "FULL");
        return signedRequest(HttpMethod.POST, "/v3/order", params);
    }

    @Override
    public Map<String, Object> placeLimitBuy(String symbol, BigDecimal quantity, BigDecimal price, String clientOrderId) {
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("symbol", symbol);
        params.put("side", "BUY");
        params.put("type", "LIMIT");
        params.put("timeInForce", "GTC");
        params.put("quantity", quantity.stripTrailingZeros().toPlainString());
        params.put("price", price.stripTrailingZeros().toPlainString());
        params.put("newClientOrderId", clientOrderId);
        params.put("newOrderRespType", "RESULT");
        return signedRequest(HttpMethod.POST, "/v3/order", params);
    }

    @Override
    public Map<String, Object> placeMarketSell(String symbol, BigDecimal quantity, String clientOrderId) {
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("symbol", symbol);
        params.put("side", "SELL");
        params.put("type", "MARKET");
        params.put("quantity", quantity.stripTrailingZeros().toPlainString());
        params.put("newClientOrderId", clientOrderId);
        params.put("newOrderRespType", "FULL");
        return signedRequest(HttpMethod.POST, "/v3/order", params);
    }

    @Override
    public Map<String, Object> placeOcoSell(String symbol, BigDecimal quantity,
                                            BigDecimal takeProfitPrice, BigDecimal stopPrice,
                                            BigDecimal stopLimitPrice,
                                            String listClientOrderId,
                                            String tpClientOrderId,
                                            String slClientOrderId) {
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("symbol", symbol);
        params.put("side", "SELL");
        params.put("quantity", quantity.stripTrailingZeros().toPlainString());
        params.put("aboveType", "LIMIT_MAKER");
        params.put("abovePrice", takeProfitPrice.stripTrailingZeros().toPlainString());
        params.put("aboveClientOrderId", tpClientOrderId);
        params.put("belowType", "STOP_LOSS_LIMIT");
        params.put("belowStopPrice", stopPrice.stripTrailingZeros().toPlainString());
        params.put("belowPrice", stopLimitPrice.stripTrailingZeros().toPlainString());
        params.put("belowTimeInForce", "GTC");
        params.put("belowClientOrderId", slClientOrderId);
        params.put("listClientOrderId", listClientOrderId);
        params.put("newOrderRespType", "RESULT");
        return signedRequest(HttpMethod.POST, "/v3/orderList/oco", params);
    }

    @Override
    public Map<String, Object> queryOrder(String symbol, Long orderId) {
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("symbol", symbol);
        params.put("orderId", String.valueOf(orderId));
        return signedRequest(HttpMethod.GET, "/v3/order", params);
    }

    @Override
    public Map<String, Object> queryOrderList(Long orderListId) {
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("orderListId", String.valueOf(orderListId));
        return signedRequest(HttpMethod.GET, "/v3/orderList", params);
    }

    @Override
    public Map<String, Object> cancelOrder(String symbol, Long orderId) {
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("symbol", symbol);
        params.put("orderId", String.valueOf(orderId));
        return signedRequest(HttpMethod.DELETE, "/v3/order", params);
    }

    @Override
    public Map<String, Object> cancelOrderList(String symbol, Long orderListId) {
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("symbol", symbol);
        params.put("orderListId", String.valueOf(orderListId));
        return signedRequest(HttpMethod.DELETE, "/v3/orderList", params);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> publicGet(String path, Map<String, String> params) {
        String query = buildQuery(params);
        String url = publicBaseUrl() + path + (query.isEmpty() ? "" : "?" + query);
        try {
            return restTemplate.getForObject(url, Map.class);
        } catch (HttpStatusCodeException e) {
            throw new BizException("Binance公开接口失败: " + e.getResponseBodyAsString());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> signedRequest(HttpMethod method, String path, Map<String, String> params) {
        String apiKey = botConfigService.getRequiredString("BINANCE_API_KEY");
        String apiSecret = botConfigService.getRequiredString("BINANCE_API_SECRET");
        String recvWindow = botConfigService.getEnabledConfigMap().get("BINANCE_RECV_WINDOW");
        params.put("recvWindow", recvWindow == null ? "5000" : recvWindow);
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));

        String query = buildQuery(params);
        String signature = HmacHelper.hmacSha256Hex(query, apiSecret);
        String body = query + "&signature=" + urlEncode(signature);
        String url = restBaseUrl() + path;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("X-MBX-APIKEY", apiKey);
        HttpEntity<String> entity = new HttpEntity<String>(body, headers);
        try {
            return restTemplate.exchange(url, method, entity, Map.class).getBody();
        } catch (HttpStatusCodeException e) {
            e.printStackTrace();
            throw new BizException("Binance签名接口失败: " + e.getResponseBodyAsString());
        }
    }

    private String restBaseUrl() {
        boolean useTestnet = botConfigService.getBoolean("BINANCE_USE_TESTNET", true);
        return useTestnet ? "https://testnet.binance.vision/api" : "https://api.binance.com/api";
    }

    private String publicBaseUrl() {
        boolean useTestnet = botConfigService.getBoolean("BINANCE_USE_TESTNET", true);
        return useTestnet ? "https://testnet.binance.vision/api" : "https://data-api.binance.vision/api";
    }

    private String buildQuery(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) {
                sb.append("&");
            }
            first = false;
            sb.append(urlEncode(entry.getKey())).append("=").append(urlEncode(entry.getValue()));
        }
        return sb.toString();
    }

    private String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 策略行情建议固定走主网公共行情
     * 不要跟下单 testnet 混用
     */
    private static final String MARKET_DATA_BASE_URL = "https://data-api.binance.vision";

    private final OkHttpClient okHttpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, Object> getTicker24h(String symbol) {
        try {
            String url = MARKET_DATA_BASE_URL + "/api/v3/ticker/24hr?symbol=" + urlEncode(symbol);
            String body = doGet(url);
            return objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new RuntimeException("获取24小时ticker失败，symbol=" + symbol, e);
        }
    }

    @Override
    public Recent24hRangeDTO getRecent24hRange(String symbol) {
        try {
            long endTime = System.currentTimeMillis();
            long startTime = endTime - 24L * 60L * 60L * 1000L;

            List<List<Object>> allKlines = new ArrayList<List<Object>>();

            long cursor = startTime;
            while (cursor < endTime) {
                String url = MARKET_DATA_BASE_URL
                        + "/api/v3/klines?symbol=" + urlEncode(symbol)
                        + "&interval=1m"
                        + "&startTime=" + cursor
                        + "&endTime=" + endTime
                        + "&limit=1000";

                String body = doGet(url);

                List<List<Object>> klines = objectMapper.readValue(
                        body,
                        new TypeReference<List<List<Object>>>() {}
                );

                if (klines == null || klines.isEmpty()) {
                    break;
                }

                allKlines.addAll(klines);

                // kline:
                // 0 openTime
                // 1 open
                // 2 high
                // 3 low
                // 4 close
                // 5 volume
                // 6 closeTime
                List<Object> last = klines.get(klines.size() - 1);
                long lastCloseTime = Long.parseLong(String.valueOf(last.get(6)));

                // 防止死循环
                if (lastCloseTime < cursor) {
                    break;
                }

                cursor = lastCloseTime + 1;

                // 少于1000说明已经拉完
                if (klines.size() < 1000) {
                    break;
                }
            }

            if (allKlines.isEmpty()) {
                throw new RuntimeException("最近24小时k线为空，symbol=" + symbol);
            }

            BigDecimal highPrice = null;
            BigDecimal lowPrice = null;

            for (List<Object> kline : allKlines) {
                BigDecimal high = new BigDecimal(String.valueOf(kline.get(2)));
                BigDecimal low = new BigDecimal(String.valueOf(kline.get(3)));

                if (highPrice == null || high.compareTo(highPrice) > 0) {
                    highPrice = high;
                }
                if (lowPrice == null || low.compareTo(lowPrice) < 0) {
                    lowPrice = low;
                }
            }

            BigDecimal currentPrice = getCurrentPrice(symbol);

            Recent24hRangeDTO dto = new Recent24hRangeDTO();
            dto.setHighPrice(highPrice);
            dto.setLowPrice(lowPrice);
            dto.setCurrentPrice(currentPrice);
            dto.setStartTime(startTime);
            dto.setEndTime(endTime);
            dto.setKlineCount(allKlines.size());
            return dto;

        } catch (Exception e) {
            throw new RuntimeException("计算最近24小时价格区间失败，symbol=" + symbol, e);
        }
    }

    /**
     * 获取当前最新价格
     */
    private BigDecimal getCurrentPrice(String symbol) throws Exception {
        String url = MARKET_DATA_BASE_URL + "/api/v3/ticker/price?symbol=" + urlEncode(symbol);
        String body = doGet(url);

        Map<String, Object> result = objectMapper.readValue(
                body,
                new TypeReference<Map<String, Object>>() {}
        );

        Object price = result.get("price");
        if (price == null) {
            throw new RuntimeException("获取当前价格失败，返回中无 price 字段，symbol=" + symbol);
        }
        return new BigDecimal(String.valueOf(price));
    }

    private String doGet(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        Response response = okHttpClient.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("HTTP请求失败，code=" + response.code() + ", url=" + url);
        }

        if (response.body() == null) {
            throw new IOException("HTTP响应体为空，url=" + url);
        }

        return response.body().string();
    }


    @Override
    public Long getServerTime() {
        try {
            String url = MARKET_DATA_BASE_URL + "/api/v3/time";
            String body = doGet(url);

            Map<String, Object> result = objectMapper.readValue(
                    body,
                    new TypeReference<Map<String, Object>>() {}
            );

            Object serverTime = result.get("serverTime");
            if (serverTime == null) {
                throw new RuntimeException("获取Binance服务器时间失败，返回中无 serverTime 字段");
            }
            return Long.parseLong(String.valueOf(serverTime));
        } catch (Exception e) {
            throw new RuntimeException("获取Binance服务器时间失败", e);
        }
    }
}
