package com.example.btcbot.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class BotConfigSaveRequest {
    private Long id;
    @NotBlank(message = "configKey不能为空")
    private String configKey;
    @NotBlank(message = "configValue不能为空")
    private String configValue;
    @NotBlank(message = "valueType不能为空")
    private String valueType;
    private String remark;
    @NotNull(message = "enabled不能为空")
    private Integer enabled;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getConfigKey() { return configKey; }
    public void setConfigKey(String configKey) { this.configKey = configKey; }
    public String getConfigValue() { return configValue; }
    public void setConfigValue(String configValue) { this.configValue = configValue; }
    public String getValueType() { return valueType; }
    public void setValueType(String valueType) { this.valueType = valueType; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Integer getEnabled() { return enabled; }
    public void setEnabled(Integer enabled) { this.enabled = enabled; }
}
