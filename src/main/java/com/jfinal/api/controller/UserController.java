package com.jfinal.api.controller;

import com.jfinal.core.Controller;
import com.jfinal.kit.Kv;

/**
 * 用户管理
 * @action /user
 * @author zhangby
 * @date 2018/6/12 下午3:26
 */
public class UserController extends Controller{

    /**
     * 用户登录功
     * @title 登录接口
     * @param username|用户名|string|必填
     * @param password|密码|string|必填
     * @resqParam code|用户名|String|必填
     * @resqParam data|数据|object|非必填
     * @resqParam msg|消息信息|String|必填
     * @respBody {"code":"000","data":"","msg":"success"}
     * @requestType post
     * @author zhangby
     * @date 2018/6/12 下午4:23
     */
    public void login() {
        renderJson(Kv.create().set("code","000"));
    }
}
