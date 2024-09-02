package com.yami.shop.card.api.params;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ReceiveCardParam {
    private Long cardId;


    @NotNull(message="会员卡号不能为空")
    private String cardNumber;

//    @NotNull(message="会员卡密码不能为空")
    private String password;

}
