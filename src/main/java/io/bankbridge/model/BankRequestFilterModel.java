package io.bankbridge.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class BankRequestFilterModel {
    private Integer pageNumber;
    private Integer pageSize;

    private String id;
    private String name;
    private String countryCode;
    private String product;
    private String auth;
}
