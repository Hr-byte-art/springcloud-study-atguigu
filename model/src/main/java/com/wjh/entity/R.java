package com.wjh.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class R {
    // Getters and Setters
    private Integer code;
    private String msg;
    private Object data;

    // Static methods for OK responses
    public static R ok() {
        R r = new R();
        r.setCode(200);
        r.setMsg("success");
        r.data=null;
        return r;
    }

    public static R ok(String msg) {
        R r = new R();
        r.setCode(200);
        r.setMsg(msg);
        r.data=null;
        return r;
    }

    public static R ok(Integer code, String msg) {
        R r = new R();
        r.setCode(code);
        r.setMsg(msg);
        r.data=null;
        return r;
    }

    public static R ok(Object data) {
        R r = ok();
        r.setCode(200);
        r.setMsg("success");
        r.setData(data);
        return r;
    }

    // Static methods for ERROR responses
    public static R error() {
        R r = new R();
        r.setCode(500);
        r.setMsg("error");
        r.data=null;
        return r;
    }

    public static R error(Integer code, String msg) {
        R r = new R();
        r.setCode(code);
        r.setMsg(msg);
        r.data=null;
        return r;
    }

    public static R error(String msg) {
        R r = new R();
        r.setCode(500);
        r.setMsg(msg);
        r.data=null;
        return r;
    }

    public static R error(Object data) {
        R r = error();
        r.setData(data);
        return r;
    }
}