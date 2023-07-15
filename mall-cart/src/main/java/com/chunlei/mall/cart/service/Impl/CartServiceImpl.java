package com.chunlei.mall.cart.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.chunlei.mall.cart.feign.ProductFeignService;
import com.chunlei.mall.cart.intercept.CartInterceptor;
import com.chunlei.mall.cart.service.CartService;
import com.chunlei.mall.cart.vo.Cart;
import com.chunlei.mall.cart.vo.CartItem;
import com.chunlei.mall.cart.vo.SkuInfoVo;
import com.chunlei.mall.cart.vo.UserInfoTo;
import com.chunlei.mall.common.utils.R;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    ThreadPoolExecutor threadPoolExecutor;

    private final String CART_PREFIX = "mall:cart:";

    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();

        String s = (String) cartOps.get(skuId.toString());
        if (StringUtils.isEmpty(s)){
            //无商品
            CartItem cartItem = new CartItem();
            CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() -> {
                //远程查询sku
                R skuInfo = productFeignService.getSkuInfo(skuId);
                SkuInfoVo info = (SkuInfoVo) skuInfo.get("skuInfo");
                cartItem.setCheck(true);
                cartItem.setCount(num);
                cartItem.setImage(info.getSkuDefaultImg());
                cartItem.setTitle(info.getSkuTitle());
                cartItem.setPrice(info.getPrice());
                cartItem.setSkuId(info.getSkuId());
            }, threadPoolExecutor);

            CompletableFuture<Void> getSaleAttrsTask = CompletableFuture.runAsync(() -> {
                List<String> skuSaleAttrValue = productFeignService.getSkuSaleAttrValue(skuId);
                cartItem.setSkuAttr(skuSaleAttrValue);
            }, threadPoolExecutor);

            CompletableFuture.allOf(getSkuInfoTask,getSaleAttrsTask).get();
            String jsonString = JSON.toJSONString(cartItem);
            cartOps.put(skuId.toString(), jsonString);
            return cartItem;
        }else {
            CartItem cartItem = JSON.parseObject(s, CartItem.class);
            cartItem.setCount(cartItem.getCount()+num);
            String jsonString = JSON.toJSONString(cartItem);
            cartOps.put(skuId.toString(), jsonString);
            return cartItem;
        }
    }

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();

        String s  = (String) cartOps.get(skuId.toString());
        CartItem cartItem = JSON.parseObject(s, CartItem.class);
        return cartItem;
    }

    private List<CartItem> getCartItems(String cartKey){
        List<Object> list = redisTemplate.opsForHash().values(cartKey);
        if (list!=null && list.size()>0){
            List<CartItem> collect = list.stream().map((obj) -> {
                return JSON.parseObject((String) obj, CartItem.class);
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {
        Cart cart = new Cart();
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId()!=null){
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
            BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps(cartKey);
            //2.如果零食购物车的数据还没有清除
            List<CartItem> tempCartItems = getCartItems(CART_PREFIX + userInfoTo.getUserKey());
            if (tempCartItems!=null){
                for (CartItem tempCartItem : tempCartItems) {
                    addToCart(tempCartItem.getSkuId(), tempCartItem.getCount());
                }
                //清空临时购物车
                clearCart(CART_PREFIX + userInfoTo.getUserKey());
            }
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);
        }else {
            String cartKey = CART_PREFIX + userInfoTo.getUserKey();
            //获取临时购物车数据
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);
        }
        return cart;
    }

    @Override
    public void clearCart(String carKey) {
        redisTemplate.delete(carKey);
    }

    @Override
    public void checkItem(Long skuId, Integer check) {
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCheck(check==1?true:false);

        String jsonString = JSON.toJSONString(cartItem);
        getCartOps().put(skuId.toString(),jsonString);
    }

    @Override
    public void changeItemCount(Long skuId, Integer num) {
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCount(num);

        String jsonString = JSON.toJSONString(cartItem);
        getCartOps().put(skuId.toString(),jsonString);
    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }

    @Override
    public List<CartItem> getUserCartItems() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId()==null){
            return null;
        }else {
            String cartKey = CART_PREFIX+userInfoTo.getUserId();
            List<CartItem> cartItems = getCartItems(cartKey);
            List<CartItem> collect = cartItems.stream().filter(CartItem::getCheck).map(item->{
                item.setPrice(productFeignService.getPrice(item.getSkuId()));
                return item;
            }).collect(Collectors.toList());
            return collect;
        }
    }

    public BoundHashOperations<String, Object, Object> getCartOps(){
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        String cartKey = "";
        if (userInfoTo.getUserId() != null){
            cartKey = CART_PREFIX+userInfoTo.getUserId();
        }else {
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps(cartKey);
        return hashOperations;
    }


}
