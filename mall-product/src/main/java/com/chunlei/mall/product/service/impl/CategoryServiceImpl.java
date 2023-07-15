package com.chunlei.mall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.chunlei.mall.product.service.CategoryBrandRelationService;
import com.chunlei.mall.product.vo.Catelog2Vo;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chunlei.mall.common.utils.PageUtils;
import com.chunlei.mall.common.utils.Query;

import com.chunlei.mall.product.dao.CategoryDao;
import com.chunlei.mall.product.entity.CategoryEntity;
import com.chunlei.mall.product.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    RedissonClient redissonClient;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //查询所以分类
        List<CategoryEntity> entities = baseMapper.selectList(null);
        //组装成父子的树形结构
        //2.1找到所有一级分类
        List<CategoryEntity> level1Menus = entities.stream().filter(categoryEntity ->
                categoryEntity.getParentCid() == 0).map((menu)->{
                    menu.setChildren(getChildren(menu,entities));
                    return menu;
        }).sorted(Comparator.comparingInt(CategoryEntity::getSort)).collect(Collectors.toList());
        return level1Menus;
    }

    @Override
    public void removeMenusByIds(List<Long> list) {
        baseMapper.deleteBatchIds(list);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        findParentPath(catelogId, paths);
        Collections.reverse(paths);
        return paths.toArray(new Long[paths.size()]);
    }

    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(),category.getName());
    }

    @Override
    public List<CategoryEntity> getLevelCategorys() {
        List<CategoryEntity> entities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return entities;
    }

    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson() {
        /**
         * 1. 空结果缓存，解决缓存穿透
         * 2. 设置过期时间，解决缓存雪崩
         * 3. 加锁，解决缓存击穿
         */


        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        if (StringUtils.isEmpty(catalogJSON)){
            RLock lock = redissonClient.getLock("CatalogJson-lock");
            lock.lock();
            try{
                Map<String, List<Catelog2Vo>> catelogJsonFromJson = getCatelogJsonFromJson();
                return catelogJsonFromJson;
            }finally {
                lock.unlock();
            }
        }
        Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });
        return result;
    }


    public Map<String, List<Catelog2Vo>> getCatelogJsonFromJson() {
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        if (!StringUtils.isEmpty(catalogJSON)){
            Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
            return result;
        }
        //查出所有分类
        List<CategoryEntity> level1Categorys = getLevelCategorys();
        Map<String, List<Catelog2Vo>> map = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //每一个一级分类，查到这个一级分类的二级分类
            List<CategoryEntity> entityList = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
            List<Catelog2Vo> catelog2Vos = null;
            if (entityList != null) {
                catelog2Vos = entityList.stream().map(item -> {
                    List<CategoryEntity> level3Catelog = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", item.getCatId()));
                    List<Catelog2Vo.Catelog3Vo> catelog3Vos = null;
                    if (level3Catelog != null){
                        catelog3Vos = level3Catelog.stream().map(l3 -> {
                            Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(item.getCatId().toString(),l3.getCatId().toString(),l3.getName());
                            return catelog3Vo;
                        }).collect(Collectors.toList());
                    }
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(),catelog3Vos, item.getCatId().toString(), item.getName());
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));
        String jsonStr = JSON.toJSONString(map);
        redisTemplate.opsForValue().set("catalogJSON", jsonStr);
        return map;
    }

    private void findParentPath(Long catelogId, List<Long> paths){
        paths.add(catelogId);
        CategoryEntity id = this.getById(catelogId);
        if (id.getParentCid() != 0){
            findParentPath(id.getParentCid(), paths);
        }
    }

    public List<CategoryEntity> getChildren(CategoryEntity parent, List<CategoryEntity> list){
        return list.stream().filter(menu->menu.getParentCid()==parent.getCatId())
                .map(menu->{
                    //递归找子菜单
                    menu.setChildren(getChildren(menu,list));
                    return menu;
                }).sorted(Comparator.comparingInt(menu->Optional.ofNullable(menu.getSort()).orElse(0))).collect(Collectors.toList());
    }


}