package com.shepherdmoney.interviewproject.vo.request;

import lombok.Data;

@Data
public class CreateUserPayload {

    private String name;

    private String email;
}
