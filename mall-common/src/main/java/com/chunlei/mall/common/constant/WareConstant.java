package com.chunlei.mall.common.constant;

public class WareConstant {
    public enum PurchaseStatusEnum {
        CREATED(1,"新建"),ASSIGNED(0,"已分配"),
        RECEIVE(2,"已领取"),FINISH(3,"已完成"),HASERROR(4,"有异常");
        private int code;
        private String msg;

        PurchaseStatusEnum(int code, String msg){
            this.code = code;
            this.msg = msg;
        }
        public int getCode() {
            return code;
        }
        public String getMsg() {
            return msg;
        }

    }
    public enum PurchaseDetailStatusEnum {
        CREATED(1,"新建"),ASSIGNED(0,"已完成"),
        BUYING(2,"正在采购"),FINISH(3,"已完成"),HASERROR(4,"采购失败");
        private int code;
        private String msg;

        PurchaseDetailStatusEnum(int code, String msg){
            this.code = code;
            this.msg = msg;
        }
        public int getCode() {
            return code;
        }
        public String getMsg() {
            return msg;
        }

    }
}
