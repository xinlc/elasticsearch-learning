package com.jd.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.context.annotation.Configuration;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * @projectName: jd-search-api
 * @className: com.jd.config.BigDecimalSerializer
 * @description: BigDecimal序列化设置
 * @author: tong.li
 * @createTime: 2020/12/10 20:16
 * @version: v1.0
 * @copyright: 版权所有 © 李彤
 */
@Configuration
public class BigDecimalSerializer extends JsonSerializer<BigDecimal> {

    @Override
    public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null || value.compareTo(BigDecimal.ZERO) == 0) {
            gen.writeString("0.00");
            return;
        }
        DecimalFormat df = new DecimalFormat("#.00");
        gen.writeString(df.format(value));
    }

}
