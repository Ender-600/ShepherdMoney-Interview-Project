package com.shepherdmoney.interviewproject.vo.request;

import lombok.Data;

@Data

public class AddCreditCardToUserPayload {

    private int userId;

    private String cardIssuanceBank;

    private String cardNumber;
}
