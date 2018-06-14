package com.jfinal.api.api.util;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.jfinal.kit.StrKit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * 常用工具类
 *
 * @author zhangby
 * @date 2018/6/12 下午3:39
 */
public class CommUtil {
    /**
     * 字符串模板替换 截取
     * ClassTest::getDictList4Function,{}::{} ->[ClassTest,getDictList4Function]
     *
     * @param str
     * @param temp
     * @return
     */
    public static List<String> splitStr4Temp(String str, String temp) {
        List<String> rsList = Lists.newArrayList();
        Iterator<String> iterator = Splitter.on("{}").omitEmptyStrings().split(temp).iterator();
        while (iterator.hasNext()) {
            str = str.replace(iterator.next(), "〆");
        }
        Iterator<String> split = Splitter.on("〆").omitEmptyStrings().split(str).iterator();
        while (split.hasNext()) {
            rsList.add(split.next());
        }
        return rsList;
    }

    /**
     * 字符串分割
     *
     * @param Str  字符串
     * @param temp 分割字符串
     * @return
     */
    public static List<String> splitStr(String Str, String temp) {
        if (StrKit.isBlank(Str)) return Collections.EMPTY_LIST;
        List<String> li = Lists.newArrayList();
        Iterator<String> split = Splitter.on(temp).omitEmptyStrings().trimResults().split(Str).iterator();
        while (split.hasNext()) {
            li.add(split.next());
        }
        return li;
    }

    /**
     * list数据转换
     * @param list list对象
     * @param func lamdba 表达式 function
     * @param <E> 原对象
     * @param <T> 转换完的对象
     * @return
     */
    public static <E,T> List<E> convers(List<T> list, Function<T, E> func) {
        if (ObjectUtil.isNull(list)) return Collections.EMPTY_LIST;
        return list.stream().collect(ArrayList::new, (li, p) -> li.add(function(p, func)), List::addAll);
    }
    /**
     * 接收T对象，返回E对象
     * @param t
     * @param func
     * @param <T>
     * @param <E>
     * @return
     */
    public static  <T,E> E function(T t,Function<T,E> func){
        return func.apply(t);
    }

    /**
     * JsonArray list -> Dict list
     * @param list
     * @return
     *
     * @author zhangby
     * @date 2018/4/26 上午11:40
     */
    public static List<Dict> jsonArray2DictList(Object list) {
        Function<Object, Dict> func = d -> JSON.parseObject(JSON.toJSONString(d), Dict.class);
        List<Object> li = Convert.convert(List.class, list);
        return convers(li,func);
    }
}
