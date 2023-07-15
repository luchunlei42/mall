package com.chunlei.mall.product.web;

import com.chunlei.mall.product.entity.CategoryEntity;
import com.chunlei.mall.product.service.CategoryService;
import com.chunlei.mall.product.vo.Catelog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;
    @GetMapping({"/","/index.html"})
    public  String indexPage(Model model){
        //1 查出所有的1级分类
        List<CategoryEntity> categoryEntityList = categoryService.getLevelCategorys();

        model.addAttribute("categorys",categoryEntityList);
        return "index";

    }

    @ResponseBody
    @GetMapping("/index/json/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatelogJson(){
        Map<String, List<Catelog2Vo>> map = categoryService.getCatelogJson();
        return map;
    }
}
