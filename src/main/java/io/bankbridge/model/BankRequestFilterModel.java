package io.bankbridge.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/** Model to preserve the request filters. */
@Getter
@Setter
@AllArgsConstructor
public class BankRequestFilterModel {
    /** Pagination properties. */
    private Integer pageNumber;
    private Integer pageSize;

    /** Filter properties. */
    private String id;
    private String name;
    private String countryCode;
    private String product;
    private String auth;
}
