package com.wjh.exception;

import com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSONObject;
import com.wjh.entity.R;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
/**
 * @author 王哈哈
 */
@Component
public class MyBlockExceptionHandler implements BlockExceptionHandler {
    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, String s, BlockException e) throws Exception {
        httpServletResponse.setContentType("application/json;Charset=Utf-8");

        PrintWriter writer = httpServletResponse.getWriter();
        R error = R.error(e + "被Sentinel限制了，原因：" + e.getClass());
        String jsonString = JSONObject.toJSONString(error);
        writer.write(jsonString);
        writer.flush();
        writer.close();
    }
}
