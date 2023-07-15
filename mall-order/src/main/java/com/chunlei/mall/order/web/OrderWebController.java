package com.chunlei.mall.order.web;

import com.chunlei.mall.order.service.OrderService;
import com.chunlei.mall.order.vo.OrderConfirmVo;
import com.chunlei.mall.order.vo.OrderSubmitVo;
import com.chunlei.mall.order.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.concurrent.ExecutionException;

@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;
    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = orderService.confirmOrder();
        model.addAttribute("orderConfirmData", confirmVo);
        return "confirm";
    }

    @PostMapping("/submitOrder")
    public String submitOrder(@RequestBody OrderSubmitVo vo,Model model){

        SubmitOrderResponseVo responseVo = orderService.submitOrder(vo);
        if (responseVo.getCode() == 0){
            model.addAttribute("submitOrderResp", responseVo);
            //下单成功
            return "pay";
        }
        return "redirect:http://order.mall.com/toTrade";
    }
}
