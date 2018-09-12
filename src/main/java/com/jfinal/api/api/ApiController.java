package com.jfinal.api.api;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jfinal.api.api.util.AnnotationParse;
import com.jfinal.api.api.util.ApiClassDoc;
import com.jfinal.api.api.util.CommUtil;
import com.jfinal.core.Controller;
import com.jfinal.kit.*;
import com.jfinal.template.stat.ast.If;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * 生成API调用接口
 *
 * @author zhangby
 * @date 2018/5/17 下午1:24
 */
public class ApiController extends Controller {

    //指定扫描包
    private static List<String> packages = Lists.newArrayList(getApiProp().get("packages").split(","));
    //指定需要的过滤Controller
    private static List<String> filters = Lists.newArrayList(getApiProp().get("filters").split(","));
    //生成解析实例
    private static AnnotationParse annotationParse = AnnotationParse.getInstance(packages).filter(filters);

    public void index() {
        render("/api/index.html");
    }

    /**
     * 获取主题色，暂时只支持：dark、light
     *
     * @author zhangby
     * @date 2018/6/21 下午2:59
     */
    public void getTheme() {
       renderText(JSON.toJSONString(Kv.create().set("theme",getApiProp().get("theme","dark"))));
    }

    /**
     * 获取菜单数据
     *
     * @author zhangby
     * @date 2018/5/19 下午1:55
     */
    public void getMenuData() {
        //调用解析方法
        Map<String, Dict> dataMap = annotationParse.getApiData4Depth().getApiDataMap();
        // => 生成返回数据
        List<Kv> menuList = Lists.newArrayList();
        dataMap.values().forEach(dict -> {
            Kv kv = Kv.create()
                    .set("menuName", StrKit.isBlank(dict.getStr("commentText")) ? dict.get("action") : dict.get("commentText"))
                    .set("href", dict.get("action"))
                    .set("actionName", dict.get("name"));
            List<Kv> children = Lists.newArrayList();
            //添加方法
            Convert.convert(Map.class, dict.get("methods")).forEach((k, v) -> {
                Dict dt = Convert.convert(Dict.class, v);
                Kv child = Kv.create()
                        .set("menuName", StrKit.isBlank(dt.getStr("title")) ? dt.get("name") : dt.getStr("title"))
                        .set("href", dt.get("name"))
                        .set("methodName", dt.get("name"))
                        .set("author", dt.get("author"));
                children.add(child);
            });
            kv.set("children", children);
            menuList.add(kv);
        });
        renderText(JSON.toJSONString(menuList));
    }

    /**
     * 首页统计
     *
     * @author zhangby
     * @date 2018/6/12 下午4:55
     */
    public void getApiHome() {
        //调用解析方法
        Map<String, Dict> dataMap = annotationParse.getApiData4Depth().getApiDataMap();
        List<String> actionList = Lists.newArrayList();
        List<String> methodList = Lists.newArrayList();
        Set<String> authorList = Sets.newHashSet();
        dataMap.values().forEach(d -> {
            actionList.add(d.getStr("name"));
            Convert.convert(Map.class, d.get("methods")).forEach((k, v) -> {
                Dict dt = Convert.convert(Dict.class, v);
                methodList.add(dt.getStr("name"));
                authorList.add(dt.getStr("author"));
            });
        });
        Dict dict = Dict.create()
                .set("actionNum", actionList.size())
                .set("methodNum", methodList.size())
                .set("authorNum", authorList.size())
                .set("authorList", authorList);
        renderText(JSON.toJSONString(dict));
    }

    /**
     * 接口贡献度查询
     *
     * @author zhangby
     * @date 2018/6/19 下午6:53
     */
    public void getCalendarData() {
        //调用解析方法
        Map<String, Dict> dataMap = annotationParse.getApiData4Depth().getApiDataMap();
        List<Kv> methodAll = Lists.newArrayList();
        dataMap.values().forEach(dict -> {
            Convert.convert(Map.class, dict.get("methods")).forEach((k, v) -> {
                Dict dt = Convert.convert(Dict.class, v);
                if (StrKit.notBlank(dt.getStr("date"))) {
                    String api_date = CommUtil.splitStr(dt.getStr("date"), " ").get(0);
                    methodAll.add(Kv.create()
                            .set("date", DateUtil.formatDate(CommUtil.parseDate(api_date))));
                }
            });
        });
        Map<String, Long> date = methodAll.stream()
                .collect(Collectors.groupingBy(dt -> dt.getStr("date"), Collectors.counting()));
        List<List<String>> rsList = Lists.newArrayList();
        date.forEach((k, v) -> rsList.add(Lists.newArrayList(k, v.toString())));
        renderText(JSON.toJSONString(
                Kv.create()
                        .set("date", rsList)
                        .set("year", Lists.newArrayList(DateUtil.formatDate(new Date()), DateUtil.formatDate(DateUtil.offsetDay(new Date(), -365))))
        ));
    }

    /**
     * 周更新
     *
     * @author zhangby
     * @date 2018/6/20 下午4:25
     */
    public void getWeekGroup() {
        //调用解析方法
        Map<String, Dict> dataMap = annotationParse.getApiData4Depth().getApiDataMap();
        List<Kv> methodAll = Lists.newArrayList();
        dataMap.values().forEach(dict -> {
            Convert.convert(Map.class, dict.get("methods")).forEach((k, v) -> {
                Dict dt = Convert.convert(Dict.class, v);
                if (StrKit.notBlank(dt.getStr("date")) || StrKit.notBlank(dt.getStr("author"))) {
                    String api_date = CommUtil.splitStr(dt.getStr("date"), " ").get(0);
                    methodAll.add(Kv.create()
                            .set("date", DateUtil.formatDate(CommUtil.parseDate(api_date))));
                }
            });
        });
        Okv weekDate = Okv.create();
        for (int i = 6; i >= 0; i--) {
            weekDate.put(DateUtil.formatDate(DateUtil.offsetDay(new Date(), -i)), 0);
        }
        methodAll.stream()
                .filter(kv ->
                        kv.getStr("date").compareTo(DateUtil.formatDate(new Date())) <= 0 &&
                                kv.getStr("date").compareTo(DateUtil.formatDate(DateUtil.offsetWeek(new Date(), -1))) >= 0)
                .collect(Collectors.groupingBy(kv -> kv.getStr("date"), Collectors.counting()))
                .forEach((k, v) -> weekDate.put(k, v));
        Kv weekGroup = Kv.create()
                .set("xAxis", weekDate.keySet())
                .set("series", weekDate.values());

        renderText(JSON.toJSONString(weekGroup));
    }

    /**
     * 模块分组
     */
    public void getModelGroup() {
        //调用解析方法
        Map<String, Dict> dataMap = annotationParse.getApiData4Depth().getApiDataMap();
        List<Kv> modelGroup = CommUtil.convers(dataMap.values(), dict ->
                Kv.create()
                        .set("name", StrKit.isBlank(dict.getStr("commentText")) ? dict.get("action") : dict.get("commentText"))
                        .set("value", Convert.convert(Map.class, dict.get("methods")).size()));
        renderText(JSON.toJSONString(modelGroup));
    }


    /**
     * 获取接口分组数据 [作者统计]
     *
     * @author zhangby
     * @date 2018/6/20 上午11:49
     */
    public void getAuthorGroup() {
        //调用解析方法
        Map<String, Dict> dataMap = annotationParse.getApiData4Depth().getApiDataMap();
        List<Kv> methodAll = Lists.newArrayList();
        dataMap.values().forEach(dict -> {
            Convert.convert(Map.class, dict.get("methods")).forEach((k, v) -> {
                Dict dt = Convert.convert(Dict.class, v);
                if (StrKit.notBlank(dt.getStr("date")) || StrKit.notBlank(dt.getStr("author"))) {
                    String api_date = CommUtil.splitStr(dt.getStr("date"), " ").get(0);
                    methodAll.add(Kv.create()
                            .set("author", dt.get("author")));
                }
            });
        });

        /** 作者分组 */
        List<Kv> list = Lists.newArrayList();
        methodAll.stream()
                .collect(Collectors.groupingBy(kv -> kv.getStr("author"), Collectors.counting()))
                .forEach((k, v) -> list.add(Kv.create().set("author", k).set("num", v)));
        //排序
        List<Kv> authorGroup = list.stream().sorted((k1, k2) -> -k1.getStr("num").compareTo(k2.getStr("num"))).collect(Collectors.toList());
        //添加头像
        List<String> headImgs = Lists.newArrayList(
                "https://img.alicdn.com/tfs/TB1j159r21TBuNjy0FjXXajyXXa-499-498.png_80x80.jpg",
                "https://img.alicdn.com/tfs/TB1FGimr1SSBuNjy0FlXXbBpVXa-499-498.png_80x80.jpg",
                "https://img.alicdn.com/tfs/TB1AdOerVOWBuNjy0FiXXXFxVXa-499-498.png_80x80.jpg");
        for (int i=0;i<authorGroup.size();i++) {
            authorGroup.get(i).set("headImg", headImgs.get(i % 3));
        }
        renderText(JSON.toJSONString(authorGroup));
    }

    /**
     * 获取API名称
     *
     * @author zhangby
     * @date 2018/5/19 下午1:56
     */
    public void getMethodTitle() {
        Dict methodTitle = Dict.create();
        //调用解析方法
        Map<String, Dict> dataMap = annotationParse.getApiData4Depth(getPara("actionName"), getPara("method")).getApiDataMap();
        System.out.println(dataMap);
        if (!dataMap.isEmpty()) {
            Dict actionDict = dataMap.get(getPara("actionName"));
            Dict methodDict = Convert.convert(Dict.class, Convert.convert(Map.class, actionDict.get("methods")).get(getPara("method")));
            methodTitle.set("title", StrKit.isBlank(methodDict.getStr("title")) ? methodDict.get("name") : methodDict.getStr("title"))
                    .set("actionTitle", StrKit.isBlank(actionDict.getStr("commentText")) ? actionDict.get("action") : actionDict.get("commentText"))
                    .set("desc", methodDict.get("commentText"))
                    .set("url", ("/".equals(actionDict.get("action")) ? "" : actionDict.get("action")) + "/" + methodDict.get("name"))
                    .set("respText", ObjectUtil.isNull(methodDict.get("respBody")) ? "{}" : methodDict.get("respBody"))
                    .set("author", methodDict.get("author"))
                    .set("requestType", StrKit.notBlank(methodDict.getStr("requestType")) ?
                            methodDict.getStr("requestType").split(",") : new String[]{"post"});
        }
        System.out.println(JSON.toJSONString(methodTitle));
        renderText(JSON.toJSONString(methodTitle));
    }

    /**
     * 获取参数数据
     * action controller 名称
     * method 方法名称
     * paramType 参数类型：1 入参，2 出参
     *
     * @author zhangby
     * @date 2018/5/19 下午10:54
     */
    public void getReqParam() {
        String json = "";
        Dict dataMap = annotationParse.getApiData4Depth(getPara("actionName"), getPara("method")).getMethodInfo();
        if (Objects.equals("1", getPara("paramType"))) {
            List<String> param = Convert.convert(List.class, dataMap.get("param") instanceof String ? Lists.newArrayList(dataMap.get("param")) : dataMap.get("param"));
            json = parseApiTableJson(param, (l, p) -> {
                List<String> splitStr4Temp = CommUtil.splitStr4Temp(p, "{}|");
                List<String> keys = CommUtil.splitStr(splitStr4Temp.get(0), ".");
                l.put(splitStr4Temp.get(0),
                        Kv.create()
                                .set("name", keys.get(keys.size() - 1))
                                .set("desc", splitStr4Temp.get(1))
                                .set("type", splitStr4Temp.get(2))
                                .set("required", splitStr4Temp.get(3))
                );
            });
        } else {
            List<String> param = Convert.convert(List.class, dataMap.get("resqParam") instanceof String ? Lists.newArrayList(dataMap.get("resqParam")) : dataMap.get("resqParam"));
            json = parseApiTableJson(param, (l, p) -> {
                List<String> splitStr4Temp = CommUtil.splitStr4Temp(p, "{}|");
                List<String> keys = CommUtil.splitStr(splitStr4Temp.get(0), ".");
                l.put(splitStr4Temp.get(0),
                        Kv.create()
                                .set("name", keys.get(keys.size() - 1))
                                .set("desc", splitStr4Temp.get(1))
                                .set("type", splitStr4Temp.get(2))
                                .set("required", splitStr4Temp.size() > 3 ? splitStr4Temp.get(3) : "")
                );
            });
        }
        renderText(json);
    }

    /**
     * 获取数据请求数据
     *
     * @author zhangby
     * @date 2018/5/20 下午12:24
     */
    public void getPostManData() {
        //调用解析方法
        ApiClassDoc apiData4Depth = annotationParse.getApiData4Depth(getPara("actionName"), getPara("method"));
        Dict controllerInfo = apiData4Depth.getControllerInfo();
        Dict methodInfo = apiData4Depth.getMethodInfo();
        List<String> reqParams = Convert.convert(List.class, methodInfo.get("param") instanceof String ? Lists.newArrayList(methodInfo.get("param")) : methodInfo.get("param"));
        Kv kv = Kv.create()
                .set("url", ("/".equals(controllerInfo.getStr("action")) ? "" : controllerInfo.getStr("action")) + "/" + methodInfo.getStr("name"))
                .set("requestType", CommUtil.convers(CommUtil.splitStr(methodInfo.getStr("requestType"), ","),
                        type -> Dict.create().set("text", type.toUpperCase()).set("value", type)));
        String json = parseApiTableJson(reqParams, (l, p) -> {
            List<String> splitStr4Temp = CommUtil.splitStr4Temp(p, "{}|");
            List<String> keys = CommUtil.splitStr(splitStr4Temp.get(0), ".");
            l.put(splitStr4Temp.get(0),
                    Kv.create().set("name", keys.get(keys.size() - 1))
            );
        });
        Dict dict = recursive(JSON.parseArray(json, Dict.class));
        List<String> filterList = filterList(dict, "");
        List<Dict> reqParamsList = CommUtil.convers(reqParams,
                param -> {
                    List<String> params = CommUtil.splitStr(Convert.convert(String.class, param), "|");
                    return Dict.create()
                            .set("id", params.get(0)).set("key", params.get(0)).set("desc", params.get(1));
                }).stream().filter(p -> filterList.contains(p.getStr("key"))).collect(Collectors.toList());
        kv.set("jsonParams", dict)
                .set("reqParams", reqParamsList)
                .set("selectedRowKeys", CommUtil.convers(reqParamsList, param -> param.getStr("id")));
        renderText(JSON.toJSONString(kv));
    }

    private Dict recursive(List<Dict> list) {
        Dict dict = Dict.create();
        if (list.isEmpty()) {
            return dict;
        } else {
            list.forEach(d -> {
                Object children = d.get("children");
                dict.set(d.getStr("name"), ObjectUtil.isNotNull(children) ? recursive(CommUtil.jsonArray2DictList(children)) : "");
            });
        }
        return dict;
    }

    private List<String> filterList(Dict dict, String pre) {
        List<String> list = Lists.newArrayList();
        if (dict.isEmpty()) {
            return list;
        } else {
            dict.forEach((k, v) -> {
                if (ObjectUtil.isNotNull(v) && StrKit.notBlank(v.toString())) {
                    list.addAll(filterList(Convert.convert(Dict.class, v), StrKit.isBlank(pre) ? k : pre + "." + k));
                } else {
                    list.add(StrKit.isBlank(pre) ? k : pre + "." + k);
                }
            });
        }
        return list;
    }

    /**
     * 解析表格数据json
     *
     * @return
     */
    private String parseApiTableJson(List<String> paramList, BiConsumer<Okv, String> accumulator) {
        if (ObjectUtil.isNull(paramList) || paramList.isEmpty()) {
            return JSON.toJSONString(Lists.newArrayList());
        }
        List<String> sortParamList = paramList.stream().map(p -> p.split("\\|")[0]).sorted().collect(Collectors.toList());
        Okv okv = paramList.stream().collect(Okv::create, accumulator, Okv::putAll);
        List<Kv> list = Lists.newArrayList();
        sortParamList.forEach(p -> {
            List<String> splitKey = CommUtil.splitStr4Temp(Convert.toStr(p), ".");
            String remove = splitKey.remove(splitKey.size() - 1);
            if (!splitKey.isEmpty()) {
                Kv kv = getLastKv(list, splitKey);
                if (ObjectUtil.isNotNull(kv)) {
                    if (ObjectUtil.isNotNull(kv) && ObjectUtil.isNull(kv.get("children"))) {
                        kv.set("children", Lists.newArrayList(okv.get(p)));
                    } else {
                        List children = Convert.convert(List.class, kv.get("children"));
                        children.add(okv.get(p));
                        kv.set("children", children);
                    }
                }
            } else {
                list.add(Convert.convert(Kv.class, okv.get(p)));
            }
        });
        return JSON.toJSONString(list);
    }

    private static Kv getLastKv(List<Kv> list, List<String> keys) {
        List<Kv> rsList = list;
        Kv kv = Kv.create();
        if (!keys.isEmpty()) {
            for (String key : keys) {
                kv = getKvByName(rsList, key);
                if (ObjectUtil.isNotNull(kv) && ObjectUtil.isNotNull(kv.get("children"))) {
                    rsList = Convert.convert(List.class, kv.get("children"));
                }
            }
        }
        return kv;
    }

    private static Kv getKvByName(List<Kv> list, String name) {
        Kv kv = null;
        try {
            kv = list.stream().filter(p -> p.get("name").equals(name)).findFirst().get();
        } catch (Exception e) {
        }
        return kv;
    }

    private static Prop getApiProp() {
        return PropKit.use("api.properties");
    }
}
