package com.xms.common.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.lang.tree.parser.NodeParser;
import com.xms.common.utils.reflect.ReflectUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 扩展 hutool TreeUtil 封装系统树构建
 *
 * @author ruoyi
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TreeBuildUtils extends TreeUtil {

    /**
     * 根据前端定制差异化字段
     */
    public static final TreeNodeConfig DEFAULT_CONFIG = TreeNodeConfig.DEFAULT_CONFIG.setNameKey("label");

    public static <T, K> List<Tree<K>> build(List<T> list, NodeParser<T, K> nodeParser) {
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        K k = ReflectUtils.invokeGetter(list.get(0), "parentId");
        return TreeUtil.build(list, k, DEFAULT_CONFIG, nodeParser);
    }

    /**
     * 使用Map合成树
     *
     * @param menuList       需要合成树的List
     * @param pId            对象中的父ID字段,如:Menu:getPid
     * @param id             对象中的id字段 ,如：Menu:getId
     * @param rootCheck      判断E中为根节点的条件，如：x->x.getPId()==-1L , x->x.getParentId()==null,x->x.getParentMenuId()==0
     * @param setSubChildren E中设置下级数据方法，如： Menu::setSubMenus
     * @param <T>            ID字段类型
     * @param <E>            泛型实体对象
     * @return
     */
    public static <T, E> List<E> makeTree(List<E> menuList, Function<E, T> pId, Function<E, T> id, Predicate<E> rootCheck, BiConsumer<E, List<E>> setSubChildren) {
        //按原数组顺序构建父级数据Map，使用Optional考虑pId为null
        Map<Optional<T>, List<E>> parentMenuMap = menuList.stream().collect(Collectors.groupingBy(
            node -> Optional.ofNullable(pId.apply(node)),
            LinkedHashMap::new,
            Collectors.toList()
        ));
        List<E> result = new ArrayList<>();
        for (E node : menuList) {
            //添加到下级数据中
            setSubChildren.accept(node, parentMenuMap.get(Optional.ofNullable(id.apply(node))));
            //如里是根节点，加入结构
            if (rootCheck.test(node)) {
                result.add(node);
            }
        }
        return result;
    }

    /**
     * 树中过滤
     * @param tree  需要过滤的树
     * @param predicate  过滤条件
     * @param getChildren 获取下级数据方法，如：MenuVo::getSubMenus
     * @return List<E> 过滤后的树
     * @param <E> 泛型实体对象
     */
    public static <E> List<E> filter(List<E> tree, Predicate<E> predicate, Function<E, List<E>> getChildren) {
        return tree.stream().filter(item -> {
            if (predicate.test(item)) {
                List<E> children = getChildren.apply(item);
                if (children != null && !children.isEmpty()) {
                    filter(children, predicate, getChildren);
                }
                return true;
            }
            return false;
        }).collect(Collectors.toList());
    }


    /**
     * 树中搜索
     * @param tree
     * @param predicate
     * @param getSubChildren
     * @return 返回搜索到的节点及其父级到根节点
     * @param <E>
     */
    public static <E> List<E> search(List<E> tree, Predicate<E> predicate, Function<E, List<E>> getSubChildren) {
        Iterator<E> iterator = tree.iterator();
        while (iterator.hasNext()) {
            E item = iterator.next();
            List<E> childList = getSubChildren.apply(item);
            if (childList != null && !childList.isEmpty()) {
                search(childList, predicate, getSubChildren);
            }
            if(!predicate.test(item) && ( childList == null || childList.isEmpty()) ){
                iterator.remove();
            }
        }
        return tree;
    }


}
