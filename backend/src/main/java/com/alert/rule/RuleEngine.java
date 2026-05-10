package com.alert.rule;

import com.alibaba.fastjson.JSON;
import com.alert.entity.AlertRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import java.util.Map;

@Slf4j
@Component
public class RuleEngine {
    private final ExpressionParser parser = new SpelExpressionParser();

    public boolean evaluate(AlertRule rule, Map<String, Object> data) {
        try {
            StandardEvaluationContext context = new StandardEvaluationContext();
            data.forEach(context::setVariable);
            Boolean result = parser.parseExpression(rule.getRuleExpression()).getValue(context, Boolean.class);
            log.debug("规则[{}]评估结果: {}, 表达式: {}, 数据: {}", 
                    rule.getName(), result, rule.getRuleExpression(), JSON.toJSONString(data));
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("规则评估失败: {}", e.getMessage());
            return false;
        }
    }

    public String formatMessage(String template, Map<String, Object> data) {
        if (template == null) return JSON.toJSONString(data);
        String result = template;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            result = result.replace("${" + entry.getKey() + "}", String.valueOf(entry.getValue()));
        }
        return result;
    }
}
