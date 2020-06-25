package com.capsule.flow.sample.globalhandler;

import com.alibaba.fastjson.JSONObject;
import com.capsule.flow.exception.FlowException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class FlowExceptionHandler {

    @ExceptionHandler(value = FlowException.class)
    @ResponseBody
    public String exceptionHandler(FlowException e) {
        JSONObject result = new JSONObject();
        result.put("code", e.getErrorCode());
        result.put("message", e.getMessage());
        return result.toJSONString();
    }
}
